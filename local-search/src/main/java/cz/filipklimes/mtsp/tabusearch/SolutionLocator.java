package cz.filipklimes.mtsp.tabusearch;

import br.uece.tabusearch.BestNeighborSolutionLocator;
import br.uece.tabusearch.Solution;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.*;

public class SolutionLocator implements BestNeighborSolutionLocator
{

    @Override
    public Solution findBestNeighbor(final List<Solution> neighborsSolutions, final List<Solution> tabuList)
    {
        //remove any neighbor that is in tabu list
        CollectionUtils.filterInverse(neighborsSolutions, tabuList::contains);

        //sort the neighbors
        neighborsSolutions.sort(Comparator.comparing(Solution::getValue));

        //get the neighbor with lowest value
        return neighborsSolutions.get(0);
    }

}
