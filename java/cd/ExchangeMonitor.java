/**
 * This is a class that holds the comparison information on every exchange in My Market
 */

//project cd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.math.*; 

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

/**
 * @author cd
 *
 */
abstract public class ExchangeMonitor {

    private BigDecimal tradeFee; // Fractional fee
    private BigDecimal withdrawFee;
    
    abstract public void init();

	// Get permanent data members: name, exchange, currencyPairs

    abstract public String getName();

    abstract public Exchange getExchange();

    abstract public List<CurrencyPair> getCurrencyPairs();

    abstract public Set<Currency> getCurrencies();

    abstract public BigDecimal getTradeFee();

    abstract public BigDecimal getWithdrawFee();

    // Refresh info from remote exchange and view it

    abstract public boolean loadTicker(CurrencyPair pair);

    abstract public Ticker viewTicker(CurrencyPair pair);

    abstract public boolean loadOrderBook(CurrencyPair pair);

    abstract public OrderBook viewOrderBook(CurrencyPair pair);

    abstract public BigDecimal getExchangeRate(Currency from, Currency to, BigDecimal tradeAmount);
}