package vellum.monitor;

/**
 *
 * @author evanx
 */
public class LongAggregate {
    String type;
    long sum;
    long max;
    long min = Long.MIN_VALUE;
    long count;
    
    public LongAggregate(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
    void ingest(long value) {
        if (value < min) min = value;
        if (value > max) max = value;
        sum += value;
        count++;
    }

    void ingest(LongAggregate agg) {
        count += agg.count;
        sum += agg.sum;
        if (agg.min < min) min = agg.min;
        if (agg.max > max) max = agg.max;
    }
    
    long avg() {
        return sum/count;
    }

    @Override
    public String toString() {
        if (count == 0) {
            return "empty";
        } else {
            return String.format("count %d, sum %d, avg %d", count, sum, sum/count);
        }
    }        
}
