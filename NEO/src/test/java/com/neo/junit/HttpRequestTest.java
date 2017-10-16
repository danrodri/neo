package com.neo.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.stream.Collectors;

import com.neo.main.NearEarthObjects;
import com.neo.request.HttpRequest;

import org.junit.Test;

public class HttpRequestTest {
		
	@Test
	public void testValidDateRangeRequest() {
		
		InputStream in = HttpRequest.sendGet("2017-10-12", "2017-10-13");
	    assertNotNull(in);
	    String result = new BufferedReader(new InputStreamReader(in))
	    		  .lines().collect(Collectors.joining("\n"));
	    System.out.println(result);
		try {
			in.close();
		} catch (IOException e) {
			// does not really matter in this test
		}
	}

	@Test
	public void testInvalidDateString() {
		Calendar cal1 = NearEarthObjects.validateAndParseDate("98-11-21");
	    assertNull(cal1);
	}

	@Test
	public void testValidDateString() {
		Calendar cal1 = NearEarthObjects.validateAndParseDate("2017-10-16");
	    assertNotNull(cal1);
	}

	@Test
	public void testInvalidDateRange_beginAfterEnd() {
		Calendar dateBegin = Calendar.getInstance();
		Calendar dateEnd = Calendar.getInstance();

		// begin after end
		dateBegin.set(2017, 04, 21);
		dateEnd.set(2017, 04, 19);
		boolean result = NearEarthObjects.isRangeValid(dateBegin, dateEnd);
	    assertFalse("Should fail with begin date after end date.", result);
	}

	@Test
	public void testInvalidDateRange_tooLong() {
		Calendar dateBegin = Calendar.getInstance();
		Calendar dateEnd = Calendar.getInstance();

		// range more than MAX_DAYS_RANGE
		int beginDay = 05;
		dateBegin.set(2017, 04, beginDay);
		dateEnd.set(2017, 04, beginDay + NearEarthObjects.MAX_DAYS_RANGE + 4);
		boolean result = NearEarthObjects.isRangeValid(dateBegin, dateEnd);
	    assertFalse("Should fail with more than "+ NearEarthObjects.MAX_DAYS_RANGE +" days.", result);
	}


}