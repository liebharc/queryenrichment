package com.github.liebharc.queryenrichment;

/**
 * Gives rough statistics about execution performance. Rough means that in doubt a detailed benchmarking should
 * still be executed.
 */
public class ExecutionStatistics {
    private long queryCount = 0;
    private long queryDuration = 0;
    private long totalCount = 0;
    private long totalDuration = 0;

    public synchronized void addQueryTime(long duration) {
        queryCount++;
        queryDuration += duration;
    }

    public synchronized void addTotal(long duration) {
        totalCount++;
        totalDuration += duration;
    }

    @Override
    public String toString() {
        return "ExecutionStatistics{" +
                "queryCount=" + queryCount +
                ", queryDuration=" + queryDuration +
                ", queryAverage=" + queryDuration / queryCount +
                ", totalCount=" + totalCount +
                ", totalDuration=" + totalDuration +
                ", totalAverage=" + totalDuration / totalCount +
                '}';
    }
}
