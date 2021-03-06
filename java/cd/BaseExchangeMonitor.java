import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import java.math.*; 

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

// XChange Core
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cd
 *
 */
public class BaseExchangeMonitor extends ExchangeMonitor
{
    protected final Logger LOG = LoggerFactory.getLogger(getClass()); 

    private String name;
    private Exchange exchange;
    private List<CurrencyPair> currencyPairs;
    private Set<Currency> currencies = new HashSet();
    private BigDecimal tradeFee; // Fractional fee

    // True if all currencies have the same withdraw fee.
    // This is indicated in the json by currency "ALL" which is the Albanian Lek which we'll probably never use.

    private class News
    {
        public Ticker ticker;
        public OrderBook orderBook;

        // trades, times, balances?
    }
    
    private Map<CurrencyPair, News> news = new HashMap();
    private Map<Currency, BigDecimal> withdrawFees = new HashMap();

    /**
     * Constructor without API keys
     * Loads private data members - Name, currencyPairs, currencies, and fees.
     * Does NOT retrieve tickers and orderbooks. Use init() or individual calls to loadTicker(c) and loadOrderbook(c).
     */
    public BaseExchangeMonitor(String exchangeClassName) 
    {
        // Get actual name
        for(int i = 0; i < exchangeClassName.length(); i++)
        {
            name += exchangeClassName.charAt(i);
            if (exchangeClassName.charAt(i) == '.')
                name = "";
        }

        // Remove 'Exchange' from end
        for (int i = 0; i < name.length(); i++)
        {
            if (name.substring(i).compareTo("Exchange") == 0)
                name = name.substring(0, i);
        }

        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName);
        String metadata = exchange.getExchangeMetaData().toJSONString();

        PrintWriter metajson = null;

        // Print out the metadata from XChange even though it's not used, yet.
        try 
        {
            metajson = new PrintWriter(new FileWriter("/home/cd/Eclipse-Workspace/tradebot/src/main/resources/" + name + ".json"));
        } 
        catch(Exception e)
        {
            // Handle exception.
            System.out.println("ERROR: File Writer failed");
        }

        metajson.println(metadata);
        

        // Remote init even though we aren't in init()
        /*
        try 
        { 
            exchange.remoteInit(); 
        } 
        catch (Exception e)
        {
            System.out.println(name + " Monitor: remoteInit() failed");
        }
        */

        // Get all pairs and currencies
        currencyPairs = exchange.getExchangeSymbols();

        System.out.println(name + " CurrencyPairs and currencies:");
        for (CurrencyPair pair : currencyPairs)
        {
            currencies.add(pair.base);
            currencies.add(pair.counter);
            System.out.print(pair + ", ");
        }

        System.out.println();
        for (Currency c : currencies) System.out.print(c + ",");
        System.out.println();

        for (CurrencyPair pair : currencyPairs)
        {
            news.put(pair, new News());
        }

        // Load fees from Json
        FeeParser parser = new FeeParser();
        if (parser.read(name))
        {
            tradeFee = parser.tradeFee;
            System.out.println(name + " tradefee:" + tradeFee);

            BigDecimal all = parser.withdrawFees.get(new Currency("ALL"));
            if (all != null)
            {
                withdrawFees.put(new Currency("ALL"), all);
                System.out.println("  ALL" + " withdrawfee: " + all);
            }

            for(Currency currency : currencies)
            {
                BigDecimal f = parser.withdrawFees.get(currency);

                if (f != null)
                {
                    withdrawFees.put(currency, f);
                }

                System.out.println("  " + currency + " withdrawfee: " + f);
            }
        }
        else
        {
            tradeFee = new BigDecimal(0);
            System.out.println(name + " tradeFee:" + tradeFee + "Fee data not loaded from json");
        }

        System.out.println("Created " + exchangeClassName + " " + name + " Monitor with no login");
    }

    /**
     * Constructor with API keys
     */
    public BaseExchangeMonitor(String exchangeClassName, String apiKey, String secretKey) 
    {
        // Do everything in the above constructor, but with a different createExchange call.

        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName, apiKey, secretKey);
        System.out.println(name + "\n" + exchange.getExchangeMetaData().toJSONString());

        System.out.println("Created " + exchangeClassName + " " + name + " Monitor with keys " + apiKey + " " + secretKey);
    }

    /**
     * Load all tickers and order books at once. Makes a lot of requests and takes a long time.
     */
    public void init()
    {
        System.out.println("Initializing " + name);

        // Get all tickers and orderbooks from remote
        for (CurrencyPair pair : currencyPairs)
        {
            if (!loadTicker(pair))
            {
                System.out.println("Ticker load failed: " + name + " " + pair);
            }

            if (!loadOrderBook(pair))
            {
                System.out.println("Orderbook load failed: " + name + " " + pair);
                continue;
            }

            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                System.out.println(e);
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public Exchange getExchange()
    {
        return exchange;
    }

    public List<CurrencyPair> getCurrencyPairs()
    {
        return currencyPairs;
    }

    public Set<Currency> getCurrencies()
    {
        return currencies;
    }

    public BigDecimal getTradeFee()
    {
        return tradeFee;
    }

    public BigDecimal getWithdrawFee(Currency c)
    {

        // Check if the currency is present
        if (withdrawFees.get(c) != null)
        {
            return withdrawFees.get(c);
        }
        // Check if "ALL" is present
        else if (withdrawFees.get(new Currency("ALL")) != null && currencies.contains(c))
        {
            return withdrawFees.get(new Currency("ALL"));
        }
        else return null;
    }

    // Factor in trade fees, etc to getExchangeRate2
    // Always returns a value of 0 or more, never null.
    public BigDecimal fakeTrade(Currency baseC, Currency counterC, BigDecimal baseAmt)
    {
        BigDecimal ret;
        
        // check if trade or withdraw
        if (baseC.compareTo(counterC) == 0) // Withdraw
        {
            BigDecimal withdrawFee = getWithdrawFee(baseC);
            System.out.println("baseC=" + baseC + " counterC=" + counterC + " baseAmt=" + baseAmt + " withdrawFee=" + withdrawFee);
            ret = baseAmt.subtract(withdrawFee);
        }
        else // Trade
        {
            // tradeAmt = (baseAmt - baseAmt*tradefee)
            // exchangerate(tradeAmt) * tradeAmt
            BigDecimal fee = baseAmt.multiply(getTradeFee());
            BigDecimal tradeAmt = baseAmt.subtract(fee);
            ret = getExchangeRate2(baseC, counterC, tradeAmt).multiply(tradeAmt);
        }

        if (ret == null || ret.compareTo(new BigDecimal(0)) < 0)
        {
            ret = new BigDecimal(0);
        }

        return ret;
    }

    public boolean loadTicker(CurrencyPair pair)
    {
        if (currencyPairs.contains(pair))
        {
            Ticker ticker;

            try
            {
                ticker = exchange.getMarketDataService().getTicker(pair);
            }
            catch(Exception e)
            {
                System.out.println("ERROR getting " + name + " ticker" + ": " + e);
                return false;
            }

            news.get(pair).ticker = ticker;
            return true;
        }

        System.out.println(name + ": Does not exchange " + pair);
        return false;
    }

    public boolean loadOrderBook(CurrencyPair pair)
    {
        if (currencyPairs.contains(pair))
        {
            OrderBook book;

            try
            {
                book = exchange.getMarketDataService().getOrderBook(pair);
            }
            catch(Exception e)
            {
                System.out.println("  Error getting " + name + " orders" + ": " + e);
                return false;
            }

            if (book == null) 
            {
                System.out.println("  " + name + ": marketDataService().getOrderBook("+pair+") returned NULL");
                return false;
            }

            news.get(pair).orderBook = book;
            return true;
        }
        else
        {
            System.out.println(name + ": Does not exchange " + pair);
            return false;
        }
    }

    public Ticker viewTicker(CurrencyPair pair)
    {
        return news.get(pair).ticker;
    }

    public OrderBook viewOrderBook(CurrencyPair pair)
    {
        return news.get(pair).orderBook;
    }

    /**
    *   Loops over orderbook until it can fulfill tradeAmount, and returns the rate in the forward direction.
    *   Orderbook prices are number of Counter for a Base. ASKS - you give Counter. BIDS - you take Counter.
    *   order.OriginalAmount is in terms of Base.
    *   @from Currency to give.  Asks are from counter to base, BIDS are from base to counter.
    *   @to Currency to receive. Asks are from counter to base, BIDS are from base to counter.
    *   @tradeAmount In terms of the "from" currency.
    *   @return The amount of "to" currency you end up with. Returns 0 on error.
   // ASKS - FROM counter TO base
   // BIDS - FROM base TO counter
    */
    public BigDecimal getExchangeRate(Currency from, Currency to, BigDecimal tradeAmount)
    {
        boolean success = false;
        boolean fromBase = false;
        CurrencyPair pair;
        BigDecimal amountAtDepth = new BigDecimal(0);
        BigDecimal rateAtDepth = new BigDecimal(0);

        // Check if we have data for this pair
        if (news.get(new CurrencyPair(from,to)) != null)
        {
            pair = new CurrencyPair(from, to);
            fromBase = true;
        }
        else if(news.get(new CurrencyPair(to,from)) != null)
        {
            pair = new CurrencyPair(to, from);
            fromBase = false;
        }
        else
        {
            return new BigDecimal(0);
        }

        /*
         * LTC/BTC example
         * If trading from base to counter: look at Bids. LTC * .005 = BTC
         * If trading from counter to base: look at Asks. BTC * 1/.005 = LTC
         */

        // This always confuses me:
        // LimitPrice is in terms of trading Base-->Counter. Asks are ordered low to high (less Counter for more Base) from Counter to Base.

        List<LimitOrder> orders;

        if(news.get(pair) == null ) 
        {
            System.out.println("  "+name+": " + pair + ") doesn't exist."); 
            return new BigDecimal(0);
        }
        else if(news.get(pair).orderBook == null )
        {
            System.out.println("\n  "+name+": orderbook("+pair+")"+" doesn't exist."); 
            return new BigDecimal(0);
        }

        // Get orders
        if(fromBase) orders = news.get(pair).orderBook.getBids();
        else orders = news.get(pair).orderBook.getAsks();

        if(orders.isEmpty())
        {
            System.out.println("    "+ name + " GetExchangeRate: Orderbook is empty!");
            return new BigDecimal(0);
        }

        try
        {
            // Cycle through the orders until you get the price at a sufficient depth.
            for (LimitOrder order : orders)
            {
                if (fromBase) // Bids - base to counter
                {
                    rateAtDepth = order.getLimitPrice();
                    amountAtDepth = amountAtDepth.add(order.getOriginalAmount());
                }
                else // Asks - counter to base
                {   
                    // Rate to receive counter is 1/LimitPrice
                    // Amount of counter traded is rate *
                    // Trying to maintain the precision when inverting the rate, using the method here: https://stackoverflow.com/questions/7572309/any-neat-way-to-limit-significant-figures-with-bigdecimal
                    BigDecimal priceInOtherDirection = order.getLimitPrice();
                    int precision = priceInOtherDirection.precision(); // Desired precision
                    BigDecimal preRateAtDepth = (new BigDecimal(1)).divide(priceInOtherDirection, 20, RoundingMode.HALF_UP);
                    rateAtDepth = preRateAtDepth.setScale(precision - preRateAtDepth.precision() + preRateAtDepth.scale(), RoundingMode.HALF_UP);
                    amountAtDepth = amountAtDepth.add(order.getOriginalAmount().multiply(rateAtDepth));
                }

                // Calculate
                if (amountAtDepth.compareTo( tradeAmount ) > 0 ) // There's enough depth to cover trade. Stop searching.
                {
                    success = true;
                    break;
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("    "+ name + " GetExchangeRate: Orderbook exception: " + e);
            return new BigDecimal(0);
        }

        if (!success) // Ran out of orders
        {
            System.out.println("    "+ name + " GetExchangeRate: Ran out of orders!");
            return new BigDecimal(0);
        }

        System.out.println("    "+ name + " GetExchangeRate: " + rateAtDepth+ " " + pair + "FromBase: " + fromBase);
        return rateAtDepth;
    }

    /**
    *   Loops over orderbook until it can fulfill tradeAmount, and returns the rate in the forward direction.
    *   Orderbook prices are number of Counter for a Base. ASKS - you give Counter. BIDS - you take Counter.
    *   order.OriginalAmount is in terms of Base. (For Bids and Asks, I believe.)
    *   @tradeAmount In terms of the "from" currency you want to give away
    *   @return the rate you can trade currency 'from' for currency 'to'
    */
    public BigDecimal getExchangeRate2(Currency from, Currency to, BigDecimal tradeAmount)
    {
        boolean success = false;
        boolean fromBase = false;
        CurrencyPair pair;

        // Check if we have data for this pair
        if (news.get(new CurrencyPair(from,to)) != null)
        {
            pair = new CurrencyPair(from, to);
            fromBase = true;
        }
        else if(news.get(new CurrencyPair(to,from)) != null)
        {
            pair = new CurrencyPair(to, from);
            fromBase = false;
        }
        else
        {
            return new BigDecimal(0);
        }

        /*
         * LTC/BTC example
         * If trading from base to counter: look at Bids. LTC * .005 = BTC
         * If trading from counter to base: look at Asks. BTC * 1/.005 = LTC
         */

        // This always confuses me:
        // LimitPrice is in terms of trading Base-->Counter. Asks are ordered low to high (less Counter for more Base) from Counter to Base.

        List<LimitOrder> orders;

        if(news.get(pair) == null ) 
        {
            System.out.println("  "+name+": " + pair + ") doesn't exist."); 
            return new BigDecimal(0);
        }
        else if(news.get(pair).orderBook == null )
        {
            System.out.println("\n  "+name+": orderbook("+pair+")"+" doesn't exist."); 
            return new BigDecimal(0);
        }

        // Get orders
        if(fromBase) orders = news.get(pair).orderBook.getBids();
        else orders = news.get(pair).orderBook.getAsks();

        if(orders.isEmpty())
        {
            System.out.println("    "+ name + " GetExchangeRate2: Orderbook is empty!");
            return new BigDecimal(0);
        }

        /*
            If you have to dig through multiple orders, the effective exchange rate
            for your trade amount is the weighted average rate: 
            Rate = Rate1*amt_1/amt + Rate2*amt_2/amt + Rate3*amt_3/amt
                 = (Rate1*amt_1 + Rate2*amt_2 + Rate3*amt_3)amt

            Where sum(amt_i) == amt
        */

        BigDecimal cumulativeAmt = new BigDecimal(0);
        BigDecimal rate = new BigDecimal(0);

        try
        {
            // Cycle through the orders until you get the price at a sufficient depth.
            for (LimitOrder order : orders)
            {
                BigDecimal originalAmount;
                BigDecimal limitPrice;

                // 1. Get this order's price and amount in the right direction
                if (fromBase)
                {
                    originalAmount = order.getOriginalAmount();
                    limitPrice = order.getLimitPrice();
                }
                else 
                {   
                    // Convert from base to counter: originalAmount * limitPrice 
                    originalAmount = order.getOriginalAmount().multiply(order.getLimitPrice());
                    BigDecimal prePrice = (new BigDecimal(1)).divide(order.getLimitPrice(), 20, RoundingMode.HALF_UP);

                    // Using the method here to get an inverted price with same precision: 
                    // https://stackoverflow.com/questions/7572309/any-neat-way-to-limit-significant-figures-with-bigdecimal 
                    int precision = order.getLimitPrice().precision();
                    limitPrice = prePrice.setScale(precision - prePrice.precision() + prePrice.scale(), RoundingMode.HALF_UP);
                }

                // 2. Calculate cumulative rate

                // Check if this order satisfies tradeAmount
                if (tradeAmount.compareTo(cumulativeAmt.add(originalAmount)) > 0)
                {
                    // It's not enough. Need another order.
                    cumulativeAmt = cumulativeAmt.add(originalAmount);
                    rate = rate.add(limitPrice.multiply(originalAmount).divide(tradeAmount));
                }
                else // Is enough
                {
                    BigDecimal remainingAmt = tradeAmount.subtract(cumulativeAmt);
                    cumulativeAmt = cumulativeAmt.add(remainingAmt);
                    rate = rate.add(limitPrice.multiply(remainingAmt).divide(tradeAmount, 20, RoundingMode.HALF_UP));
                    success = true;
                    break;
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("    "+ name + " GetExchangeRate2: Orderbook exception: " + e);
            return new BigDecimal(0);
        }

        if (!success) // Ran out of orders
        {
            System.out.println("    " + name + " GetExchangeRate2: Ran out of orders!");
            return new BigDecimal(0);
        }

        System.out.println("    " + name + " GetExchangeRate2: " + rate + " " + pair + "FromBase: " + fromBase);
        return rate;
    }

}