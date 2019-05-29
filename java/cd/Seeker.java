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

import java.io.PrintWriter;
import java.io.FileWriter;
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
    private BigDecimal startBalance;
    private final Map<Currency, BigDecimal> allPrices;
    private final String LOG_DIRECTORY;
    public final BigDecimal COST_INFINITE = new BigDecimal(0x0FFFFFFF); 
    // This is an acceptable value for a max cost. BigDecimal is stored as an array of ints. Arrays are indexed using ints.
    // Therefore, the largest BigDecimal is Integer.MAX_VALUE[Integer.MAX_VALUE]

    // Assumes that the ExchangeMonitors already have ther OrderBooks loaded
    public Seeker(List<ExchangeMonitor> monitors, String logDirectory, Map<Currency, BigDecimal> prices)
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
        graph = new Graph();

        LOG_DIRECTORY=logDirectory;
        allPrices = prices;
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
        startBalance = startAmount;

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
                // TODO: Speed this up by maintaining a 'touched' list of nodes whose residual changed, 
                // skip calculations on nodes that aren't downstream 

                // Check for removals
                //if (!node.bestPath.empty() && node.balance.compareTo(new BigDecimal(0)) <= 0)
                //{
                //  toRemove.add(node);
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

    // Maybe make this a member of Graph
    // Should be automatically called whenever Node.balance gets changed
    public touch(Node n)
    {
        // Every edge leading from this node is an edge that's ready to try
        // To start, this will only be edges with n as its 'from' node
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

        public String bestPathToString()
        {
            String ret = new String();

            for (Node node : bestPath)
            {
                ret += node.monitor.getName() + "_" + node.currency + ",";
            }

            ret += monitor.getName() + "_" + currency;
            return ret;
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
     *  ASKS you take the base and give the counter
     *  BIDS you take the counter and give the base
     *
     *  ASK: (Get LTC)  LTC <------ BTC    
     *  BID: (Get BTC)  LTC ------> BTC    
     */
    private class Edge
    {
        private Node from;
        private Node to;

        public Edge(Node fromNode, Node toNode)
        {
            from = fromNode;
            to = toNode;
        }

        public BigDecimal calculateResidual(BigDecimal tradeAmount)
        {
            BigDecimal ret = from.fakeTrade(from.currency, to.currency, tradeAmount);
            System.out.println(this.toString()+":\n  CalculatingResidual: tradeAmount="+tradeAmount+" result="+ret);
        }

        /*
        * fee = tradeAmount * tradeFee OR withdrawalFee
        * Works for either direction, since it doesn't depend on the exchange rate.
        * @tradeAmount is the "from" currency
        */
        public BigDecimal fee(BigDecimal tradeAmount)
        {
            if (from.monitor.getName().compareTo(to.monitor.getName()) == 0) // Intra-exchange
            {
                return tradeAmount.multiply(tradeFee());
            }
            else // Withdraw-Deposit inter-exchange
            {
                System.out.println(from.monitor.getName() + " != " + to.monitor.getName() + ". Getting withdrawalFee.");
                return withdrawalFee();
            }
        }

        private BigDecimal tradeFee()
        {
            BigDecimal fee = from.monitor.getTradeFee();

            System.out.println("  [tradeFee " + fee + "] " + this.toString());
            return fee;
        }

        private BigDecimal withdrawalFee()
        {
            if(from.currency.compareTo(to.currency) != 0) 
            {
                System.out.println("  WithdrawalFee: 'from' and 'to' currencies don't match: " + from.currency + "->" + to.currency);
            }

            System.out.println("WithdrawFee:"+from.monitor.getWithdrawFee(from.currency));
            BigDecimal fee = from.monitor.getWithdrawFee(from.currency);

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
                        //  adjacencyMatrix[i][j] = new Edge(nodes.get(i), nodes.get(j));
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

        // Prints adjacency tables to csv
        public void dump()
        {
            /*
             * Write each table to its own file.
             */
            PrintWriter rateWriter          = null;
            PrintWriter edgeWriter          = null;
            PrintWriter tradeFeeWriter      = null;
            PrintWriter withdrawFeeWriter   = null;
            PrintWriter feeWriter           = null;
            PrintWriter residualWriter      = null;
            PrintWriter pathWriter          = null;
            PrintWriter valueWriter         = null;

            try 
            {
                rateWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_rates.txt"));      
                edgeWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_edges.txt"));      
                tradeFeeWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_tradeFees.txt"));  
                withdrawFeeWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_withdrawFees.txt"));    
                feeWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_fees.txt"));  
                residualWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_residuals.txt"));
                pathWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_paths.txt"));  
                valueWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY+"table_values.txt"));
            } 
            catch(Exception e)
            {
              // Handle exception.
               System.out.println("ERROR: File Writer failed");
               return;
            }

            /*
             * Make raw adjacency tables with labels
             * Add +2 size to length and height
             * Presumes dijkstra has been run
             */
            String labelTable[][]       = new String[nodes.size() + 2][nodes.size() + 2];
            String rateTable[][]        = new String[nodes.size() + 2][nodes.size() + 2];
            String edgeTable[][]        = new String[nodes.size() + 2][nodes.size() + 2];
            String tradeFeeTable[][]    = new String[nodes.size() + 2][nodes.size() + 2];
            String withdrawFeeTable[][] = new String[nodes.size() + 2][nodes.size() + 2];
            String feeTable[][]         = new String[nodes.size() + 2][nodes.size() + 2];
            String residualTable[][]    = new String[nodes.size() + 2][nodes.size() + 2];
            String valueTable[][]       = new String[nodes.size() + 2][5];
            String pathTable[][]        = new String[nodes.size() + 2][3];

            // Initialize
            for (int i = 0; i < (nodes.size()+2); i++)
            {
                for (int j = 0; j < (nodes.size()+2); j++)
                {
                    labelTable[i][j] = "";
                }
            }

            // Labels
            labelTable[2][0] = nodes.get(0).monitor.getName();
            labelTable[2][1] = nodes.get(0).currency.toString();
            labelTable[0][2] = nodes.get(0).monitor.getName();
            labelTable[1][2] = nodes.get(0).currency.toString();
            labelTable[1][0] = java.time.LocalDateTime.now().toString();
            for (int i = 1; i < nodes.size(); i++)
            {
                if ( i > 1 ) labelTable[i][i] = "\\ \\ \\ ";
                if (nodes.get(i).monitor != nodes.get(i-1).monitor)
                {
                    labelTable[i+2][0] = nodes.get(i).monitor.getName();
                    labelTable[0][i+2] = nodes.get(i).monitor.getName();    
                }

                labelTable[i+2][1] = nodes.get(i).currency.toString();
                labelTable[1][i+2] = nodes.get(i).currency.toString();
            }

            // Copy Labels and init values
            for (int i = 0; i < (nodes.size()+2); i++)
            {
                for (int j = 0; j < (nodes.size()+2); j++)
                {
                    rateTable[i][j]         = labelTable[i][j];
                    edgeTable[i][j]         = labelTable[i][j];
                    tradeFeeTable[i][j]     = labelTable[i][j];
                    withdrawFeeTable[i][j]  = labelTable[i][j];
                    feeTable[i][j]          = labelTable[i][j];
                    residualTable[i][j]     = labelTable[i][j];

                    if (j < 3)
                    {
                        pathTable[i][j] = labelTable[i][j];
                    }
                    if (j < 4)
                    {
                        valueTable[i][j] = labelTable[i][j];
                    }
                }
            }
            
            valueTable[1][2] = "Residual";
            valueTable[1][3] = "USD Value";
            
            rateTable[0][0]         = "ExchangeRate";
            edgeTable[0][0]         = "Edge";
            tradeFeeTable[0][0]     = "TradeFee";
            withdrawFeeTable[0][0]  = "WithdrawFee";
            feeTable[0][0]          = "Fee";
            residualTable[0][0]     = "Residual";
            valueTable[0][0]        = "Value";
            pathTable[0][0]         = "Path";

            // Insert Values
            for (int i = 0; i < nodes.size(); i++)
            {
                for (int j = 0; j < nodes.size(); j++)
                {
                    if (i == j)
                    {
                        continue;
                    }
                    else if (adjacencyMatrix[i][j] == null) continue;
                    edgeTable[i+2][j+2] = adjacencyMatrix[i][j].toString();
                    if (adjacencyMatrix[i][j].rate != null) rateTable[i+2][j+2] = adjacencyMatrix[i][j].rate.toString();
                    if (adjacencyMatrix[i][j].fee != null) feeTable[i+2][j+2] = adjacencyMatrix[i][j].fee.toString();
                    if (adjacencyMatrix[i][j].to.balance != null) residualTable[i+2][j+2] = adjacencyMatrix[i][j].to.balance.toString();
                    
                    if (adjacencyMatrix[i][j].from.monitor.getName().compareTo(adjacencyMatrix[i][j].to.monitor.getName()) == 0)
                    {
                        if (adjacencyMatrix[i][j].tradeFee() != null) tradeFeeTable[i+2][j+2] = adjacencyMatrix[i][j].tradeFee().toString();
                    }
                    else
                    {
                        if (adjacencyMatrix[i][j].withdrawalFee() != null) withdrawFeeTable[i+2][j+2]  = adjacencyMatrix[i][j].withdrawalFee().toString();
                    }
                }

                pathTable[i+2][2] = nodes.get(i).bestPathToString();
                
                if (allPrices.get(nodes.get(i).currency) != null && nodes.get(i).balance != null) 
                {
                    BigDecimal bal = nodes.get(i).balance;
                    BigDecimal val = bal.multiply(allPrices.get(nodes.get(i).currency)); // Dollar value of bal
                    BigDecimal startVal = startBalance.multiply(allPrices.get(startNode.currency));
                    valueTable[i+2][2] = bal.toString();
                    valueTable[i+2][3] = val.toString();

                    try{
                    // Percentage of original amount
                    if (val.compareTo(startVal) >= 0)
                    {
                        valueTable[i+2][4] = val.subtract(startVal).divide(startVal, 4, BigDecimal.ROUND_HALF_UP).toString(); 
                    }
                    else
                    {
                        valueTable[i+2][4] = "-" + startVal.subtract(val).divide(startVal, 4, BigDecimal.ROUND_HALF_UP).toString(); 
                    }
                    }
                    catch(Exception e)
                    {
                        valueTable[i+2][4] = "ex: Start value=" + startVal.toString() + " remaining=" + val + "  " + e;
                    }
                }
            }

            /*
             * Turn tables into csv strings
             */
            String rateString           = "";
            String edgeString           = "";
            String tradeFeeString       = "";
            String withdrawFeeString    = "";
            String feeString            = "";
            String residualString       = "";
            String pathString           = "";
            String valueString          = "";

            // Copy Values into print string
            for (int i = 0; i < nodes.size() + 2; i++)
            {
                for (int j = 0; j < nodes.size() + 2; j++)
                {
                    rateString          += rateTable[i][j] + ",";
                    edgeString          += edgeTable[i][j] + ",";
                    tradeFeeString      += tradeFeeTable[i][j] + ",";
                    withdrawFeeString   += withdrawFeeTable[i][j] + ",";
                    feeString           += feeTable[i][j] + ",";
                    residualString      += residualTable[i][j] + ",";
                }

                pathString += pathTable[i][0] + "," + pathTable[i][1] + "," + pathTable[i][2];
                valueString += valueTable[i][0] + "," + valueTable[i][1] + "," + valueTable[i][2] + "," + valueTable[i][3] + "," + valueTable[i][4];

                rateString          += "\n";
                edgeString          += "\n";
                tradeFeeString      += "\n";
                withdrawFeeString   += "\n";
                feeString           += "\n";
                residualString      += "\n";
                pathString          += "\n";
                valueString         += "\n";
            }

            rateWriter.println(rateString);
            edgeWriter.println(edgeString);
            tradeFeeWriter.println(tradeFeeString);
            withdrawFeeWriter.println(withdrawFeeString);
            feeWriter.println(feeString);
            residualWriter.println(residualString);
            pathWriter.println(pathString);
            valueWriter.println(valueString);

            rateWriter.close();
            edgeWriter.close();
            tradeFeeWriter.close();
            withdrawFeeWriter.close();
            feeWriter.close();
            residualWriter.close();
            pathWriter.close();
            valueWriter.close();
        }
    }
}






