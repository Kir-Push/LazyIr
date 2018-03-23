package com.example.buhalo.lazyir.old;

/**
 * Created by buhalo on 26.11.17.
 */
// just DTO object over bitmap
public class ImageDTO {

    private int[] pixels;
    private int width;
    private int height;
    private int size;

    public ImageDTO(int[] pixels, int width, int height, int size) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.size = size;
    }

    public ImageDTO(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.size = width * height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
