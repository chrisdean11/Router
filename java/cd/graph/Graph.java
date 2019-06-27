import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.math.*;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

// To check all connections, find your node in nodes and go through nodes again.
// adjacencyMatrix[i][j] i = from, j = to
public class Graph
{
    List<Node> nodes;

    // For every node, there is a (possibly null) edge to each other node
    public Edge[][] adjacencyMatrix;

    public Graph(List<Node> nodesIn)
    {
        System.out.println("Building Graph");
        nodes = nodesIn;
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
                    /* Base vs Counter shouldn't matter here. Every i edge to every j edge */
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
    public void dump(Map<Currency, BigDecimal> allPrices, BigDecimal startVal, String logDirectory)
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
            rateWriter = new PrintWriter(new FileWriter(logDirectory+"table_rates.txt"));      
            edgeWriter = new PrintWriter(new FileWriter(logDirectory+"table_edges.txt"));      
            tradeFeeWriter = new PrintWriter(new FileWriter(logDirectory+"table_tradeFees.txt"));  
            withdrawFeeWriter = new PrintWriter(new FileWriter(logDirectory+"table_withdrawFees.txt"));    
            feeWriter = new PrintWriter(new FileWriter(logDirectory+"table_fees.txt"));  
            residualWriter = new PrintWriter(new FileWriter(logDirectory+"table_residuals.txt"));
            pathWriter = new PrintWriter(new FileWriter(logDirectory+"table_paths.txt"));  
            valueWriter = new PrintWriter(new FileWriter(logDirectory+"table_values.txt"));
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
        
        rateTable[0][0]         = "ExchangeRate2";
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
                if (i == j || adjacencyMatrix[i][j] == null)
                {
                    continue;
                }

                Edge thisEdge = adjacencyMatrix[i][j];

                edgeTable[i+2][j+2] = thisEdge.toString();

                if (thisEdge.fee(thisEdge.from.balance) != null) feeTable[i+2][j+2] = thisEdge.fee(thisEdge.from.balance).toString();
                if (thisEdge.to.balance != null) residualTable[i+2][j+2] = thisEdge.to.balance.toString();
                
                // Exchange rate and trade fee, or withdraw fee if different exchanges
                if (thisEdge.from.monitor.getName().compareTo(thisEdge.to.monitor.getName()) == 0)
                {
                    if (thisEdge.tradeFee() != null) tradeFeeTable[i+2][j+2] = thisEdge.tradeFee().toString();
                    rateTable[i+2][j+2] = thisEdge.from.monitor.getExchangeRate2(thisEdge.from.currency, thisEdge.to.currency, thisEdge.from.balance).toString();
                }
                else
                {
                    if (thisEdge.withdrawalFee() != null) withdrawFeeTable[i+2][j+2]  = thisEdge.withdrawalFee().toString();
                }
            }

            pathTable[i+2][2] = nodes.get(i).bestPathToString();
            
            if (allPrices.get(nodes.get(i).currency) != null && nodes.get(i).balance != null) 
            {
                BigDecimal bal = nodes.get(i).balance;
                BigDecimal val = bal.multiply(allPrices.get(nodes.get(i).currency)); // Dollar value of bal
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