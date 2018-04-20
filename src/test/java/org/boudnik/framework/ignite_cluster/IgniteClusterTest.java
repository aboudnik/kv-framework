package org.boudnik.framework.ignite_cluster;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * @author Dmitrii_Kniazev
 * @since 04/20/2018
 */
public class IgniteClusterTest {

	@Test
	public void checkNumberOfNodesTest() {
		Ignition.setClientMode(true);
		try (final Ignite ignite = Ignition.start()) {
			final IgniteCluster cluster = ignite.cluster();
			final Collection<ClusterNode> nodes = cluster.nodes();
			final long actualNodesNumber = nodes.stream().filter(node -> !node.isClient()).count();
			Assert.assertEquals(2L,actualNodesNumber);
		}
	}
}
