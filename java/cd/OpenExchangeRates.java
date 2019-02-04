import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.math.*;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.oer.OER;
import org.knowm.xchange.oer.dto.marketdata.OERTickers;
import org.knowm.xchange.oer.dto.marketdata.OERRates;
import org.knowm.xchange.service.marketdata.MarketDataService;
import si.mazi.rescu.RestProxyFactory;
import si.mazi.rescu.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/*

https://openexchangerates.org/api/latest.json?app_id=YOUR_APP_ID

Result Format
 200 OK
{
    disclaimer: "https://openexchangerates.org/terms/",
    license: "https://openexchangerates.org/license/",
    timestamp: 1449877801,
    base: "USD",
    rates: {
        AED: 3.672538,
        AFN: 66.809999,
        ALL: 125.716501,
        AMD: 484.902502,
        ANG: 1.788575,
        AOA: 135.295998,
        ARS: 9.750101,
        AUD: 1.390866,
        ...
    }
}

*/

/**
 * Get exchange rates for all fiat currencies against USD
 */
public class OpenExchangeRates {

    public OERRates rates;
    private final OERInterface oerInterface;

    public OpenExchangeRates() throws IOException
    {
        /*
            XChange seems to only allow you to retrieve one currency at a 
            time but OER has a call for doing it all at once.

            So the interface to OER is totally independent of XChange, but their
            data structures are still used to read the JSON response.

            Just need to make an empty ClientConfig for the REST call. 
            This is what XChange uses in BaseExchangeService, which is the same
            ClientConfig that OERMarketDataService would ultimately use.
        */

        ClientConfig rescuConfig = new ClientConfig(); // create default rescu config
        rescuConfig.setHttpConnTimeout(0);
        rescuConfig.setHttpReadTimeout(0);

        this.oerInterface = RestProxyFactory.createProxy(OERInterface.class, "http://openexchangerates.org", rescuConfig);
    }

    public OERRates loadOERRates(String key) throws IOException 
    {
        // Request data
        OERTickers oERTickers = OERInterface.getTickers(key);
        
        if (oERTickers == null)
        {
          throw new Exception("Null response returned from Open Exchange Rates!");
        }

        rates = oERTickers.getRates();

        System.out.println("\n\nOER RATES:\n\n");
        System.out.println(rates.toString());
        System.out.println("\n\nEND OER RATES\n\n");

        return rates;
    }

    public void copyRatesToMap(Map<Currency, BigDecimal> _allPrices)
    {
        _allPrices.put(Currency.AED, new BigDecimal(rates.getAED()));
        _allPrices.put(Currency.AFN, new BigDecimal(rates.getAFN()));
        _allPrices.put(Currency.ALL, new BigDecimal(rates.getALL()));
        _allPrices.put(Currency.AMD, new BigDecimal(rates.getAMD()));
        _allPrices.put(Currency.ANG, new BigDecimal(rates.getANG()));
        _allPrices.put(Currency.AOA, new BigDecimal(rates.getAOA()));
        _allPrices.put(Currency.ARS, new BigDecimal(rates.getARS()));
        _allPrices.put(Currency.AUD, new BigDecimal(rates.getAUD()));
        _allPrices.put(Currency.AWG, new BigDecimal(rates.getAWG()));
        _allPrices.put(Currency.AZN, new BigDecimal(rates.getAZN()));
        _allPrices.put(Currency.BAM, new BigDecimal(rates.getBAM()));
        _allPrices.put(Currency.BBD, new BigDecimal(rates.getBBD()));
        _allPrices.put(Currency.BDT, new BigDecimal(rates.getBDT()));
        _allPrices.put(Currency.BGN, new BigDecimal(rates.getBGN()));
        _allPrices.put(Currency.BHD, new BigDecimal(rates.getBHD()));
        _allPrices.put(Currency.BIF, new BigDecimal(rates.getBIF()));
        _allPrices.put(Currency.BMD, new BigDecimal(rates.getBMD()));
        _allPrices.put(Currency.BND, new BigDecimal(rates.getBND()));
        _allPrices.put(Currency.BOB, new BigDecimal(rates.getBOB()));
        _allPrices.put(Currency.BRL, new BigDecimal(rates.getBRL()));
        _allPrices.put(Currency.BSD, new BigDecimal(rates.getBSD()));
        _allPrices.put(Currency.BTC, new BigDecimal(rates.getBTC()));
        _allPrices.put(Currency.BTN, new BigDecimal(rates.getBTN()));
        _allPrices.put(Currency.BWP, new BigDecimal(rates.getBWP()));
        _allPrices.put(Currency.BYR, new BigDecimal(rates.getBYR()));
        _allPrices.put(Currency.BZD, new BigDecimal(rates.getBZD()));
        _allPrices.put(Currency.CAD, new BigDecimal(rates.getCAD()));
        _allPrices.put(Currency.CDF, new BigDecimal(rates.getCDF()));
        _allPrices.put(Currency.CHF, new BigDecimal(rates.getCHF()));
        _allPrices.put(Currency.CLF, new BigDecimal(rates.getCLF()));
        _allPrices.put(Currency.CLP, new BigDecimal(rates.getCLP()));
        _allPrices.put(Currency.CNY, new BigDecimal(rates.getCNY()));
        _allPrices.put(Currency.COP, new BigDecimal(rates.getCOP()));
        _allPrices.put(Currency.CRC, new BigDecimal(rates.getCRC()));
        _allPrices.put(Currency.CUP, new BigDecimal(rates.getCUP()));
        _allPrices.put(Currency.CVE, new BigDecimal(rates.getCVE()));
        _allPrices.put(Currency.CZK, new BigDecimal(rates.getCZK()));
        _allPrices.put(Currency.DJF, new BigDecimal(rates.getDJF()));
        _allPrices.put(Currency.DKK, new BigDecimal(rates.getDKK()));
        _allPrices.put(Currency.DOP, new BigDecimal(rates.getDOP()));
        _allPrices.put(Currency.DZD, new BigDecimal(rates.getDZD()));
        _allPrices.put(Currency.EEK, new BigDecimal(rates.getEEK()));
        _allPrices.put(Currency.EGP, new BigDecimal(rates.getEGP()));
        _allPrices.put(Currency.ETB, new BigDecimal(rates.getETB()));
        _allPrices.put(Currency.EUR, new BigDecimal(rates.getEUR()));
        _allPrices.put(Currency.FJD, new BigDecimal(rates.getFJD()));
        _allPrices.put(Currency.FKP, new BigDecimal(rates.getFKP()));
        _allPrices.put(Currency.GBP, new BigDecimal(rates.getGBP()));
        _allPrices.put(Currency.GEL, new BigDecimal(rates.getGEL()));
        _allPrices.put(Currency.GHS, new BigDecimal(rates.getGHS()));
        _allPrices.put(Currency.GIP, new BigDecimal(rates.getGIP()));
        _allPrices.put(Currency.GMD, new BigDecimal(rates.getGMD()));
        _allPrices.put(Currency.GNF, new BigDecimal(rates.getGNF()));
        _allPrices.put(Currency.GTQ, new BigDecimal(rates.getGTQ()));
        _allPrices.put(Currency.GYD, new BigDecimal(rates.getGYD()));
        _allPrices.put(Currency.HKD, new BigDecimal(rates.getHKD()));
        _allPrices.put(Currency.HNL, new BigDecimal(rates.getHNL()));
        _allPrices.put(Currency.HRK, new BigDecimal(rates.getHRK()));
        _allPrices.put(Currency.HTG, new BigDecimal(rates.getHTG()));
        _allPrices.put(Currency.HUF, new BigDecimal(rates.getHUF()));
        _allPrices.put(Currency.IDR, new BigDecimal(rates.getIDR()));
        _allPrices.put(Currency.ILS, new BigDecimal(rates.getILS()));
        _allPrices.put(Currency.INR, new BigDecimal(rates.getINR()));
        _allPrices.put(Currency.IQD, new BigDecimal(rates.getIQD()));
        _allPrices.put(Currency.IRR, new BigDecimal(rates.getIRR()));
        _allPrices.put(Currency.ISK, new BigDecimal(rates.getISK()));
        _allPrices.put(Currency.JEP, new BigDecimal(rates.getJEP()));
        _allPrices.put(Currency.JMD, new BigDecimal(rates.getJMD()));
        _allPrices.put(Currency.JOD, new BigDecimal(rates.getJOD()));
        _allPrices.put(Currency.JPY, new BigDecimal(rates.getJPY()));
        _allPrices.put(Currency.KES, new BigDecimal(rates.getKES()));
        _allPrices.put(Currency.KGS, new BigDecimal(rates.getKGS()));
        _allPrices.put(Currency.KHR, new BigDecimal(rates.getKHR()));
        _allPrices.put(Currency.KMF, new BigDecimal(rates.getKMF()));
        _allPrices.put(Currency.KPW, new BigDecimal(rates.getKPW()));
        _allPrices.put(Currency.KRW, new BigDecimal(rates.getKRW()));
        _allPrices.put(Currency.KWD, new BigDecimal(rates.getKWD()));
        _allPrices.put(Currency.KYD, new BigDecimal(rates.getKYD()));
        _allPrices.put(Currency.KZT, new BigDecimal(rates.getKZT()));
        _allPrices.put(Currency.LAK, new BigDecimal(rates.getLAK()));
        _allPrices.put(Currency.LBP, new BigDecimal(rates.getLBP()));
        _allPrices.put(Currency.LKR, new BigDecimal(rates.getLKR()));
        _allPrices.put(Currency.LRD, new BigDecimal(rates.getLRD()));
        _allPrices.put(Currency.LSL, new BigDecimal(rates.getLSL()));
        _allPrices.put(Currency.LTL, new BigDecimal(rates.getLTL()));
        _allPrices.put(Currency.LVL, new BigDecimal(rates.getLVL()));
        _allPrices.put(Currency.LYD, new BigDecimal(rates.getLYD()));
        _allPrices.put(Currency.MAD, new BigDecimal(rates.getMAD()));
        _allPrices.put(Currency.MDL, new BigDecimal(rates.getMDL()));
        _allPrices.put(Currency.MGA, new BigDecimal(rates.getMGA()));
        _allPrices.put(Currency.MKD, new BigDecimal(rates.getMKD()));
        _allPrices.put(Currency.MMK, new BigDecimal(rates.getMMK()));
        _allPrices.put(Currency.MNT, new BigDecimal(rates.getMNT()));
        _allPrices.put(Currency.MOP, new BigDecimal(rates.getMOP()));
        _allPrices.put(Currency.MRO, new BigDecimal(rates.getMRO()));
        _allPrices.put(Currency.MUR, new BigDecimal(rates.getMUR()));
        _allPrices.put(Currency.MVR, new BigDecimal(rates.getMVR()));
        _allPrices.put(Currency.MWK, new BigDecimal(rates.getMWK()));
        _allPrices.put(Currency.MXN, new BigDecimal(rates.getMXN()));
        _allPrices.put(Currency.MYR, new BigDecimal(rates.getMYR()));
        _allPrices.put(Currency.MZN, new BigDecimal(rates.getMZN()));
        _allPrices.put(Currency.NAD, new BigDecimal(rates.getNAD()));
        _allPrices.put(Currency.NGN, new BigDecimal(rates.getNGN()));
        _allPrices.put(Currency.NIO, new BigDecimal(rates.getNIO()));
        _allPrices.put(Currency.NOK, new BigDecimal(rates.getNOK()));
        _allPrices.put(Currency.NPR, new BigDecimal(rates.getNPR()));
        _allPrices.put(Currency.NZD, new BigDecimal(rates.getNZD()));
        _allPrices.put(Currency.OMR, new BigDecimal(rates.getOMR()));
        _allPrices.put(Currency.PAB, new BigDecimal(rates.getPAB()));
        _allPrices.put(Currency.PEN, new BigDecimal(rates.getPEN()));
        _allPrices.put(Currency.PGK, new BigDecimal(rates.getPGK()));
        _allPrices.put(Currency.PHP, new BigDecimal(rates.getPHP()));
        _allPrices.put(Currency.PKR, new BigDecimal(rates.getPKR()));
        _allPrices.put(Currency.PLN, new BigDecimal(rates.getPLN()));
        _allPrices.put(Currency.PYG, new BigDecimal(rates.getPYG()));
        _allPrices.put(Currency.QAR, new BigDecimal(rates.getQAR()));
        _allPrices.put(Currency.RON, new BigDecimal(rates.getRON()));
        _allPrices.put(Currency.RSD, new BigDecimal(rates.getRSD()));
        _allPrices.put(Currency.RUB, new BigDecimal(rates.getRUB()));
        _allPrices.put(Currency.RWF, new BigDecimal(rates.getRWF()));
        _allPrices.put(Currency.SAR, new BigDecimal(rates.getSAR()));
        _allPrices.put(Currency.SBD, new BigDecimal(rates.getSBD()));
        _allPrices.put(Currency.SCR, new BigDecimal(rates.getSCR()));
        _allPrices.put(Currency.SDG, new BigDecimal(rates.getSDG()));
        _allPrices.put(Currency.SEK, new BigDecimal(rates.getSEK()));
        _allPrices.put(Currency.SGD, new BigDecimal(rates.getSGD()));
        _allPrices.put(Currency.SHP, new BigDecimal(rates.getSHP()));
        _allPrices.put(Currency.SLL, new BigDecimal(rates.getSLL()));
        _allPrices.put(Currency.SOS, new BigDecimal(rates.getSOS()));
        _allPrices.put(Currency.SRD, new BigDecimal(rates.getSRD()));
        _allPrices.put(Currency.STD, new BigDecimal(rates.getSTD()));
        _allPrices.put(Currency.SVC, new BigDecimal(rates.getSVC()));
        _allPrices.put(Currency.SYP, new BigDecimal(rates.getSYP()));
        _allPrices.put(Currency.SZL, new BigDecimal(rates.getSZL()));
        _allPrices.put(Currency.THB, new BigDecimal(rates.getTHB()));
        _allPrices.put(Currency.TJS, new BigDecimal(rates.getTJS()));
        _allPrices.put(Currency.TMT, new BigDecimal(rates.getTMT()));
        _allPrices.put(Currency.TND, new BigDecimal(rates.getTND()));
        _allPrices.put(Currency.TOP, new BigDecimal(rates.getTOP()));
        _allPrices.put(Currency.TRY, new BigDecimal(rates.getTRY()));
        _allPrices.put(Currency.TTD, new BigDecimal(rates.getTTD()));
        _allPrices.put(Currency.TWD, new BigDecimal(rates.getTWD()));
        _allPrices.put(Currency.TZS, new BigDecimal(rates.getTZS()));
        _allPrices.put(Currency.UAH, new BigDecimal(rates.getUAH()));
        _allPrices.put(Currency.UGX, new BigDecimal(rates.getUGX()));
        _allPrices.put(Currency.UYU, new BigDecimal(rates.getUYU()));
        _allPrices.put(Currency.UZS, new BigDecimal(rates.getUZS()));
        _allPrices.put(Currency.VEF, new BigDecimal(rates.getVEF()));
        _allPrices.put(Currency.VND, new BigDecimal(rates.getVND()));
        _allPrices.put(Currency.VUV, new BigDecimal(rates.getVUV()));
        _allPrices.put(Currency.WST, new BigDecimal(rates.getWST()));
        _allPrices.put(Currency.XAF, new BigDecimal(rates.getXAF()));
        _allPrices.put(Currency.XCD, new BigDecimal(rates.getXCD()));
        _allPrices.put(Currency.XDR, new BigDecimal(rates.getXDR()));
        _allPrices.put(Currency.XOF, new BigDecimal(rates.getXOF()));
        _allPrices.put(Currency.XPF, new BigDecimal(rates.getXPF()));
        _allPrices.put(Currency.YER, new BigDecimal(rates.getYER()));
        _allPrices.put(Currency.ZAR, new BigDecimal(rates.getZAR()));
        _allPrices.put(Currency.ZMK, new BigDecimal(rates.getZMK()));
        _allPrices.put(Currency.ZWL, new BigDecimal(rates.getZWL()));
    }

    /*
        This wasn't finished. First attempt at doing the REST stuff to OER 
        without using anything from XChange. Tries to use javax directly.
    */
    public void getRates_DoesntWork()//Map<Currency, BigDecimal> _allPrices)
    {

        Client client = ClientBuilder.newClient();
        String uri = "https://openexchangerates.org/api/latest.json?app_id=" + key;
        String name = client.target(uri).request(MediaType.APPLICATION_JSON).get(String.class);

        System.out.println("\n\nOER RESPONSE:\n\n");
        System.out.println(name);
        System.out.println("\n\nEND OER RESPONSE\n\n");
    }
}