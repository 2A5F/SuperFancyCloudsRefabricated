package com.rimo.sfcr.util.gl;

public final class FVec2 {
    public final float[] xy;

    public FVec2(float x, float y) {
        this.xy = new float[]{x, y};
    }

    public boolean eqXYZ(float x, float y) {
        return this.xy[0] == x && this.xy[1] == y;
    }

    public void setXY(float x, float y) {
        this.xy[0] = x;
        this.xy[1] = y;
    }

    public float getX() {
        return this.xy[0];
    }

    public float getY() {
        return this.xy[1];
    }


    public void setX(float v) {
        this.xy[0] = v;
    }

    public void setY(float v) {
        this.xy[1] = v;
    }

}
