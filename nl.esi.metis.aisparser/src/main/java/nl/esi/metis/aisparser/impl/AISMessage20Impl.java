package nl.esi.metis.aisparser.impl;

import nl.esi.metis.aisparser.AISMessage20;
import nl.esi.metis.aisparser.AISMessage20Unit;
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


/** AIS Message 20
 * Data Link Management Message
 * 
 * Field Nr     Field Name                          NrOf Bits   (from, to )
 * ------------------------------------------------------------------------
 *  1	messageID                               	   6	(   1,   6)
 *  2	repeatIndicator                         	   2	(   7,   8)
 *  3	userID                                  	  30	(   9,  38)
 *  4	spare1                                  	   2	(  39,  40)
 *  5	offsetNumber1                           	  12	(  41,  52)
 *  6	numberOfSlots1                          	   4	(  53,  56)
 *  7	timeOut1                                	   3	(  57,  59)
 *  8	increment1                              	  11	(  60,  70)
 *  9	offsetNumber2                           	  12	(  71,  82)
 * 10	numberOfSlots2                          	   4	(  83,  86)
 * 11	timeOut2                                	   3	(  87,  89)
 * 12	increment2                              	  11	(  90, 100)
 * 13	offsetNumber3                           	  12	( 101, 112)
 * 14	numberOfSlots3                          	   4	( 113, 116)
 * 15	timeOut3                                	   3	( 117, 119)
 * 16	increment3                              	  11	( 120, 130)
 * 17	offsetNumber4                           	  12	( 131, 142)
 * 18	numberOfSlots4                          	   4	( 143, 146)
 * 19	timeOut4                                	   3	( 147, 149)
 * 20	increment4                              	  11	( 150, 160)
 * 21	spare2                                  	   0	
 *                                                      ---- +
 *                           (maximum) number of bits    160
 */
class AISMessage20Impl extends AISMessageImpl implements AISMessage20 {
	private static final int LENGTHWITH1SLOT  =  72;
    private static final int LENGTHWITH2SLOTS = 104;
    private static final int LENGTHWITH3SLOTS = 136;
	private static final int LENGTHWITH4SLOTS = 160;
	
	public static boolean validLength(int length)
	{
		return ( (length == LENGTHWITH1SLOT ) ||
				 (length == LENGTHWITH2SLOTS) ||
				 (length == LENGTHWITH3SLOTS) ||
				 (length == LENGTHWITH4SLOTS) );
	}

	private static final int SPARE1_FROM = 39;
	private static final int SPARE1_TO = 40;

	private int spare1;
	/** spare1
	 * @return int value of spare1 (2 bits [39,40])
	 */
	public int getSpare1() { return spare1; }


	private AISMessage20Unit[] units;
	/** units
	 * @return Array of AISMessage20Unit containing all units in the AIS Message
	 */
	public AISMessage20Unit[] getUnits() { return units; }


	private int spare2;
	/** spare2
	 * @return int value of spare2 (0-6 bits)
	 */
	public int getSpare2() { return spare2; }

    
	/** AISMessage 20 constructor
	 * @param content AIS content
	 * @param prov the provenance of the message
	 * @precondition validLength(content.length()) && AISMessageBase.getMessageId(content)==20
	 */
	public AISMessage20Impl(Sixbit content, VDMMessageProvenance prov)
	{
		super(content, prov);
		assert(validLength(content.length()));
		assert(getMessageID() == 20);

		spare1 = content.getIntFromTo(SPARE1_FROM,SPARE1_TO);
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare1))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare1", spare1, UtilsSpare.range));
	    }
		switch (content.length())
		{
			case LENGTHWITH1SLOT:
				units = new AISMessage20Unit[1];
				break;
			case LENGTHWITH2SLOTS:
				units = new AISMessage20Unit[2];
				break;
			case LENGTHWITH3SLOTS:
				units = new AISMessage20Unit[3];
				break;
			case LENGTHWITH4SLOTS:
				units = new AISMessage20Unit[4];
				break;
		}
		int offset = 41;
		for(int i=0; i < units.length; i++)
		{
			units[i] = new AISMessage20UnitImpl(offset, content);
			offset+=30;
		}
		spare2 = content.getIntFromTo(offset, content.length());
	    if (!UtilsSpare.isSpareSemanticallyCorrect(spare2))
	    {
	    	annotations.add(new AISIllegalValueAnnotation("getSpare2", spare2, UtilsSpare.range));
	    }	}
}