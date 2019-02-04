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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;


public class KeyParser{

    Map<String, String> keyMap = new HashMap<Currency,BigDecimal>();

    public Map<String, String> read()
    {
        try
        {
            String jsonData = new String(Files.readAllBytes(Paths.get("../keys/keys.json")));
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
            Map<String, Object> map = mapper.readValue(jsonData, type);

            for(Map.Entry<String, Object> entry : map.entrySet())
            {
                keyMap.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        catch(IOException e)
        {
            System.out.println("Error loading key info: " + e);
            return null;
        }

        return keyMap;
    }
}