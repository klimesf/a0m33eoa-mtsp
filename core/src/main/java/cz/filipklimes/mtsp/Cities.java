package cz.filipklimes.mtsp;

import java.util.*;

public class Cities
{

    private final RealCity depot;
    private final List<RealCity> cities;

    public Cities(final RealCity depot, final List<RealCity> cities)
    {
        this.depot = depot;
        this.cities = cities;
    }

    public RealCity getDepot()
    {
        return depot;
    }

    public List<RealCity> getCities()
    {
        return cities;
    }

}
