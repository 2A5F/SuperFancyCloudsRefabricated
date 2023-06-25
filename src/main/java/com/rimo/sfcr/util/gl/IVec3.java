package com.rimo.sfcr.util.gl;

public final class IVec3 {
    public final int[] xyz;

    public IVec3(int x, int y, int z) {
        this.xyz = new int[]{x, y, z};
    }

    public boolean eqXYZ(int x, int y, int z) {
        return this.xyz[0] == x && this.xyz[1] == y && this.xyz[2] == z;
    }

    public void setXYZ(int x, int y, int z) {
        this.xyz[0] = x;
        this.xyz[1] = y;
        this.xyz[2] = z;
    }

    public int getX() {
        return this.xyz[0];
    }

    public int getY() {
        return this.xyz[1];
    }

    public int getZ() {
        return this.xyz[2];
    }

    public void setX(int v) {
        this.xyz[0] = v;
    }

    public void setY(int v) {
        this.xyz[1] = v;
    }

    public void setZ(int v) {
        this.xyz[2] = v;
    }
}
