package com.neo.finder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
// use to debug
//import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.neo.util.PrettyPrintJson;

public class JsonStreamParser {
	private static Logger logger = LogManager.getLogger();
	private JsonParser jParser;
	static final String[] NEO_SIZE_PATH = {"estimated_diameter", "meters", "estimated_diameter_max"};
	static final String[] NEO_DIST_PATH = {"close_approach_data", "miss_distance", "lunar"};
	
	private int neo_number;
	private ArrayList<JsonValue> jValArray;

	public ArrayList<JsonValue> getjValArray() {
		return jValArray;
	}

	public int getNeo_number() {
		return neo_number;
	}

	public JsonStreamParser(InputStream in) {
		neo_number = 0;
		jValArray = new ArrayList<>();
		this.jParser = Json.createParser(in);
	}
	
	protected void finalize() {
		jParser.close();
	}

	public JsonParser getjParser() {
		return jParser;
	}

	public void setjParser(JsonParser jParser) {
		this.jParser = jParser;
	}
	
	public void parseStream() {
	    while (jParser.hasNext()) {
	        Event event = jParser.next();
	        if (event == JsonParser.Event.KEY_NAME ) {
	            String key = jParser.getString();
	            event = jParser.next();
	            if (key.equals("element_count")) {
	            	neo_number = jParser.getInt();
	            }
	            else if(key.equals("near_earth_objects"))
	            {
	            	for(JsonValue jVal:jParser.getObject().values())
	            	{
	            		JsonArray jArray = jVal.asJsonArray();
	            		List<JsonValue> neoList = jArray.stream().collect(Collectors.toList());
	            		jValArray.addAll(neoList);
	            	}
	            }
	        }
	    }
	}

	public double findJsonValue(JsonValue jVal, String[] PATH) {
		JsonValue current = jVal;
		for(int i=0;i<PATH.length;i++)
		{
			if (current.getValueType() == ValueType.ARRAY)
			{
				final String target = PATH[i];
				JsonArray jArray = current.asJsonArray();
				List<JsonValue> jsonItems = IntStream.range(0, jArray.size())
			            .mapToObj(index -> (JsonValue) jArray.get(index)).filter(p -> (p.getValueType() == ValueType.OBJECT && 
			            		p.asJsonObject().containsKey(target))).map(p -> p.asJsonObject().getJsonObject(target))
			            .collect(Collectors.toList());
				if(!jsonItems.isEmpty())
				{
					current = jsonItems.get(0);
				}
				else
				{
					logger.error("Problem traversing path to JSON value");
					return 0.0d;
				}
			}
			else
			{
				current = current.asJsonObject().get(PATH[i]);
			}
			if(current == null)
			{
				logger.error("FindValue failed somewhere in the search path");
				System.out.println("FindValue failed somewhere in the search path");
				return 0.0d;
			}
		}
		
		return Double.parseDouble(current.toString().replaceAll("\"", ""));
	}

	public JsonValue findLargestNeo(boolean useStdOut) {
    	//for debugging
    	//Consumer<JsonValue> consumerPrintJsonVal = p -> System.out.println(p);
    	//jValArray.stream().forEach(consumerPrintJsonVal);
    	
    	Comparator<JsonValue> byNeoSize =
    			(JsonValue o1, JsonValue o2)->Double.compare(findJsonValue(o1,NEO_SIZE_PATH), findJsonValue(o2,NEO_SIZE_PATH));
    	
    	// find largest in size
    	Optional<JsonValue> largestNeo = jValArray.stream().max(byNeoSize);
    	

		if(largestNeo.isPresent())
		{
			if(useStdOut)
			{
				System.out.println("Largest NEO:");
				PrettyPrintJson.print(largestNeo.get());
			}
			return largestNeo.get();
		}

		logger.error("Problem: nothing found for largest NEO in findLargestNeo.");
    	return null;
	}

	public JsonValue findClosestNeo(boolean useStdOut) {
    	// for debugging
    	//Consumer<JsonValue> consumerPrintJsonVal = p -> System.out.println(p);
    	//jValArray.stream().forEach(consumerPrintJsonVal);
    	
        Comparator<JsonValue> byNeoDist =
            			(JsonValue o1, JsonValue o2)->Double.compare(findJsonValue(o1,NEO_DIST_PATH), findJsonValue(o2,NEO_DIST_PATH));
    	
    	// find closest to earth
    	Optional<JsonValue> closestNeo = jValArray.stream().min(byNeoDist);

		if(closestNeo.isPresent())
		{
			if(useStdOut)
			{
	    		System.out.println("Closest NEO:");
	    		PrettyPrintJson.print(closestNeo.get());
			}
    		return closestNeo.get();
		}

		logger.error("Problem: nothing found for closest NEO in findclosestNeo.");
    	return null;
	}

	public void findClosestAndLargest() {
		JsonValue closestNeo = findClosestNeo(false);
		JsonValue largestNeo = findLargestNeo(false);

    	System.out.println("Number of NEO objects: " + this.neo_number);

    	if(largestNeo != null && closestNeo != null && 
    			largestNeo.asJsonObject().getString("neo_reference_id").equals(
    					closestNeo.asJsonObject().getString("neo_reference_id")))
    	{
    		// largest and closest are the same
    		System.out.println("Largest and closest NEO:");
    		PrettyPrintJson.print(largestNeo);
    	}
    	else
    	{
	    	if(closestNeo != null)
			{
	    		System.out.println("Closest NEO:");
	    		PrettyPrintJson.print(closestNeo);
			}
	    	if (largestNeo != null)
			{
				System.out.println("Largest NEO:");
				PrettyPrintJson.print(largestNeo);
			}
    	}
	}
}