package main.utils.statistics;

import main.constants.Statistic;
import main.utils.database.mongodb.StatisticsDB;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsManager {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private StatisticsManager() {};

    public void incrementStatistic(long increment, Statistic statistic) {
        executorService.execute(() -> StatisticsDB.ins().incrementStatistic(increment, statistic));
    }

    public static StatisticsManager ins() {
        return new StatisticsManager();
    }


}
