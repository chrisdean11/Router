
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.core.ProductSubscription;

import info.bitrich.xchangestream.service.netty.NettyStreamingService;

import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingMarketDataService;
import info.bitrich.xchangestream.gdax.GDAXStreamingService;

import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingMarketDataService;

import io.reactivex.disposables.Disposable;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import org.knowm.xchange.currency.CurrencyPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
    Handles communication with an exchange and maintains a local snapshot of its current state

    THIS DOES NOT WORK IF YOU PUT MORE THAN A COUPLE CURRENCY PAIRS IN 'symbols'
*/
public class StreamingExchangeMonitor
{
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(NettyStreamingService.class);
    private static List<CurrencyPair> symbols;
    private static String name;

    public static void main(String [] args) throws Exception
    {
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange.class.getName());
        symbols = exchange.getExchangeSymbols();
        name = exchange.getExchangeSpecification().getExchangeName();
        System.out.println(name + " currencies: " + symbols);

        ProductSubscription.ProductSubscriptionBuilder productSubscriptionBuilder = ProductSubscription.create();

        /*
        symbols = new ArrayList<CurrencyPair>(Arrays.asList(
            new CurrencyPair("ETH/BTC"),
            new CurrencyPair("LTC/EUR"),
            new CurrencyPair("BTC/GBP"),
            new CurrencyPair("BTC/USD"),
            new CurrencyPair("ETH/USD"),
            new CurrencyPair("BCH/USD"),
            new CurrencyPair("BCH/EUR"),
            new CurrencyPair("BTC/EUR"),
            new CurrencyPair("ETH/EUR"),
            new CurrencyPair("ETH/BTC"),
            new CurrencyPair("LTC/USD"),
            new CurrencyPair("LTC/BTC")
            ));
        */

        for(CurrencyPair pair : symbols)
        {
            productSubscriptionBuilder.addAll(pair);
        }


        // productSubscriptionBuilder.addAll(new CurrencyPair("ETH/BTC"));
        // productSubscriptionBuilder.addAll(new CurrencyPair("LTC/EUR"));
        // Connect to the Exchange WebSocket API. Blocking wait for the connection.
        exchange.connect(productSubscriptionBuilder.build()).blockingAwait();

/*
        // Subscribe to live trades, orderbooks, and ticker streams
        Disposable trades = exchange.getStreamingMarketDataService().getTrades(CurrencyPair.ETH_BTC)
                                    .subscribe(trade -> { System.out.println("Incoming trade: {" + trade +"}"); }, 
                                            throwable -> { LOG.error("Error in subscribing trades.", throwable); }
                                        );
        
        // Subscribe order book data with the reference to the subscription.
        Disposable orders = exchange.getStreamingMarketDataService().getOrderBook(CurrencyPair.ETH_BTC)
                                    .subscribe(orderBook -> { System.out.println("Incoming orderBook: {}" + orderBook); },
                                            throwable -> { LOG.error("Error in subscribing orders.", throwable); }
                                        );
*/

        // Subscribe to tickers for the each symbol
        List<Disposable> tickers = new ArrayList<Disposable>();
        for(CurrencyPair pair : symbols)
        {
            Disposable tick = exchange.getStreamingMarketDataService().getTicker(pair)
                                    .subscribe(ticker -> { System.out.println("Incoming orderBook: {}" + ticker); },
                                            throwable -> { System.out.println("Error in subscribing orders." + throwable); }
                                        );
            tickers.add(tick);
        }


/*
        // Subscribe ticker data with the reference to the subscription.
        Disposable tick = exchange.getStreamingMarketDataService().getTicker(CurrencyPair.LTC_EUR)
                                    .subscribe(ticker -> { System.out.println("Incoming ticker: {}" + ticker); },
                                            throwable -> { System.out.println("Error in subscribing orders." + throwable); }
                                        );

        // Subscribe ticker data with the reference to the subscription.
        Disposable tick2 = exchange.getStreamingMarketDataService().getTicker(CurrencyPair.ETH_BTC)
                                    .subscribe(ticker -> { System.out.println("Incoming ticker: {}" + ticker); },
                                            throwable -> { System.out.println("Error in subscribing orders." + throwable); }
                                        );

*?


/*      // Subscribe special GDAX formatted ticker data with the reference to the subscription.
        Disposable rawTickers = exchange.getStreamingMarketDataService().getRawTicker(CurrencyPair.BTC_USD)
                                    .subscribe(ticker -> { System.out.println("Incoming orderBook: {}" + ticker); },
                                            throwable -> { LOG.error("Error in subscribing orders.", throwable); }
                                        );
*/
        // Unsubscribe.
        System.out.println("Uhhhhhhh.");
        //trades.dispose();
        //orders.dispose();
        //tickers.dispose();
        //rawTickers.dispose();
        
        // Disconnect from exchange (non-blocking)
        //exchange.disconnect().subscribe(() -> LOG.info("Disconnected from the Exchange"));
    }
}