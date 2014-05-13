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
    ByteArrayDistribution series = new ByteArrayDistribution(10);
    
    public LongAggregate(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
    public boolean ingest(long value) {
        sum += value;
        count++;
        if (value < min) min = value;
        if (value > max) {
            max = value;
            return true;
        } else {
            return false;
        }
    }

    public boolean ingest(LongAggregate agg) {
        count += agg.count;
        sum += agg.sum;
        if (agg.min < min) min = agg.min;
        if (agg.max > max) {
            max = agg.max;
            return true;
        } else {
            return false;
        }
    }
    
    long avg() {
        return sum/count;
    }

    @Override
    public String toString() {
        if (count == 0) {
            return "empty";
        } else {
            return String.format("%s: %d, avg %dms", type, count, sum/count);
        }
    }
}
