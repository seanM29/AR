package com.seanshend.objloader;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Debug;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer extends GLSurfaceView implements Renderer {

    private final float TOUCH_SCALE = 0.4f;        //Proved to be good for normal rotation ( NEW )
    /**
     * Triangle instance
     */
    private OBJParser parser, parser2;
    private TDModel model;
    // model2;
    /* Rotation values */
    private float[] xrot = {0.0f, 0.0f};                    //X Rotation

    /* Rotation speed values */
    private float[] yrot = {0.0f, 0.0f};                    //Y Rotation
    int choice =0;      //which obj to change

    private float xspeed;                //X Rotation Speed ( NEW )
    private float yspeed;                //Y Rotation Speed ( NEW )
    private float z = 0.0f;
    private float oldX;
    private float oldY;
    private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};

    private float[] spot_direction = {0.0f, 0.0f, -1.0f};

    private FloatBuffer lightAmbientBuffer;
    private FloatBuffer lightDiffuseBuffer;
    private FloatBuffer lightPositionBuffer;
    private FloatBuffer spot_directionBuffer;

    private boolean light = false;

    //camera:
    private float cx = 0f;
    private float cy = 0f;
    private float cz = -5f;

    private Context context;
    ;
    private float[] objx = {0.0f, 0.0f};
    private float[] objy = {0.0f, 1.5f};
    private float[] objz = {0.0f, 0.f};


    public MyRenderer(Context ctx) {
        super(ctx);
        context = ctx;

        parser = new OBJParser(ctx);
        parser2 = new OBJParser(ctx);

        model = parser.parseOBJ("cartoonmedhouse2.obj");
       // model2 = parser2.parseOBJ("Cow_dABF.obj");

        Debug.stopMethodTracing();
        this.setRenderer(this);
        this.requestFocus();
        this.setFocusableInTouchMode(true);

        updatebuffer();

    }

    private void updatebuffer() {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightAmbientBuffer = byteBuf.asFloatBuffer();
        lightAmbientBuffer.put(lightAmbient);
        lightAmbientBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightDiffuseBuffer = byteBuf.asFloatBuffer();
        lightDiffuseBuffer.put(lightDiffuse);
        lightDiffuseBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightPositionBuffer = byteBuf.asFloatBuffer();
        lightPositionBuffer.put(lightPosition);
        lightPositionBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(spot_direction.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        spot_directionBuffer = byteBuf.asFloatBuffer();
        spot_directionBuffer.put(spot_direction);
        spot_directionBuffer.position(0);


    }

    /**
     * The Surface is created/init()
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//And there'll be light!
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);        //Setup The Ambient Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);        //Setup The Diffuse Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);    //Position The Light ( NEW )
        gl.glEnable(GL10.GL_LIGHT0);                                            //Enable Light 0 ( NEW )

        //Settings
        gl.glDisable(GL10.GL_DITHER);                //Disable dithering ( NEW )
        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping
        gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    //Black Background
        gl.glClearDepthf(1.0f);                    //Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);            //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);


        model.loadGLTexture(gl, this.context, 1);
        //model2.loadGLTexture(gl, this.context, 2);

    }

    /**
     * Here we do our drawing
     */
    public void onDrawFrame(GL10 gl) {


        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();                    //Reset The Current Modelview Matrix

        //Check if the light flag has been set to enable/disable lighting
        if (light) {
            gl.glEnable(GL10.GL_LIGHTING);
        } else {
            gl.glDisable(GL10.GL_LIGHTING);
        }

        // When using GL_MODELVIEW, you must set the view point
        GLU.gluLookAt(gl, cx, cy, cz, 0f, 0f, 0f, 0f, 1.0f, 0.0f);


        gl.glPushMatrix();
        gl.glTranslatef(objx[0], objy[0], objz[0]);    //Move down 1.2 Unit And Into The Screen 6.0

        gl.glRotatef(xrot[0], 1.0f, 0.0f, 0.0f);    //X
        gl.glRotatef(yrot[0], 0.0f, 1.0f, 0.0f);    //Y
        model.draw(gl);                        //Draw the square
        gl.glPopMatrix();


        gl.glPushMatrix();

        gl.glRotatef(xrot[1], 1.0f, 0.0f, 0.0f);    //X
        gl.glRotatef(yrot[1], 0.0f, 1.0f, 0.0f);    //Y
        gl.glTranslatef(objx[1], objy[1], objz[1]);    //Move down 1.2 Unit And Into The Screen 6.0

//        model2.draw(gl);                        //Draw the square
        gl.glPopMatrix();


        gl.glLoadIdentity();

        //   xrot += xspeed;
        // yrot += yspeed;

    }

    /**
     * If the surface changes, reset the view
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) {                        //Prevent A Divide By Zero By
            height = 1;                        //Making Height Equal One
        }

        gl.glViewport(0, 0, width, height);    //Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
        gl.glLoadIdentity();                    //Reset The Projection Matrix

        //Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 500.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);    //Select The Modelview Matrix
        gl.glLoadIdentity();                    //Reset The Modelview Matrix
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //
        float x = event.getX();
        float y = event.getY();
        /*for(int i=0;i<3;i++){
            lightAmbient[i]=0.0f;
			lightDiffuse[i]=0.0f;
		}*/



        //If a touch is moved on the screen
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //Calculate the change
            float dx = x - oldX;
            float dy = y - oldY;
            //Define an upper area of 10% on the screen
            int upperArea = this.getHeight() / 10;
            int lowerArea = 9 * this.getHeight() / 10;
            int leftArea = this.getWidth() / 4;
            int rightArea = 3 * this.getWidth() / 4;


            //chooose which obj to change
            if (x < leftArea) {
                if (choice == 0) {
                    choice = 1;
                } else {
                    choice = 0;
                }
            } else if (x > leftArea && x < rightArea) {

                //Zoom in/out if the touch move has been made in the upper
                if (y < upperArea) {
                    objz[choice] -= dx * TOUCH_SCALE / 2;


                }

                //Rotate around the axis otherwise
                else if (y >= upperArea && y <= lowerArea) {

                    xrot[choice] += dy * TOUCH_SCALE;
                    yrot[choice] += dx * TOUCH_SCALE;
                }

                //摄像机漫游
                else {
                    cx += dx * TOUCH_SCALE/2;
                }
            } else {

                //光照
                if (y > this.getHeight() / 2) {
                    if (light) {
                        light = false;
                    } else {
                        light = true;
                    }
                }
                //移动物体
                else {
                    objy[choice]+=dy*TOUCH_SCALE/10;
                }
            }
            //A press on the screen
        } else if (event.getAction() == MotionEvent.ACTION_UP) {


            //  light = true;

        }

        //Remember the values
        oldX = x;
        oldY = y;


        updatebuffer();
        //We handled the event
        return true;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            z -= 3;

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            z += 3;

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

        }

        //We handled the event
        return true;
    }
}