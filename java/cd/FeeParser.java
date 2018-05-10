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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;


/*
 * FeeParser is for reading in my own static, manually-entered fee values per exchange.
 * This will be replaced by another mechanism for obtaining these values, but for now I need something simple for testing out the graph.
 */
public class FeeParser{

	Fees fees = new Fees();

	public boolean read(String name)
	{
		try
		{
			//read json file data to String
			byte[] jsonData = Files.readAllBytes(Paths.get("../src/main/resources/fees/" + name + ".json"));
		
			//create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();
		
			//convert json string to object
			fees = objectMapper.readValue(jsonData, Fees.class);
		}
		catch(IOException e)
		{
			System.out.println("  " + name + " Fee file not found: " + e);
			return false;
		}

		return true;
	}
	
	public class Fees
	{
		public int tradeFee;
		public int withdrawFee; // Will eventually be the actual fee per currency instead of this dummy value.
	}
}