package com.github.liebharc.queryenrichment;

/**
 * Gives rough statistics about execution performance. Rough means that in doubt a detailed benchmarking should
 * still be executed.
 */
public class ExecutionStatistics {
    private long count = 0;
    private long durationSum = 0;

    public synchronized void add(long duration) {
        count++;
        durationSum += duration;
    }

    @Override
    public String toString() {
        return "ExecutionStatistics{" +
                "count=" + count +
                ", durationSum=" + durationSum +
                ", durationAverage=" + (durationSum / count)+
                '}';
    }
}
