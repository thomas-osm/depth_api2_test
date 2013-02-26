package nl.esi.metis.aisparser.impl;

import nl.esi.metis.aisparser.AISMessage24PartB;
import nl.esi.metis.aisparser.Sixbit;
import nl.esi.metis.aisparser.UtilsSpare;
import nl.esi.metis.aisparser.UtilsString;
import nl.esi.metis.aisparser.annotations.AISIllegalValueAnnotation;
import nl.esi.metis.aisparser.provenance.VDMMessageProvenance;
import cern.colt.bitvector.BitVector;

/* ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 * 
 */

/** This class implements AIS messages of type 24 Part B.
 * <pre>
 * Field Nr     Field Name                          NrOf Bits   (from, to )
 * ------------------------------------------------------------------------
 *  1           messageID                                  6    (   1,   6)
 *  2           repeatIndicator                            2    (   7,   8)
 *  3           userID                                    30    (   9,  38)
 *  4           partNumber                                 2    (  39,  40)
 *  5           typeOfShipAndCargoType                     8    (  41,  48)
 *  6           vendorID                                  42    (  49,  90)
 *  7           callSign                                  42    (  91, 132)
 *  8           dimensionOrMotherID                       30    ( 133, 162)
 *  9           spare                                      6    ( 163, 168)
 *                                                      ---- +
 *                           (maximum) number of bits    168
 * </pre>
 * @author Pierre van de Laar
 * @author Pierre America
 * @author Brian C. Lane
 */
class AISMessage24PartBImpl extends AISMessage24Impl implements AISMessage24PartB {

	/** The length of a valid message of type 24 Part B. */
	public static final int LENGTHPartB = 168;

	/** Checks whether the content is a valid message of type 24 Part B. (length & Part number)
	 * @param content possible message type 24 B
	 * @return true if this could be a valid message of type 24 Part B
	 */
	public static boolean valid(Sixbit content)
	{
		return ( (content.length() == LENGTHPartB) && (content.getIntFromTo(PARTNUMBER_FROM,PARTNUMBER_TO) == 1) );
	}

	/** The number of the first bit of the ship type field */ 
	private static final int TYPEOFSHIPANDCARGOTYPE_FROM = 41;

	/** The number of the last bit of the ship type field */ 
	private static final int TYPEOFSHIPANDCARGOTYPE_TO = 48;

	/** The ship and cargo type */
	private int typeOfShipAndCargoType;

	/** Returns the type of ship and cargo type.
	 * @return an integer value representing the type of ship and cargo type (8 bits)
	 */
	public int getTypeOfShipAndCargoType() { return typeOfShipAndCargoType; }

	/** The number of the first bit of the vendor ID field */ 
	private static final int VENDORID_FROM = 49;

	/** The number of the last bit of the vendor ID field */ 
	private static final int VENDORID_TO = 90;

	/** The vendor ID */
	private BitVector vendorID;

	/** Returns the vendor ID.
	 * @return a <code>BitVector</code> object representing the vendor ID (42 bits)
	 */
	public BitVector getVendorID() { return vendorID; }

	/** The number of the first bit of the call sign field */ 
	private static final int CALLSIGN_FROM = 91;

	/** The number of the last bit of the call sign field */ 
	private static final int CALLSIGN_TO = 132;

	/** The call sign */
	private String callSign;
	
	/** Returns the call sign.
	 * @return a string representing the call sign (42 bits)
	 */
	public String getCallSign() { return callSign; }

	/** The number of the first bit of the dimensions or mother ID field */ 
	private static final int DIMENSIONORMOTHERID_FROM = 133;

	/** The number of the last bit of the dimensions or mother ID field */ 
	private static final int DIMENSIONORMOTHERID_TO = 162;

	/** The dimension or mother ID */
	private BitVector dimensionOrMotherID;

	/** Returns the dimensions or mother ID.
	 * @return a <code>BitVector</code> object representing the dimensions or mother ID (30 bits)
	 */
	public BitVector getDimensionOrMotherID() { return dimensionOrMotherID; }

	/** The number of the first spare bit */ 
	private static final int SPARE_FROM = 163;

	/** The number of the last spare bit */ 
	private static final int SPARE_TO = 168;

	/** The value of the spare bits */
	private int spare;

	/** Returns the spare bits.
	 * @return the integer value of the spare bits.
	 */
	public int getSpare() { return spare; }
	
	/** Returns the part number.
	 * @return the part number. This is always 1 for a Part B message.
	 */
	@Override
	public int getPartNumber() {
		return 1;
	}

	/** Constructs an AIS message 24 Part B object.
	 * @param content the content of the AIS message
	 * @param prov the provenance of the message
	 * @precondition valid(content)
	 */
	public AISMessage24PartBImpl(Sixbit content, VDMMessageProvenance prov)
	{		
		super(content, prov);
		assert(valid(content));
		typeOfShipAndCargoType = content.getIntFromTo(TYPEOFSHIPANDCARGOTYPE_FROM,TYPEOFSHIPANDCARGOTYPE_TO);
		vendorID = content.getBitVectorFromTo(VENDORID_FROM,VENDORID_TO);
		callSign = UtilsString.stripAtSigns(content.getStringFromTo(CALLSIGN_FROM,CALLSIGN_TO));
		dimensionOrMotherID = content.getBitVectorFromTo(DIMENSIONORMOTHERID_FROM,DIMENSIONORMOTHERID_TO);
		spare = content.getIntFromTo(SPARE_FROM,SPARE_TO);
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare", spare, UtilsSpare.range));
	    }
	}
}