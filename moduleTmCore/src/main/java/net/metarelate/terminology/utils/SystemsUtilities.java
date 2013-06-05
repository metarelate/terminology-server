package net.metarelate.terminology.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SystemsUtilities {
	public static String getStandardTimeStamp() {
		Calendar myCalendar=new GregorianCalendar();
		//String timeStamp=Integer.toString(new GregorianCalendar().get(GregorianCalendar.YEAR));
		String timeStamp=Integer.toString(myCalendar.get(GregorianCalendar.YEAR))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.DAY_OF_YEAR))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.HOUR_OF_DAY))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.MINUTE))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.SECOND))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.MILLISECOND));
		return timeStamp;
	}
}
