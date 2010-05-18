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
