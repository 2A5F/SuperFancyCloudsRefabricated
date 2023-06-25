package com.rimo.sfcr.util.gl;

public final class FVec3 {
    public final float[] xyz;

    public FVec3(float x, float y, float z) {
        this.xyz = new float[]{x, y, z};
    }

    public boolean eqXYZ(float x, float y, float z) {
        return this.xyz[0] == x && this.xyz[1] == y && this.xyz[2] == z;
    }

    public void setXYZ(float x, float y, float z) {
        this.xyz[0] = x;
        this.xyz[1] = y;
        this.xyz[2] = z;
    }

    public float getX() {
        return this.xyz[0];
    }

    public float getY() {
        return this.xyz[1];
    }

    public float getZ() {
        return this.xyz[2];
    }

    public void setX(float v) {
        this.xyz[0] = v;
    }

    public void setY(float v) {
        this.xyz[1] = v;
    }

    public void setZ(float v) {
        this.xyz[2] = v;
    }
}
