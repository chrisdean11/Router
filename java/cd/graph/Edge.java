import java.math.*;
import org.knowm.xchange.currency.Currency;

/**
 *  ASKS you take the base and give the counter
 *  BIDS you take the counter and give the base
 *
 *  ASK: (Get LTC)  LTC <------ BTC    
 *  BID: (Get BTC)  LTC ------> BTC    
 */
public class Edge
{
    public Node from;
    public Node to;

    public Edge(Node fromNode, Node toNode)
    {
        from = fromNode;
        to = toNode;
    }

    public BigDecimal calculateResidual(BigDecimal tradeAmount)
    {
        BigDecimal ret = from.monitor.fakeTrade(from.currency, to.currency, tradeAmount);
        System.out.println(this.toString()+":\n  CalculatingResidual: tradeAmount="+tradeAmount+" result="+ret);
        return ret;
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

    public BigDecimal tradeFee()
    {
        BigDecimal fee = from.monitor.getTradeFee();

        System.out.println("  [tradeFee " + fee + "] " + this.toString());
        return fee;
    }

    public BigDecimal withdrawalFee()
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
