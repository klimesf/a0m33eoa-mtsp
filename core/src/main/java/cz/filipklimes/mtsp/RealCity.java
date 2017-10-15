package cz.filipklimes.mtsp;

public class RealCity implements City
{

    private int x;
    private int y;

    public RealCity(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public double distanceTo(RealCity city)
    {
        int xDistance = Math.abs(getX() - city.getX());
        int yDistance = Math.abs(getY() - city.getY());

        return Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
    }

    @Override
    public String toString()
    {
        return getX() + ", " + getY();
    }

    @Override
    public void accept(final CityVisitor visitor)
    {
        visitor.visit(this);
    }

}
