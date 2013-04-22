import java.util.List;

import org.hibernate.shards.Shard;
import org.hibernate.shards.ShardOperation;
import org.hibernate.shards.strategy.access.ShardAccessStrategy;
import org.hibernate.shards.strategy.exit.ExitStrategy;
import org.hibernate.shards.strategy.exit.ExitOperationsCollector;

public class MyShardAccessStrategy implements ShardAccessStrategy {
	public <T> T apply(List<Shard> shards, ShardOperation<T> operation, ExitStrategy<T> exitStrategy, ExitOperationsCollector exitOperationsCollect) {
		return null;
	}
}
