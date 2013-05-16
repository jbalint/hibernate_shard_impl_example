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
import com.mysql.fabric.*;

/*
 * Setup:

create table domain_table(domain_id int not null primary key, value varchar(100) not null);

fabric sharding define RANGE hibtest -> 7
fabric sharding add_mapping 7 test.domain_table domain_id
fabric sharding add_shard 7 1 100 group_id-1 ENABLED
fabric sharding add_shard 7 101 200 group_id-2 ENABLED

 */

public class AppFabric {
    public static void main(String args[]) throws Exception {
	System.out.println("Hibernate Sharding Example - with FABRIC");

	AppFabric a = new AppFabric();

	SessionFactory sf = a.createSessionFactory("http://localhost:8080/RPC2");

	Session session = sf.openSession();
	session.beginTransaction();

	// add some Domain objects
	for (int i = 1; i < 10; ++i) {
	    int j = i;
	    Domain d = new Domain();
	    // put a few in the other shard
	    if ((j % 2) == 0)
		j += 100;
	    d.setId(j);
	    d.setValue("Hey! My id is *" + j + "*");
	    session.save(d);
	}

	session.getTransaction().commit();
	session.close();
    }

    public SessionFactory createSessionFactory(final String fabricUrl) throws Exception {
	/* TODO make this not depend on XML */
	Configuration prototypeConfig = new Configuration();
	Configuration c = prototypeConfig;
		c.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
		c.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		/* TODO need from user: database, username, password */
		c.setProperty("hibernate.connection.url", "jdbc:mysql://XYZBAD/test");
		c.setProperty("hibernate.connection.username", "root");
		c.setProperty("hibernate.connection.password", "");
		c.setProperty("hibernate.connection.shard_id", "0");
	prototypeConfig.addResource("domain.hbm.xml");

	List<ShardConfiguration> shardConfigs = new ArrayList<ShardConfiguration>();
	final Map<Server, Integer> shardMapping = new HashMap<>();
	Client fabricClient = new Client(fabricUrl);
	/* TODO connect to all groups? not sure what to do here.... */
	List<String> groupNames = fabricClient.getGroupNames();
	int shardId = 0;
	for (String groupName : groupNames) {
	    Group g = fabricClient.getGroup(groupName);
	    for (Server s : g.getServers()) {
		shardId++;

		/*Configuration*/ c = new Configuration();
		c.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
		c.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		/* TODO need from user: database, username, password */
		c.setProperty("hibernate.connection.url", "jdbc:mysql://" + s.getHostname() + "/test");
		c.setProperty("hibernate.connection.username", "root");
		c.setProperty("hibernate.connection.password", "");
		c.setProperty("hibernate.connection.shard_id", ""+shardId);
		ConfigurationToShardConfigurationAdapter adapter = new ConfigurationToShardConfigurationAdapter(c);
		shardConfigs.add(adapter);
		shardMapping.put(s, shardId);
	    }
	}

	ShardStrategyFactory shardStrategyFactory = new ShardStrategyFactory() {
		public ShardStrategy newShardStrategy(List<ShardId> shardIds) {
		    MyShardStrategy s = new MyShardStrategy(fabricUrl, shardMapping);
		    //ShardAccessStrategy pas = new MyShardAccessStrategy();
		    ShardAccessStrategy pas = new SequentialShardAccessStrategy();
		    return new ShardStrategyImpl(s, s, pas);
		}
	    };

	ShardedConfiguration shardedConfig = new ShardedConfiguration(prototypeConfig, shardConfigs, shardStrategyFactory);
	return shardedConfig.buildShardedSessionFactory();
    }
}
