package com.example.imagefilter.transformations;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.squareup.picasso.Transformation;

public class DitherTransformation implements Transformation {


    @Override
    public Bitmap transform(Bitmap imgBitmap) {

        Bitmap resultBitmap = imgBitmap.copy(Bitmap.Config.ARGB_8888, true); // copy original into new bitmap
        imgBitmap.recycle(); // release old memory

        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        colorRGB[][] d = new colorRGB[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                d[y][x] = new colorRGB(resultBitmap.getPixel(x, y));
            }
        }

        for (int y = 0; y < resultBitmap.getHeight(); y++) {
            for (int x = 0; x < resultBitmap.getWidth(); x++) {

                colorRGB oldColor = d[y][x];
                colorRGB newColor = findClosestPaletteColor(oldColor, palette);
                resultBitmap.setPixel(x, y, newColor.toIntRGBColor());

                colorRGB err = oldColor.sub(newColor);

                if (x + 1 < width) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
                }

                if (x - 1 >= 0 && y + 1 < height) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                }

                if (y + 1 < height) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
                }

                if (x + 1 < width && y + 1 < height) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                }
            }
        }

        return resultBitmap;
    }

    @Override
    public String key() {
        return "DitherTransformation";
    }

    private final colorRGB[] palette = new colorRGB[]{
            new colorRGB(0, 0, 0), // black
            new colorRGB(0, 0, 255), // green
            new colorRGB(0, 255, 0), // blue
            new colorRGB(0, 255, 255), // cyan
            new colorRGB(255, 0, 0), // red
            new colorRGB(255, 0, 255), // purple
            new colorRGB(255, 255, 0), // yellow
            new colorRGB(255, 255, 255)  // white
    };

    static class colorRGB {
        int r, g, b;

        public colorRGB(int c) {
            //Color color = new Color();
            r = Color.red(c);
            g = Color.green(c);
            b = Color.blue(c);
        }

        public colorRGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public colorRGB add(colorRGB o) {
            return new colorRGB(r + o.r, g + o.g, b + o.b);
        }

        public int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }

        public int diff(colorRGB o) {
            int Rdiff = o.r - r;
            int Gdiff = o.g - g;
            int Bdiff = o.b - b;
            int distanceSquared = Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
            return distanceSquared;
        }

        public colorRGB mul(double d) {
            return new colorRGB((int) (d * r), (int) (d * g), (int) (d * b));
        }

        public colorRGB sub(colorRGB o) {
            return new colorRGB(r - o.r, g - o.g, b - o.b);
        }

        public int toIntRGBColor() {
            return  Color.rgb(clamp(r),clamp(g),clamp(b));
        }
    }

    private static colorRGB findClosestPaletteColor(colorRGB c, colorRGB[] palette) {
        colorRGB closest = palette[0];

        for (colorRGB n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }
}
