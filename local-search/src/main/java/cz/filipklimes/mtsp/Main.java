package cz.filipklimes.mtsp;

import br.uece.tabusearch.IterationsStopCondition;
import br.uece.tabusearch.StaticTabuList;
import br.uece.tabusearch.TabuSearch;
import cz.filipklimes.mtsp.tabusearch.MtspSolution;
import cz.filipklimes.mtsp.tabusearch.SolutionLocator;

import java.util.*;

public class Main
{

    private static final int MAX_ITERATIONS = 100_000;
    private static final int TABU_LIST_SIZE = 200;
    private static final int NUMBER_OF_TRAVELLERS = 5;

    public static void main(String[] args)
    {
        Cities cities = DataLoader.load200();

        Tour initialSolution = createInitialSolution(cities, NUMBER_OF_TRAVELLERS);
        System.out.printf("Initial solution: %d%n", initialSolution.calculateDistance());
        printTour(cities, initialSolution);

        TabuSearch tabuSearch = new TabuSearch(new StaticTabuList(TABU_LIST_SIZE), new IterationsStopCondition(MAX_ITERATIONS), new SolutionLocator());
        MtspSolution solution = (MtspSolution) tabuSearch.run(new MtspSolution(initialSolution));

        System.out.printf("Best solution: %d%n", solution.getTour().calculateDistance());
        printTour(cities, solution.getTour());
    }

    private static void printTour(final Cities cities, final Tour initialSolution)
    {
        initialSolution.getSubTourList().forEach(subTour -> {
            StringJoiner joiner = new StringJoiner(" -> ");
            joiner.add(cities.getDepot().toString());
            subTour.getCities().forEach(city -> {
                joiner.add(city.toString());
            });
            joiner.add(cities.getDepot().toString());
            System.out.println(joiner.toString());
        });
    }

    private static Tour createInitialSolution(final Cities cities, final int numberOfTravellers)
    {
        final int size = cities.getCities().size();
        if (size < numberOfTravellers) {
            throw new RuntimeException("Too many travellers!");
        }

        final List<SubTour> subTourList = new ArrayList<>();
        List<RealCity> currentSubtour = new ArrayList<>();

        int citiesPerTraveller = (size / numberOfTravellers);
        int remainingItems = (size % numberOfTravellers);

        int citiesLoaded = 0;
        for (int i = 1; i <= numberOfTravellers; i++) {
            int extra = (i <= remainingItems) ? 1 : 0;
            for (int j = 0; j < citiesPerTraveller + extra; j++, citiesLoaded++) {
                currentSubtour.add(cities.getCities().get(citiesLoaded));
            }
            subTourList.add(new SubTour(cities.getDepot(), new ArrayList<>(currentSubtour)));
            currentSubtour.clear();
        }

        return new Tour(cities.getDepot(), subTourList);
    }

}
