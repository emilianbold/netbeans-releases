/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
