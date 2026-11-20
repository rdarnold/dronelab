package dronelab._3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import dronelab.Scenario;
import dronelab.collidable.Obstacle;
import dronelab.collidable.Person;
import dronelab.utils.Constants;
import dronelab.utils.Distance;

final class SurvivorView {
    private static final int GROUP_DISTANCE = 32;
    private static final int UNGROUP_DISTANCE = 40;
    private static final int FIGURE_LIMIT = 48;
    private static final int ROOF_CELL_SIZE = 128;
    private static final int BADGE_HEIGHT = 24;
    private static final int[] BADGE_OFFSETS = {0, -26, 26, -52, 52, -78, 78};
    private static final double FIGURE_HEIGHT_METERS = 1.75;
    private static final Color WAITING = Color.web("#ffb74b");
    private static final Color RESCUED = Color.web("#83d6ff");
    private static final PhongMaterial VEST = new PhongMaterial(WAITING);
    private static final PhongMaterial RESCUED_VEST = new PhongMaterial(RESCUED);
    private static final PhongMaterial CLOTHES = new PhongMaterial(Color.web("#263444"));
    private static final PhongMaterial HEAD = new PhongMaterial(Color.web("#e8bd90"));

    private final Scenario scenario;
    private final SubScene view;
    private final Canvas overlay = new Canvas();
    private final Group figures = new Group();
    private final List<Figure> figurePool = new ArrayList<>();
    private final List<Badge> badges = new ArrayList<>();
    private final Map<RoofCell, List<Obstacle>> roofs = new HashMap<>();
    private final Set<Person> grouped = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Tooltip tooltip = new Tooltip();
    private long lastUpdate;
    private boolean revealAll;
    private boolean pointerInView;
    private double pointerX;
    private double pointerY;
    private double pointerScreenX;
    private double pointerScreenY;

    SurvivorView(Scenario scenario, Group root, SubScene view) {
        this.scenario = scenario;
        this.view = view;
        root.getChildren().add(figures);
        figures.setMouseTransparent(true);
        overlay.setMouseTransparent(true);
        overlay.widthProperty().bind(view.widthProperty());
        overlay.heightProperty().bind(view.heightProperty());
        view.setOnMouseMoved(event -> {
            pointerInView = true;
            pointerX = event.getX();
            pointerY = event.getY();
            pointerScreenX = event.getScreenX();
            pointerScreenY = event.getScreenY();
            showDetails();
        });
        view.setOnMouseExited(event -> {
            pointerInView = false;
            tooltip.hide();
        });
        reset();
    }

    Canvas overlay() {
        return overlay;
    }

    void revealAll(boolean reveal) {
        revealAll = reveal;
        tooltip.hide();
        lastUpdate = 0;
        update();
    }

    void reset() {
        grouped.clear();
        roofs.clear();
        for (Obstacle obstacle : scenario.obstacles) {
            if (obstacle.getElevation() > 0) {
                indexRoof(obstacle);
            }
        }
        tooltip.hide();
        lastUpdate = 0;
        update();
    }

    void update() {
        long now = System.nanoTime();
        if (now - lastUpdate < 66_000_000 && lastUpdate != 0) {
            return;
        }
        lastUpdate = now;
        PerspectiveCamera camera = (PerspectiveCamera) view.getCamera();
        double width = view.getWidth();
        double height = view.getHeight();
        double fieldSize = camera.isVerticalFieldOfView() ? height : width;
        double focalLength = fieldSize / (2 * Math.tan(Math.toRadians(camera.getFieldOfView() / 2)));
        double figureHeight = Distance.pixelsFromMeters(FIGURE_HEIGHT_METERS);
        Transform worldToCamera;
        try {
            worldToCamera = camera.getLocalToSceneTransform().createInverse();
        } catch (NonInvertibleTransformException e) {
            return;
        }

        Map<Cell, List<Cluster>> grid = new HashMap<>();
        List<Cluster> clusters = new ArrayList<>();
        for (Person person : scenario.getVictims()) {
            if (!revealAll && !person.isLocated() && !person.isRescued()) {
                continue;
            }
            Surface surface = surfaceFor(person);
            double elevation = surface.roof == null ? person.getElevation() : surface.roof.getElevation();
            Point3D point = worldToCamera.transform(person.getX(), person.getY(), -elevation - figureHeight);
            double depth = point.getZ();
            if (depth < camera.getNearClip() || depth > camera.getFarClip()) {
                continue;
            }
            double x = width / 2 + point.getX() * focalLength / depth;
            double y = height / 2 + point.getY() * focalLength / depth;
            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }
            int col = (int) x / UNGROUP_DISTANCE;
            int row = (int) y / UNGROUP_DISTANCE;
            Cluster cluster = findCluster(grid, surface, col, row, x, y, person);
            if (cluster == null) {
                cluster = new Cluster(person, x, y, depth, elevation);
                clusters.add(cluster);
                grid.computeIfAbsent(new Cell(surface, col, row), ignored -> new ArrayList<>()).add(cluster);
            }
            cluster.add(person, x, y);
        }

        grouped.clear();
        int figureCount = 0;
        GraphicsContext graphics = overlay.getGraphicsContext2D();
        graphics.clearRect(0, 0, width, height);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 14));
        graphics.setTextAlign(TextAlignment.CENTER);
        badges.clear();
        int badgeColumns = Math.max(1, (int) Math.ceil(width / 16));
        int badgeRows = Math.max(1, (int) Math.ceil(height / 16));
        boolean[][] occupied = new boolean[badgeRows][badgeColumns];
        for (Cluster cluster : clusters) {
            if (cluster.members.size() > 1) {
                grouped.addAll(cluster.members);
            } else if (figureCount < FIGURE_LIMIT && focalLength * figureHeight / cluster.depth >= 13) {
                showFigure(figureCount++, cluster.anchor, figureHeight, cluster.elevation);
            }
            drawBadge(graphics, cluster, occupied, width, height);
        }
        for (int i = figureCount; i < figurePool.size(); i++) {
            figurePool.get(i).group.setVisible(false);
        }
        if (pointerInView) {
            showDetails();
        }
    }

    private Cluster findCluster(Map<Cell, List<Cluster>> grid, Surface surface, int col, int row,
            double x, double y, Person person) {
        Cluster nearest = null;
        double bestDistance = UNGROUP_DISTANCE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                List<Cluster> candidates = grid.get(new Cell(surface, col + dx, row + dy));
                if (candidates == null) {
                    continue;
                }
                for (Cluster candidate : candidates) {
                    double distance = Math.hypot(x - candidate.x, y - candidate.y);
                    boolean wasGrouped = grouped.contains(person) && grouped.contains(candidate.anchor);
                    if (distance <= (wasGrouped ? UNGROUP_DISTANCE : GROUP_DISTANCE) && distance < bestDistance) {
                        nearest = candidate;
                        bestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private void indexRoof(Obstacle obstacle) {
        double minX;
        double minY;
        double maxX;
        double maxY;
        if (obstacle.getShapeType() == Constants.ShapeType.CIRCLE) {
            double radius = obstacle.getRadius();
            minX = obstacle.getX() - radius;
            minY = obstacle.getY() - radius;
            maxX = obstacle.getX() + radius;
            maxY = obstacle.getY() + radius;
        } else {
            Bounds bounds = obstacle.getCanvasPolygon().getBoundsInLocal();
            minX = bounds.getMinX();
            minY = bounds.getMinY();
            maxX = bounds.getMaxX();
            maxY = bounds.getMaxY();
        }
        int lastRow = roofCell(maxY);
        int lastCol = roofCell(maxX);
        for (int row = roofCell(minY); row <= lastRow; row++) {
            for (int col = roofCell(minX); col <= lastCol; col++) {
                roofs.computeIfAbsent(new RoofCell(col, row), ignored -> new ArrayList<>()).add(obstacle);
            }
        }
    }

    private Surface surfaceFor(Person person) {
        double x = person.getX();
        double y = person.getY();
        double elevation = person.getElevation();
        Obstacle highestRoof = null;
        List<Obstacle> candidates = roofs.getOrDefault(new RoofCell(roofCell(x), roofCell(y)), List.of());
        for (Obstacle roof : candidates) {
            if (roof.getElevation() < elevation) {
                continue;
            }
            boolean onFootprint = roof.getShapeType() == Constants.ShapeType.CIRCLE
                    ? Math.hypot(x - roof.getX(), y - roof.getY()) <= roof.getRadius()
                    : roof.getCanvasPolygon().contains(x, y);
            if (onFootprint) {
                highestRoof = roof;
                elevation = roof.getElevation();
            }
        }
        return new Surface(highestRoof, (int) Math.round(Math.max(elevation, 0) * 2));
    }

    private int roofCell(double coordinate) {
        return (int) Math.floor(coordinate / ROOF_CELL_SIZE);
    }

    private void showFigure(int index, Person person, double height, double elevation) {
        if (index == figurePool.size()) {
            Figure figure = new Figure();
            figurePool.add(figure);
            figures.getChildren().add(figure.group);
        }
        Figure figure = figurePool.get(index);
        figure.scale.setX(height);
        figure.scale.setY(height);
        figure.scale.setZ(height);
        figure.group.setTranslateX(person.getX());
        figure.group.setTranslateY(person.getY());
        figure.group.setTranslateZ(-elevation);
        figure.vest.setMaterial(person.isRescued() ? RESCUED_VEST : VEST);
        figure.group.setVisible(true);
    }

    private void drawBadge(GraphicsContext graphics, Cluster cluster, boolean[][] occupied,
            double width, double height) {
        String text = badgeText(cluster);
        double badgeWidth = 16 + 9 * text.length();
        double anchorX = cluster.sumX / cluster.members.size();
        double anchorY = cluster.sumY / cluster.members.size();
        double x = Math.clamp(anchorX - badgeWidth / 2, 0, Math.max(0, width - badgeWidth));
        double y = Math.clamp(anchorY - BADGE_HEIGHT - 3, 0, Math.max(0, height - BADGE_HEIGHT));
        int left = (int) x / 16;
        int right = Math.min(occupied[0].length - 1, (int) (x + badgeWidth) / 16);
        // Keep nearby badges for different surfaces legible while retaining their own anchors.
        for (int offset : BADGE_OFFSETS) {
            double candidateY = y + offset;
            if (candidateY < 0 || candidateY + BADGE_HEIGHT > height) {
                continue;
            }
            int top = (int) candidateY / 16;
            int bottom = Math.min(occupied.length - 1, (int) (candidateY + BADGE_HEIGHT - 1) / 16);
            boolean free = true;
            for (int row = top; row <= bottom; row++) {
                for (int col = left; col <= right; col++) {
                    free &= !occupied[row][col];
                }
            }
            if (free) {
                for (int row = top; row <= bottom; row++) {
                    for (int col = left; col <= right; col++) {
                        occupied[row][col] = true;
                    }
                }
                y = candidateY;
                break;
            }
        }
        Color accent = cluster.waiting > 0 ? WAITING : cluster.rescued > 0 ? RESCUED : Color.LIGHTGRAY;
        graphics.setStroke(accent);
        graphics.strokeLine(anchorX, anchorY, x + badgeWidth / 2, y + BADGE_HEIGHT / 2.0);
        graphics.setFill(Color.web("#172231", 0.96));
        graphics.fillRoundRect(x, y, badgeWidth, BADGE_HEIGHT, 9, 9);
        graphics.strokeRoundRect(x + 0.5, y + 0.5, badgeWidth - 1, BADGE_HEIGHT - 1, 9, 9);
        graphics.setFill(accent);
        graphics.fillText(text, x + badgeWidth / 2, y + 17);
        badges.add(new Badge(x, y, badgeWidth, cluster));
    }

    private String badgeText(Cluster cluster) {
        String waiting = statusText("!", cluster.waiting);
        String rescued = statusText("✓", cluster.rescued);
        String undetected = statusText("?", cluster.undetected);
        return (waiting + " " + rescued + " " + undetected).trim().replace("  ", " ");
    }

    private String statusText(String symbol, int count) {
        return count == 0 ? "" : count == 1 ? symbol : symbol + count;
    }

    private void showDetails() {
        for (int i = badges.size() - 1; i >= 0; i--) {
            Badge badge = badges.get(i);
            if (pointerX >= badge.x && pointerX <= badge.x + badge.width
                    && pointerY >= badge.y && pointerY <= badge.y + BADGE_HEIGHT) {
                Cluster cluster = badge.cluster;
                String counts = cluster.waiting + " awaiting rescue, " + cluster.rescued + " rescued";
                if (revealAll && cluster.undetected > 0) {
                    counts += ", " + cluster.undetected + " undetected";
                }
                if (cluster.members.size() == 1) {
                    counts = "Survivor " + cluster.anchor.getId() + "\n" + counts;
                }
                tooltip.setText(counts);
                if (!tooltip.isShowing()) {
                    tooltip.show(view, pointerScreenX + 12, pointerScreenY + 14);
                }
                return;
            }
        }
        tooltip.hide();
    }

    private record Surface(Obstacle roof, int elevation) { }
    private record RoofCell(int col, int row) { }
    private record Cell(Surface surface, int col, int row) { }
    private record Badge(double x, double y, double width, Cluster cluster) { }

    private static final class Cluster {
        final Person anchor;
        final double x;
        final double y;
        final double depth;
        final double elevation;
        final List<Person> members = new ArrayList<>();
        double sumX;
        double sumY;
        int waiting;
        int rescued;
        int undetected;

        Cluster(Person anchor, double x, double y, double depth, double elevation) {
            this.anchor = anchor;
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.elevation = elevation;
        }

        void add(Person person, double newX, double newY) {
            members.add(person);
            sumX += newX;
            sumY += newY;
            if (person.isRescued()) {
                rescued++;
            } else if (person.isLocated()) {
                waiting++;
            } else {
                undetected++;
            }
        }
    }

    private static final class Figure {
        final Group group = new Group();
        final Scale scale = new Scale();
        final Box vest = new Box(0.32, 0.23, 0.41);

        Figure() {
            group.getTransforms().add(scale);
            Sphere head = new Sphere(0.12, 8);
            head.setTranslateZ(-0.88);
            head.setMaterial(HEAD);
            vest.setTranslateZ(-0.55);
            Box leftLeg = leg(-0.085);
            Box rightLeg = leg(0.085);
            group.getChildren().addAll(head, vest, leftLeg, rightLeg);
        }

        private Box leg(double x) {
            Box leg = new Box(0.13, 0.19, 0.35);
            leg.setTranslateX(x);
            leg.setTranslateZ(-0.175);
            leg.setMaterial(CLOTHES);
            return leg;
        }
    }
}
