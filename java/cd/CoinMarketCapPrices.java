import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.*;
import java.net.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.knowm.xchange.coinmarketcap.dto.marketdata.CoinMarketCapTicker;

public class CoinMarketCapPrices 
{
    final private String urlToRead = "https://api.coinmarketcap.com/v1/ticker/";

    public List<CoinMarketCapTicker> tickers;

    public CoinMarketCapPrices() throws IOException
    {
        String response = new String();

        // Get https://api.coinmarketcap.com/v1/ticker/ response saved as string
        try
        {
            response = getHTML();
        }
        catch(Exception e)
        {
            System.out.println("Exception getting CoinMarketCap data");
        }

        //System.out.println("CMC response:\n" + response);

        // Convert save and save into 'tickers'
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        tickers = objectMapper.readValue(response, typeFactory.constructCollectionType(List.class, CoinMarketCapTicker.class));
    }

    private String getHTML() throws Exception 
    {
      StringBuilder result = new StringBuilder();
      URL url = new URL(urlToRead);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      
      while ((line = rd.readLine()) != null) 
      {
         result.append(line);
      }
      
      rd.close();
      return result.toString();
   }
}