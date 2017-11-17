package cz.filipklimes.mtsp;

import cz.eoa.configuration.EvolutionConfiguration;
import cz.eoa.configuration.EvolutionConfigurationBuilder;
import cz.eoa.cycle.EvolutionExecutor;
import cz.eoa.templates.Individual;
import cz.eoa.templates.IndividualWithAssignedFitness;
import cz.eoa.templates.StatisticsPerEpoch;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class Main
{

    private static final Random RANDOM = new Random();
    private static final int NUMBER_OF_TRAVELLERS = 3;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final Cities cities = DataLoader.load50();
    private static final RealCity depot = cities.getDepot();
    private static final double PROBABILITY_OF_CROSSOVER = 1;
    private static final int POPULATION_SIZE = 3_000;
    private static final int EPOCHS_COUNT = 500;
    private static final double PROBABILITY_OF_MUTATION = 0.25;

    public static void main(String[] args)
    {
        EvolutionConfiguration<List<City>, Tour, Long, MyStatisticsPerEpoch> evolutionConfiguration = (new EvolutionConfigurationBuilder<List<City>, Tour, Long, MyStatisticsPerEpoch>())
            .crossover(Main::crossOver)
            .mutation(Main::mutateBySwapping)
            .selector(Main::tournamentSelection)
            .replacement(currentPopulation -> new ArrayList<>()) //generational replacement strategy. keep nothing from previous population
            .populationInitialization(Main::intializeRandomIndividual) //strategy to initialize single individual - do it randomly
            .decoding(Main::decode)
            .fitnessAssessment(Main::calculateFitness)
            .fitnessIsMaximized(true)
            .parallel(true)
            .probabilityOfCrossover(PROBABILITY_OF_CROSSOVER)
            .populationSize(POPULATION_SIZE)
            .terminationCondition(epochs -> epochs.size() < EPOCHS_COUNT)  //when to terminate evolution, after 100 epochs has been reached
            .statisticsCreation(MyStatisticsPerEpoch::new)
            .build();

        EvolutionExecutor<List<City>, Tour, Long, MyStatisticsPerEpoch> evolutionExecutor = new EvolutionExecutor<>(evolutionConfiguration);
        List<MyStatisticsPerEpoch> statistics = evolutionExecutor.run();
        long time = statistics.stream().mapToLong(StatisticsPerEpoch::getExecution).sum();
        MyStatisticsPerEpoch bestEpoch = statistics.stream().max(Comparator.comparing(stats -> stats.getBestIndividual().getFitness())).get();
        logger.info("Executed in " + time + ", best solution in epoch " + bestEpoch.getEpoch());
        printTour(bestEpoch.getBestIndividual().getIndividual().decode(Main::decode));
        System.out.println("Worst epoch");
        MyStatisticsPerEpoch worstEpoch = statistics.stream().min(Comparator.comparing(stats -> stats.getBestIndividual().getFitness())).get();
        printTour(worstEpoch.getBestIndividual().getIndividual().decode(Main::decode));
    }

    private static IndividualWithAssignedFitness<List<City>, Tour, Long> tournamentSelection(final List<IndividualWithAssignedFitness<List<City>, Tour, Long>> population)
    {
        int winnerIndex = RANDOM.nextInt(population.size());
        for (int i = 0; i < 3; i++) {
            int candidate = RANDOM.nextInt(population.size());
            if (population.get(candidate).getFitness() > population.get(winnerIndex).getFitness()) {
                winnerIndex = candidate;
            }
        }

        return population.get(winnerIndex);
    }

    private static Optional<Individual<List<City>, Tour>> mutateBySwapping(final Individual<List<City>, Tour> individual)
    {
        List<City> genes = new ArrayList<>(individual.getGenes());
        if (RANDOM.nextDouble() < PROBABILITY_OF_MUTATION) {
            int x = RANDOM.nextInt(genes.size());
            int y;
            do {
                y = RANDOM.nextInt(genes.size());
            } while (y == x);
            Collections.swap(genes, x, y);

            return validate(genes)
                ? Optional.of(new Individual<>(genes))
                : Optional.of(individual);
        }

        return Optional.of(individual);
    }

    private static List<Individual<List<City>, Tour>> crossOver(final Individual<List<City>, Tour> firstParent, final Individual<List<City>, Tour> secondParent)
    {
        List<City> firstSetOfGenes = new ArrayList<>(firstParent.getGenes().size() - NUMBER_OF_TRAVELLERS + 1);
        List<City> secondSetOfGenes = new ArrayList<>(secondParent.getGenes().size() - NUMBER_OF_TRAVELLERS + 1);

        List<Integer> whereToSplit = new ArrayList<>();

        for (int i = 0; i < firstParent.getGenes().size(); i++) {
            City city = firstParent.getGenes().get(i);
            if (city instanceof SplitterCity) {
                whereToSplit.add(i);
                continue;
            }
            firstSetOfGenes.add(city);
        }
        for (City city : secondParent.getGenes()) {
            if (city instanceof SplitterCity) {
                continue;
            }
            secondSetOfGenes.add(city);
        }

        int a = RANDOM.nextInt(cities.getCities().size());
        int b = RANDOM.nextInt(cities.getCities().size());
        int start = Math.min(a, b);
        int end = Math.max(a, b);

        List<City> finalGenes = new ArrayList<>(firstSetOfGenes.size() + NUMBER_OF_TRAVELLERS - 1);
        for (int i = 0; i < firstSetOfGenes.size(); i++) {
            finalGenes.add(SplitterCity.INSTANCE);
        }

        for (int i = start; i <= end; i++) {
            finalGenes.set(i, firstSetOfGenes.get(i));
        }

        int i = 0;
        for (City city : secondSetOfGenes) {
            if (finalGenes.contains(city)) {
                continue;
            }
            if (i >= start && i <= end) {
                i = end + 1;
            }

            finalGenes.set(i, city);
            ++i;
        }

        for (Integer integer : whereToSplit) {
            finalGenes.add(integer, SplitterCity.INSTANCE);
        }

        if (!validate(finalGenes)) {
            throw new RuntimeException("Invalid genes");
        }

        return Collections.singletonList(new Individual<>(finalGenes));
    }

    @NotNull
    private static Individual<List<City>, Tour> intializeRandomIndividual()
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

        Tour tour = new Tour(cities.getDepot(), subTourList);
        return new Individual<>(tour.generateListOfAll());
    }

    @NotNull
    private static Tour decode(List<City> genes)
    {
        List<SubTour> subTourList = new ArrayList<>();
        List<RealCity> subTour = new ArrayList<>();
        CountingVisitor visitor = new CountingVisitor();

        for (City city : genes) {
            city.accept(visitor);
            if (visitor.encounteredSplitter()) {
                subTourList.add(new SubTour(depot, new ArrayList<>(subTour)));
                subTour.clear();
            } else {
                subTour.add((RealCity) city);
            }
        }

        subTourList.add(new SubTour(depot, new ArrayList<>(subTour)));
        return new Tour(depot, subTourList);
    }

    private static long calculateFitness(Tour tour)
    {
        return - tour.calculateDistance();
    }

    /**
     * Validate genes of MTSP solution.
     *
     * @param genes Genes information of the solution.
     * @return TRUE if the solution is valid, FALSE otherwise.
     */
    private static boolean validate(final List<City> genes)
    {
        int splitterCounter = 0;
        CountingVisitor visitor = new CountingVisitor();
        for (City city : genes) {
            city.accept(visitor);
            if (visitor.encounteredSplitter()) {
                splitterCounter++;
            }
            if (visitor.encounteredTwoSplittersInARow()) {
                return false;
            }
        }

        // Splitter at the end is no good
        if (visitor.encounteredSplitter()) {
            return false;
        }

        if (splitterCounter != NUMBER_OF_TRAVELLERS - 1) {
            return false;
        }

        return true;
    }

    private static void printTour(final Tour solution)
    {
        solution.getSubTourList().forEach(subTour -> {
            StringJoiner joiner = new StringJoiner(" -> ");
            joiner.add(cities.getDepot().toString());
            subTour.getCities().forEach(city -> {
                joiner.add(city.toString());
            });
            joiner.add(cities.getDepot().toString());
            System.out.println(joiner.toString());
        });
    }

    /**
     * Own implementation of class with statistics, most important is method getSummary(). It is used to store and print results
     */
    private static class MyStatisticsPerEpoch extends StatisticsPerEpoch<List<City>, Tour, Long>
    {

        MyStatisticsPerEpoch(int epoch, long execution, int countOfFitnessEvaluations, IndividualWithAssignedFitness<List<City>, Tour, Long> bestIndividual, List<IndividualWithAssignedFitness<List<City>, Tour, Long>> population)
        {
            super(epoch, execution, countOfFitnessEvaluations, bestIndividual, population);
        }

        public String getSummary()
        {
            Tour result = decode(bestIndividual.getGenes());
            return "Epoch "
                + epoch
                + ", avg. fitness: "
                + population.stream().mapToDouble(IndividualWithAssignedFitness::getFitness).average().orElse(0)
                + ", #fitness evaluations: "
                + countOfFitnessEvaluations
                + ", execution time:"
                + execution
                + "\n"
                + "result: "
                + result
                + ", best fitness: "
                + bestIndividual.getFitness().toString();
        }

        public IndividualWithAssignedFitness<List<City>, Tour, Long> getBestIndividual()
        {
            return bestIndividual;
        }

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

}
