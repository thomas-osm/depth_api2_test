package nl.esi.metis.aisparser.impl;

import java.text.DecimalFormat;

import nl.esi.metis.aisparser.AISMessagePositionReport;
import nl.esi.metis.aisparser.Sixbit;
import nl.esi.metis.aisparser.UtilsAngle12;
import nl.esi.metis.aisparser.UtilsLatitude27;
import nl.esi.metis.aisparser.UtilsLongitude28;
import nl.esi.metis.aisparser.UtilsNavStatus;
import nl.esi.metis.aisparser.UtilsPositionInfo;
import nl.esi.metis.aisparser.UtilsRateOfTurn8;
import nl.esi.metis.aisparser.UtilsSpare;
import nl.esi.metis.aisparser.UtilsSpecialManoeuvreIndicator2;
import nl.esi.metis.aisparser.UtilsTimeStamp;
import nl.esi.metis.aisparser.UtilsAngle9;
import nl.esi.metis.aisparser.UtilsTwosComplement;
import nl.esi.metis.aisparser.annotations.AISIllegalValueAnnotation;
import nl.esi.metis.aisparser.provenance.VDMMessageProvenance;

/** ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 */

/** This is the base class implementation for all AIS position report messages. It implements the fields that they all have in common. 
 * <pre>
 * Field Nr     Field Name                          NrOf Bits   (from,  to)
 * ------------------------------------------------------------------------
 *  1           messageID                                  6    (   1,   6)
 *  2           repeatIndicator                            2    (   7,   8)
 *  3           userID                                    30    (   9,  38)
 *  4           navigationalStatus                         4    (  39,  42)
 *  5           rateOfTurn                                 8    (  43,  50)
 *  6           speedOverGround                           10    (  51,  60)
 *  7           positionAccuracy                           1    (  61,  61)
 *  8           longitude                                 28    (  62,  89)
 *  9           latitude                                  27    (  90, 116)
 * 10           courseOverGround                          12    ( 117, 128)
 * 11           trueHeading                                9    ( 129, 137)
 * 12           timeStamp                                  6    ( 138, 143)
 * 13           specialManoeuvre                           2    ( 144, 145)
 * 14           spare                                      3    ( 146, 148)
 * 15           raimFlag                                   1    ( 149, 149)
 * 16           communicationState                        19    ( 150, 168)
 *                                                       ---- +
 *                       (maximum) number of bits        168
 * </pre>
 * 
 * @author Pierre van de Laar
 * @author Pierre America
 * @author Brian C. Lane
 */
abstract class AISMessagePositionReportImpl extends AISMessageImpl implements AISMessagePositionReport {
	
	/** The position of the first bit of the navigational status. */
	private static final int NAVIGATIONALSTATUS_FROM = 39;

	/** The position of the last bit of the navigational status. */
	private static final int NAVIGATIONALSTATUS_TO = 42;

	/** The navigational status. */
	private int navigationalStatus;

	/** Returns the navigational status.
	 * This can be further analyzed using utility class {@link UtilsNavStatus}.
	 * @return an integer in the range of 0 to 15
	 */
	public int getNavigationalStatus() { return navigationalStatus; }

	/** The position of the first bit of the rate of turn. */
	private static final int RATEOFTURN_FROM = 43;

	/** The position of the last bit of the rate of turn. */
	private static final int RATEOFTURN_TO = 50;

	/** The rate of turn. */
	private int rateOfTurn;

	/** Returns the rate of turn. 
	 * This value can be analyzed further with utility class {@link UtilsRateOfTurn8}.
	 * @return an integer value in the range of -128 to 127
	 */
	public int getRateOfTurn() { return rateOfTurn; }

	/** The position of the first bit of the speed over ground. */
	private static final int SPEEDOVERGROUND_FROM = 51;

	/** The position of the last bit of the speed over ground. */
	private static final int SPEEDOVERGROUND_TO = 60;

	/** The speed over ground */
	private int speedOverGround;

	/** Returns the speed over ground.
	 * @return an integer value in the range of 0 to 1023, 
	 * representing the speed over ground in 1/10 knot steps (0-102.2 knots) <br>
	 * 1023 = not available <br>
	 * 1022 = 102.2 knots or higher
	 */
	public int getSpeedOverGround() { return speedOverGround; }

	/** The format we use to format the speed over ground */
	private static final DecimalFormat SOG_FORMAT = new DecimalFormat("##0.0");

	/** Returns the speed over ground as a string.
	 * @return a string representing the speed over ground
	 */
	public String getSpeedOverGroundString() {
		String sogString;
		if (speedOverGround == 1023)
			sogString = "no SOG";
		else if (speedOverGround == 1022)
			sogString = ">=102.2";
		else
			sogString = SOG_FORMAT.format(speedOverGround / 10.0);
		return sogString;
	}

	/** The position of the position accuracy flag */
	private static final int POSITIONACCURACY_BITINDEX = 61;

	/** The position accuracy flag */
	private boolean positionAccuracy;

	/** Returns the position accuracy.
	 * @return a boolean value representing position accuracy: <br>
	 * true = high (&le; 10 m) <br>
	 * false = low (&gt; 10 m)
	 */
	public boolean getPositionAccuracy() { return positionAccuracy; }

	/** The position of the first bit of the longitude. */
	private static final int LONGITUDE_FROM = 62;

	/** The position of the last bit of the longitude. */
	private static final int LONGITUDE_TO = 89;
	
	/** The longitude. */
	private double longitude;

	/** Returns the longitude in degrees.
	 * This value can be analyzed further with utility class {@link UtilsPositionInfo}.
	 * @return a double value representing the longitude in degrees (&plusmn;180&deg;, East = positive, West = negative;  181&deg; = (6791AC0 hex) = not available = default).
	 */
	public double getLongitudeInDegrees() { return longitude; }

	/** The position of the first bit of the latitude. */
	private static final int LATITUDE_FROM = 90;

	/** The position of the last bit of the latitude. */
	private static final int LATITUDE_TO = 116;

	/** The latitude. */
	private double latitude;

	/** Returns the latitude in degrees.
	 * This value can be analyzed further with utility class {@link UtilsPositionInfo}.
	 * @return a double value representing the latitude in degrees (&plusmn;90&deg;, North = positive, South = negative; 91&deg; = (3412140 hex) = not available = default).
	 */
	public double getLatitudeInDegrees() { return latitude; }

	/** The position of the first bit of the course over ground. */
	private static final int COURSEOVERGROUND_FROM = 117;

	/** The position of the last bit of the course over ground. */
	private static final int COURSEOVERGROUND_TO = 128;

	/** The course over ground. */
	private int courseOverGround;

	/** Returns the course over ground.
	 * This value can be analyzed further with utility class {@link UtilsAngle12}.
	 * @return an integer value representing the course over ground in 1/10&deg; for values in the range of 0 to 3599.<br>
	 * 3600 (E10h) = not available. <br>
	 * 3601 or higher should not be used
	 */
	public int getCourseOverGround() { return courseOverGround; }

	/** The position of the first bit of the true heading. */
	private static final int TRUEHEADING_FROM = 129;

	/** The position of the last bit of the true heading. */
	private static final int TRUEHEADING_TO = 137;

	/** The true heading. */
	private int trueHeading;

	/** Returns the true heading.
	 * @return an integer value representing the true heading in degrees (0-359). <br>
	 * 511 indicates not available
	 */
	public int getTrueHeading() { return trueHeading; }

	/** The position of the first bit of the time stamp. */
	private static final int TIMESTAMP_FROM = 138;

	/** The position of the last bit of the time stamp. */
	private static final int TIMESTAMP_TO = 143;

	/** The time stamp. */
	private int timeStamp;

	/** Returns the time stamp contained in the message.
	 * This can be analyzed further using utility class {@link UtilsTimeStamp}.
	 * @return an integer value representing the UTC second when the report was generated by the electronic position fixing system (EPFS) (0-59) <br>
	 * 60 if time stamp is not available <br>
	 * 61 if positioning system is in manual input mode <br>
	 * 62 if electronic position fixing system operates in estimated (dead reckoning) mode <br>
	 * 63 if the positioning system is inoperative
	 */
	public int getTimeStamp() { return timeStamp; }

	/** The position of the first bit of the special maneuver indicator. */
	private static final int SPECIALMANOEUVRE_FROM = 144;

	/** The position of the last bit of the special maneuver indicator. */
	private static final int SPECIALMANOEUVRE_TO = 145;

	/** The special maneuver indicator. */
	private int specialManoeuvreIndicator;

	/** Returns the special maneuver indicator.
	 * @return an integer value with the following meaning: <br>
	 * 0 = not available <br>
	 * 1 = not engaged in special maneuver <br>
	 * 2 = engaged in special maneuver (i.e.: regional passing arrangement on Inland Waterway)
	 */
	public int getSpecialManoeuvre() { return specialManoeuvreIndicator; }

	/** The position of the first of the spare bits. */
	private static final int SPARE_FROM = 146;

	/** The position of the last of the spare bits. */
	private static final int SPARE_TO = 148;

	/** The value of the spare bits */
	private int spare;

	/** Returns the spare bits.
	 * @return the integer value of the spare bits, which should be zero.
	 */
	public int getSpare() { return spare; }

	/** The position of the RAIM flag */
	private static final int RAIMFLAG_BITINDEX = 149;

	/** The RAIM flag */
	private boolean raimFlag;
	/** Returns the RAIM flag, which describes the receiver autonomous integrity monitoring status of the electronic position fixing device.
	 * @return a boolean value: <br>
	 * false = RAIM not in use <br>
	 * true = RAIM in use
	 */
	public boolean getRaimFlag() { return raimFlag; }

	/** The position of the first bit of the communication state (for use in subclasses). */
	protected static final int COMMUNICATIONSTATE_FROM = 150;

	/** The length in bytes that a valid AIS position report message should have */
	public static final int LENGTH = 168;

	/** Checks whether the given number can be the length in bytes of the contents of a valid AIS position report message.
	 * @param length an integer value representing the length of a message in bytes
	 * @return true if this can be the length of a valid AIS position report message
	 */
	public static boolean validLength(int length)
	{
		return (length == AISMessagePositionReportImpl.LENGTH);
	}
	
	/** Constructs an AIS Message Position Report.
	 * @param content AIS content
	 * @param prov the provenance of the message
	 * @precondition validLength(content.length())
	 */
	protected AISMessagePositionReportImpl(Sixbit content, VDMMessageProvenance prov)
	{
		super(content, prov);
		assert(validLength(content.length()));
		
	    navigationalStatus = content.getIntFromTo(NAVIGATIONALSTATUS_FROM,NAVIGATIONALSTATUS_TO);
	    if (!UtilsNavStatus.isNavStatusSemanticallyCorrect(navigationalStatus))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getNavigationalStatus", navigationalStatus, UtilsNavStatus.range));
	    }
	    rateOfTurn = UtilsTwosComplement.convertFrom8Bits( content.getIntFromTo(RATEOFTURN_FROM,RATEOFTURN_TO) );
	    speedOverGround = content.getIntFromTo(SPEEDOVERGROUND_FROM,SPEEDOVERGROUND_TO);
	    positionAccuracy = content.getBoolean(POSITIONACCURACY_BITINDEX);
	    longitude = UtilsLongitude28.toDegrees( UtilsTwosComplement.convertFrom28Bits( content.getIntFromTo(LONGITUDE_FROM,LONGITUDE_TO) ) );
	    if (!UtilsPositionInfo.isLongitudeSemanticallyCorrect(longitude))
	    {
	    	annotations.add (new AISIllegalValueAnnotation("getLongitudeInDegrees", longitude, UtilsPositionInfo.rangeLongitude));
	    }
	    latitude = UtilsLatitude27.toDegrees( UtilsTwosComplement.convertFrom27Bits( content.getIntFromTo(LATITUDE_FROM,LATITUDE_TO) ) );
	    if (!UtilsPositionInfo.isLatitudeSemanticallyCorrect(latitude))
	    {
	    	annotations.add (new AISIllegalValueAnnotation("getLatitudeInDegrees", latitude, UtilsPositionInfo.rangeLatitude));
	    }
	    courseOverGround = content.getIntFromTo(COURSEOVERGROUND_FROM,COURSEOVERGROUND_TO);
	    if (!UtilsAngle12.isAngleSemanticallyCorrect(courseOverGround))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getCourseOverGround", courseOverGround, UtilsAngle12.range));
	    }
	    trueHeading = content.getIntFromTo(TRUEHEADING_FROM,TRUEHEADING_TO);
	    if(!UtilsAngle9.isAngleSemanticallyCorrect(trueHeading))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getTrueHeading",trueHeading, UtilsAngle9.range));
	    }
	    timeStamp = content.getIntFromTo(TIMESTAMP_FROM,TIMESTAMP_TO);
	    specialManoeuvreIndicator = content.getIntFromTo(SPECIALMANOEUVRE_FROM,SPECIALMANOEUVRE_TO);
	    if (!UtilsSpecialManoeuvreIndicator2.isSpecialManoeuvreIndicatorSemanticallyCorrect(specialManoeuvreIndicator))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpecialManoeuvre",specialManoeuvreIndicator, UtilsSpecialManoeuvreIndicator2.range));
	    }
	    spare = content.getIntFromTo(SPARE_FROM,SPARE_TO);
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare", spare, UtilsSpare.range));
	    }
	    raimFlag = content.getBoolean(RAIMFLAG_BITINDEX);	    
	}

	/** Generates a String representing the AIS message. 
	 * Format: all fields are shown in the order as specified by the standard separated by the SEPARATOR string.
	 */
	@Override
	public String toString() {
		String result = super.toString();
		result += UtilsNavStatus.toString(navigationalStatus);
		result += SEPARATOR + UtilsRateOfTurn8.toString(rateOfTurn);
		result += SEPARATOR + getSpeedOverGroundString();
		result += SEPARATOR + (positionAccuracy ? "high" : "low") + " accuracy";
		result += SEPARATOR + UtilsPositionInfo.longitudeToString(longitude);
		result += SEPARATOR + UtilsPositionInfo.latitudeToString(latitude);
		result += SEPARATOR + UtilsAngle12.toString(courseOverGround);
		result += SEPARATOR + UtilsAngle9.toString(trueHeading);
		result += SEPARATOR + UtilsTimeStamp.toString(timeStamp);
		result += SEPARATOR + UtilsSpecialManoeuvreIndicator2.toString(specialManoeuvreIndicator);
		result += SEPARATOR + "RAIM " + (raimFlag ? "in use" : "not in use");
		return result;
	}
}