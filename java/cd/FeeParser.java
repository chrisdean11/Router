import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.math.*; 

import org.knowm.xchange.currency.Currency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;


/*
 * FeeParser is for reading in my own static, manually-entered fee values per exchange.
 * Expected format of data:
        exchange : "ExchangeName"
        tradefee : .01
        withdrawfees :{
            BTC : .005
            NEO : 0
            (...)
        }
 */
public class FeeParser{

    String exchange;
    BigDecimal tradeFee;
    Map<Currency, BigDecimal> withdrawFees = new HashMap<Currency,BigDecimal>();

    public boolean read(String name)
    {
        try
        {
            //read json file data to String
            String jsonData = new String(Files.readAllBytes(Paths.get("./src/main/resources/fees/" + name + ".json")));
            System.out.println("  " + name + " Fee info found: ./src/main/resources/fees/" + name + ".json");

            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
            Map<String, Object> map = mapper.readValue(jsonData, type);
            Map<String, Object> fees = new HashMap<String, Object>();

            exchange = (String)map.get("exchange");
            tradeFee = getNumVal(map.get("tradefee"));

            if (map.get("withdrawfees") instanceof Map)
            {
                fees = (Map)map.get("withdrawfees");
            }
            else
            {
                System.out.println("Error: withdrawfees is not a map: " + map.get("withdrawfees").getClass());
                return false;
            }

            for(Map.Entry<String, Object> entry : fees.entrySet())
            {
                withdrawFees.put(new Currency(entry.getKey()), getNumVal(entry.getValue()));
            }
        }
        catch(IOException e)
        {
            System.out.println("  " + name + " Error loading fee info: " + e);
            return false;
        }

        return true;
    }

    private BigDecimal getNumVal(Object val)
    {
        BigDecimal f;

        // The entry could be read in as a string, integer, or decimal value
        try
        {
            String fee = (String)val;
            f = new BigDecimal(fee);
        }
        catch(Exception ex)
        {
            try
            {
                f = new BigDecimal((double)val).setScale(8, RoundingMode.HALF_EVEN);
                System.out.println(ex + ": " + f);
            }
            catch(Exception e)
            {
                f = new BigDecimal((int)val);
                System.out.println(e + ": " + f);
            }
        }

        return f.stripTrailingZeros();
    }

    /*
    public boolean read(String name)
    {
        try
        {
            //read json file data to String
            byte[] jsonData = Files.readAllBytes(Paths.get("./src/main/resources/fees/" + name + ".json"));
            System.out.println("  " + name + " Fee info found: ./src/main/resources/fees/" + name + ".json");

            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
        
            //convert json string to object
            fees = objectMapper.readValue(jsonData, Fees.class);
        }
        catch(IOException e)
        {
            System.out.println("  " + name + " Error loading fee info: " + e);
            return false;
        }

        return true;
    }

    static class Fees
    {
        public double tradeFee;
        public double withdrawFee; // Will eventually be the actual fee per currency instead of this dummy value.
    }
    */
}