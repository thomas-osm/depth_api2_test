package nl.esi.metis.aisparser.impl;

import nl.esi.metis.aisparser.AISMessage12;
import nl.esi.metis.aisparser.Sixbit;
import nl.esi.metis.aisparser.UtilsSpare;
/** ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 * 
 * @author Pierre van de Laar
 * @author Brian C. Lane
 */
import nl.esi.metis.aisparser.annotations.AISIllegalValueAnnotation;
import nl.esi.metis.aisparser.provenance.VDMMessageProvenance;


/** AIS Message 12
 * Addressed Safety Related Message
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
 *  8	safetyRelatedText                       	 936	(  73,1008)
 *                                                      ---- +
 *                           (maximum) number of bits   1008
 */
class AISMessage12Impl extends AISMessageImpl implements AISMessage12 {
	public static final int MINLENGTH = 72;
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


	private static final int SPARE1_FROM = 72;
	private static final int SPARE1_TO = 72;

	private int spare1;
	/** spare1
	 * @return int value of spare1 (1 bits [72,72])
	 */
	public int getSpare1() { return spare1; }

	private int spare2;
	/** spare2
	 * @return int value of spare2 (at end to adhere to byte boundaries)
	 */
	public int getSpare2() { return spare2; }

	
	private static final int SAFETYRELATEDTEXT_FROM = 73;
	
	private String safetyRelatedText;
	/** safetyRelatedText
	 * @return String value of safetyRelatedText (936 bits [73,1008])
	 */
	public String getSafetyRelatedText() { return safetyRelatedText; }

	/** AISMessage 12 constructor
	 * @param content AIS content
	 * @param prov the provenance of the message
	 * @precondition validLength(content.length()) && AISMessageBase.getMessageId(content)==12
	 */
	public AISMessage12Impl(Sixbit content, VDMMessageProvenance prov)
	{
		super(content, prov);
		assert(validLength(content.length()));
		assert(getMessageID() == 12);
	       
	    sequenceNumber = content.getIntFromTo(SEQUENCENUMBER_FROM,SEQUENCENUMBER_TO);
	    destinationID = content.getIntFromTo(DESTINATIONID_FROM,DESTINATIONID_TO);
	    retransmitFlag = content.getBoolean(RETRANSMITFLAG_BITINDEX);
	    spare1 = content.getIntFromTo(SPARE1_FROM,SPARE1_TO);
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare1))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare1", spare1, UtilsSpare.range));
	    }
	    int nrofSpare = (content.length()-SAFETYRELATEDTEXT_FROM+1)%Sixbit.BITSNEEDEDFORCHAR;
	    safetyRelatedText = content.getStringFromTo(SAFETYRELATEDTEXT_FROM,content.length()-nrofSpare);
	    spare2 = content.getIntFromTo(content.length()-nrofSpare,content.length());
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare2))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare2", spare2, UtilsSpare.range));
	    }
	}
}