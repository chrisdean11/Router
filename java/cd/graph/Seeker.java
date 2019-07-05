// Standard Libraries
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.math.*; 
import java.time.LocalDateTime;

// XChange Core
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
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
    private Node startNode;
    public final BigDecimal COST_INFINITE = new BigDecimal(0x0FFFFFFF); 
    // This is an acceptable value for a max cost. BigDecimal is stored as an array of ints. Arrays are indexed using ints.
    // Therefore, the largest BigDecimal is Integer.MAX_VALUE[Integer.MAX_VALUE]

// TODO need some way to reduce graph to currencies of interest

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
                System.out.println("    " + currency.toString() + " Fees: " + monitor.getTradeFee() + monitor.getWithdrawFee(currency));
            }
        }

        // Construct the graph
        graph = new Graph(nodes);
    }

    // TODO: Return residual of the final node in the path, for any arbitrary path.
    public BigDecimal calculatePath(BigDecimal tradeAmount, List<Node> path)
    {
        return new BigDecimal(0);
    }

// One path CAN NEVER TAKE THE SAME EDGE TWICE - priceAtDepth only covers the first traversal (can go through same node twice via different edges -- just make sure there's enough in account if trying to trade simultaneously)
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
                // Wait -- this means that coming back the way you came is a higher residual somehow. Should investigate if this is ever possible.
                // Would have to take into account that you have to go deeper into the order books
                // But, a trade in each direction would cancel each other out, so you'd only have to trade their difference.
            // Else
                // Calculate and set its residual, set its pred array, and continue loop.
        // If one has a higher residual AND this edge was already taken in the current node's predecessor array, 
            // Is this an ERROR? You had arrived at this node before, with a higher residual, and somehow got back to it
        // Else this path is a dead end: It can't take any edges and give its neighbor a better residual than what's already been found.
            // Remove this node from allNodes***

// This simplification is what's actually being used in dijkstra()
// Rethinking the above algorithm, it seems like desirable paths will be missed.
    // Just keep checking the connections to every single node until a loop finds nothing new

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
        startNode = start;

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
                node.bestPath = new ArrayList<Node>();
                node.touched = true;
            }
        }

        // Seed the map by getting residuals for all nodes adjacent to start node
        int s = nodes.indexOf(start);

        for (int j = 0; j < nodes.size(); j++)
        {
            if (graph.adjacencyMatrix[s][j] == null) continue; // Not adjacent
            Edge edge = graph.adjacencyMatrix[s][j];

            edge.to.balance = edge.calculateResidual(edge.from.balance);
            edge.to.bestPath = new ArrayList<Node>();
            edge.to.bestPath.add(edge.from);
            edge.to.touched = true;
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
                // TODO: Speed this up by maintaining a 'touched' list of nodes whose residual changed last loop, 
                // skip calculations on nodes that aren't downstream 
                node.touchedLast = node.touched;
                node.touched = false;

                // Check for removals
                //if (!node.bestPath.empty() && node.balance.compareTo(new BigDecimal(0)) <= 0)
                //{
                //  toRemove.add(node);
                //}

                // Nodes connected to search so far and have been changed since the last turn
                if (!node.bestPath.isEmpty() && node.touched == false)
                {
                    int i = nodes.indexOf(node);

                    // Check all adjacents
                    for (int j = 0; j < nodes.size(); j++)
                    {
                        // adjacencyMatrix: i is 'from' node, j is 'to' node
                        if (graph.adjacencyMatrix[i][j] == null) continue; // Not adjacent

                        Edge edge = graph.adjacencyMatrix[i][j];

                        // If first contact, or if this is a better path than edge.to.bestPath
                        if (edge.to.bestPath.isEmpty() || edge.to.balance.compareTo(edge.calculateResidual(edge.from.balance)) < 0)
                        {
                            edge.to.balance = edge.calculateResidual(edge.from.balance);
                            edge.to.bestPath = new ArrayList<Node>(edge.from.bestPath);
                            edge.to.bestPath.add(edge.from);
                            edge.to.touched = true;
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
}






