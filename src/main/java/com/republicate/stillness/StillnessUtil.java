package com.republicate.stillness;

import org.apache.velocity.app.VelocityEngine;

/**
 * Utility class for Stillness
 *
 * @author Claude Brisson
 */
public abstract class StillnessUtil {

	static final char sSpecialChars[]={ ' ','\t',0x0D,0x0A,0x09};

    /**
     * Normalize the text given in input : erase all useless characters (multiple space,
     * tabulation characters...)
     *
     * @param src Text to be normlized
     * @return normailized version of src
     */
	public static String normalize(String src) {
		String special=new String(sSpecialChars);
		char c;
		int n,l;
		boolean bTwice=false;

		l=src.length();
		StringBuffer buff=new StringBuffer(l);
		for (n=0;n<l;n++)
		{
			c=src.charAt(n);
			if (special.indexOf(c)==-1)
			{
				bTwice=false;
				buff.append(c);
			}
			else if (!bTwice)
			{
				buff.append(' ');
				bTwice=true;
			}
		}
		return buff.toString();
	}

}
