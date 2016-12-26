package com.seanshend.objloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class TDModel {
    Vector<Float> v;
    Vector<Float> vn;
    Vector<Float> vt;
    Vector<TDModelPart> parts;
    FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    /**
     * The buffer holding the indices
     */
    private ByteBuffer indexBuffer;

    /**
     * Our texture pointer
     */
    private int[] textures = new int[3];


    public TDModel(Vector<Float> v, Vector<Float> vn, Vector<Float> vt,
                   Vector<TDModelPart> parts) {
        super();
        this.v = v;
        this.vn = vn;
        this.vt = vt;
        this.parts = parts;
    }

    private static float[] toPrimitiveArrayF(Vector<Float> vector) {
        float[] f;
        f = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            f[i] = vector.get(i);
        }
        return f;
    }

    public String toString() {
        String str = new String();
        str += "Number of parts: " + parts.size();
        str += "\nNumber of vertexes: " + v.size();
        str += "\nNumber of vns: " + vn.size();
        str += "\nNumber of vts: " + vt.size();
        str += "\n/////////////////////////\n";
        for (int i = 0; i < parts.size(); i++) {
            str += "Part " + i + '\n';
            str += parts.get(i).toString();
            str += "\n/////////////////////////";
        }
        return str;
    }

    public void draw(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        //Enable the vertex, texture and normal state
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);


        TDModelPart t = parts.get(0);
        Material m = t.getMaterial();
        if (m != null) {
            FloatBuffer a = m.getAmbientColorBuffer();
            FloatBuffer d = m.getDiffuseColorBuffer();
            FloatBuffer s = m.getSpecularColorBuffer();
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, a);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, s);
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, d);
        }
        //Point to our buffers
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, t.getNormalBuffer());


        gl.glDrawElements(GL10.GL_TRIANGLES, t.getFacesCount(), GL10.GL_UNSIGNED_SHORT, t.getFaceBuffer());
        //gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

    }

    public void buildVertexBuffer() {
        ByteBuffer vBuf = ByteBuffer.allocateDirect(v.size() * 4);
        vBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = vBuf.asFloatBuffer();
        vertexBuffer.put(toPrimitiveArrayF(v));
        vertexBuffer.position(0);
    }

    public void buildTextureBuffer() {
        ByteBuffer  byteBuf = ByteBuffer.allocateDirect(vt.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(toPrimitiveArrayF(vt));
        textureBuffer.position(0);
    }

    public void loadGLTexture(GL10 gl, Context context,int flag) {
        //Get the texture from the Android resource directory
        InputStream is;
        if(flag==1) {
             is = context.getResources().openRawResource(+R.drawable.cartoonmedhouse2);
        }
        else{
             is = context.getResources().openRawResource(+R.drawable.cartoonmedhouse2);
        }
       // InputStream is = context.getResources().openRawResource(+R.drawable.stone);
        Bitmap bitmap = null;
        try {
            //BitmapFactory is an Android graphics utility for images
            bitmap = BitmapFactory.decodeStream(is);

        } finally {
            //Always clear and close
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }

        //Generate there texture pointer
        gl.glGenTextures(3, textures, 0);

        //Create Nearest Filtered Texture and bind it to texture 0
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        //Create Linear Filtered Texture and bind it to texture 1
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        //Create mipmapped textures and bind it to texture 2
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[2]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
        /*
		 * This is a change to the original tutorial, as buildMipMap does not exist anymore
		 * in the Android SDK.
		 *
		 * We check if the GL context is version 1.1 and generate MipMaps by flag.
		 * Otherwise we call our own buildMipMap implementation
		 */
        if (gl instanceof GL11) {
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            //
        } else {
            buildMipmap(gl, bitmap);
        }

        //Clean up
        bitmap.recycle();
    }

    /**
     * Our own MipMap generation implementation.
     * Scale the original bitmap down, always by factor two,
     * and set it as new mipmap level.
     * <p>
     * Thanks to Mike Miller (with minor changes)!
     *
     * @param gl     - The GL Context
     * @param bitmap - The bitmap to mipmap
     */
    private void buildMipmap(GL10 gl, Bitmap bitmap) {
        //
        int level = 0;
        //
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        //
        while (height >= 1 || width >= 1) {
            //First of all, generate the texture from our bitmap and set it to the according level
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);

            //
            if (height == 1 || width == 1) {
                break;
            }

            //Increase the mipmap level
            level++;

            //
            height /= 2;
            width /= 2;
            Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);

            //Clean up
            bitmap.recycle();
            bitmap = bitmap2;
        }
    }


}


