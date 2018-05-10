import java.io.IOException;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.oer.OERExchange;
import org.knowm.xchange.oer.dto.marketdata.OERRates;
import org.knowm.xchange.oer.service.OERMarketDataServiceRaw;
import org.knowm.xchange.service.marketdata.MarketDataService;

/**
 * Demonstrate requesting Ticker at Open Exchange Rates
 */
public class OpenExchangeRates {

    private ExchangeSpecification exchangeSpecification;
    private Exchange openExchangeRates;
    private MarketDataService marketDataService;

    public OpenExchangeRates()
    {
        // Use the factory to get the Open Exchange Rates exchange API
        exchangeSpecification = new ExchangeSpecification(OERExchange.class.getName());
        exchangeSpecification.setPlainTextUri("http://openexchangerates.org");
        exchangeSpecification.setApiKey("257da32e4b584f3282beb4bc9623ca3a");

        openExchangeRates = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        marketDataService = openExchangeRates.getMarketDataService();
    }


    public Ticker getTicker(CurrencyPair pair)
    {
        // Get the latest ticker data showing EUR/USD
        try
        {
            return marketDataService.getTicker(pair);//(CurrencyPair.EUR_USD);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
}