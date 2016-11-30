package main;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import sgraph.IScenegraph;
import util.PolygonMesh;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;


/**
 * Created by Dario Rendon on 11/28/2016.
 */
public class Raytracer {
    private int width, height;
    private IScenegraph<VertexAttrib> scenegraph;
    private Stack<Matrix4f> modelview;
    private float FOV;

    private final float marginOfError = .0001f;

    public Raytracer(int width, int height, sgraph.IScenegraph<VertexAttrib> scenegraph, Stack<Matrix4f> modelview, float FOV) {
        this.width = width;
        this.height = height;
        this.scenegraph = scenegraph;
        this.modelview = modelview;
        this.FOV = FOV;
    }

    public void raytrace() {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r, g, b;
                // set to background color
                r = g = b = 0;
                Vector4f rayOrigin = new Vector4f(0, 0, 0, 0);
                Vector4f rayDirection = new Vector4f((float) (i - width / 2), (float) (j - height / 2), (float) (-height / (2 * Math.tan(FOV / 2))), 0);

                List<Float> intersections;
                intersections = scenegraph.getRoot().raytrace(this, rayOrigin, rayDirection);
                int indexOfLowestIntersection = getIndexOfLowestIntersection(intersections);

                Color c = shader(/* TODO fill here with relevant parameters*/);

                output.setRGB(i, j, new Color(r, g, b).getRGB());
            }
        }

        OutputStream outStream = null;

        try {
            outStream = new FileOutputStream("output/raytrace.png");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }

        try {
            ImageIO.write(output, "png", outStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }
        System.out.println("Done Raytracing");
    }

    private int getIndexOfLowestIntersection(List<Float> intersections) {
        int indexOfLowestIntersection = -1;
        if (intersections.size() == 0) {
            indexOfLowestIntersection = -1;
        } else if (intersections.size() == 1) {
            indexOfLowestIntersection = 0;
        } else {
            float min = -1;
            for (int k = 0; k < intersections.size(); k++) {
                Float f = intersections.get(k);
                if (k == 0 || 0 < f && f < min) {
                    min = f;
                    indexOfLowestIntersection = k;
                }
            }
        }
        return indexOfLowestIntersection;
    }


    Color shader(/* TODO make useful parameters */) {
        // TODO once you know what you hit, get it's color
        // idk how to actually do this past getting the ambient and basic shadows
        // maybe re implementing the phong shaders won't be too hard and then we can get everything else that's there
        // without too much trouble
        // texture mapping I don't get how to do at all, the rest I have a vague idea at least
        return null;
    }


    public Float findMeshIntersection(String objInstanceName, Vector4f rayOrigin, Vector4f rayDirection) {
        PolygonMesh<VertexAttrib> mesh = scenegraph.getPolygonMeshes().get(objInstanceName);
//        mesh.
        return null;
    }
}
