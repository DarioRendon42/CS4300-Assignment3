import org.joml.Matrix4f;
import sgraph.INode;

import java.util.Map;

/**
 * Created by xiangfan on 10/25/16.
 */


public class robotAnimation {
  private int angleOfRotation;
  private Map<String, INode> nodes;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;


  public robotAnimation(sgraph.IScenegraph<VertexAttrib> scenegraph) {
    this.scenegraph = scenegraph;
    this.nodes = scenegraph.getNodes();
    this.angleOfRotation = 0;
  }
  public void animation(String name, Matrix4f matrix) {
    nodes.get(name).setAnimationTransform(matrix);
  }

  public void transform (double time) {
    angleOfRotation = (angleOfRotation + 1)%360;
    animation("robot1-whole-robot", new Matrix4f().rotate((float)Math.toRadians(angleOfRotation),0, 1,0));
    animation("robot2-whole-robot", new Matrix4f().rotate((float)Math.toRadians(angleOfRotation),0, 1,0));

    animation("robot1-armLower", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot1-armLower-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot1-claw1-mesh", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot1-claw2-mesh", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
    animation("robot1-claw1-mesh-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot1-claw2-mesh-r", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));



    animation("robot2-armLower", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot2-armLower-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot2-claw1-mesh", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot2-claw2-mesh", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
    animation("robot2-claw1-mesh-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot2-claw2-mesh-r", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
  }


}
