package cz.filipklimes.mtsp;

import java.io.*;
import java.util.*;

public class StatisticsExport
{

    private static FileWriter writer;
    private static List<String> values = new ArrayList<>();

    public synchronized static void addValue(final String value)
    {
        values.add(value);
    }

    public synchronized static void createFile(final String type, final String size) throws IOException
    {
        if (writer != null) {
            throw new IllegalStateException("writer already initialized");
        }
        String csvFile = type + "_" + size + ".csv";
        writer = new FileWriter(csvFile);
    }

    public synchronized static void writeValues() throws IOException
    {
        if (writer == null) {
            throw new IllegalStateException("writer not initialized");
        }
        CSVUtils.writeLine(writer, values, ';', '"');
        values.clear();
    }

    public synchronized static void flush() throws IOException
    {
        if (writer == null) {
            throw new IllegalStateException("writer not initialized");
        }
        writer.flush();
        writer.close();
    }

}
