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

    private class News
    {
        public Ticker ticker;
        public OrderBook orderBook;

        // TODO: trades, times, fee info, bank, etc
        // TODO: BittrexTicker ticker; and other market-specific items 
    }
    
    private Map<CurrencyPair, News> news = new HashMap();
    private Map<Currency, BigDecimal> withdrawFees = new HashMap();

    /**
     * Constructor without API keys
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
    /*
        // Read api keys
        try 
        {
            File file = new File("/home/cd/Eclipse-Workspace/keys");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) 
            {
                String sections = line.split(',');
                if (sections[0] = "CoinbaseExchange")
                {
                    // do stuff
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    */

        exchange = ExchangeFactory.INSTANCE.createExchange(exchangeClassName, apiKey, secretKey);
        System.out.println(name + "\n" + exchange.getExchangeMetaData().toJSONString());

        System.out.println("Created " + exchangeClassName + " " + name + " Monitor with keys " + apiKey + " " + secretKey);
    }

    /**
     * Initialize this class by loading data from remote exchange. Makes a lot of requests and takes a long time.
     */
    public void init()
    {
        System.out.println("Initializing " + name);

        // Get all orderbooks from remote
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
        return withdrawFees.get(c);
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
    *   @tradeAmount In terms of the "from" currency you want to give away
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
        // LimitPrice is in terms of Base to Counter. Asks are ordered low to high (less Counter for more Base) from Counter to Base.

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

}