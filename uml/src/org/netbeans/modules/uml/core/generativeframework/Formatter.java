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

/*
 * File       : Formatter.java
 * Created on : Oct 28, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import java.util.Iterator;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class Formatter
{
    /**
     * This is what a newline looks like. We're keeping this to the classic
     * "\n" - the native IO libraries will take care of converting that to the
     * native newline sequence when we write to a file.
     */
    public static final String NEWLINE = "\n";
    
    /**
	 * Makes sure that the formatting of the expanded text is put in the right
	 * place in terms of where the expansion variable indicator was in the
	 * stream. That is, if the expansion variable was 3 spaces in, the all text
	 * in the expansion variable should be expanded 3 spaces in.
	 * 
	 * @param prevText
	 *            The context of the expansion text. This is what determines
	 *            how the expansion variable result text is formatted.
	 * @param varRes
	 *            The actual expansion variable result text.
	 * 
	 * @return The reformatted text.
	 */
    public static String convertNewLines(String prevText, String varRes)
    {
        StringBuffer converted = new StringBuffer(varRes);
        
        if (prevText != null && prevText.length() > 0)
        {    
            int pos = prevText.lastIndexOf(NEWLINE);
            if (pos != -1)
            {
                int newlinePos = converted.indexOf(NEWLINE);
                if (newlinePos != -1)
                {
                    String replaceStr = 
                        validateReplace( prevText.substring(pos + 1) );
                    if (replaceStr != null && replaceStr.length() > 0)
                    {
                        ETList<String> tokens = 
                            StringUtilities.splitOnDelimiter(
                                converted.toString(), NEWLINE);
                        
                        Iterator<String> iter = tokens.iterator();
                        boolean isFirst = true;
                        while (iter.hasNext())
                        {    
                            String cur = iter.next();
                            if (isFirst)
                            {    
                                isFirst = false;
                                converted = new StringBuffer(cur);
                                converted.append(NEWLINE);
                            }
                            else
                            {    
                                if (cur.length() > replaceStr.length())
                                {
                                    String beginning = 
                                        cur.substring(0, replaceStr.length());
                                    if (beginning.equals(replaceStr))
                                        converted.append(replaceStr)
                                            .append(
                                                cur.substring(
                                                        replaceStr.length()));
                                    else
                                        converted.append(replaceStr)
                                            .append(cur);
                                }
                                else
                                {
                                    if (replaceStr.startsWith(cur))
                                        converted.append(replaceStr);
                                    else
                                        converted.append(replaceStr)
                                            .append(cur);
                                }
                            }
                            
                            if (iter.hasNext())
                                converted.append(NEWLINE);
                        }
                    }
                }
            }
        }
        return converted.toString();
    }

    /**
     * @param string
     * @return
     */
    private static String validateReplace(String replace)
    {
        int numAlphas = 0, firstAlpha = 0;
        boolean clean = false;
        
        for (int i = 0; i < replace.length(); ++i)
        {
            if (Character.isLetter(replace.charAt(i)))
            {
                if (++numAlphas > 1)
                {
                    clean = true;
                    break;
                }
                else
                {
                    firstAlpha = i;
                }
            }
        }
        
        if (clean)
            replace = replace.substring(0, firstAlpha);

        return replace;
    }
}