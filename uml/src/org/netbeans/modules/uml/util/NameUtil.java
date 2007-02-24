/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.util;

/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NameUtil
{
	/**
	 *
	 *
	 */
	private NameUtil()
	{
		super();
	}


	/**
	 *
	 *
	 */
	public static String capitalize(String value)
	{
		if (value==null || value.length()==0)
			return value;

		char nameChars[] = value.toCharArray();
		nameChars[0] = Character.toUpperCase(nameChars[0]);
		return new String(nameChars);
	}


	/**
	 *
	 *
	 */
	public static String decapitalize(String value)
	{
		if (value==null || value.length()==0)
			return value;

		char nameChars[] = value.toCharArray();
		nameChars[0] = Character.toLowerCase(nameChars[0]);
		return new String(nameChars);
	}


	/**
	 *
	 *
	 */
	public static boolean isAllUpperCase(String value)
	{
		if (value==null || value.length()==0)
			return false;

		for (int i=0; i<value.length(); i++)
		{
			char currentChar=value.charAt(i);

			if (Character.isWhitespace(currentChar) || 
				Character.isDigit(currentChar))
			{
				continue;
			}

			if (!Character.isUpperCase(currentChar))
				return false;
		}

		return true;
	}


	/**
	 *
	 *
	 */
	public static String toJavaIdentifier(String value)
	{
		if (value==null || value.length()==0)
			return value;

		String result=java.beans.Introspector.decapitalize(value);
		if (isAllUpperCase(result))
			result=value.toLowerCase();

		return result;
	}


	/**
	 *
	 *
	 */
	/**
	 *
	 *
	 */
	public static String toDisplayName(String value)
	{
	    if (value==null || value.length()==0)
		    return value;

	    value = capitalize(value);
	    StringBuffer javaConstant = new StringBuffer(value.length());
	    
	    char curChar;
	    boolean upperFound = false;
	    boolean digitFound = false;
	    boolean specialFound = false;
	    boolean nonUpperFound = false;

	    for (int i=0; i<value.length(); i++)
	    {
		curChar = value.charAt(i);
		
		if (Character.isUpperCase(curChar))
		{
		    if ((nonUpperFound || digitFound) && i>0)
		    {
			javaConstant.append(" "); // NOI18N
			javaConstant.append(curChar);
		    }

		    else
			javaConstant.append(curChar);

		    upperFound = true;
		    digitFound = false;
		    specialFound = false;
		    nonUpperFound = false;
		}

		else if (Character.isDigit(curChar))
		{
		    if (nonUpperFound && i>0)
		    {
			javaConstant.append(" "); // NOI18N
		    }

		    javaConstant.append(curChar);
		    
		    upperFound = false;
		    digitFound = true;
		    specialFound = false;
		    nonUpperFound = false;
		}
		
		else if (curChar == CHAR_UNDERSCORE || curChar == CHAR_DASH)
		{
		    if ((upperFound || digitFound) && i>0)
		    {
			javaConstant.append(" "); // NOI18N
		    }
		    
		    else
			javaConstant.append(curChar); // NOI18N			

		    upperFound = false;
		    digitFound = false;
		    specialFound = true;
		    nonUpperFound = false;
		}

		else
		{
		    upperFound = false;
		    digitFound = false;
		    specialFound = false;
		    nonUpperFound = true;
		    
		    javaConstant.append(curChar);
		}
		
	    }

	    return javaConstant.toString();
	}


	/**
	 *
	 *
	 */
	public static String toJavaConstant(String value)
	{
		if (value==null || value.length()==0)
			return value;

		StringBuffer javaConstant = new StringBuffer(value.length());
		char curChar;
		boolean nonUpperFound = false;

		for (int i=0; i<value.length(); i++)
		{
			curChar = value.charAt(i);
			if (curChar==' ' || curChar=='_')
			{
				curChar='_';
			}
			else
			if (Character.isUpperCase(curChar))
			{
				if (nonUpperFound && i>0)
					javaConstant.append("_"); // NOI18N

				nonUpperFound = false;
				curChar=Character.toUpperCase(curChar);
			}
			else
			{
				nonUpperFound = true;
				curChar=Character.toUpperCase(curChar);
			}

			javaConstant.append(curChar);
		}

		return javaConstant.toString();
	}
	
    /** Test whether a given string is comprised of only ascii chars.
    * @param id string which should be checked
    * @return <code>true</code> if all chars are ascii
     * Also returns true if String is zero length
    */
    public static boolean isAscii(String value) {
        if (value == null) 
            throw new IllegalArgumentException("Null passed into isAscii"); // NOI18N
        
        if (value.length()==0) return true; // Arbitrary semantic decision.
        
        for (int i = 0; i < value.length(); i++) {
            char nextChar = value.charAt(i);
            if(! ((nextChar & 0xff80) == 0))
                return false;
        }
        return true;
    }

    
	public final static char CHAR_UNDERSCORE = '_';
	public final static char CHAR_DASH = '-';
}
