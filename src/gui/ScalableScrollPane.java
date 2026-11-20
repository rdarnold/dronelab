package dronelab.gui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

public class ScalableScrollPane extends ScrollPane {
    private static final double PAN_MARGIN = 0.20;
    private final Group scaleGroup = new Group();
    private final Group scaledBounds = new Group(scaleGroup);
    private final StackPane centeredContent = new StackPane(scaledBounds);
    private final Scale scaleTransform = new Scale(1, 1);
    private final AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();
    private Node currentContent;
    private Node viewport;
    private Point2D lastPanPoint;
    private boolean dragged;
    private Runnable onPan = () -> { };

    public ScalableScrollPane(Node content) {
        scaleGroup.getTransforms().add(scaleTransform);
        resetContent(content);
        setContent(centeredContent);
        setFitToWidth(true);
        setFitToHeight(true);
        // Leave room to pan on both sides even when the entire map fits.
        centeredContent.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                paddedSize(scaledBounds.getLayoutBounds().getWidth(), getViewportBounds().getWidth()),
                scaledBounds.layoutBoundsProperty(), viewportBoundsProperty()));
        centeredContent.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                paddedSize(scaledBounds.getLayoutBounds().getHeight(), getViewportBounds().getHeight()),
                scaledBounds.layoutBoundsProperty(), viewportBoundsProperty()));
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::beginPan);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::pan);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::endPan);
        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY && dragged) {
                event.consume();
            }
        });
    }

    public void setOnPan(Runnable onPan) {
        this.onPan = onPan;
    }

    public void resetContent(Node content) {
        currentContent = content;
        scaleGroup.getChildren().setAll(content);
    }

    public void fitContent() {
        zoomOperator.stop();
        applyCss();
        layout();

        Bounds contentBounds = currentContent.getLayoutBounds();
        // Recheck the viewport after the scroll pane lays out the new map.
        for (int pass = 0; pass < 2; pass++) {
            Bounds area = getViewportBounds();
            if (area.getWidth() <= 2 || area.getHeight() <= 2
                    || contentBounds.getWidth() <= 0 || contentBounds.getHeight() <= 0) {
                return;
            }
            double horizontal = (area.getWidth() - 2) / contentBounds.getWidth();
            double vertical = (area.getHeight() - 2) / contentBounds.getHeight();
            setScale(Math.min(1, Math.min(horizontal, vertical)));
            layout();
            centeredContent.layout();
        }
        setHvalue((getHmin() + getHmax()) / 2);
        setVvalue((getVmin() + getVmax()) / 2);
    }

    public void scale(double factor, double sceneX, double sceneY, Runnable finished) {
        Node view = viewport();
        if (view == null) {
            return;
        }
        Point2D focus = currentContent.sceneToLocal(sceneX, sceneY);
        Point2D mouse = view.sceneToLocal(sceneX, sceneY);
        zoomOperator.zoom(getScale(), getScale() * factor, value -> {
            setScale(value);
            layout();
            centeredContent.layout();
            Point2D position = centeredContent.sceneToLocal(currentContent.localToScene(focus));
            Bounds area = getViewportBounds();
            setHvalue(scrollValue(position.getX() - mouse.getX(),
                    centeredContent.getWidth() - area.getWidth(), getHmin(), getHmax()));
            setVvalue(scrollValue(position.getY() - mouse.getY(),
                    centeredContent.getHeight() - area.getHeight(), getVmin(), getVmax()));
        }, finished);
    }

    public double getScale() {
        return scaleTransform.getX();
    }

    public Bounds getVisibleContentBounds() {
        Node view = viewport();
        return view == null ? currentContent.getLayoutBounds()
                : currentContent.sceneToLocal(view.localToScene(view.getLayoutBounds()));
    }

    private Node viewport() {
        if (viewport == null) {
            viewport = lookup(".viewport");
        }
        return viewport;
    }

    private void setScale(double value) {
        scaleTransform.setX(value);
        scaleTransform.setY(value);
    }

    private double paddedSize(double mapSize, double viewportSize) {
        return Math.max(mapSize, viewportSize) + 2 * PAN_MARGIN * viewportSize;
    }

    private void beginPan(MouseEvent event) {
        Node view = viewport();
        if (event.getButton() != MouseButton.SECONDARY || view == null) {
            return;
        }
        dragged = false;
        Point2D point = view.sceneToLocal(event.getSceneX(), event.getSceneY());
        lastPanPoint = view.getLayoutBounds().contains(point) ? point : null;
        if (lastPanPoint != null) {
            zoomOperator.stop();
        }
    }

    private void pan(MouseEvent event) {
        if (lastPanPoint == null || !event.isSecondaryButtonDown()) {
            return;
        }
        Point2D point = viewport().sceneToLocal(event.getSceneX(), event.getSceneY());
        Point2D delta = point.subtract(lastPanPoint);
        if (!dragged && delta.magnitude() < 2) {
            return;
        }
        dragged = true;
        // Advance the drag origin even at a limit so reversing moves the map immediately.
        lastPanPoint = point;
        Bounds area = getViewportBounds();
        double oldH = getHvalue();
        double oldV = getVvalue();
        double h = panValue(oldH, delta.getX(), centeredContent.getWidth() - area.getWidth(), getHmin(), getHmax());
        double v = panValue(oldV, delta.getY(), centeredContent.getHeight() - area.getHeight(), getVmin(), getVmax());
        if (h != oldH || v != oldV) {
            setHvalue(h);
            setVvalue(v);
            onPan.run();
        }
        event.consume();
    }

    private void endPan(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && lastPanPoint != null) {
            lastPanPoint = null;
            if (dragged) {
                event.consume();
            }
        }
    }

    private double panValue(double value, double delta, double overflow, double min, double max) {
        return overflow <= 0 ? value : Math.clamp(value - delta / overflow * (max - min), min, max);
    }

    private double scrollValue(double offset, double overflow, double min, double max) {
        return overflow <= 0 ? (min + max) / 2 : min + Math.clamp(offset / overflow, 0, 1) * (max - min);
    }
}
