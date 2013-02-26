/** ESI AIS Parser
 * 
 * Copyright 2011/2012 by Pierre van de Laar (Embedded Systems Institute)
 * Copyright 2008 by Brian C. Lane <bcl@brianlane.com>
 * All Rights Reserved
 * 
 * @author Pierre van de Laar
 * @author Brian C. Lane
 */
package nl.esi.metis.aisparser.impl;

import java.util.ArrayList;
import java.util.List;

import nl.esi.metis.aisparser.AISMessageIllegal;
import nl.esi.metis.aisparser.Sixbit;
import nl.esi.metis.aisparser.annotations.AISNoCompleteMessageIdAnnotation;
import nl.esi.metis.aisparser.annotations.AISNoCompleteRepeatIndicatorAnnotation;
import nl.esi.metis.aisparser.annotations.AISNoCompleteUserIdAnnotation;
import nl.esi.metis.aisparser.annotations.Annotation;
import nl.esi.metis.aisparser.provenance.AISMessageProvenance;
import nl.esi.metis.aisparser.provenance.VDMMessageProvenance;

//TODO also store content of inconsistent message?

/** AIS Message Inconsistent
 * Class to store AIS Messages that don't adhere to the standard
 */
class AISMessageIllegalImpl implements AISMessageIllegal {
	private static final int MESSAGEID_FROM = 1;
	private static final int MESSAGEID_TO = 6;

	private int messageID;
	/** messageID
	 * @return int value of messageID (6 bits [1,6])
	 */
	public int getMessageID() { return messageID; }


	private static final int REPEATINDICATOR_FROM = 7;
	private static final int REPEATINDICATOR_TO = 8;

	private int repeatIndicator;
	/** repeatIndicator
	 * @return int value of repeatIndicator (2 bits [7,8])
	 */
	public int getRepeatIndicator() { return repeatIndicator; }


	private static final int USERID_FROM = 9;
	private static final int USERID_TO = 38;

	private int userID;
	/** userID
	 * @return int value of userID (30 bits [9,38])
	 */
	public int getUserID() { return userID; }
	

	/** The provenance */
	private VDMMessageProvenance provenance;

	/** The annotations */
	protected List<Annotation> annotations = new ArrayList<Annotation>();
	
	/** Returns the provenance, i.e., a description of where the AIS message came from, including a time stamp.
	 * @return the provenance
	 */
	public AISMessageProvenance getProvenance() {
		return new AISMessageProvenance(provenance, annotations);
	}
	

	/** Constructs an object representing an illegal message.
     * @param content the content of the message
     * @param prov the provenance of the message
     */
	public AISMessageIllegalImpl(Sixbit content, VDMMessageProvenance prov)
	{
    	this.provenance = prov;

    	if (content.length() < MESSAGEID_TO){
    		messageID = content.getIntFromTo(MESSAGEID_FROM,content.length());
    		annotations.add(new AISNoCompleteMessageIdAnnotation(content.length()));
    		annotations.add(new AISNoCompleteRepeatIndicatorAnnotation(0));
    		annotations.add(new AISNoCompleteUserIdAnnotation(0));
    	}
    	else {
    		messageID = content.getIntFromTo(MESSAGEID_FROM,MESSAGEID_TO);
    		
	    	if (content.length() < REPEATINDICATOR_TO)
	    	{
	    		repeatIndicator = content.getIntFromTo(REPEATINDICATOR_FROM,content.length());
	    		annotations.add(new AISNoCompleteRepeatIndicatorAnnotation(content.length()+1-REPEATINDICATOR_FROM));
	    		annotations.add(new AISNoCompleteUserIdAnnotation(0));
	    	}
	    	else {
	    		repeatIndicator = content.getIntFromTo(REPEATINDICATOR_FROM,REPEATINDICATOR_TO);

	    		assert (content.length() < USERID_TO); 
	    		userID = content.getIntFromTo(USERID_FROM,content.length());
	    		annotations.add(new AISNoCompleteUserIdAnnotation(content.length()+1-USERID_FROM));
	    	}
    	}
	}

    /**
     * Generates a String representing the parsed AISMessage 
     * Format:
     * all fields are shown in the order and units as specified by the standard separated by SEPARATORs
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
    	return  Integer.toString(messageID) + AISMessageImpl.SEPARATOR +
    			Integer.toString(repeatIndicator) + AISMessageImpl.SEPARATOR +
    			Integer.toString(userID) + AISMessageImpl.SEPARATOR ;
    }
}