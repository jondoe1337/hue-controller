package de.jondoe.hue;

public enum CommonColors
{
 WHITE(255, 255, 255);

    private int r;
    private int g;
    private int b;

    private CommonColors(int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed()
    {
        return r;
    }

    public int getGreen()
    {
        return g;
    }

    public int getBlue()
    {
        return b;
    }
}