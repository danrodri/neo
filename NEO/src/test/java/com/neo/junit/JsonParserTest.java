package com.neo.junit;

import javax.json.JsonValue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neo.finder.JsonStreamParser;


public class JsonParserTest {
	JsonStreamParser jparser;
	// JSON test data file created from a Postman request to NASA NEO REST API
	static final String JSON_FILE = "src/test/resources/sampleHttpResponse.json";
	static final String[] NEO_INVALID_PATH = {"close_approach_data", "invalid", "lunar"};
	static final String[] NEO_SIZE_PATH = {"estimated_diameter", "meters", "estimated_diameter_max"};
	private static MockedAppender mockedAppender;
	private static Logger logger;

	private static class MockedAppender extends AbstractAppender {
	    List<String> message = new ArrayList<>();

	    protected MockedAppender() {
	        super("MockedAppender", null, null);
	    }

	    @Override
	    public void append(LogEvent event) {
	        message.add(event.getMessage().getFormattedMessage());
	    }
	}
	
	@Before
	public void init() {
		mockedAppender.message.clear();
		File resourcesFile = new File(JSON_FILE);
		assertNotNull(resourcesFile);
		InputStream in = null;
		try {
			in = new FileInputStream(resourcesFile);
			jparser = new JsonStreamParser(in);
			jparser.parseStream();
		} catch (FileNotFoundException e) {
			fail("JSON file not found");
		}
		finally
		{
			if(in != null)
			{
				try {
					in.close();
				} catch (IOException e) {
					logger.error("Unable to close InputStream in JsonParserTest:init.");
				}
			}
		}
	}

	
	@Test
	public void testParsingJsonFromNEO() {
	    assertNotNull(jparser);
	    assertEquals("Must have right number of NEO.", 25, jparser.getNeo_number());
	}

	@BeforeClass
	public static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(JsonStreamParser.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.INFO);
	}

	@AfterClass
	public static void teardown() {
	    logger.removeAppender(mockedAppender);
	}
	
	@Test
	public void testFindJsonWithInvalidPath() {
	    assertNotNull(jparser);

	    ArrayList<JsonValue> jValArray =  jparser.getjValArray();
	    double result = jparser.findJsonValue(jValArray.get(0), NEO_INVALID_PATH);
	    
	    assertEquals("Should have 0.0d since error happened with incorrect path", 0.0d, result, 0.0d);
	    
	    boolean foundMsg = false;
	    for (String msg : mockedAppender.message) {
	    	if(msg.equals("Problem traversing path to JSON value"))
	    	{
	    		foundMsg = true;
	    	}
	    }
	    assertTrue("Should find log generated when processing incorrect path", foundMsg);
	}
	
	@Test
	public void testFindJsonWithValidPath() {
	    assertNotNull(jparser);

	    ArrayList<JsonValue> jValArray =  jparser.getjValArray();
	    double result = jparser.findJsonValue(jValArray.get(0), NEO_SIZE_PATH);
	    
	    assertEquals("Should find correct value.", 475.8170266383, result, 0.0d);
	}

	@Test
	public void testFindLargestNEO() {
	    assertNotNull(jparser);

	    JsonValue largestNeo = jparser.findLargestNeo(false);
	    assertNotNull("Should find largest NEO.", largestNeo);
	    assertTrue("Must Find correct largest NEO.", largestNeo.asJsonObject().getString("name").contains("2002 QD7"));
	}

	@Test
	public void testFindClosestNEO() {
	    assertNotNull(jparser);
	    
	    JsonValue closestNeo = jparser.findClosestNeo(false);
	    assertNotNull("Should find closest NEO.", closestNeo);
	    assertTrue("Must Find correct closest NEO.", closestNeo.asJsonObject().getString("name").contains("2017 SZ11"));
	}

}