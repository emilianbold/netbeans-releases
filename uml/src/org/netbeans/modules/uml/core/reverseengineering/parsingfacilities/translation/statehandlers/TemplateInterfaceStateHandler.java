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

package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

/**
 *
 * @author Trey Spiva
 */
public class TemplateInterfaceStateHandler extends InterfaceStateHandler
{
    
    /** Creates a new instance of TemplateInterfaceStateHandler */
    public TemplateInterfaceStateHandler(String language, String packageName)
    {
       this(language, packageName, false);
    }

    public TemplateInterfaceStateHandler(String language, String packageName, boolean isInner)
    {
        super(language, packageName, isInner);
    }


    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if("Template Parameter".equals(stateName))
        {
            retVal = new TemplateParameterStateHandler();
        }
        else
        {
            retVal = super.createSubStateHandler(stateName, language);
        }

        if((retVal != null) && (retVal != this))
        {   
            Node pClassNode = getDOMNode();

            if(pClassNode != null)
            {
                retVal.setDOMNode(pClassNode);
            }
        }
        return retVal;
    }
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String stateName)
    {
        super.stateComplete(stateName);
    }
    
}
