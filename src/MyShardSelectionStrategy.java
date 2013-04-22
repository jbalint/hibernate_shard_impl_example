import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

public class MyShardSelectionStrategy implements ShardSelectionStrategy {
	/**
	 * Determine the specific shard on which this object should reside
	 */
	public ShardId selectShardIdForNewObject(Object obj) {
		return null;
	}
}
