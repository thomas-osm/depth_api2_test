package nl.esi.metis.aisparser.impl;

import nl.esi.metis.aisparser.AISMessage16Unit;
import nl.esi.metis.aisparser.Sixbit;
/** ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 * 
 * @author Pierre van de Laar
 * @author Brian C. Lane
 */

/**
 * Field Nr     Field Name                          NrOf Bits   (from, to )
 * ------------------------------------------------------------------------
 *  1	destinationID                          	  30	(  0,  29)
 *  2	offset                                 	  12	(  30,  41)
 *  3	increment                              	  10	(  42,  51)
 *                                               ---- +
 *                     (maximum) number of bits    52
 */
class AISMessage16UnitImpl implements AISMessage16Unit {
	private static final int DESTINATIONID_FROM = 0;
	private static final int DESTINATIONID_TO = 29;

	private int destinationID;
	/** destinationID
	 * @return int value of destinationID (30 bits offset + [0,29])
	 */
	public int getDestinationID() { return destinationID; }


	private static final int OFFSET_FROM = 30;
	private static final int OFFSET_TO = 41;

	private int offset;
	/** offset
	 * @return int value of offset (12 bits offset+ [30,41])
	 */
	public int getOffset() { return offset; }


	private static final int INCREMENT_FROM = 42;
	private static final int INCREMENT_TO = 51;

	private int increment;
	/** increment
	 * @return int value of increment (10 bits offset+ [42,51])
	 */
	public int getIncrement() { return increment; }

	public AISMessage16UnitImpl (int offset, Sixbit content)
	{
		assert(content.length() >= offset + INCREMENT_TO);
		destinationID = content.getIntFromTo(offset+DESTINATIONID_FROM,offset+DESTINATIONID_TO);
		offset = content.getIntFromTo(offset+OFFSET_FROM,offset+OFFSET_TO);
		increment = content.getIntFromTo(offset+INCREMENT_FROM,offset+INCREMENT_TO);
	}
}