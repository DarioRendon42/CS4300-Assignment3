package main;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import sgraph.IScenegraph;
import util.Light;
import util.Material;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
        System.out.println(modelview.size());
        System.out.print(modelview.peek());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r, g, b;
                // set to background color
                r = g = b = 0;
                Vector4f rayOrigin = new Vector4f(0, 0, 0, 1);
                Vector4f rayDirection = new Vector4f((float) (i - width / 2), (float) (j - height / 2), (float) (-height / (2 * Math.tan(FOV / 2))), 1);
                rayOrigin.normalize();
                rayDirection.normalize();

//                rayOrigin.mul(modelview.peek());
//                rayDirection.mul(modelview.peek());


//                System.out.print(rayOrigin + "," + rayDirection);

                List<Float> intersections;
                Stack<Matrix4f> model = new Stack<>();
                model.push(new Matrix4f());
                model.peek().identity();
                intersections = scenegraph.getRoot().raytrace(this, rayOrigin, rayDirection, modelview);
//                System.out.print(Arrays.toString(intersections.toArray()));
                int indexOfLowestIntersection = getIndexOfLowestIntersection(intersections);

                Vector4f P = rayOrigin.add(rayDirection.mul(intersections.get(indexOfLowestIntersection)));

                Color c = shader(indexOfLowestIntersection, P);

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


    Color shader(int objectHit, Vector4f pointOnObject) {
        // TODO once you know what you hit, get it's color
        // idk how to actually do this past getting the ambient and basic shadows
        // maybe re implementing the phong shaders won't be too hard and then we can get everything else that's there
        // without too much trouble
        // texture mapping I don't get how to do at all, the rest I have a vague idea at least
        // objectHit is -1 if it didn't hit anything
//        System.out.println(objectHit);
        if (objectHit >= 0) {
            Material m = materials.get(objectHit);
            Vector4f a = m.getAmbient();
            Stack<Matrix4f> s = new Stack<>();
            s.push(new Matrix4f());
            for (Light l: scenegraph.getRoot().getLightsInView(s)) {
                Vector4f rayDirection = new Vector4f();
                rayDirection.sub(l.getPosition());
                if (-1 == getIndexOfLowestIntersection(scenegraph.getRoot().raytrace(this, pointOnObject, rayDirection,s))) {
                    // the ray to light has hit nothing
//                    m = m.setAmbient(m.getAmbient().mul(l.getAmbient()));
                }
            }
            return new Color(a.x, a.y, a.z);
        } else {
            return Color.black;
        }
    }


    public float findObjectIntersection(String objInstanceName, Vector4f rayOrigin, Vector4f rayDirection, Matrix4f modelview) {
        // put the rays in the object coordinate system
        Matrix4f invert = new Matrix4f(modelview);
        invert.invert();
        Vector4f o = new Vector4f(rayOrigin);
        o.mul(invert);
        Vector4f d = new Vector4f(rayDirection);
        d.mul(invert);
        switch (objInstanceName) {
            case "sphere":
                return findSphereIntersection(o, d);
            case "box":
                return findBoxIntersection(o, d);
            default:
                return findMeshIntersection(objInstanceName, o, d);
        }
    }

    private float findSphereIntersection(Vector4f rayOrigin, Vector4f rayDirection) {
        rayOrigin = rayOrigin.normalize();
        rayDirection = rayDirection.normalize();

        // a should be 1 since the vectors are normalized
        float b = (2 * (rayOrigin.x) * rayDirection.x) + (2 * (rayOrigin.y) * rayDirection.y) + (2 * (rayOrigin.z) * rayDirection.z);
        float c = (float) (Math.pow(rayOrigin.x, 2) + Math.pow(rayOrigin.y, 2) + Math.pow(rayOrigin.z, 2) - 1);

        float discriminant = b * b - 4 * c;
        // if the discriminant > 0 then we hit the sphere
        if (discriminant > 0) {
            float root_1 = (float) (((-1 * b - Math.sqrt(discriminant)) / 2) - marginOfError);

            if (root_1 > 0) {
                return root_1;
            } else {
                // inside sphere
                float root_2 = (float) (((-1 * b + Math.sqrt(discriminant)) / 2) - marginOfError);
                return root_2;
            }
        }
        return -1f;
    }

    private float findBoxIntersection(Vector4f rayOrigin, Vector4f rayDirection) {
        float tmin, tmax, tymin, tymax, tzmin, tzmax;

        tmin = (-.5f - rayOrigin.x) / rayDirection.x;
        tmax = (.5f - rayOrigin.x) / rayDirection.x;
        tymin = (-.5f - rayOrigin.y) / rayDirection.y;
        tymax = (.5f - rayOrigin.y) / rayDirection.y;

        if ((tmin > tymax) || (tymin > tmax))
            return -1f;
        if (tymin > tmin)
            tmin = tymin;
        if (tymax < tmax)
            tmax = tymax;

        tzmin = (-.5f - rayOrigin.z) / rayDirection.z;
        tzmax = (.5f - rayOrigin.z) / rayDirection.z;

        if ((tmin > tzmax) || (tzmin > tmax))
            return -1f;
        if (tzmin > tmin)
            tmin = tzmin;
        if (tzmax < tmax)
            tmax = tzmax;

        if (tmin > 0) {
            return tmin;
        } else {
            // inside box
            return tmax;
        }
    }

    private float findMeshIntersection(String objInstanceName, Vector4f rayOrigin, Vector4f rayDirection) {
//        System.out.println("No generic polygon mesh intersections yet");
        // TODO do barycentric math here
        return -1f;
    }
}
