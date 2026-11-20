package dronelab._3d;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

import dronelab.utils.Distance;

final class BuildingFacade {
    private static final int TILE_SIZE = 64;
    private static final Color WINDOW = Color.web("#435862");
    private static final Color WINDOW_REFLECTION = Color.web("#829aa3");
    private static final Color DOOR = Color.web("#51483e");

    private BuildingFacade() { }

    static PhongMaterial material(Color wall, boolean tall) {
        WritableImage atlas = new WritableImage(TILE_SIZE, TILE_SIZE * 2);
        PixelWriter pixels = atlas.getPixelWriter();
        fill(pixels, 0, 0, TILE_SIZE, TILE_SIZE * 2, wall);
        for (int offset : new int[] {0, TILE_SIZE}) {
            fill(pixels, 0, offset + 2, TILE_SIZE, 2, wall.deriveColor(0, 1, 0.91, 1));
            fill(pixels, 0, offset + 61, TILE_SIZE, 3, wall.deriveColor(0, 1, 0.84, 1));
        }
        int left = tall ? 9 : 16;
        int width = TILE_SIZE - 2 * left;
        fill(pixels, left, 15, width, 28, WINDOW);
        fill(pixels, left + 2, 17, width - 4, 6, WINDOW_REFLECTION);
        fill(pixels, 31, 15, 2, 28, wall);
        fill(pixels, left - 2, 43, width + 4, 2, wall.darker());

        fill(pixels, 19, 79, 26, 49, DOOR);
        fill(pixels, 22, 82, 20, 18, WINDOW);
        fill(pixels, 36, 108, 4, 3, wall);

        PhongMaterial material = new PhongMaterial(Color.WHITE);
        material.setDiffuseMap(atlas);
        return material;
    }

    private static void fill(PixelWriter pixels, int x, int y, int width, int height, Color color) {
        for (int row = y; row < y + height; row++) {
            for (int col = x; col < x + width; col++) {
                pixels.setColor(col, row, color);
            }
        }
    }

    static void addWalls(TriangleMesh mesh, float[] points, int corners, double height, boolean tall) {
        if (height <= 0) {
            return;
        }
        int window = mesh.getTexCoords().size() / mesh.getTexCoordElementSize();
        addTileCoordinates(mesh, 0);
        addTileCoordinates(mesh, TILE_SIZE);
        int plain = window + 8;
        mesh.getTexCoords().addAll(0.5f / TILE_SIZE, 0.5f / (TILE_SIZE * 2));

        // Pick one entrance on the widest wall. Short edges get plain siding.
        int entrance = 0;
        double widest = 0;
        for (int wall = 0; wall < corners; wall++) {
            int end = (wall + corners - 1) % corners;
            double width = Math.hypot(points[end * 3] - points[wall * 3],
                    points[end * 3 + 1] - points[wall * 3 + 1]);
            if (width > widest) {
                widest = width;
                entrance = wall;
            }
        }
        int floors = Math.max(1, (int) Math.round(Distance.metersFromPixels(height) / (tall ? 3.5 : 3.1)));
        for (int wall = 0; wall < corners; wall++) {
            int start = wall * 3;
            int end = ((wall + corners - 1) % corners) * 3;
            float x = points[start];
            float y = points[start + 1];
            float dx = points[end] - x;
            float dy = points[end + 1] - y;
            double width = Distance.metersFromPixels(Math.hypot(dx, dy));
            if (width == 0) {
                continue;
            }
            int bays = Math.max(1, (int) Math.round(width / (tall ? 2.8 : 3.4)));
            boolean hasRoomForWindows = width >= 1.8 && Distance.metersFromPixels(height) >= 2;
            for (int floor = 0; floor < floors; floor++) {
                float bottom = (float) (-height * floor / floors);
                float top = (float) (-height * (floor + 1) / floors);
                for (int bay = 0; bay < bays; bay++) {
                    float left = (float) bay / bays;
                    float right = (float) (bay + 1) / bays;
                    boolean door = floor == 0 && wall == entrance && bay == bays / 2;
                    int coords = hasRoomForWindows ? window + (door ? 4 : 0) : plain;
                    addPanel(mesh, x + left * dx, y + left * dy,
                            x + right * dx, y + right * dy, top, bottom, coords, plain);
                }
            }
        }
    }

    private static void addTileCoordinates(TriangleMesh mesh, int row) {
        float left = 0.5f / TILE_SIZE;
        float right = 1 - left;
        float top = (row + 0.5f) / (TILE_SIZE * 2);
        float bottom = (row + TILE_SIZE - 0.5f) / (TILE_SIZE * 2);
        mesh.getTexCoords().addAll(left, top, right, top, left, bottom, right, bottom);
    }

    private static void addPanel(TriangleMesh mesh, float leftX, float leftY, float rightX,
            float rightY, float top, float bottom, int coords, int plain) {
        int point = mesh.getPoints().size() / mesh.getPointElementSize();
        mesh.getPoints().addAll(leftX, leftY, top, rightX, rightY, top,
                leftX, leftY, bottom, rightX, rightY, bottom);
        int topRight = coords == plain ? plain : coords + 1;
        int bottomLeft = coords == plain ? plain : coords + 2;
        int bottomRight = coords == plain ? plain : coords + 3;
        mesh.getFaces().addAll(point + 3, bottomRight, point, coords, point + 2, bottomLeft,
                point + 3, bottomRight, point + 1, topRight, point, coords);
    }
}
