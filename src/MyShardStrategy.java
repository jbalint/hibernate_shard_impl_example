import java.util.*;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.resolution.ShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardResolutionStrategyData;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

import com.mysql.fabric.*;

public class MyShardStrategy implements ShardSelectionStrategy, ShardResolutionStrategy {
	private Client fabricClient;
	private Map<Server, Integer> shardMapping;

	public MyShardStrategy(String fabricUrl, Map<Server, Integer> shardMapping) {
		try {
			this.fabricClient = new Client(fabricUrl);
			this.shardMapping = shardMapping;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Determine the specific shard on which this object should reside (be created)
	 */
	public ShardId selectShardIdForNewObject(Object obj) {
		try {
			Domain d = (Domain)obj;
			List<Server> servers = this.fabricClient.getServersForKey("test.domain_table", d.getId());
			/* TODO make sure we only return a master server */
			Integer shardId = shardMapping.get(servers.get(0));
			System.out.println("*********************** Domain id: " + d.getId() + " maps to shard " + shardId + " (" + servers.get(0) + ")");
			if (shardId != null)
				return new ShardId(shardId);
			else
				return null;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Determine the specific shard to fetch this object
	 */
	public List<ShardId> selectShardIdsFromShardResolutionStrategyData(ShardResolutionStrategyData data) {
		try {
			List<Server> servers = this.fabricClient.getServersForKey("test.domain_table", (Integer)data.getId());
			/* TODO same as above, but we don't need to return a master server */
			Integer shardId = shardMapping.get(servers.get(0));
			if (shardId != null) {
				List<ShardId> shards = new ArrayList<>();
				shards.add(new ShardId(shardId));
				return shards;
			} else {
				return null;
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
