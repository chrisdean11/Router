import java.math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.knowm.xchange.currency.Currency;

public class Node 
{
    public ExchangeMonitor monitor;
    public Currency currency;

    // dijkstra results
    public BigDecimal balance;
    public List<Node> bestPath;
    public boolean touched;
    public boolean touchedLast;

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
        touched = false;
        touchedLast = false;
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