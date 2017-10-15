package cz.filipklimes.mtsp;

import java.io.*;
import java.util.*;

public class DataLoader
{

    public static Cities load50()
    {
        return load("mtsp_data/mTSP_50.data");
    }

    public static Cities load100()
    {
        return load("mtsp_data/mTSP_100.data");
    }

    public static Cities load200()
    {
        return load("mtsp_data/mTSP_200.data");
    }

    private static Cities load(final String filename)
    {
        File file = new File(DataLoader.class.getClassLoader().getResource(filename).getFile());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String text;

            City depot = null;
            final List<City> cities = new ArrayList<>();

            while ((text = reader.readLine()) != null) {
                String[] split = text.split("\\s+");
                if (split.length != 2) {
                    throw new RuntimeException(String.format("Invalid line \"%s\" in file %s", text, filename));
                }
                City city = new City(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                if (depot == null) {
                    depot = city;
                } else {
                    cities.add(city);
                }
            }

            if (depot == null) {
                throw new RuntimeException("There is no depot!");
            }

            return new Cities(depot, cities);
        } catch (IOException e) {
            throw new RuntimeException("Could not load input data", e);
        }
    }

}
