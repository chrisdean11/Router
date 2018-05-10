// Standard Libraries
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.math.*; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// XChange Core
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seeker
{
	public final Graph graph;
	public final List<Node> nodes;
	public final BigDecimal COST_INFINITE = new BigDecimal(0x0FFFFFFF); 
	// Acceptable value for a max cost. BigDecimal is stored as an array of ints. Arrays are indexed using ints.
	// Therefore, the largest BigDecimal is Integer.MAX_VALUE[Integer.MAX_VALUE]. Many GB.

	// Assumes that the ExchangeMonitors already have ther OrderBooks loaded
	public Seeker(List<ExchangeMonitor> monitors)
	{
		nodes = new ArrayList<Node>();
		System.out.println("Building Seeker: ");
		// Make a Node for each currency in each Monitor
		for (ExchangeMonitor monitor : monitors)
		{
			System.out.println("  " + monitor.getName());

			//for (Currency currency : monitor.getExchange().getExchangeMetaData().getCurrencies().keySet())
			for (Currency currency : monitor.getCurrencies())
			{
				nodes.add(new Node(monitor, currency));
				System.out.println("    " + currency.toString() + " Fees: " + monitor.getTradeFee() + monitor.getWithdrawFee());
			}
		}

		// Construct the graph
		graph = new Graph();
	}

	// TODO: Return residual of the final node in the path, for any arbitrary path.
	public BigDecimal calculatePath(BigDecimal tradeAmount, List<Node> path)
	{
		return new BigDecimal(0);
	}

// CAN NEVER TAKE THE SAME EDGE TWICE - priceAtDepth only covers the first traversal (can go through same node twice via different edges -- just make sure there's enough in account if trying to trade simultaneously)
// Make list of allNodes
// Make start node residual = tradeAmount, predecessor array = itself or null.
// While allNodes not empty:
	// Get a node in allNodes where predecessor array IS set: this node is connected to search-so-far and has stored its best path so far 
	// If any of its neighbors has unset preds:
		// Calculate and set its residual, set its pred array, and continue this loop.
	// Else they all have pred arrays:
		// If any one of them has a lower residual than what is calculated from this node:
			// If it's an edge that is already in current node's predecessor array 
				// Ignore it.
			// Else
				// Calculate and set its residual, set its pred array, and continue loop.
		// If one has a higher residual AND this edge was already taken in the current node's predecessor array, 
			// Is this an ERROR? You had arrived at this node before, with a higher residual, and somehow got back to it
		// Else this path is a dead end: It can't take any edges and give its neighbor a better residual than what's already been found.
			// Remove this node from allNodes***

// *** Maybe a performance improvement would be to have each node keep a list of its adjacent nodes left to shake out
// and remove them when they're no longer worth trying so you're not checking the same adjacent node again and again.
// Then the main loop can just go through the main list and look for nodes without an empty list of adjacencies.
	public void dijkstra(ExchangeMonitor startM, Currency startC, ExchangeMonitor endM, Currency endC, BigDecimal startAmount)
	{
		// Verify these monitors & currencies are part of the graph
		if (!startM.getCurrencies().contains(startC))
		{
			System.out.println("Graph: " + startM.getName() + " does not trade in " + startC);
			return;
		} 
		else if (!endM.getCurrencies().contains(endC))
		{
			System.out.println("Graph: " + endM.getName() + " does not trade in " + endC);
			return;
		}
		
		Node start = getNode(startM, startC);
		Node end = getNode(endM, endC);

		if (start == null)
		{
			System.out.println("Graph: Node not part of graph. " + startM.getName());
			return;
		}
		else if (end == null)
		{
			System.out.println("Graph: Node not part of graph. " + endM.getName());
			return;
		}

		for (Node node : nodes)
		{
			node.resetDijkstra();

			if (node == start)
			{
				node.balance = startAmount;
				node.bestPath.add(node);
			}
		}

		// Shallow copy of nodes list to chew through
		// List<Node> remainingNodes = new ArrayList<Node>(nodes);

		boolean madeChange = true; // Inefficient hack to get around remainingNodes questions.

		// remainingNodes are Nodes that still have adjacentNodes where a better path hasn't been found
		while (madeChange == true) // instead of (!remainingNodes.empty())
		{
			madeChange = false;
			//Set<Node> toRemove = new ArrayList<Node>();

			// Find a node that's been connected in the search so far
			for (Node node : nodes) //remainingNodes)
			{	
				// Check for removals
				//if (!node.bestPath.empty() && node.balance.compareTo(new BigDecimal(0)) <= 0)
				//{
				//	toRemove.add(node);
				//}
				// Nodes connected to search so far
				if (!node.bestPath.isEmpty())
				{
					int i = nodes.indexOf(node);

					// Check all adjacents
					for (int j = 0; j < nodes.size(); j++)
					{
						if (graph.adjacencyMatrix[i][j] == null) continue; // Not adjacent

						Edge edge = graph.adjacencyMatrix[i][j];

						// If first contact, or if this is a better path than edge.to.bestPath
						if (edge.to.bestPath.isEmpty() || edge.to.balance.compareTo(edge.calculateResidual(edge.from.balance)) < 0)
						{
							edge.to.balance = edge.calculateResidual(edge.from.balance);
							edge.to.bestPath = new ArrayList<Node>(edge.from.bestPath);
							edge.to.bestPath.add(edge.from);
							madeChange = true;
						}
					}
				}

			}

			// This may not work: A node could get slated for removal before a better path to it is found.
			// remainingNodes.removeAll(toRemove);
		}

	}

	public Node getNode(ExchangeMonitor monitor, Currency currency)
	{
		for (Node node : nodes)
		{
			if (monitor == node.monitor && currency == node.currency) return node;
		}

		return null;
	}

	private class Node 
	{
	    private ExchangeMonitor monitor;
	    private Currency currency;

	    // dijkstra results
	    public BigDecimal balance;
	    public List<Node> bestPath;

	    public Node(ExchangeMonitor m, Currency c) 
	    {
	        this.monitor = m;
	        this.currency = c;
	        resetDijkstra();
	    }

	    public void resetDijkstra()
	    {
	    	balance = new BigDecimal(0);
	    	bestPath = new ArrayList<Node>();
	    }
	
	    @Override
	    public boolean equals(Object o) 
	    {
	        if (this == o) return true;
	        if (!(o instanceof Node)) return false;
	        Node node = (Node) o;
	        return monitor == node.monitor && currency == node.currency;
	    }
	}

	/**
	 *  ASKS take the base and give the counter
	 *  BIDS take the counter and give the base
	 *
	 *	ASK: (Get LTC)  LTC <------ BTC    
	 *  BID: (Get BTC)  LTC ------> BTC    
	 */
	private class Edge
	{
		private Node from;
		private Node to;
		private BigDecimal rate;
		private BigDecimal cost;

		public Edge(Node fromNode, Node toNode)
		{
			from = fromNode;
			to = toNode;
		}

		// residual = (tradeAmount - cost) * exchangeRate
		public BigDecimal calculateResidual(BigDecimal tradeAmount)
		{
			rate = exchangeRate(tradeAmount);
			cost = cost(tradeAmount);
			System.out.println(this.toString()+":\n  CalculatingResidual: tradeAmount="+tradeAmount+" rate="+rate+" cost="+cost);
			BigDecimal amountMinusCost = tradeAmount.subtract(cost);
			BigDecimal residual = amountMinusCost.multiply(rate);

			// Never engage in a path where one's node goes to zero
			if (residual.compareTo(new BigDecimal(0)) < 0)
			{
				residual = new BigDecimal(0);
			}

			return residual;
		}

		// Get the exchange rate in the correct direction
		// NEED TO MAKE SURE TRADEAMOUNT IS IN TERMS OF BASE
		private BigDecimal exchangeRate(BigDecimal tradeAmount)
		{
			BigDecimal rate;

			if(from.monitor != to.monitor) // Withdraw
			{
				rate = new BigDecimal(1);
			}
			else
			{
				rate = from.monitor.getExchangeRate(from.currency, to.currency, tradeAmount);
			}

			System.out.print(this.toString() + " Exchange Rate = ");
			System.out.println(rate);
			return rate;
		}

		/*
		* cost = tradeAmount * tradeFee OR withdrawalFee
		* Works for either direction, since it doesn't depend on the exchange rate.
		* @tradeAmount is the "from" currency
		*/
		public BigDecimal cost(BigDecimal tradeAmount)
		{
			if (from.monitor.getName().compareTo(to.monitor.getName()) == 0) // Intra-exchange
			{
				return tradeAmount.multiply(tradeFee());
			}
			else // Withdraw-Deposit inter-exchange
			{
				System.out.println(from.monitor.getName() + " != " + to.monitor.getName());
				return withdrawalFee();
			}
		}

		private BigDecimal tradeFee()
		{
			Map<CurrencyPair, CurrencyPairMetaData> map = from.monitor.getExchange().getExchangeMetaData().getCurrencyPairs();
			BigDecimal fee;

			if(map.keySet().contains(new CurrencyPair(from.currency, to.currency)))
			{
				fee = map.get(new CurrencyPair(from.currency, to.currency)).getTradingFee();
			}
			else if(map.keySet().contains(new CurrencyPair(to.currency, from.currency)))
			{
				fee = map.get(new CurrencyPair(to.currency, from.currency)).getTradingFee();
			}
			else fee = COST_INFINITE;

			fee = from.monitor.getTradeFee(); // Temporary hack

			System.out.println("  [tradeFee " + fee + "] " + this.toString());
			return fee;
		}

		private BigDecimal withdrawalFee()
		{
			Map<Currency, CurrencyMetaData> map = from.monitor.getExchange().getExchangeMetaData().getCurrencies();
			BigDecimal fee = COST_INFINITE;

			if(from.currency.compareTo(to.currency) != 0) 
			{
				System.out.println("  WithdrawalFee: 'from' and 'to' currencies don't match: " + from.currency + "->" + to.currency);
			}
			else if (!map.keySet().contains(from.currency)) 
			{
				System.out.println("  WithdrawalFee: ExchangeMetaData doesn't contain " + from.currency);
			}
			else if (map.get(from.currency).getWithdrawalFee() == null) 
			{
				System.out.println("  WithdrawalFee: Not Found " + from.currency);
			}
			else fee = map.get(from.currency).getWithdrawalFee();

			System.out.println("FEE: " + from.monitor.getWithdrawFee());
			fee = from.monitor.getWithdrawFee(); // Temporary hack

			System.out.println("  [WithdrawalFee: " + fee + "] " + this.toString());
			return fee;
		}

		public String toString()
		{
			return new String(from.monitor.getName() + "."+ from.currency + "->" + to.monitor.getName() + "."+ to.currency);
		}
	}

	// To check all connections, find your node in nodes and go through nodes again.
	// adjacencyMatrix[i][j] i = from, j = to
	public class Graph
	{
		// For every node, there is a (possibly null) edge to each other node
		private Edge[][] adjacencyMatrix;

		public Graph()
		{
			System.out.println("Building Graph");
			List<Node> nodes = Seeker.this.nodes; // Just to make this function easier to read
			adjacencyMatrix = new Edge[nodes.size()][nodes.size()];

			for (int i = 0; i < nodes.size(); i++)
			{
				for (int j = 0; j < nodes.size(); j++)
				{
					// If there's no connection or if i==j, position in table is null
					adjacencyMatrix[i][j] = null;

					// Intra-exchange edge (Same exchange, different node)
					if ( (nodes.get(i).monitor == nodes.get(j).monitor) && (i != j))
					{
						/*Base vs Counter shouldn't matter here. Every i edge to every j edge*/
						// base is i and counter is j
						//if ( nodes.get(i).monitor.getCurrencyPairs().contains( new CurrencyPair(nodes.get(i).currency, nodes.get(j).currency) ))
						//{
						//	adjacencyMatrix[i][j] = new Edge(nodes.get(i), nodes.get(j));
						//}
						if(nodes.get(i).monitor.getCurrencyPairs().contains(new CurrencyPair(nodes.get(i).currency, nodes.get(j).currency))
							|| nodes.get(i).monitor.getCurrencyPairs().contains(new CurrencyPair(nodes.get(j).currency, nodes.get(i).currency)) 
							)
						{
							adjacencyMatrix[i][j] = new Edge(nodes.get(i), nodes.get(j));
						}
						else System.out.println(nodes.get(i).monitor.getName() + " doesn't trade " + nodes.get(i).currency + "-" + nodes.get(j).currency);
					}
					// Inter-exchange withdrawal (Different exchange, same currrency)
					else if( (nodes.get(i).monitor != nodes.get(j).monitor) && (nodes.get(i).currency.compareTo(nodes.get(j).currency) == 0) ) 
					{
						adjacencyMatrix[i][j] = new Edge(nodes.get(i), nodes.get(j));
					}
				}
			}

			System.out.println("Graph constructor complete.");
		}

		// Prints to csv in the form of adjacency tables
		// i,j == row,column
		public String toString()
		{
			/*
			 * Make raw adjacency tables with labels
			 * Add +2 size to length and height
			 * Presumes dijkstra has been run
			 */
			String edgeTable[][]     	= new String[nodes.size() + 2][nodes.size() + 2];
			String rateTable[][]     	= new String[nodes.size() + 2][nodes.size() + 2];
			String tradeFeeTable[][] 	= new String[nodes.size() + 2][nodes.size() + 2];
			String withdrawFeeTable[][] = new String[nodes.size() + 2][nodes.size() + 2];
			String costTable[][] 		= new String[nodes.size() + 2][nodes.size() + 2];
			String residualTable[][] 	= new String[nodes.size() + 2][nodes.size() + 2];

			// Initialize
			for (int i = 0; i < (nodes.size()+2); i++)
			{
				for (int j = 0; j < (nodes.size()+2); j++)
				{
					rateTable[i][j] = "";
					tradeFeeTable[i][j] = ""; 	
					withdrawFeeTable[i][j] = ""; 
					costTable[i][j] = ""; 		
					residualTable[i][j] = ""; 	
				}
			}

			// Labels
			rateTable[2][0] = nodes.get(0).monitor.getName();
			rateTable[2][1] = nodes.get(0).currency.toString();
			rateTable[0][2] = nodes.get(0).monitor.getName();
			rateTable[1][2] = nodes.get(0).currency.toString();
			for (int i = 1; i < nodes.size(); i++)
			{
				if (nodes.get(i).monitor != nodes.get(i-1).monitor)
				{
					rateTable[i+2][0] = nodes.get(i).monitor.getName();
					rateTable[0][i+2] = nodes.get(i).monitor.getName();	
				}

				rateTable[i+2][1] = nodes.get(i).currency.toString();
			}

			// Copy Values
			for (int i = 0; i < nodes.size(); i++)
			{
				for (int j = 0; j < nodes.size(); j++)
				{
					if (adjacencyMatrix[i][j] == null) continue;
					edgeTable[i+2][j+2] = adjacencyMatrix[i][j].toString();
					if (adjacencyMatrix[i][j].rate != null) rateTable[i+2][j+2] = adjacencyMatrix[i][j].rate.toString();
					if (adjacencyMatrix[i][j].tradeFee() != null) tradeFeeTable[i+2][j+2] = adjacencyMatrix[i][j].tradeFee().toString();
					if (adjacencyMatrix[i][j].withdrawalFee() != null) withdrawFeeTable[i+2][j+2]  = adjacencyMatrix[i][j].withdrawalFee().toString();
					if (adjacencyMatrix[i][j].cost != null) costTable[i+2][j+2] = adjacencyMatrix[i][j].cost.toString();
					if (adjacencyMatrix[i][j].to.balance != null) residualTable[i+2][j+2] = adjacencyMatrix[i][j].to.balance.toString();
				}
			}

			/*
			 * Turn tables into csv strings
			 */
			String rateString 			= "";
			String tradeFeeString 		= "";
			String withdrawFeeString 	= "";
			String costString 			= "";
			String redidualString 		= "";
			// Copy Values
			for (int i = 0; i < nodes.size() + 2; i++)
			{
				for (int j = 0; j < nodes.size() + 2; j++)
				{
					rateString 			+= rateTable[i][j] + ",";
					tradeFeeString 	 	+= tradeFeeTable[i][j] + ",";
					withdrawFeeString  	+= withdrawFeeTable[i][j] + ",";
					costString 	 		+= costTable[i][j] + ",";
					redidualString 		+= residualTable[i][j] + ",";
				}

				rateString 			+= "\n";
				tradeFeeString 	 	+= "\n";
				withdrawFeeString  	+= "\n";
				costString 	 		+= "\n";
				redidualString 		+= "\n";
			}

			return rateString + "\n\n" + tradeFeeString + "\n\n" + withdrawFeeString + "\n\n" + costString + "\n\n" + redidualString;
		}
	}
}






