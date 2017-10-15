package cz.filipklimes.mtsp.tabusearch;

import br.uece.tabusearch.Solution;
import cz.filipklimes.mtsp.City;
import cz.filipklimes.mtsp.CityVisitor;
import cz.filipklimes.mtsp.RealCity;
import cz.filipklimes.mtsp.SplitterCity;
import cz.filipklimes.mtsp.SubTour;
import cz.filipklimes.mtsp.Tour;

import java.util.*;

public class MtspSolution implements Solution
{

    private final Tour tour;

    public MtspSolution(final Tour tour)
    {
        this.tour = tour;
    }

    @Override
    public Double getValue()
    {
        return (double) tour.calculateDistance();
    }

    @Override
    public List<Solution> getNeighbors()
    {
        List<Solution> neighbors = new ArrayList<>();

        // One-point neighbourhood
        // Select one point randomly and relocate it to all other positions of the tour
        // Care for invalid instances!

        List<City> forPermutation = tour.generateListOfAll();
        int toSwap = new Random().nextInt(forPermutation.size());

        for (int i = 0; i < forPermutation.size(); i++) {
            List<City> perm = new ArrayList<>(forPermutation);
            Collections.swap(perm, toSwap, i);
            if (validate(perm)) {
                neighbors.add(buildSolution(tour.getDepot(), perm));
            }
        }

        return neighbors;
    }

    private boolean validate(final List<City> swapped)
    {
        CountingVisitor visitor = new CountingVisitor();
        for (City city : swapped) {
            city.accept(visitor);
            if (visitor.encounteredTwoSplittersInARow()) {
                return false;
            }
        }

        // Splitter at the end is no good
        if (visitor.encounteredSplitter()) {
            return false;
        }

        return true;
    }

    private Solution buildSolution(final RealCity depot, final List<City> perm)
    {
        List<SubTour> subTourList = new ArrayList<>();

        List<RealCity> subTour = new ArrayList<>();
        CountingVisitor visitor = new CountingVisitor();
        for (City city : perm) {
            city.accept(visitor);
            if (visitor.encounteredSplitter()) {
                subTourList.add(new SubTour(depot, new ArrayList<>(subTour)));
                subTour.clear();
            } else {
                subTour.add((RealCity) city);
            }
        }

        subTourList.add(new SubTour(depot, new ArrayList<>(subTour)));

        return new MtspSolution(new Tour(depot, subTourList));
    }

    public Tour getTour()
    {
        return tour;
    }

    public static class CountingVisitor implements CityVisitor
    {
        private boolean justVisitedSplitter = true;
        private boolean secondSplitter = false;

        @Override
        public void visit(final RealCity city)
        {
            justVisitedSplitter = false;
        }

        @Override
        public void visit(final SplitterCity city)
        {
            if (justVisitedSplitter) {
                secondSplitter = true;
            }
            justVisitedSplitter = true;
        }

        public boolean encounteredSplitter()
        {
            return justVisitedSplitter;
        }

        public boolean encounteredTwoSplittersInARow()
        {
            return secondSplitter;
        }

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

        MtspSolution that = (MtspSolution) o;

        return tour != null ? tour.equals(that.tour) : that.tour == null;
    }

    @Override
    public int hashCode()
    {
        return tour != null ? tour.hashCode() : 0;
    }

}
