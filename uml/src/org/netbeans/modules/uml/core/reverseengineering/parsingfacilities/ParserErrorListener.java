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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 */
public class ParserErrorListener implements IParserErrorListener
{
    /**
     * The OnError event is fired when the parser encounters an parse error in the
     * source file.  The error event will be forwarded to the UMLParser event
     * dispatcher.
     * 
     * @param error [in] The error that occured.
     */ 
    public void onError(IErrorEvent error)
    {
        if (error == null) return;
        
        IUMLParserEventDispatcher disp = getEventDispatcher();
        if (disp != null)
            disp.fireError("", error, null);
    }
    
    /**
     * Retrieves the UML Parser event dispatcher.  The UML Parser event dispatcher 
     * is used to send UML Events.
     * 
     * @param pVal [out] The dispatcher.
     */
    private IUMLParserEventDispatcher getEventDispatcher()
    {
        // In order to retrieve the correct dispatcher I must first retrieve the faciltiy manager
        // from the core product.  There will only be one facility factory on the core product.
        // Therefore, every one will be using the same dispatcher.
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if (prod != null)
        {
            IFacilityManager man = prod.getFacilityManager();
            if (man != null)
            {
                IFacility fac = man.retrieveFacility("Parsing.UMLParser");
                if (fac instanceof IUMLParser)
                    return ((IUMLParser) fac).getUMLParserDispatcher();
            }
        }
        return null;
    }
}
