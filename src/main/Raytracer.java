package main;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import sgraph.IScenegraph;
import util.Material;
import util.PolygonMesh;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
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
    private List<Material> materials;

    public Raytracer(int width, int height, sgraph.IScenegraph<VertexAttrib> scenegraph, Stack<Matrix4f> modelview, float FOV) {
        this.width = width;
        this.height = height;
        this.scenegraph = scenegraph;
        this.modelview = modelview;
        this.FOV = FOV;
        materials = scenegraph.getRoot().getObjectMaterial();
    }

    public void raytrace() {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        modelview.add(modelview.peek());
        modelview.peek().invert();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r, g, b;
                // set to background color
                r = g = b = 0;
                Vector4f rayOrigin = new Vector4f(0, 0, 0, 1);
                Vector4f rayDirection = new Vector4f((float) (i - width / 2), (float) (j - height / 2), (float) (-height / (2 * Math.tan(FOV / 2))), 1);

                rayOrigin.mul(modelview.peek());
                rayDirection.mul(modelview.peek());


//                System.out.print(rayOrigin + "," + rayDirection);

                List<Float> intersections;
                intersections = scenegraph.getRoot().raytrace(this, rayOrigin, rayDirection);
//                System.out.print(Arrays.toString(intersections.toArray()));
                int indexOfLowestIntersection = getIndexOfLowestIntersection(intersections);

                Color c = shader(indexOfLowestIntersection);

                output.setRGB(i, j, c.getRGB());
            }
//            System.out.println();
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
        } else {
            float min = Float.MAX_VALUE;
            for (int k = 0; k < intersections.size(); k++) {
                float f = intersections.get(k);
                if (0 < f - marginOfError && f < min - marginOfError) {
                    min = f;
                    indexOfLowestIntersection = k;
                }
            }
        }
        return indexOfLowestIntersection;
    }


    Color shader(int objectHit) {
        // TODO once you know what you hit, get it's color
        // idk how to actually do this past getting the ambient and basic shadows
        // maybe re implementing the phong shaders won't be too hard and then we can get everything else that's there
        // without too much trouble
        // texture mapping I don't get how to do at all, the rest I have a vague idea at least
        // objectHit is -1 if it didn't hit anything
//        System.out.println(objectHit);
        Color c;
        if(objectHit >= 0) {
            Material m = materials.get(objectHit);
            Vector4f a = m.getAmbient();
            return new Color(a.x, a.y, a.z);
        } else {
            return Color.black;
        }
    }


    public Float findObjectIntersection(String objInstanceName, Vector4f rayOrigin, Vector4f rayDirection) {
        switch (objInstanceName) {
            case "sphere":
                return findSphereIntersection(rayOrigin, rayDirection);
            case "box":
                return findBoxIntersection(rayOrigin, rayDirection);
            default:
                return findMeshIntersection(objInstanceName, rayOrigin, rayDirection);
        }
    }

    private Float findSphereIntersection(Vector4f rayOrigin, Vector4f rayDirection) {
        rayOrigin = rayOrigin.normalize();
        rayDirection = rayDirection.normalize();


        float b = (2*(rayOrigin.x)*rayDirection.x) + (2*(rayOrigin.y)*rayDirection.y) + (2*(rayOrigin.z)*rayDirection.z);
        float c = (float ) (Math.pow(rayOrigin.x, 2) + Math.pow(rayOrigin.y, 2) + Math.pow(rayOrigin.z, 2) - 1);

        float discriminant = b * b - 4 * c;
        if (discriminant > 0) {
            float root_1 = (float) (((-1*b - Math.sqrt(discriminant))/2) - marginOfError);

            if (root_1 > 0) {
                return root_1;
            } else {
                float root_2 = (float) (((-1*b + Math.sqrt(discriminant))/2) - marginOfError);
                return root_2;
            }
        }
        return -1f;
    }

    private Float findBoxIntersection(Vector4f rayOrigin, Vector4f rayDirection) {
//        System.out.println("No box intersection yet");
        return -1f;
    }

    private Float findMeshIntersection(String objInstanceName, Vector4f rayOrigin, Vector4f rayDirection) {
//        System.out.println("No generic polygon mesh intersections yet");
        // TODO do barycentric math here
        return -1f;
    }
}
