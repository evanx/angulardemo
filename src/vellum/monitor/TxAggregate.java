package vellum.monitor;

/**
 *
 * @author evanx
 */
public class TxAggregate {
    TxAggregateMap aggregateMap; 
    
    void ingest(TimestampedTransaction tx) {
        LongAggregate agg = aggregateMap.get(tx.getType());
        agg.ingest(tx.getDuration());
    }
}
