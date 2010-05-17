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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;




/**
 * A complement to <code>java.util.StringTokenizer</code> that provides
 * tokenizing by substrings instead of single characters as well as static
 * utility methods for standard tasks.
 * This class is semantically compatible with StringTokenizer.  There is
 * not a default delimiter as there is with
 * <code>java.util.StringTokenizer</code>. There is only a single delimiter 
 * whereas <code>java.util.StringTokenizer</code> allows for multiple 
 * characters. The delimiter may be optionally case sensitive.
 * 
 * @see "java.util.StringTokenizer"
 *
 * @author	Todd Fast, todd.fast@sun.com
 * @author	Mike Frisino, michael.frisino@sun.com
 */
public class StringTokenizer2 extends Object 
	implements Enumeration, Iterator
{
	/**
	 * Create tokenizer with property <code>returnTokens</code> set to 
	 * <code>false</code> and property <code>ignoreCase</code> set to 
	 * <code>false</code>. Blank delimiter results in the entire text 
	 * as a single token.
	 * 
	 * @param text string to be parsed (must not be null)
	 * @param delimiter to be be used to tokenize text (must not be null)
	 * 
	 */
	public StringTokenizer2(String text, String delimiter)
	{
		this(text,delimiter,false);
	}


	/**
	 * Create tokenizer with option for property <code>returnTokens</code> 
	 * and property <code>ignoreCase</code> set to <code>false</code>. Blank 
	 * delimiter results in the entire text as a single token.
	 * 
	 * @param text string to be parsed (must not be null)
	 * @param delimiter to be be used to tokenize text (must not be null)
	 * @param returnTokens mimics <code>java.util.StringTokenizer</code> in that when <code>true</code> delimiters are returned as tokens
	 */
	public StringTokenizer2(String text, String delimiter, boolean returnTokens)
	{
		this(text,delimiter,returnTokens,false);
	}


	/**
	 * Create tokenizer with options for properties 
	 * <code>returnTokens</code> and <code>ignoreCase</code>. Blank delimiter 
	 * results in the entire text as a single token.
	 * 
	 * @param text string to be parsed (must not be null)
	 * @param delimiter to be be used to tokenize text (must not be null)
	 * @param returnTokens mimics <code>java.util.StringTokenizer</code> in that when <code>true</code> delimiters are returned as tokens
	 * @param ignoreCase delimiters not case sensitive when <code>true</code>
	 */
	public StringTokenizer2(String text, String delimiter, boolean returnTokens, boolean ignoreCase)
	{
		super();
		this.text=text;
		this.delimiter=delimiter;
		this.returnDelimiterTokens=returnTokens;
		parse(ignoreCase);
	}


	/**
	 *
	 *
	 */
	private void parse(boolean ignoreCase)
	{
		String matchText=null;
		String matchDelim=null;

		if (ignoreCase)
		{
			matchText=text.toUpperCase();
			matchDelim=delimiter.toUpperCase();
		}
		else
		{
			matchText=text;
			matchDelim=delimiter;
		}

		int startIndex=0;
		int endIndex=matchText.indexOf(matchDelim,startIndex);
		while (endIndex!=-1)
		{
			String token=text.substring(startIndex,endIndex);
			parsedTokens.add(token);
			if (returnDelimiterTokens)
				parsedTokens.add(delimiter);
			startIndex=endIndex+delimiter.length();
			endIndex=matchText.indexOf(matchDelim,startIndex);
		}

		parsedTokens.add(text.substring(startIndex));
	}


	/**
	 *
	 * @see "java.util.StringTokenizer.hasNext()"
	 */
	public boolean hasNext()
	{
		return hasMoreTokens();
	}


	/**
	 *
	 * @see "java.util.StringTokenizer.next()"
	 */
	public Object next()
	{
		return nextToken();
	}


	/**
	 * Feature not supported
	 *
	 */
	public void remove()
	{
		throw new UnsupportedOperationException();
	}


	/**
	 * 
	 * @see "java.util.StringTokenizer.hasMoreTokens()"
	 */
	public boolean hasMoreTokens()
	{
		return tokenIndex<parsedTokens.size();
	}



	/**
	 *
	 * @see "java.util.StringTokenizer.hasMoreElements()"
	 */
	public boolean hasMoreElements()
	{
		return hasMoreTokens();
	}


	/**
	 *
	 * @see "java.util.StringTokenizer.countTokens()"
	 */
	public int countTokens()
	{
		return parsedTokens.size();
	}


	/**
	 *
	 * @see "java.util.StringTokenizer.nextToken()"
	 */
	public String nextToken()
	{
		return (String)parsedTokens.get(tokenIndex++);
	}


	/**
	 *
	 * @see "java.util.StringTokenizer.nextElement()"
	 */
	public Object nextElement()
	{
		return nextToken();
	}




	////////////////////////////////////////////////////////////////////////////////
	// Static utility methods
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * Performs a classic string find and replace of FIRST occurrence only
	 *
	 * @param str original string to be modified
	 * @param findValue text to be replaced throughout string (must not be null)
	 * @param replaceValue text to replace found tokens (must not be null)
	 * @return modified string
	 */
	public static String replaceFirst(String str, String findValue, String replaceValue)
	{
		StringTokenizer2 tok=new StringTokenizer2(str,findValue,false);
		String result=""; // NOI18N
		for (int i=0; i<tok.countTokens(); i++) {
			if(i == 0 )
				result+=tok.nextToken()+replaceValue;
			else
				result+=tok.nextToken();
		}
		return result;
	}	

	/**
	 * Performs a classic string find and replace
	 *
	 * @param str original string to be modified
	 * @param findValue text to be replaced throughout string (must not be null)
	 * @param replaceValue text to replace found tokens (must not be null)
	 * @return modified string
	 */
	public static String replace(String str, String findValue, String replaceValue)
	{
		StringTokenizer2 tok=new StringTokenizer2(str,findValue,false);
		
		String result=""; // NOI18N
		for (int i=0; i<tok.countTokens()-1; i++)
			result+=tok.nextToken()+replaceValue;
		result+=tok.nextToken();

		return result;
	}


	/**
	 * Performs a classic string find & replace, optionally ignoring the case
	 * of the string
	 *
	 * @param str original string to be modified
	 * @param findValue search text to be replaced throughout string (must not be null)
	 * @param replaceValue text to replace found tokens (must not be null)
	 * @param ignoreCase search text case insensitive when <code>true</code>
	 * @return modified string
	 */
	public static String replace(String str, String findValue, String replaceValue, 
		boolean ignoreCase)
	{
		StringTokenizer2 tok=new StringTokenizer2(str,findValue,false,ignoreCase);
		
		StringBuffer result=new StringBuffer();
		for (int i=0; i<tok.countTokens()-1; i++)
			result.append(tok.nextToken()).append(replaceValue);
		result.append(tok.nextToken());

		return result.toString();
	}


	/**
	 * Shortcut to {@link #tokenize(String,String,boolean,boolean) generalized 
	 * search method} with property <code>trim</code> set to <code>false
	 * </code> and property <code>ignoreCase</code> set to <code>false
	 * </code>
	 */
	public static String[] tokenize(String str, String findValue)
	{
		return tokenize(str,findValue,false);
	}


	/**
	 * Shortcut to {@link #tokenize(String,String,boolean,boolean) generalized 
	 * search method} with property <code>ignoreCase</code> set to <code>false
	 * </code>
	 *
	 */
	public static String[] tokenize(String str, String findValue, boolean trim)
	{
		return tokenize(str,findValue,trim,false);
	}


	/**
	 * Utility method to create array of string tokens with optional support for 
	 * trimming results and ignoring case when searching.
	 * 
	 * @param str text to be searched (must not be null)
	 * @param findValue search string (must not be null)
	 * @param trim flag indicating that resulting tokens should be trimmed
	 * @param ignoreCase flag indicating that search should be case insensitive
	 * @return array of string tokens resulting from search
	 *
	 */
	public static String[] tokenize(String str, String findValue, boolean trim, 
		boolean ignoreCase)
	{
		StringTokenizer2 tok=new StringTokenizer2(str,findValue,false,ignoreCase);
		
		List result=new LinkedList();
		for (int i=0; i<tok.countTokens(); i++)
		{
			if (trim)
				result.add(((String)tok.nextToken()).trim());
			else
				result.add(((String)tok.nextToken()));
		}

		return (String[])result.toArray(new String[result.size()]);
	}


	/**
	 * Utility method to breakup larger string into array of strings, 
	 * one string per line.
	 * 
	 */
	public static String[] tokenizeLines(String string)
	{
		StringTokenizer tok=new StringTokenizer(string,"\n\r",true); // NOI18N

		List result=new LinkedList();
		String previousToken=null;
		while (tok.hasMoreTokens())
		{
			String token=tok.nextToken();
			if (token.equals("\r")) // NOI18N
				; //Discard
			else
			if (token.equals("\n")) // NOI18N
			{
				if (previousToken!=null)
					result.add(previousToken);
				else
					result.add("");  // NOI18N // Add a blank line

				previousToken=null;
			}
			else
				previousToken=token;
		}

		// Make sure we get the last line, even if it didn't end
		// with a carriage return
		if (previousToken!=null)
			result.add(previousToken);

		return (String[])result.toArray(new String[result.size()]);
	}


	/**
	 * Converts an array of Objects into a delimited string of values
	 *
	 */
	public static String delimitedString(Object[] vals, String delimiter)
	{
		// Make sure we have a valid array
		if (vals == null)
		{
			return null;
		}

		// Get one less than the size, so we can add on the last seperately.
		int lastIndex = vals.length-1;
		if (lastIndex < 0)
		{
			// Handle empty array as special case
			return ""; // NOI18N
		}

		// Iterate over the elements
		StringBuffer buf = new StringBuffer();
		for (int count=0; count<lastIndex; count++)
		{
			// Add element + delimiter
			buf.append(vals[count]);
			buf.append(delimiter);
		}

		// Add on last element
		buf.append(vals[lastIndex]);
		return buf.toString();

	}


	/**
	 * Makes a String array from a delimited String of values
	 *
	 */
	public static String[] toArray(
		String delimitedStr, String delimiter)
	{
		if (delimitedStr == null || delimitedStr.length()==0)
			return new String[0];

		return StringTokenizer2.tokenize(delimitedStr,delimiter);
	}


	/**
	 *
	 *
	 */
	public static boolean delimitedStringContains(
		String delimitedStr, String delimiter, String findStr)
	{
		return Arrays.asList(toArray(delimitedStr,delimiter)).contains(findStr);
	}




	////////////////////////////////////////////////////////////////////////////	
	// Name utility methods
	////////////////////////////////////////////////////////////////////////////	
	
	/**
	 *
	 * @deprected	Use NameUtil instead
	 */
	public static String upcaseFirstLetter(String value)
	{
		return NameUtil.capitalize(value);
	}


	/**
	 *
	 * @deprected	Use NameUtil instead
	 */
	public static String lowcaseFirstLetter(String value)
	{
		return NameUtil.decapitalize(value);
	}


	/**
	 *
	 * @deprected	Use NameUtil instead
	 */
	public static String toDisplayName(String value)
	{
		return NameUtil.toDisplayName(value);
	}


	/**
	 *
	 * @deprected	Use NameUtil instead
	 */
	public static String toJavaConstant(String value)
	{
		return NameUtil.toJavaConstant(value);
	}




	////////////////////////////////////////////////////////////////////////////////
	// Instance variables
	////////////////////////////////////////////////////////////////////////////////

	private String text;
	private String delimiter;
	private boolean returnDelimiterTokens=false;
	private List parsedTokens=new ArrayList();
	private int tokenIndex=0;
}
