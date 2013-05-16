import java.util.*;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.shards.*;
import org.hibernate.shards.cfg.*;
import org.hibernate.shards.loadbalance.*;
import org.hibernate.shards.strategy.*;
import org.hibernate.shards.strategy.access.*;
import org.hibernate.shards.strategy.resolution.*;
import org.hibernate.shards.strategy.selection.*;

public class App {
	public static void main(String args[]) {
		System.out.println("Hibernate Sharding Example");

		App a = new App();
		SessionFactory sf = a.createSessionFactory();
		Session session = sf.openSession();
		session.beginTransaction();

		// add some Domain objects
		for (int i = 0; i < 10; ++i) {
			Domain d = new Domain();
			d.setId(i);
			d.setValue("Hey! My id is *" + i + "*");
			session.save(d);
		}

		session.getTransaction().commit();
		session.close();
	}

	public SessionFactory createSessionFactory() {
		Configuration prototypeConfig = new Configuration().configure("shard0.hibernate.cfg.xml");
		prototypeConfig.addResource("domain.hbm.xml");
		List<ShardConfiguration> shardConfigs = new ArrayList<ShardConfiguration>();
		shardConfigs.add(buildShardConfig("shard0.hibernate.cfg.xml"));
		shardConfigs.add(buildShardConfig("shard1.hibernate.cfg.xml"));
		ShardStrategyFactory shardStrategyFactory = buildShardStrategyFactory();
		ShardedConfiguration shardedConfig = new ShardedConfiguration(prototypeConfig, shardConfigs, shardStrategyFactory);
		return shardedConfig.buildShardedSessionFactory();
	}

	ShardStrategyFactory buildShardStrategyFactory() {
        ShardStrategyFactory shardStrategyFactory = new ShardStrategyFactory() {
				public ShardStrategy newShardStrategy(List<ShardId> shardIds) {
					RoundRobinShardLoadBalancer loadBalancer = new RoundRobinShardLoadBalancer(shardIds);
					ShardSelectionStrategy pss = new RoundRobinShardSelectionStrategy(loadBalancer);
					ShardResolutionStrategy prs = new AllShardsShardResolutionStrategy(shardIds);
					ShardAccessStrategy pas = new SequentialShardAccessStrategy();
					return new ShardStrategyImpl(pss, prs, pas);
				}
			};

			return shardStrategyFactory;
	}

	ShardConfiguration buildShardConfig(String configFile) {
		Configuration config = new Configuration().configure(configFile);
		return new ConfigurationToShardConfigurationAdapter(config);
	}
}
