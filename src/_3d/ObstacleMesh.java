package dronelab._3d;

import java.util.List;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Cylinder;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.CullFace;
import javafx.scene.image.Image;
import dronelab.utils.*;
import dronelab.collidable.*;

public class ObstacleMesh extends MeshView {
    public Obstacle bldg;

    public ObstacleMesh(Obstacle obs) {

        bldg = obs;
        setMesh(createMesh());
        //super(new Shape3DRectangle(200, 200));

        //setRotationAxis(Rotate.Y_AXIS);
        //setTranslateX(250);
        //setTranslateY(250);
        setCullFace(CullFace.NONE);

        setDrawMode(DrawMode.FILL);
        PhongMaterial mat = new PhongMaterial();
        //m.setDiffuseColor(new Color(Math.random(),Math.random(),Math.random(), 1));
        //m.setSpecularColor(new Color(Math.random(),Math.random(),Math.random(), 1));
        if (obs.getElevation() > 25) {
            //mat.setDiffuseMap(GraphicsHelper.bldgTexture1); //6000, 0, true, true);
        } else {
            //mat.setDiffuseMap(GraphicsHelper.houseTexture1); //6000, 0, true, true);
        }
            //new Image(getResource("res/Duke3DprogressionSmall.jpg").toExternalForm())); 
        mat.setDiffuseColor(Color.rgb(100, 100, 0));
        mat.setSpecularColor(Color.rgb(150, 150, 0));
        mat.setSpecularPower(100);
        //mat.setDiffuseColor(Color.rgb(150, 50, 50, 1));
        //mat.setSpecularColor(Color.PINK);
        setMaterial(mat);
        //setMaterial(new PhongMaterial(Color.RED));
        update();
    }

    // Take an array with only the face corners, not
    // the texture coordinates, and populate it with
    // just zeroes for the tex coords
    public int[] populateTexCoords(int faces[]) {
        int newFaces[] = new int[faces.length * 2];
        for (int i = 0; i < newFaces.length; i+=2) {
            newFaces[i] = faces[i/2];
            newFaces[i+1] = 0;
        }
        return newFaces;
    }

    // Returns the next index into the faces array
    public int populatePolygonMeshTop(int[] faces) {
        int min = 0;
        int max = getNumTopTriangles() * 6;

        // Create our top of the polygon
        // NEW strrrategy
        // base corner is actually the second to last corner.
        int lastCorner = bldg.getCanvasPolygon().getNumCorners() - 1;
        int baseCorner = lastCorner - 1;
        int cornerNum = 0;
        for (int i = min; i < max; i+=6) {
            faces[i] = baseCorner;
            faces[i+1] = 4; // Texture
            faces[i+2] = cornerNum;
            faces[i+3] = 6; // Texture
            faces[i+4] = lastCorner;
            faces[i+5] = 5; // Texture
            lastCorner = cornerNum;
            cornerNum++;
        }

        /*
        int baseCorner = 0;
        int cornerNum = baseCorner + 2;
        for (int i = min; i < max; i+=6) {
            faces[i] = baseCorner;
            faces[i+1] = 4; // Texture
            faces[i+2] = cornerNum;
            faces[i+3] = 6; // Texture
            faces[i+4] = cornerNum - 1;
            faces[i+5] = 5; // Texture
            cornerNum++;
        }*/
        return max;
    }

    public void populatePolygonMeshBottom(int[] faces) {
        int min = getNumTopTriangles() * 6;
        int max = getNumTopTriangles() * 12;

        // Create our bottom of the polygon
        // If we have 4 sides then the new base corner is the 5th
        // corner in the points array (index 4; 0-3 are the top face)
        int baseCorner = bldg.getCanvasPolygon().getNumSides();
        int cornerNum = baseCorner + 2;
        for (int i = min; i < max; i+=6) {
            faces[i] = baseCorner;
            faces[i+1] = 4; // Texture
            // Since we want to look up from the bottom we need to 
            // flip this around from the top of the polygon.
            faces[i+2] = cornerNum - 1;
            faces[i+3] = 6; // Texture
            faces[i+4] = cornerNum;
            faces[i+5] = 5; // Texture
            cornerNum++;
        }
    }


        /**
            * points:
            * 1      3
            *  -------   texture:
            *  |\    |  1,1    1,0
            *  | \   |    -------
            *  |  \  |    |     |
            *  |   \ |    |     |
            *  |    \|    -------
            *  -------  0,1    0,0
            * 0      2
            *
            * texture[3] 0,0 maps to vertex 2
            * texture[2] 0,1 maps to vertex 0
            * texture[0] 1,1 maps to vertex 1
            * texture[1] 1,0 maps to vertex 3
            
            int[] faces = {
                    2, 2, 1, 1, 0, 0,
                    2, 2, 3, 3, 1, 1
                    
                    2, , 1, , 0, ,
                    2, , 3, , 1, 
            };*/

    public int populatePolygonMeshOneSide(int[] faces, int index, int topLeftCorner) {
        // Create one side of the polygon or one square face, this will take 
        // 6 corners.
        int topRightCorner = topLeftCorner - 1;
        if (topRightCorner < 0) {
            topRightCorner = bldg.getCanvasPolygon().getNumCorners() - 1;
        }
        int bottomRightCorner = topRightCorner + getNumSides();
        int bottomLeftCorner = topLeftCorner + getNumSides();
        // We always start from the top left corner of the square face.

        // Triangle 1
        faces[index] = bottomRightCorner;
        faces[index+1] = 2; // Texture
        faces[index+2] = topLeftCorner;
        faces[index+3] = 1; // Texture
        faces[index+4] = bottomLeftCorner;
        faces[index+5] = 0; // Texture

        // Triangle 2
        faces[index+6] = bottomRightCorner;
        faces[index+7] = 2; // Texture
        faces[index+8] = topRightCorner;
        faces[index+9] = 3; // Texture
        faces[index+10] = topLeftCorner;
        faces[index+11] = 1; // Texture
        
        /*
        faces[index] = topLeftCorner;
        faces[index+1] = topLeftCorner + getNumSides();
        faces[index+2] = topRightCorner;

        // Triangle 2
        faces[index+3] = topLeftCorner + getNumSides();
        faces[index+4] = topRightCorner + getNumSides();
        faces[index+5] = topRightCorner;*/
        return topRightCorner;
    }

    // This is done last after top and bottom faces are done.
    public void populatePolygonMeshSides(int[] faces) {
        // We have done two sides already - top and bottom - and the number
        // of sides is equal to half the num of triangles.  So for both top
        // and bottom, the sides is equal to the top triangles (half total triangles)

        // Create our sides of the polygon
        // 3 corners for each triangle, so its number of side triangles times 3
        int topLeftCorner = 0;
        int faceIndex = getNumTopTriangles() * 12;
        for (int i = 0; i < getNumSides(); i++) {
            // Do the faces one at a time
            topLeftCorner = populatePolygonMeshOneSide(faces, faceIndex, topLeftCorner);
            faceIndex += 12;
        }
    }

    public int getNumSides() {
        return bldg.getCanvasPolygon().getNumSides();
    }

    // Double the corners of the underlying polygon
    public int getNumCorners() {
        return bldg.getCanvasPolygon().getNumCorners() * 2;
    }

    // Various numbers of triangles in the mesh
    public int getNumTopTriangles() {
        return (getNumSides() - 2);
    }

    public int getNumSideTriangles() {
        // 2 triangles for each side
        return (getNumSides() * 2);
    }

    public int getTotalTriangles() {
        // Now, how many triangles are there going to be?  
        // For top and bottom, each will have n - 2 triangles where n is the number of sides.
        // Each wall has two triangles, so the number of wall triangles = sides * 2.  
        // Ultimately:
        //   Top:       numSides - 2
        //   Bottom:    numSides - 2
        //   Sidewalls: numSides * 2

        // The number of sides is equal to the number of coordinate pairs in the polygon
        //   (so half the number of points in the array)
        // This is just the three above values added together.
        return (getNumTopTriangles() * 2) + getNumSideTriangles();
    }

    public enum Slot {
        X, Y, Z
    }

    private TriangleMesh createMesh() {
        TriangleMesh mesh = new TriangleMesh();

        switch (bldg.getShapeType()) {
            case CIRCLE:
                // Cylinder...
                return null;
        }

        // Ok, let's do this...
        CanvasPolygon poly = bldg.getCanvasPolygon();
        List<Double> polyPoints = poly.getPoints();

        // We will need firstly, a z point for every x/y pair,
        // so that adds 50% to the list
        // Then, we need to double that, since we need points for
        // top and bottom of the mesh.
        // 2 points x 1.5 = 3 points, x 2 = 6 points.
        // So, we ultimately need 3x the points.
        float[] points = new float[polyPoints.size() * 3];

        // We will ignore these for now, they 
        // are useful when you’re using a material that contains an image that 
        // should be stretched in a specific way over the framework of the mesh. 
        // They allow you to associate a specific x-, y-coordinate in the image 
        // with each corner of each face.
        //mesh.getTexCoords().addAll(0, 0, 1300, 0, 0, 866, 1300, 866);

        /*
            1,1  1,.5  1,0
              -----------
              |         |
           .5,1       .5,0
              |         |
              -----------
            0,1  0,.5  0,0
        */
        // 1, 1 is top left, 0, 0 is bottom right.  Strangely enough ..
        float[] texCoords = {
            1, 0.5f, // idx t0
            1, 0, // idx t1
            0, 0.5f, // idx t2
            0, 0,  // idx t3
            
            1, 1, // idx t0
            1, 0.5f, // idx t1
            0, 1, // idx t2
            0, 0.5f  // idx t3
        };
        mesh.getTexCoords().addAll(texCoords);

        // We will need to normalize them by subtracting this coordinate so that
        // the origin 0 0 centers on the leftmost x,y which is also the getX()
        // and getY() in the 2d polygon.
        float subX = (float)poly.getLeftX();
        float subY = (float)poly.getLeftY();

        // Now lets put it all together.  First, the points.
        Slot slot = Slot.X;
        int polyIndex = 0;
        double zVal = bldg.getElevation() * -1;
        for (int i = 0; i < points.length; i++) {
            // Top firstly
            switch (slot) {
                case X:
                    points[i] = polyPoints.get(polyIndex).floatValue() - subX;
                    slot = Slot.Y;
                    polyIndex++;
                    break;
                case Y:
                    points[i] = polyPoints.get(polyIndex).floatValue() - subY;
                    slot = Slot.Z;
                    break;
                case Z:
                    points[i] = (float)zVal;
                    slot = Slot.X;
                    polyIndex++;
                    break;
            }
            if (polyIndex >= polyPoints.size()) {
                // Restart, this is now the lower level of points.
                polyIndex = 0;
                zVal = 0;
            }
        }

        // Now do the faces
        // Each triangle has 6 associated numbers
        // which are 3 coordinates, and then an index for the texture coordinate.
        // So multiply the number of triangles * 6.
        //int[] faces = new int[numTriangles * 6];
        // But do a corners only which is half the number of points as the faces.
        // That way we can just auto-populate with texture coords which makes things
        // easier to understand.
        int[] faces = new int[getTotalTriangles() * 6];
        populatePolygonMeshTop(faces);
        populatePolygonMeshBottom(faces);
        populatePolygonMeshSides(faces);

        /*for (int i = 0; i < faces.length; i++) {
            // If we are putting in a texture coordinate just put zero.  We
            // arent using textures yet.
            if (addTex == true) {
                faces[i] = 0;
            }
            else {
                // Otherwise, populate with appropriate corner.
            }
            addTex = !addTex;
        }*/
        
        /*int[] facesCornersOnly = {
            // Listing out the triangles here for a cube, starting on top,
            // going counter-clockwise more or less:
            // Top
            0, 0, 0,
            0, 0, 0,
            // Bottom
            5, 7, 4,
            5, 6, 7,
            // Sides
            4, 3, 0,
            4, 7, 3,
            6, 2, 3,
            7, 6, 3,
            5, 1, 2,
            6, 5, 2,
            4, 1, 5,
            4, 0, 1,
        };
        populatePolygonMeshTop(facesCornersOnly);*/

        //int[] faces = populateTexCoords(facesCornersOnly);
        
        /* Pyramid faces:
        // Remember the right number of each pair is the index of the texture coordinate
        // that ties to this corner.  We arent using that for this run so its
        // all 0.
        0,0,  2,0,  1,0,          // Front left face
        0,0,  1,0,  3,0,          // Front right face
        0,0,  3,0,  4,0,          // Back right face
        0,0,  4,0,  2,0,          // Back left face
        4,0,  1,0,  2,0,          // Bottom rear face
        4,0,  3,0,  1,0           // Bottom front face
        */


        /*int wid = 100;
        int hgt = 100;
        float[] points2 = {
                -wid/2,  hgt/2, 0, // idx p0
                -wid/2, -hgt/2, 0, // idx p1
                wid/2,  hgt/2, 0, // idx p2
                wid/2, -hgt/2, 0  // idx p3
        };*/
        /*float[] texCoords = {
                1, 1, // idx t0
                1, 0, // idx t1
                0, 1, // idx t2
                0, 0  // idx t3
        };*/
        /**
            * points:
            * 1      3
            *  -------   texture:
            *  |\    |  1,1    1,0
            *  | \   |    -------
            *  |  \  |    |     |
            *  |   \ |    |     |
            *  |    \|    -------
            *  -------  0,1    0,0
            * 0      2
            *
            * texture[3] 0,0 maps to vertex 2
            * texture[2] 0,1 maps to vertex 0
            * texture[0] 1,1 maps to vertex 1
            * texture[1] 1,0 maps to vertex 3
            *
            * Two triangles define rectangular faces:
            * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
            * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
            */

// if you use the co-ordinates as defined in the above comment, it will be all messed up
//            int[] faces = {
//                    0, 0, 1, 1, 2, 2,
//                    0, 0, 2, 2, 3, 3
//            };

// try defining faces in a counter-clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 2, 1, 1, 0, 0,
//                    2, 2, 3, 3, 1, 1
//            };

// try defining faces in a clockwise order to see what the difference is.
        /*int[] faces2 = {
                2, 0, 0, 0, 1, 0,
                2, 0, 1, 0, 3, 0
        };*/


        mesh.getPoints().setAll(points);
        //mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);
        return mesh;
    }

    public void update() {
        setTranslateX(bldg.getX());
        setTranslateY(bldg.getY());
        //setTranslateZ(-1 * (bldg.getElevation() / 2));
    }
}