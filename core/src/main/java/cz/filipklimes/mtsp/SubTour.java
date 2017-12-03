package cz.filipklimes.mtsp;

import java.util.*;

public class SubTour
{

    private final RealCity depot;
    private List<RealCity> cities;
    private Long distCache = null;

    public SubTour(final RealCity depot, final List<RealCity> cities)
    {
        this.depot = depot;
        this.cities = cities;
    }

    public long calculateDistance()
    {
        if (distCache == null) {
            long dist = 0;

            RealCity from = depot;
            Iterator<RealCity> iterator = cities.iterator();
            RealCity to;
            while (iterator.hasNext()) {
                to = iterator.next();
                dist += from.distanceTo(to);
                from = to;
            }

            to = depot;
            dist += from.distanceTo(to);

            distCache = dist;
        }

        return distCache;
    }

    public List<RealCity> getCities()
    {
        return cities;
    }

    public void setCities(final List<RealCity> cities)
    {
        this.cities = cities;
        this.distCache = null;
    }

}
