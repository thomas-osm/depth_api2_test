package nl.esi.metis.aisparser.impl;

import nl.esi.metis.aisparser.AISMessage06;
import nl.esi.metis.aisparser.Sixbit;
import nl.esi.metis.aisparser.UtilsSpare;
import nl.esi.metis.aisparser.annotations.AISIllegalValueAnnotation;
import nl.esi.metis.aisparser.provenance.VDMMessageProvenance;
import cern.colt.bitvector.BitVector;
/* ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 * 
 * @author Pierre van de Laar
 * @author Brian C. Lane
 */


/** AIS Message 6
 * Addressed Binary Message
 * 
 * Field Nr     Field Name                          NrOf Bits   (from, to )
 * ------------------------------------------------------------------------
 *  1	messageID                               	   6	(   1,   6)
 *  2	repeatIndicator                         	   2	(   7,   8)
 *  3	userID                                  	  30	(   9,  38)
 *  4	sequenceNumber                          	   2	(  39,  40)
 *  5	destinationID                           	  30	(  41,  70)
 *  6	retransmitFlag                          	   1	(  71,  71)
 *  7	spare                                   	   1	(  72,  72)
 *  8	applicationID                           	  16	(  73,  88)
 *  9	applicationBinaryData                   	 920	(  89,1008)
 *                                                  ---- +
 *                       (maximum) number of bits   1008
 */
class AISMessage06Impl extends AISMessageImpl implements AISMessage06 {

	public static final int MINLENGTH = 88;
	public static final int MAXLENGTH = 1008;
	
	public static boolean validLength(int length)
	{
		return (MINLENGTH <= length && length <= MAXLENGTH);
	}

	private static final int SEQUENCENUMBER_FROM = 39;
	private static final int SEQUENCENUMBER_TO = 40;

	private int sequenceNumber;
	/** sequenceNumber
	 * @return int value of sequenceNumber (2 bits [39,40])
	 */
	public int getSequenceNumber() { return sequenceNumber; }


	private static final int DESTINATIONID_FROM = 41;
	private static final int DESTINATIONID_TO = 70;

	private int destinationID;
	/** destinationID
	 * @return int value of destinationID (30 bits [41,70])
	 */
	public int getDestinationID() { return destinationID; }


	private static final int RETRANSMITFLAG_BITINDEX = 71;

	private boolean retransmitFlag;
	/** retransmitFlag
	 * @return boolean value of retransmitFlag (bit 71)
	 */
	public boolean getRetransmitFlag() { return retransmitFlag; }


	private static final int SPARE_FROM = 72;
	private static final int SPARE_TO = 72;

	private int spare;
	/** spare
	 * @return int value of spare (1 bits [72,72])
	 */
	public int getSpare() { return spare; }


	private static final int APPLICATIONID_FROM = 73;
	private static final int APPLICATIONID_TO = 88;

	private int applicationID;
	/** applicationID
	 * @return int value of applicationID (16 bits [73,88])
	 */
	public int getApplicationID() { return applicationID; }


	private static final int APPLICATIONBINARYDATA_FROM = 89;

	private BitVector applicationBinaryData;
	/** applicationBinaryData
	 * @return BitVector value of applicationBinaryData (maximally 920 bits [89,1008])
	 */
	public BitVector getApplicationBinaryData() { return applicationBinaryData; }

	/** AISMessage 6 constructor
	 * @param content AIS content
	 * @param prov the provenance of the message
	 * @precondition validLength(content.length()) && AISMessageBase.getMessageId(content)== 6
	 */
	public AISMessage06Impl(Sixbit content, VDMMessageProvenance prov)
	{
		super(content, prov);
		assert(validLength(content.length()));
		assert(getMessageID() == 6);

		sequenceNumber = content.getIntFromTo(SEQUENCENUMBER_FROM,SEQUENCENUMBER_TO);
		destinationID = content.getIntFromTo(DESTINATIONID_FROM,DESTINATIONID_TO);
		retransmitFlag = content.getBoolean(RETRANSMITFLAG_BITINDEX);
		spare = content.getIntFromTo(SPARE_FROM,SPARE_TO);
		if (!UtilsSpare.isSpareSemanticallyCorrect(spare))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare", spare, UtilsSpare.range));
	    }
		applicationID = content.getIntFromTo(APPLICATIONID_FROM,APPLICATIONID_TO);
		applicationBinaryData = content.getBitVectorFromTo(APPLICATIONBINARYDATA_FROM,content.length());
	}
}