/**
 * 
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.time.Instant;
import java.math.*; // BigDecimal, RoundingMode, abs, max, min, MathContext
import java.time.LocalDateTime;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.lang.IllegalArgumentException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;

// CoinMarketCap and OpenExchangeRates
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcTicker;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcQuote;
import org.knowm.xchange.coinmarketcap.pro.v1.service.CmcMarketDataService;
import org.knowm.xchange.oer.dto.marketdata.OERRates;

// Exchange
import org.knowm.xchange.abucoins.                  AbucoinsExchange;
import org.knowm.xchange.anx.v2.                    ANXExchange;
//import org.known.xchange.acx.                       AcxExchange;
import org.knowm.xchange.bibox.                     BiboxExchange;
import org.knowm.xchange.binance.                   BinanceExchange;
import org.knowm.xchange.bitbay.                    BitbayExchange;
import org.knowm.xchange.bitcoinaverage.            BitcoinAverageExchange;
import org.knowm.xchange.bitcoincharts.             BitcoinChartsExchange;
//import org.knowm.xchange.bitcoincore.             BitcoinCoreExchange;
import org.knowm.xchange.bitcoinde.                 BitcoindeExchange;
import org.knowm.xchange.bitcoinium.                BitcoiniumExchange;
import org.knowm.xchange.bitcointoyou.              BitcointoyouExchange;
import org.knowm.xchange.bitfinex.v1.               BitfinexExchange;
import org.knowm.xchange.bitmex.                    BitmexExchange;
import org.knowm.xchange.bitflyer.                  BitflyerExchange;
import org.knowm.xchange.bitmarket.                 BitMarketExchange;
import org.knowm.xchange.bitso.                     BitsoExchange;
import org.knowm.xchange.bitstamp.                  BitstampExchange;
import org.knowm.xchange.bittrex.                   BittrexExchange;
//import org.xchange.bitz.                            BitZExchange;
import org.knowm.xchange.bleutrade.                 BleutradeExchange;
import org.knowm.xchange.blockchain.                BlockchainExchange;
import org.knowm.xchange.btcc.                      BTCCExchange;
import org.knowm.xchange.btcmarkets.                BTCMarketsExchange;
import org.knowm.xchange.btctrade.                  BTCTradeExchange;
import org.knowm.xchange.btcturk.                   BTCTurkExchange;
import org.knowm.xchange.campbx.                    CampBXExchange;
import org.knowm.xchange.ccex.                      CCEXExchange;
import org.knowm.xchange.cexio.                     CexIOExchange;
import org.knowm.xchange.coinbase.                  CoinbaseExchange;
//import org.xchange.coinegg.                         CoinEggExchange;
import org.knowm.xchange.coinmarketcap.pro.v1.      CmcExchange;
import org.knowm.xchange.coinfloor.                 CoinfloorExchange;
import org.knowm.xchange.coinmate.                  CoinmateExchange;
import org.knowm.xchange.cryptofacilities.          CryptoFacilitiesExchange;
import org.knowm.xchange.cryptopia.                 CryptopiaExchange;
//import org.knowm.xchange.cryptonit.v2.              CryptonitExchange;
import org.knowm.xchange.dsx.                       DSXExchange;
import org.knowm.xchange.gatecoin.                  GatecoinExchange;
import org.knowm.xchange.gateio.                    GateioExchange;
import org.knowm.xchange.coinbasepro.               CoinbaseProExchange;
import org.knowm.xchange.gemini.v1.                 GeminiExchange;
import org.knowm.xchange.hitbtc.v2.                 HitbtcExchange;
import org.knowm.xchange.itbit.                      ItBitExchange;
import org.knowm.xchange.independentreserve.        IndependentReserveExchange;
import org.knowm.xchange.koineks.                   KoineksExchange;
import org.knowm.xchange.koinim.                    KoinimExchange;
import org.knowm.xchange.kraken.                    KrakenExchange;
import org.knowm.xchange.kuna.                      KunaExchange;
import org.knowm.xchange.kucoin.                    KucoinExchange;
import org.knowm.xchange.lakebtc.                   LakeBTCExchange;
import org.knowm.xchange.liqui.                     LiquiExchange;
import org.knowm.xchange.livecoin.                  LivecoinExchange;
import org.knowm.xchange.luno.                      LunoExchange;
import org.knowm.xchange.mercadobitcoin.            MercadoBitcoinExchange;
import org.knowm.xchange.okcoin.                    OkCoinExchange;
import org.knowm.xchange.oer.                       OERExchange;
import org.knowm.xchange.paribu.                    ParibuExchange;
import org.knowm.xchange.paymium.                   PaymiumExchange;
import org.knowm.xchange.poloniex.                  PoloniexExchange;
//import org.knowm.xchange.quoine.                    QuoineExchange;
import org.knowm.xchange.quadrigacx.                QuadrigaCxExchange;
import org.knowm.xchange.ripple.                    RippleExchange;
import org.knowm.xchange.therock.                   TheRockExchange;
import org.knowm.xchange.truefx.                    TrueFxExchange;
import org.knowm.xchange.vaultoro.                  VaultoroExchange;
import org.knowm.xchange.vircurex.                  VircurexExchange;
import org.knowm.xchange.yobit.                     YoBitExchange;
import org.knowm.xchange.wex.v3.                    WexExchange;
import org.knowm.xchange.zaif.                      ZaifExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    /*
     * #defines
     */
    public static final int TIME_DELAY = 500; // Number of milliseconds between sending request to the same exchange 
    public static final int TIMEOUT = 5000;    // Number of milliseconds to wait for HTTP connection 
    public static final float ARBITRAGE_PERCENT = 3;
    public static final float USD_TRADE_AMOUNT  = 100;
    public static final float DEPTH_FACTOR   = 2;   // USD_TRADE_AMOUNT multiplier that must be in the orderbook before initiating a trade.
    public static final boolean CURRENCIESOFINTEREST = false; // Set to true if you want to ignore currencies that aren't part of currenciesOfInterest
    public static final boolean INITTHREADS = false; // Set to true to do multi-threaded loading of order books  

    protected static final Logger LOG = LoggerFactory.getLogger(Main.class); // TODO hook up logging for the XChange classes
    private static String LOG_DIRECTORY ="/home/cd/Eclipse-Workspace/tradebot/logs/";

    /*
     * Major data members
     */
    private static List<ExchangeMonitor>        exchangeMonitors;
    private static List<CurrencyPair>           allPairs;
    private static List<CurrencyPair>           arbitragePairs;
    private static Map<Currency, BigDecimal>    allPrices = new HashMap<Currency, BigDecimal>(); // USD prices for all currencies
    private static Set<Currency>                allCurrencies;
    private static Map<String,String>           keys;

    private static PrintWriter comparisonLog;
    private static PrintWriter detailsLog;

    static 
    {
        PrintWriter tmp = null;

        try 
        {
            comparisonLog = new PrintWriter(new FileWriter(LOG_DIRECTORY+"ComparisonLog"+java.time.LocalDateTime.now()+".txt"));
            detailsLog = new PrintWriter(new FileWriter(LOG_DIRECTORY+"DetailsLog"+java.time.LocalDateTime.now()+".txt"));
            // Prices = new PrintWriter(new FileOutputStream(LOG_DIRECTORY + "Prices.txt", false));
            // Prices.println(java.time.LocalDateTime.now());  
        } 
        catch(Exception e)
        {
           System.out.println("ERROR: File Writer failed");
        }
    }

    // If you want to limit yourself to specific currencies, set CURRENCIESOFINTEREST to true 
    final private static Map<Currency, BigDecimal> currenciesOfInterest = new HashMap<Currency, BigDecimal>(){
        {
            put(Currency.USD, new BigDecimal(0));
            put(Currency.CAD, new BigDecimal(0));
            put(Currency.EUR, new BigDecimal(0));
            put(Currency.GBP, new BigDecimal(0));
            put(Currency.ETH, new BigDecimal(0));  // Ethereum
            put(Currency.LTC, new BigDecimal(0));  // LiteCoin
            put(Currency.USDT, new BigDecimal(0)); // Tether
            put(Currency.XRP, new BigDecimal(0));  // Ripple
            put(Currency.BCH, new BigDecimal(0));  // BitcoinCash
            put(Currency.ADA, new BigDecimal(0));  // Cardano 
            put(Currency.XEM, new BigDecimal(0));  // NEM
            put(Currency.NEO, new BigDecimal(0));  // NEO
            put(Currency.XLM, new BigDecimal(0));  // Stellar
            put(Currency.EOS, new BigDecimal(0));  // EOX
            put(Currency.DASH, new BigDecimal(0)); // Dash
            put(Currency.XMR, new BigDecimal(0));  // Monero
            put(Currency.TRX, new BigDecimal(0));  // TRON
            put(Currency.BTG, new BigDecimal(0));  // BitCoin Gold
            put(Currency.ICX, new BigDecimal(0));  // ICON
            put(Currency.QTUM, new BigDecimal(0)); // Qtum
            put(Currency.ETC, new BigDecimal(0));  // Ethereum Classic
            put(Currency.LSK, new BigDecimal(0));  // Lisk
            put(Currency.VEN, new BigDecimal(0));  // VeChain
            put(Currency.OMG, new BigDecimal(0));  // Omise GO
            put(Currency.HSR, new BigDecimal(0));  // Hshare
            put(Currency.XVG, new BigDecimal(0));  // Verge
        }};

    private static Map<String,String> loadApiKeys()
    {
        KeyParser keyParser = new KeyParser();
        return keyParser.read();
    }

   /*
    *   1. Get USD values
    *   2. Make exchangeMonitors
    *   3. Fill in allPairs and arbitragePairs
    *   4. Get orderbooks
    */
    private static void init() throws InterruptedException, IOException
    {
        keys = loadApiKeys();
        allPairs = new ArrayList<CurrencyPair>();
        arbitragePairs = new ArrayList<CurrencyPair>();
        allCurrencies = new HashSet<Currency>();

        /*
         * (1) Get USD values
         */
        allPrices.put(Currency.getInstance("USD"), new BigDecimal(1));
        addCoinValues(allPrices); // "Universal" USD price for all cryptos 
        addFiatValues(allPrices); // TODO Figure out fiat OER problem 

        // TODO Check AllPairs for reversed pairs

        // Display all prices
        System.out.println("Repeating the currency prices available: ");
        for (Map.Entry<Currency, BigDecimal> entry : allPrices.entrySet())
        {
            System.out.println(entry.getKey() + "_USD: " + entry.getValue());
        }

        System.out.println("End of exchange rates");

        /*
         * (2) Construct exchangeMonitors
         */
        createExchangeMonitors();

        /*
         * (3) Set allPairs: all pairs traded on all exchanges
         *     Set arbitragepairs: all pairs traded on more than one exchange
         */
        for (ExchangeMonitor monitor : exchangeMonitors)
        {
            Set<Currency> currencies = new HashSet<Currency>();
            //detailsLog.println(monitor.getName());
            for (CurrencyPair pair : monitor.getCurrencyPairs())
            {
                // Skip if either currency is not a currency of interest.
                if( CURRENCIESOFINTEREST && ((currenciesOfInterest.get(pair.base) == null) || (currenciesOfInterest.get(pair.counter) == null)) )  
                    continue;

                // Only ever add to arbitragePairs if it's in more than one pair in allPairs
                if (allPairs.contains(pair))
                {
                    if (!arbitragePairs.contains(pair)) arbitragePairs.add(pair);
                }
                else allPairs.add(pair);

                currencies.add(pair.base);
                currencies.add(pair.counter);
                allCurrencies.add(pair.base);
                allCurrencies.add(pair.counter);
            }

            for (Currency c : currencies)
            {
                //detailsLog.println(c);
            }

            // Set this exchange's timeout stuff before the orderbook load
            // monitor.getExchange().getExchangeSpecification().setHttpReadTimeout(TIMEOUT);
            // monitor.getExchange().getExchangeSpecification().setHttpConnTimeout(TIMEOUT);
        }

        System.out.println("\nAll pairs on all exchanges: ");
        System.out.println(allPairs);
        System.out.println("\nArbitrage pairs: ");
        System.out.println(arbitragePairs);
        System.out.println();

        // Should not need to deal with currenciesOfInterest past this point. allPairs/arbitragePairs reflect this info.

        /**
         * (4) Get orderbooks either by using initThreads, or by cycling through allPairs
         */
        if (INITTHREADS) // Doesn't really work at the moment, they still block each other
        {
            System.out.println("Multi-thread");
            // Get ExecutorService from Executors utility class, thread pool size is 10
            ExecutorService executor = Executors.newFixedThreadPool(exchangeMonitors.size());
            // create a list to hold the Future object associated with Callable
            List<Future<String>> list = new ArrayList<Future<String>>();
    
            int prot = 10000;
            // Create thread instances
            for(ExchangeMonitor monitor : exchangeMonitors)
            {
                monitor.getExchange().getExchangeSpecification().setPort(prot);
                prot ++;
    
                System.out.println(monitor.getName() + " Port:" + monitor.getExchange().getExchangeSpecification().getPort() + " ProxyPort: " + monitor.getExchange().getExchangeSpecification().getProxyPort() );
    
                Callable<String> callable = new ExchangeMonitorLoadThread(monitor);
                Future<String> future = executor.submit(callable); // Submit to be executed by the thread pool
                list.add(future);
            }
    
            for(Future<String> fut : list)
            {
                try 
                {
                    // Future.get() waits for task to get completed
                    System.out.println(new Date()+ "::" + fut.get());
                } 
                catch (InterruptedException | ExecutionException e) 
                {
                    e.printStackTrace();
                }
            }

            executor.shutdown();
        }
        else
        {
            /**
             * Load up monitors the original way instead of launching threads
             */
            System.out.println("Single thread");
            long start, finish; // Enforce max rate limit per exchange without wasting time

            for (CurrencyPair pair : allPairs) // or arbitragePairs to load faster
            {
                System.out.println("Getting " + pair + " from all your favorite exchanges: ");
                start = new Date().getTime(); // getTime() is in milliseconds

                for (ExchangeMonitor monitor : exchangeMonitors)
                {
                    System.out.print("  " + monitor.getName());
                    if (monitor.getCurrencyPairs().contains(pair))
                    {
                        System.out.println("-- Pair found.");
                        if (!monitor.loadTicker(pair))
                        {
                            System.out.println("Ticker load failed: " + monitor.getName());
                            detailsLog.println("Ticker load failed: " + monitor.getName());
                        }
                        
                        if (!monitor.loadOrderBook(pair))
                        {
                            System.out.println("Orderbook load failed: " + monitor.getName());
                            detailsLog.println("Orderbook load failed: " + monitor.getName());
                            continue;
                        }
                    }
                    else 
                    {
                        System.out.println("-- Pair not found.");
                    }
                }

                // Wait before trying the next currency
                finish = new Date().getTime();
                if (finish - start < TIME_DELAY)
                {
                    // Sleep for the remaining time to satisfy TIME_DELAY
                    Thread.sleep(TIME_DELAY - (finish - start));
                }
            }
        }

        return;
    }

    /*
     *  - Init() loads remote info
     *  - Log basic arbitrage comparisons to comparisonLog
     *  - Run Search over exchanges
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        /*
         *  1. Init
         */
        init();

        /*
         *  2. Comparison Log - Record price differences for each currency pair using old method in this class.
         */
        for (CurrencyPair pair : arbitragePairs)
        {
            comparisonLog.println(pair);
            detailsLog.println("\nComparing " + pair + "\n");

            // Compare each exchange to each other exchange
            for (int i = 0; i < exchangeMonitors.size() - 1 ; i++)
            {
                ExchangeMonitor monitor = exchangeMonitors.get(i);

                if (monitor.getCurrencyPairs().contains(pair) && monitor.viewOrderBook(pair) != null)
                {
                    for (int j = i + 1; j < exchangeMonitors.size(); j++)
                    {
                        ExchangeMonitor monitor2 = exchangeMonitors.get(j);

                        if (monitor2.getCurrencyPairs().contains(pair) && monitor2.viewOrderBook(pair) !=null)
                        {
                            //comparisonLog.print(monitor.getName() + "," + monitor2.getName());
                            detailsLog.print(monitor.getName() + " vs " + monitor2.getName());

                            // Tickers
                            if ( (monitor.viewTicker(pair) != null) && (monitor2.viewTicker(pair) != null) )
                            {
                                StringBuilder res = new StringBuilder();
                                BigDecimal price1 = monitor.viewTicker(pair).getLast();
                                BigDecimal price2 = monitor2.viewTicker(pair).getLast();
                                compareTickers(res, monitor.getName(), price1, monitor2.getName(), price2);
                                //comparisonLog.println("  [Tickers: " + res + "]");
                                detailsLog.println("  [Tickers: "    + res + "]");
                            }
                            else
                            {
                                //comparisonLog.println("  Couldn't load tickers");
                                detailsLog.println("  Couldn't load tickers");
                            }
                            compareOrders(pair, monitor, monitor2);
                        }
                    }
                }
            }
        }

        detailsLog.println("\n\n\n\nALL EXCHANGE RATES");
        for (ExchangeMonitor monitor : exchangeMonitors)
        {
            for (CurrencyPair pair : monitor.getCurrencyPairs())
            {
                System.out.print(pair + " --> ");
                OrderBook book = monitor.viewOrderBook(pair);
                if (book == null) continue; // Possibly not loaded earlier because only currenciesOfInterest or arbitragePairs were load()-ed, and not allPairs. 
                BigDecimal asksPrice = getPriceAtDepth(book.getAsks());
                BigDecimal bidsPrice = getPriceAtDepth(book.getBids());

                System.out.println(monitor.getName() + " ExchangeRates: " + "Asks= " + asksPrice + " Bids=" + bidsPrice);
            }
        }

        /*
         *  3. Try search algorithm
         */
        testSearchWithGdaxAndBinance();

        /*
         *  4. End
         */
        System.out.println("Completed successfully");
        comparisonLog.println("Completed successfully");
        detailsLog.println("Completed successfully");

        comparisonLog.close();
        detailsLog.close();
        return;
    }

    /*
     * Get GDAX and Binance from the monitors list
     * Find best path from GDAX BTC to Binance LTC, for 1 BTC.
     * Dump graph to CSV files.
     */
    static void testSearchWithGdaxAndBinance()
    {
        ExchangeMonitor coinbasepro = null;
        ExchangeMonitor binance = null;

        for (ExchangeMonitor monitor : exchangeMonitors)
        {
            if (monitor.getName().equals("CoinbasePro"))
            {
                coinbasepro = monitor;
                System.out.println("Found CoinbasePro: compare fees - trade fee " + monitor.getTradeFee() + "=" + coinbasepro.getTradeFee() +  ", BTC withdraw fee " + monitor.getWithdrawFee(new Currency("BTC")) + "=" + binance.getWithdrawFee(new Currency("BTC")));
                continue;
            }

            if (monitor.getName().equals("Binance"))
            {
                binance = monitor;
                System.out.println("Found Binance: compare fees - trade fee " + monitor.getTradeFee() + "=" + binance.getTradeFee() +  ", BTC withdraw fee " + monitor.getWithdrawFee(new Currency("BTC")) + "=" + binance.getWithdrawFee(new Currency("BTC")));
                continue;
            }
        }

        // TEST SEARCH ALGORITHM
        System.out.println("Building Seeker");
        Seeker seeker = new Seeker(exchangeMonitors, LOG_DIRECTORY, allPrices);

        seeker.dijkstra(coinbasepro, Currency.BTC, binance, Currency.LTC, new BigDecimal(1));

        seeker.graph.dump();
    }

    /*
     * Waits in a loop until able to contact outside world.
     * I was trying to diagnose intermittent connectivity issues when I made this.
     */
    static boolean waitForConnection()
    {
        boolean isConnected = false;

        do{
            try 
            {
                final URL url = new URL("http://www.google.com");
                final URLConnection conn = url.openConnection();
                conn.connect();
                System.out.print("Connected to google");

                final URL url2 = new URL("http://www.xfinity.com");
                final URLConnection conn2 = url2.openConnection();
                conn2.connect();
                System.out.print("...xfinity");

                final URL url3 = new URL("http://www.facebook.com");
                final URLConnection conn3 = url3.openConnection();
                conn3.connect();
                System.out.print("...facebook. ");

                isConnected = true;
            }
            catch (MalformedURLException e) 
            {
                System.out.println("Failed. " + e);
                throw new RuntimeException(e);
            }
            catch (IOException e) 
            {
                System.out.println("Connection failed. IOException: " +e);
                try { Thread.sleep(TIME_DELAY); }
                catch (InterruptedException e2) { System.out.println(e2); }
                continue;
            }
        }
        while (isConnected == false);

        System.out.println("Connected.");
        return isConnected;
    }



   /*
    *
    *   Order Books
    *
    */

   // Compare prices on two exchanges to find the direction and % difference of a trade
   // Assumes Orderbook has already been successfully loaded
   static void compareOrders(CurrencyPair pair, ExchangeMonitor m1, ExchangeMonitor m2)
   {
        List<LimitOrder> asks1 = m1.viewOrderBook(pair).getAsks();
        List<LimitOrder> bids1 = m1.viewOrderBook(pair).getBids();
        List<LimitOrder> asks2 = m2.viewOrderBook(pair).getAsks();
        List<LimitOrder> bids2 = m2.viewOrderBook(pair).getBids();

        // DID YOU KNOW THAT BIGDECIMAL IS IMMUTABLE, SO YOU CAN SAFELY SHARE THE REFERENCE AROUND

        // Get the prices at the desired depth
        detailsLog.println("    " + m1.getName() + " asks");
        BigDecimal asks1Price = getPriceAtDepth(asks1);

        detailsLog.println("    " + m1.getName() + " bids");
        BigDecimal bids1Price = getPriceAtDepth(bids1);

        detailsLog.println("    " + m2.getName() + " asks");
        BigDecimal asks2Price = getPriceAtDepth(asks2);

        detailsLog.println("    " + m2.getName() + " bids");
        BigDecimal bids2Price = getPriceAtDepth(bids2);

        if (asks1Price.compareTo(new BigDecimal(0)) == 0 || 
            bids1Price.compareTo(new BigDecimal(0)) == 0 || 
            asks2Price.compareTo(new BigDecimal(0)) == 0 || 
            bids2Price.compareTo(new BigDecimal(0)) == 0 )
        {
            //comparisonLog.println("  Fail. OrderBook end reached.");
            detailsLog.println("  Fail. OrderBook end reached.");
        }
        else
        {
            // Compare both directions 
            // Normally ASKS price > BIDS price
            // ASKS are ordered from low to high - you pay in Counter to get Base
            // BIDS are ordered from high to low - you get Counter in exchange for Base
            // Look for when ASKS < BIDS
    
            if (asks1Price.compareTo(bids2Price) < 0) 
            {
                detailsLog.println("  "    + percentDifference(asks1Price, bids2Price) +"%  "+ m1.getName() + " Ask " + asks1Price + " vs " + m2.getName() + "  Bid " + bids2Price     ); 
                if ( percentDifference(asks1Price, bids2Price).compareTo( new BigDecimal(ARBITRAGE_PERCENT)) > 0 )
                    comparisonLog.println("  " + percentDifference(asks1Price, bids2Price) +"%  #"+ m1.getName() + " Ask " + asks1Price + " vs #" + m2.getName() + "  Bid " + bids2Price  ); 
            }
            else if (asks2Price.compareTo(bids1Price) < 0) 
            { 
                detailsLog.println("  "    + percentDifference(asks2Price, bids1Price) + "%  " + m2.getName() + " Ask " + asks2Price + " vs " + m1.getName() + "  Bid " + bids1Price); 
                if ( percentDifference(asks2Price, bids1Price).compareTo(new BigDecimal(ARBITRAGE_PERCENT)) > 0 )
                    comparisonLog.println("  " + percentDifference(asks2Price, bids1Price) + "%  #" + m2.getName() + " Ask " + asks2Price + " vs #" + m1.getName() + "  Bid " + bids1Price); 
            }
            else 
            { 
                detailsLog.println("  Lock (ask-bid): " + m1.getName() + asks1Price + "-" + bids1Price + "   " + m2.getName() + asks2Price + "-" + bids2Price); 
                //comparisonLog.println("  Lock (ask-bid): " + m1.getName() + asks1Price + "-" + bids1Price + "   " + m2.getName() + asks2Price + "-" + bids2Price); 
            }

        }
        detailsLog.println();
    }

    // Get the price at USD_TRADE_AMOUNT * DEPTH_FACTOR deep in the OrderBook.
    // TODO: Calculate the accurate price considering the cost of each order you eat up, not just the worst-case one.
    // This will always return a BigDecimal! It should never return null.
    private static BigDecimal getPriceAtDepth(List<LimitOrder> orders)
    {
        BigDecimal valueAtDepth = new BigDecimal(0);
        BigDecimal priceAtDepth = new BigDecimal(0);
        boolean success = false;

        if(orders.isEmpty())
        {
            detailsLog.println("    Orderbook is empty!");
        }

        try{ 
                // Cycle through the orders until you get the price at a sufficient depth.
                for (LimitOrder order : orders)
                {
                    // value = volume * usd price of base
                    BigDecimal usdPrice = allPrices.get(order.getCurrencyPair().base);
                    if (usdPrice.compareTo(new BigDecimal(0)) == 0 ) throw new IllegalArgumentException("No USD value was loaded for " + order.getCurrencyPair().base);
                    BigDecimal value = order.getOriginalAmount().multiply(usdPrice); //OriginalAmount is in terms of BASE
        
                    priceAtDepth = order.getLimitPrice();
                    valueAtDepth = valueAtDepth.add(value);

                    detailsLog.println( "      LimitPrice:" + order.getLimitPrice() + 
                                        "    originalAmount:" + order.getOriginalAmount() + 
                                        "    value:" + value.round(new MathContext(6, RoundingMode.HALF_UP)) +
                                        "    valueAtDepth: " + valueAtDepth
                                        );
        
                    // Calculate
                    if (valueAtDepth.compareTo( new BigDecimal(USD_TRADE_AMOUNT * DEPTH_FACTOR) ) > 0 )
                    {
                        success = true;
                        break; // There's enough depth to cover trade. Stop searching.
                    }
                }
        }
        catch(Exception e)
        {
            System.out.println("Ran out of orders or expected data was missing: \n  GetPriceAtDepth: " + e + "\n  Orders: " + orders.get(0) + "\n");
            detailsLog.println("Ran out of orders or expected data was missing: \n  GetPriceAtDepth: " + e + "\n  Orders: " + orders.get(0) + "\n");
            return new BigDecimal(0);
        }

        if (!success)
        {
            detailsLog.println("  Ran out of orders!");
            return new BigDecimal(0);
        }
        else if(priceAtDepth == null)
        {
            detailsLog.println("      Orderbook broken? LimitPrice was null.");
            return new BigDecimal(0);
        }
        else // priceAtDepth is now the price you'd be able to trade USD_TRADE_AMOUNT
        {
            return priceAtDepth;
        }
        
    }

    // diff = 100 * (larger-smaller)/larger
    static BigDecimal percentDifference(BigDecimal smaller, BigDecimal larger)
    {
        BigDecimal diff = larger.subtract(smaller);
        BigDecimal ratio = diff.divide(larger, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal percent = ratio.multiply(new BigDecimal(100));

        return percent;
    }

    /*
    *
    *   Accounts
    *   API Keys, Deposit addresses, withdrawals
    *
    */

    /*
    *
    *   Trades
    *
    */

    // Accounts and trades probably belong in ExchangeMonitor

    /*
        ASKS are ordered from low to high
        BIDS are ordered from high to low

        LTC/BTC
        - ASKS say "I want this much Counter for a Base"
        - BIDS say "I give you this much Counter for a Base"
        - ASK .016 "I want .016 BTC and I'll give you 1 LTC"
        - BID .017 "I give .017 BTC and youll give me 1 LTC"

        To take a ASK, you need Counter
        To take a BID, you need Base 
    */

    /*
     *   Make exchangeMonitor instances -- called by init()
     */
    private static void createExchangeMonitors() throws InterruptedException
    {
        exchangeMonitors = new ArrayList<ExchangeMonitor>();

        // TODO find out why detailsLog has so many "GetPriceAtDepth: java.lang.NullPointerException" 
        // Instantiate and init exchange monitors
        // exchangeMonitors.add(new BaseExchangeMonitor(AbucoinsExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(ANXExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(AcxExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BiboxExchange.class.getName()));
        exchangeMonitors.add(new BaseExchangeMonitor(BinanceExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitbayExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitcoinAverageExchange.class.getName()));
        // Not an exchange exchangeMonitors.add(new BaseExchangeMonitor(BitcoinChartsExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitcoinCoreExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitcoindeExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitcoiniumExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitcointoyouExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitfinexExchange.class.getName()));
        //  exchangeMonitors.add(new BaseExchangeMonitor(BitmexExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitflyerExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitMarketExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitsoExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitstampExchange.class.getName()));
        //exchangeMonitors.add(new BaseExchangeMonitor(BittrexExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BitZExchange.class.getName()));
        // Timeouts exchangeMonitors.add(new BaseExchangeMonitor(BleutradeExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BlockchainExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BTCCExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BTCMarketsExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BTCTradeExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(BTCTurkExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CampBXExchange.class.getName()));
        // Ticker error. Json parse exception exchangeMonitors.add(new BaseExchangeMonitor(CCEXExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CexIOExchange.class.getName()));
        //exchangeMonitors.add(new BaseExchangeMonitor(CoinbaseExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CoinEggExchange.class.getName()));
        //Not an exchange exchangeMonitors.add(new BaseExchangeMonitor(Cmc.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CoinfloorExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CoinmateExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CryptoFacilitiesExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CryptopiaExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(CryptonitExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(DSXExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(EmpoExExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(GatecoinExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(GateioExchange.class.getName()));
        exchangeMonitors.add(new BaseExchangeMonitor(CoinbaseProExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(GeminiExchange.class.getName()));
        // Terrible reputation exchangeMonitors.add(new BaseExchangeMonitor(HitbtcExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(ItBitExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(IndependentReserveExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(KoineksExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(KoinimExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(KrakenExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(KunaExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(KucoinExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(LakeBTCExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(LiquiExchange.class.getName()));
        //exchangeMonitors.add(new BaseExchangeMonitor(LivecoinExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(LunoExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(MercadoBitcoinExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(OkCoinExchange.class.getName()));
        // Not an exchange exchangeMonitors.add(new BaseExchangeMonitor(OERExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(ParibuExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(PaymiumExchange.class.getName()));
        //exchangeMonitors.add(new BaseExchangeMonitor(PoloniexExchange.class.getName()));
        // I forget why exchangeMonitors.add(new BaseExchangeMonitor(QuoineExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(QuadrigaCxExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(RippleExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(TaurusExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(TheRockExchange.class.getName()));
        // I forget why exchangeMonitors.add(new BaseExchangeMonitor(TrueFxExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(VaultoroExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(VircurexExchange.class.getName()));
        // I forget whyexchangeMonitors.add(new BaseExchangeMonitor(YoBitExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(WexExchange.class.getName()));
        // exchangeMonitors.add(new BaseExchangeMonitor(ZaifExchange.class.getName()));

        Thread.sleep(TIME_DELAY);
    }

    /*
     *   Load USD values of cryptos from Cmc -- called by init()
     */
    private static void addCoinValues(Map<Currency, BigDecimal> _allPrices)
    {
        CmcExchange coinMarketCap = new CmcExchange();
        CmcMarketDataService coinMarketCapMarketDataService = new CmcMarketDataService(coinMarketCap);
        List<CmcTicker> ticks;

        try
        {
            ticks = coinMarketCapMarketDataService.getCmcLatestDataForAllCurrencies();
            System.out.println("Number of CMC tickers = " + ticks.size());
        }
        catch (Exception e)
        {
            System.out.println("Failed to get CoinMarketCapTickers: " + e);
            return;
        }

        for (CmcTicker tick : ticks)
        {
            Map<String, CmcQuote> quotes = tick.getQuote();

            for (Map.Entry<String, CmcQuote> entry : quotes.entrySet())
            {
                System.out.println("  Ticker:" + tick.getSymbol() + " = " + entry.getValue().getPrice());
                _allPrices.put(Currency.getInstance(tick.getSymbol()), entry.getValue().getPrice());
            }
        }
    }

    /*
     *   Load fiat prices from OER -- called by init()
     */
    private static void addFiatValues(Map<Currency, BigDecimal> _allPrices)
    {
        try
        {
            OpenExchangeRates exchangeRates = new OpenExchangeRates();
            exchangeRates.loadOERRates(keys.get("OER"));
            exchangeRates.copyRatesToMap(_allPrices);
        }
        catch(Exception e)
        {
            System.out.println("Failed to make OpenExchangeRates: " + e);
        }
        
        return;
    }



    /*
    *
    *   DEPRECATED - Nearly unused code that I don't want to delete yet
    *
    */

    // ETH/BTC last/ask/bid = the number of BTC you'd get for 1 ETH
    static boolean compareTickers(StringBuilder res, String name1, BigDecimal price1, String name2, BigDecimal price2)
    {
        try
        {
            int compare = price1.compareTo(price2);
            int scale = 3;
    
            if ( compare > 0 ) res.append(name1 + ">" + name2); 
            else if ( compare < 0 ) res.append(name2 + ">" + name1); 
            else res.append(name1 + "=" + name2);

            // BigDecimal is terrible to use but there's no better alternative because Java doesn't have operator overloading
            // 100 * (price1-price2) / price
            BigDecimal difference = price1.subtract(price2).abs();  // price1-price2
            BigDecimal percent = difference.multiply(new BigDecimal(100)).divide(price1.max(price2), scale, RoundingMode.HALF_UP); // 100*diff/price
            res.append(" by " + percent + "%");

            if (percent.compareTo(new BigDecimal(ARBITRAGE_PERCENT)) > 0) return true;
            else return false;
        }
        catch(Exception e)
        {
            System.out.println("Ticker calculation error: " + e);
            System.out.println("    " + name1+price1+name2+price2);
            return false;
        }
    }

    // Cycles through each arbitragePair and does a comparison of all exhanges' prices that trade in it
    static void compareAllTickers(PrintWriter file) throws InterruptedException
    {
        // Compare price differences for each currency pair
        for (CurrencyPair pair : arbitragePairs)
        {
            HashMap<String, Ticker> tickers = new HashMap<String, Ticker>();

            // Load up each ticker for this currency pair
            for (ExchangeMonitor monitor : exchangeMonitors)
            {
                if (monitor.getCurrencyPairs().contains(pair))
                {
                    if (monitor.loadTicker(pair) == false) continue;
                    tickers.put(monitor.getName(), monitor.viewTicker(pair));
                }
            }

            file.println(pair);

            // Get tickers on this currency pair
            for(Map.Entry<String, Ticker> entry1: tickers.entrySet())
            {
                String name1    = entry1.getKey();
                Ticker ticker1  = entry1.getValue();
    
                for(Map.Entry<String, Ticker> entry2: tickers.entrySet()) 
                {
                    String name2 = entry2.getKey();
                    if (System.identityHashCode(name1) >= System.identityHashCode(name2)) continue;

                    Ticker ticker2 = entry2.getValue();

                    // Compare ticker1 and ticker2 and print out details if it's an arbitrage opportunity
                    StringBuilder last = new StringBuilder();

                    if ( compareTickers(last, name1, ticker1.getLast(), name2, ticker2.getLast()) ) 
                    {
                        StringBuilder bid = new StringBuilder();
                        StringBuilder ask = new StringBuilder();

                        compareTickers(bid, name1, ticker1.getBid(), name2, ticker2.getBid());
                        compareTickers(ask, name1, ticker1.getAsk(), name2, ticker2.getAsk());

                        file.println("    [" + name1 + "=" + ticker1.getLast() 
                                    + "] vs [" + name2  + "=" + ticker2.getLast() + "]");
                        file.println("    "+ name1 + "vs" + name2 
                                    + "        last: " + last 
                                    + "  bid: " + bid
                                    + "  ask: " + ask
                                    );
                    }
                }
            }

            Thread.sleep(TIME_DELAY);
        }

        return;
    }
}