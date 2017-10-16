package com.neo.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.neo.finder.JsonStreamParser;
import com.neo.request.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NearEarthObjects {

	private static Logger logger = LogManager.getLogger();
	public static final int MAX_DAYS_RANGE = 7;


	public static long getDateDiff(Calendar date1, Calendar date2, TimeUnit timeUnit) {
	    long diffInMillies = date1.getTimeInMillis() - date2.getTimeInMillis();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	public static Calendar validateAndParseDate(String dateStr) {
		
		if(dateStr.matches("\\d{4}-\\d{2}-\\d{2}"))
		{
			int year = Integer.parseInt(dateStr.substring(0, 4));
			int month = Integer.parseInt(dateStr.substring(5, 7));
			int day = Integer.parseInt(dateStr.substring(8, 10));
			Calendar targetDate = Calendar.getInstance();
			targetDate.set(year,month,day);
			return targetDate;
		}
		
		return null;
	}

	public static boolean isRangeValid(Calendar dateBegin, Calendar dateEnd) {
		
		// maximum 7 days
		if(getDateDiff(dateEnd,dateBegin,TimeUnit.DAYS) > MAX_DAYS_RANGE)
		{
			// error
			logger.error("Invalid date range: maximum of "+MAX_DAYS_RANGE+" days");
			return false;
		}
		
		// begin must be before end
		if(dateBegin.getTimeInMillis() > dateEnd.getTimeInMillis())
		{
			// error
			logger.error("Invalid date range: chronologic order required");
			return false;
		}
		
		return true;
	}

	private static void printHelp() {
		System.out.println("com.neo.main.NearEarthObjects \"yyyy-mm-dd\" \"yyyy-mm-dd\"");
		System.out.println("\nNB: Valid date range is "+MAX_DAYS_RANGE+" days or less.");
	}
	
	public static void main(String[] args){
		
		logger.info("Starting Near Earth Objects");
		if(args.length != 2)
		{
			// error
			logger.error("Wrong number of arguments.");
			printHelp();
			return;
		}
		Calendar dateBegin = validateAndParseDate(args[0]);
		Calendar dateEnd = validateAndParseDate(args[1]);
		if(dateBegin == null || dateEnd == null)
		{
			//error
			logger.error("Incorrect date format.");
			printHelp();
			return;
		}
		
		if(!isRangeValid(dateBegin, dateEnd))
		{
			// error
			printHelp();
			return;
		}
				
		InputStream in = HttpRequest.sendGet(args[0], args[1]);
		if(in != null)
		{
			JsonStreamParser jparser = new JsonStreamParser(in);
			jparser.parseStream();
			jparser.findClosestAndLargest();
			try {
				in.close();
			} catch (IOException e) {
				logger.error("Exception trying to close InputStream");
				return;
			}
		}
		logger.info("End of Near Earth Objects");
	}
	
}
