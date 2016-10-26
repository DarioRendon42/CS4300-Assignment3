import org.joml.Matrix4f;
import sgraph.INode;

import java.util.Map;

/**
 * Created by xiangfan on 10/25/16.
 */

//class that contains all transformation for robot arm
public class robotAnimation {
  private int angleOfRotation;
  private Map<String, INode> nodes;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;


  public robotAnimation(sgraph.IScenegraph<VertexAttrib> scenegraph) {
    this.scenegraph = scenegraph;
    this.nodes = scenegraph.getNodes();
    this.angleOfRotation = 0;
  }
  //add motion to a leaf
  public void animation(String name, Matrix4f matrix) {
    nodes.get(name).setAnimationTransform(matrix);
  }
  //animate scene graph
  public void transform (double time) {
    //set rotation angle
    angleOfRotation = (angleOfRotation + 1)%360;
    //rotate the whole robot arm
    animation("robot1-whole-robot", new Matrix4f().rotate((float)Math.toRadians(angleOfRotation),0, 1,0));
    animation("robot2-whole-robot", new Matrix4f().rotate((float)Math.toRadians(angleOfRotation),0, 1,0));
    //rotate lowerarm for robot1
    animation("robot1-armLower", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot1-armLower-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    //move claws for robot1
    animation("robot1-claw1-mesh", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot1-claw2-mesh", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
    animation("robot1-claw1-mesh-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot1-claw2-mesh-r", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
    //rotate lowerarm for robot2
    animation("robot2-armLower", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    animation("robot2-armLower-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 50) * 30),1, 0,0));
    //move claws for robot2
    animation("robot2-claw1-mesh", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot2-claw2-mesh", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
    animation("robot2-claw1-mesh-r", new Matrix4f().rotate((float)Math.toRadians(Math.sin(time / 20) * 40),0, 0,1));
    animation("robot2-claw2-mesh-r", new Matrix4f().rotate((float)Math.toRadians(- (Math.sin(time / 20) * 40)),0, 0,1));
  }


}
