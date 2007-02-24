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

package org.netbeans.modules.uml.core.reverseengineering.parsers.parsertestbed;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter;

/**
 */
public class Filter implements IStateFilter, ITokenFilter
{
    private List filteredStates = new ArrayList();
    private List filteredTokens = new ArrayList();

    public void addState(String state)
    {
        if (!filteredStates.contains(state))
            filteredStates.add(state);
    }
    
    public void addToken(String token)
    {
        if (!filteredTokens.contains(token))
            filteredTokens.add(token);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter#processState(java.lang.String, java.lang.String)
     */
    public boolean processState(String stateName, String language)
    {
        return !filteredStates.contains(stateName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter#isTokenValid(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isTokenValid(String tokenType, String stateName, String language)
    {
        return !filteredTokens.contains(tokenType);
    }
}