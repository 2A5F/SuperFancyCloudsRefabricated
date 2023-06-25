package com.rimo.sfcr.util.gl;

public final class DVec3 {
    public final double[] xyz;

    public DVec3(double x, double y, double z) {
        this.xyz = new double[]{x, y, z};
    }

    public boolean eqXYZ(double x, double y, double z) {
        return this.xyz[0] == x && this.xyz[1] == y && this.xyz[2] == z;
    }

    public void setXYZ(double x, double y, double z) {
        this.xyz[0] = x;
        this.xyz[1] = y;
        this.xyz[2] = z;
    }

    public double getX() {
        return this.xyz[0];
    }

    public double getY() {
        return this.xyz[1];
    }

    public double getZ() {
        return this.xyz[2];
    }

    public void setX(double v) {
        this.xyz[0] = v;
    }

    public void setY(double v) {
        this.xyz[1] = v;
    }

    public void setZ(double v) {
        this.xyz[2] = v;
    }
}
