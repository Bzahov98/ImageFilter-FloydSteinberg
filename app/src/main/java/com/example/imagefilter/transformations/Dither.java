package com.example.imagefilter.transformations;

import android.graphics.Bitmap;
import android.graphics.Color;
@Deprecated
public class Dither {

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

    public static Bitmap floydSteinbergDithering(Bitmap imgBitmap) {


        colorRGB[] palette = new colorRGB[]{
                new colorRGB(0, 0, 0), // black
                new colorRGB(0, 0, 255), // green
                new colorRGB(0, 255, 0), // blue
                new colorRGB(0, 255, 255), // cyan
                new colorRGB(255, 0, 0), // red
                new colorRGB(255, 0, 255), // purple
                new colorRGB(255, 255, 0), // yellow
                new colorRGB(255, 255, 255)  // white
        };

        int w = imgBitmap.getWidth();
        int h = imgBitmap.getHeight();

        colorRGB[][] d = new colorRGB[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new colorRGB(imgBitmap.getPixel(x, y));
            }
        }

        for (int y = 0; y < imgBitmap.getHeight(); y++) {
            for (int x = 0; x < imgBitmap.getWidth(); x++) {

                colorRGB oldColor = d[y][x];
                colorRGB newColor = findClosestPaletteColor(oldColor, palette);
                imgBitmap.setPixel(x, y, newColor.toIntRGBColor());

                colorRGB err = oldColor.sub(newColor);

                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
                }

                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                }

                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
                }

                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                }
            }
        }

        return imgBitmap;
    }
}