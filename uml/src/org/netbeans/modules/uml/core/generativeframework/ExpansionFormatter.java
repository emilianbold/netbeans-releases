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
 * File       : ExpansionFormatter.java
 * Created on : Oct 28, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class ExpansionFormatter
{
    public static String format(IVariableExpander expander, String initialText)
    {
        StringBuffer finalResults = new StringBuffer();

        ETList<IExpansionResult> expandResults = expander.getExpansionResults();
        if (expandResults != null && expandResults.size() > 0)
        {
            int resultCount = expandResults.size();
            Formatter formatter = new Formatter();
            
            StringBuffer initText = new StringBuffer(initialText);
            
            for (int i = 0; i < resultCount; ++i)
            {
                IExpansionResult result = expandResults.get(i);
                if (result != null)
                {
                    String preText = result.getPreText();
                    if (preText != null && preText.length() > 0)
                    {
                        String converted = 
                            formatter.convertNewLines(initText.toString(), 
                                    preText);
                        finalResults.append(converted);
                        initText.append(converted);
                    }
                    
                    IExpansionVariable var = result.getVariable();
                    if (var != null)
                    {
                        if (var instanceof ICompoundVariable)
                        {
                            ICompoundVariable compound = 
                                (ICompoundVariable) var;
                            IVariableExpander cExp = compound.getExecutionContext();
                            if (cExp != null)
                            {
                                String varResults = format(cExp, initText.toString());
                                initText.append(varResults);
                                finalResults.append(varResults);
                            }
                        }
                        else
                        {
                            String varResults = var.getResults();
                            String converted = 
                                formatter.convertNewLines( 
                                    initText.toString(), varResults );
                            initText.append(converted);
                            finalResults.append(converted);
                        }
                    }
                }
            }
        }
        
        return finalResults.toString();
    }
}