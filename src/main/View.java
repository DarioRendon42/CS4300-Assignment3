package main;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;


import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 * <p>
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
    private int WINDOW_WIDTH, WINDOW_HEIGHT;
    private Stack<Matrix4f> modelView;
    private Matrix4f projection, trackballTransform;
    private float trackballRadius;
    private Vector2f mousePos;


    private util.ShaderProgram program;
    private util.ShaderLocationsVault shaderLocations;
    private int projectionLocation;
    private sgraph.IScenegraph<VertexAttrib> scenegraph;

    private double time;
    private robotAnimation robotA;
    private boolean raytrace;
    private float FOV;


    public View() {
        projection = new Matrix4f();
        modelView = new Stack<Matrix4f>();
        trackballRadius = 300;
        trackballTransform = new Matrix4f();

        scenegraph = null;
        raytrace = false;
        FOV = 120;
    }

    public void initScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
        GL3 gl = gla.getGL().getGL3();

        if (scenegraph != null)
            scenegraph.dispose();

        program.enable(gl);

        scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());
        robotA = new robotAnimation(scenegraph);

        sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
        renderer.setContext(gla);
        Map<String, String> shaderVarsToVertexAttribs = new HashMap<String, String>();
        shaderVarsToVertexAttribs.put("vPosition", "position");
        shaderVarsToVertexAttribs.put("vNormal", "normal");
        shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
        renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
        scenegraph.setRenderer(renderer);
        program.disable(gl);
    }

    public void init(GLAutoDrawable gla) throws Exception {
        GL3 gl = gla.getGL().getGL3();


        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();

        program.createProgram(gl, "shaders/phong-multiple.vert",
                "shaders/phong-multiple.frag");

        shaderLocations = program.getAllShaderVariables(gl);

        //get input variables that need to be given to the shader program
        projectionLocation = shaderLocations.getLocation("projection");
    }

    public void setRaytrace() {
        raytrace = true;
    }

    public void draw(GLAutoDrawable gla) {
        if (raytrace) {
            drawRaytrace();
            raytrace = false;
        } else {
            drawOpenGL(gla);
        }
    }

    public void drawRaytrace() {
        modelView.push(new Matrix4f());
        modelView.peek().lookAt(new Vector3f(70, 100, -800), new Vector3f(0, 0,
                        0),
                new Vector3f(0, 1, 0))
                .mul(trackballTransform);
        Raytracer rt = new Raytracer(WINDOW_WIDTH, WINDOW_HEIGHT, scenegraph, modelView, FOV);
        rt.raytrace();
    }

    public void drawOpenGL(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(gl.GL_DEPTH_TEST);


        program.enable(gl);

        while (!modelView.empty())
            modelView.pop();


        modelView.push(new Matrix4f());
        modelView.peek().lookAt(new Vector3f(70, 100, -800), new Vector3f(0, 0,
                        0),
                new Vector3f(0, 1, 0))
                .mul(trackballTransform);
        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */



    /*
     *Supply the shader with all the matrices it expects.
    */
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));

        //a uniform texture matrix for everybody
        gl.glUniformMatrix4fv(shaderLocations.getLocation("texturematrix"),
                1, false, new Matrix4f().identity().get(fb));


        try {
            robotA.transform(time);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        scenegraph.draw(modelView);
        time++;
    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. raytrace
     *
     *This a non-blocking function. That is, it will signal OpenGL to raytrace, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
        gl.glFlush();

        program.disable(gl);


    }

    public void mousePressed(int x, int y) {
        mousePos = new Vector2f(x, y);
    }

    public void mouseReleased(int x, int y) {
        System.out.println("Released");
    }

    public void mouseDragged(int x, int y) {
        Vector2f newM = new Vector2f(x, y);

        Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
        mousePos = new Vector2f(newM);

        trackballTransform = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
                .rotate(delta.y / trackballRadius, -1, 0, 0)
                .mul(trackballTransform);
    }

    public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        projection = new Matrix4f().perspective((float) Math.toRadians(FOV),
                (float) width / height, 0.1f, 10000.0f);
        // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

    }

    public void dispose(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

    }


}
