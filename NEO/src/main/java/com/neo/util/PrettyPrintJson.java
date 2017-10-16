package com.neo.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

public class PrettyPrintJson {

	public static Map<String, Boolean> config = new HashMap<String, Boolean>();
	
	public static void print(JsonValue val) {
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		StringWriter stWriter = new StringWriter();
		JsonWriterFactory writerFactory = Json.createWriterFactory(config);
	    JsonWriter jsonWriter = writerFactory.createWriter(stWriter);
	    jsonWriter.writeObject(val.asJsonObject());
	    jsonWriter.close();
	    System.out.println(stWriter.toString());
	}

}