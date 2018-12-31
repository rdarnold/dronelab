package dronelab.gui;

import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class SimCanvas extends Canvas {
    DroneLab sim;
    Scenario scenario;

    public SimCanvas(DroneLab s) {
        setSim(s);
    }

    public SimCanvas(DroneLab s, double wid, double hgt) {
        super(wid, hgt);
        create(s);
    }

    public void setSim(DroneLab s) {
        sim = s;
        setScenario(s.scenario);
    }

    public void setScenario(Scenario scen) {
        scenario = scen;
    }

    private void create(DroneLab s) {
        setSim(s);

        EventHandler filter2 = 
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        sim.updateCanvasMouseCoordinates(event.getX(), event.getY());
                        sim.screenMouseX = event.getScreenX();
                        sim.screenMouseY = event.getScreenY();
                        sim.sceneMouseX = event.getSceneX();
                        sim.sceneMouseY = event.getSceneY();
                        //System.out.println("H: " + spCanvasContainer.getHvalue() + " Y:" + spCanvasContainer.getVvalue());
                        //System.out.println("Hm: " + spCanvasContainer.getHmax() + " Ym:" + spCanvasContainer.getVmax());
                        //System.out.println("Move: " + canvasMouseX + " Y:" + canvasMouseY);
                    }
                };
        this.addEventFilter(MouseEvent.ANY, filter2);

        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    //scenario.leftClick(event.getX(), event.getY());
                    event.consume();
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    scenario.rightClick(event.getX(), event.getY());
                }
            }
        });

        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    scenario.leftClick(event.getX(), event.getY());
                    event.consume();
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                }
            }
        });

        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
                    scenario.onMouseDrag(event.getX(), event.getY());
                    event.consume();
                }
                else if (event.isSecondaryButtonDown()) {
                }
            }
        });

        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scenario.onMouseMove(event.getX(), event.getY());
                event.consume();
            }
        });

        /*
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
                    // Left button down and dragging
                }
                if (event.isSecondaryButtonDown()) {
                    // Right button down and dragging
                    // based on the ratio between how big the canvas is,
                    // move us where we wana go.
                    double xDist = (double)clickedX - event.getX();
                    double yDist = (double)clickedY - event.getY();

                    double xMove = xDist / canvas.getWidth();
                    double yMove = yDist / canvas.getHeight();

                    //double xVal = xMove / canvas.getWidth();
                    //double yVal = yMove / canvas.getHeight();
                   // canvasContainer.setHvalue(canvasContainer.getHvalue() + xMove);
                    //canvasContainer.setVvalue(canvasContainer.getVvalue() + yMove);
                    //canvasContainer.setHvalue(xVal);
                    //canvasContainer.setVvalue(yVal);
                }
            }
        });*/
    }
}