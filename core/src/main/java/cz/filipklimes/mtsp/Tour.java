package cz.filipklimes.mtsp;

import java.util.*;

public class Tour
{

    private final List<SubTour> subTourList;
    private final int numberOfTravelers;
    private Integer totalNumberOfCities = null;
    private RealCity depot;

    private String cachedRepresentation;

    public Tour(final RealCity depot, final List<SubTour> subTourList)
    {
        this.depot = depot;
        this.subTourList = subTourList;
        this.numberOfTravelers = subTourList.size();
    }

    public long calculateDistance()
    {
        long max = Long.MIN_VALUE;
        for (SubTour subTour : subTourList) {
            long dist = subTour.calculateDistance();
            if (dist > max) {
                max = dist;
            }
        }

        return max;
    }

    public List<SubTour> getSubTourList()
    {
        return subTourList;
    }

    public int getNumberOfTravelers()
    {
        return numberOfTravelers;
    }

    public int getTotalNumberOfCities()
    {
        if (totalNumberOfCities == null) {

            int total = 0;
            for (SubTour subTour : subTourList) {
                total += subTour.getCities().size();
            }

            totalNumberOfCities = total;
        }

        return totalNumberOfCities;
    }

    public List<City> generateListOfAll()
    {
        List<City> allCities = new ArrayList<>();

        for (int i = 0; i < subTourList.size(); i++) {
            allCities.addAll(subTourList.get(i).getCities());
            if (i < subTourList.size() - 1) {
                allCities.add(SplitterCity.INSTANCE);
            }
        }

        return allCities;
    }

    public RealCity getDepot()
    {
        return depot;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tour tour = (Tour) o;

        return this.getCachedRepresentation().equals(tour.getCachedRepresentation());
    }

    public String getCachedRepresentation()
    {
        if (cachedRepresentation == null) {
            StringJoiner outerJoiner = new StringJoiner("|");
            subTourList.forEach(subTour -> {
                StringJoiner joiner = new StringJoiner("-");
                subTour.getCities().forEach(city -> {
                    joiner.add(String.valueOf(city.getX()));
                    joiner.add(",");
                    joiner.add(String.valueOf(city.getY()));
                });
                outerJoiner.add(joiner.toString());
            });
            cachedRepresentation = outerJoiner.toString();
        }
        return cachedRepresentation;
    }

    @Override
    public int hashCode()
    {
        return getCachedRepresentation().hashCode();
    }

}
