package cz.filipklimes.mtsp;

import br.uece.tabusearch.StaticTabuList;
import br.uece.tabusearch.TabuSearch;
import cz.filipklimes.mtsp.tabusearch.MtspSolution;
import cz.filipklimes.mtsp.tabusearch.SolutionLocator;

import java.io.*;
import java.util.*;

public class Main
{

    private static final int MAX_ITERATIONS = 100_000;
    private static final int MAX_EVALUATIONS = 250_000;
    private static final int TABU_LIST_SIZE = 200;
    private static final int NUMBER_OF_TRAVELLERS = 5;

    public static void main(String[] args) throws IOException
    {
        Cities cities = DataLoader.load200();
        StatisticsExport.createFile("local", "200");

        for (int i = 0; i < 5; ++i) {
            Tour initialSolution = createInitialSolution(cities, NUMBER_OF_TRAVELLERS);
            System.out.printf("Initial solution: %d%n", initialSolution.calculateDistance());
            printTour(cities, initialSolution);

            TabuSearch tabuSearch = new TabuSearch(new StaticTabuList(TABU_LIST_SIZE), (x, solution) -> Tour.getNumberOfEvaluations() > MAX_EVALUATIONS, new SolutionLocator());
            MtspSolution solution = (MtspSolution) tabuSearch.run(new MtspSolution(initialSolution));

            System.out.printf("Best solution: %f%n", solution.getValue());
            printTour(cities, solution.getTour());
            Tour.resetNumberOfEvaluations();
            StatisticsExport.writeValues();
        }

        StatisticsExport.flush();
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
        List<City> genes = new ArrayList<>(cities.getCities());
        Collections.shuffle(genes);

        // Insert dividers
        final List<SubTour> subTourList = new ArrayList<>();
        List<RealCity> currentSubtour = new ArrayList<>();
        int citiesPerTraveller = (cities.getCities().size() / NUMBER_OF_TRAVELLERS);
        int remainingItems = (cities.getCities().size() % NUMBER_OF_TRAVELLERS);

        int citiesLoaded = 0;
        for (int i = 1; i <= NUMBER_OF_TRAVELLERS; i++) {
            int extra = (i <= remainingItems) ? 1 : 0;
            for (int j = 0; j < citiesPerTraveller + extra; j++, citiesLoaded++) {
                currentSubtour.add((RealCity) genes.get(citiesLoaded));
            }
            subTourList.add(new SubTour(cities.getDepot(), new ArrayList<>(currentSubtour)));
            currentSubtour.clear();
        }

        return new Tour(cities.getDepot(), subTourList);
    }

}
