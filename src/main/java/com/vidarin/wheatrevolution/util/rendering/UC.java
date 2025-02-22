package com.vidarin.wheatrevolution.util.rendering;

/* UC stands for "Unit Converter" */
public class UC {
    public static final double PIXEL_LENGTH = 0.0625D;

    public static float fromPixels(double pixels) {
        return (float) (PIXEL_LENGTH * pixels);
    }

    public static float fromBlocks(double blocks) {
        return (float) (blocks * 16);
    }
}
