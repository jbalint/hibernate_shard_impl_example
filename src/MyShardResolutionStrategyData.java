import java.io.Serializable;

import org.hibernate.shards.strategy.selection.ShardResolutionStrategyData;

public class MyShardResolutionStrategyData implements ShardResolutionStrategyData {
	public String getEntityName() {
		return null;
	}

	public Serializable getId() {
		return null;
	}
}
