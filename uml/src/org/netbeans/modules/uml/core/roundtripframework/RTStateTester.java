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
 * File       : RTStateTester.java
 * Created on : Nov 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * @author Aztec
 */
public class RTStateTester implements IRTStateTester
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#getProject(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public IProject getProject(IElement pElement)
    {
        if(pElement == null) return null;
        
        IElement pDisp = pElement.getProject();

        if (pDisp == null)
        {
            // Let's check to see if we have a transition element
            ITransitionElement transElement = null;
            
            if( pElement instanceof ITransitionElement)
                transElement = (ITransitionElement)pElement;

            if(transElement != null)
            {
                IElement futureOwner = transElement.getFutureOwner();

                if(futureOwner != null)
                {
                    pDisp = futureOwner.getProject();
                }
            }
        }

        try
        {
            return (IProject)pDisp;
        }
        catch (ClassCastException e){}

        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#isAppInRoundTripState(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isAppInRoundTripState(IElement pElement)
    {
        boolean retval = false;

        if (pElement != null)
        {
            IProject pProject = getProject(pElement);

            if (pProject != null)
            {
                retval = isProjectInRoundTripState(pProject);
            }
        }
        return retval;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#isElementRoundTripable(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isElementRoundTripable(IElement pElement)
    {
        if(pElement != null)
        {
            return isElementRoundTripable (pElement.getElementType());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#isElementRoundTripable(java.lang.String)
     */
    public boolean isElementRoundTripable(String elementType)
    {
        boolean retval = false;

        if (elementType != null)
        {
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();

            if (pProduct != null)
            {
                IPreferenceManager2 pManager = pProduct.getPreferenceManager ();

                if (pManager != null)
                {
                    String key = "Default";
                    String path = "RoundTrip|elements";
                    
                    String prefValue = pManager.getPreferenceValue(key, path, elementType);

                    retval = "PSK_YES".equalsIgnoreCase(prefValue);
                }
            }
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#isProjectInRoundTripState(org.netbeans.modules.uml.core.metamodel.structure.IProject)
     */
    public boolean isProjectInRoundTripState(IProject pProject)
    {
        boolean retval = false;

        if (pProject != null)
        {
            String mode = pProject.getMode();

            retval = true;
            if ("Analysis".equals(mode) || "0".equals(mode) || "PSK_ANALYSIS".equals(mode))
            {
//                retval = false;
				return false;
            }

            retval = !pProject.getLibraryState();
        }
        return retval;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTStateTester#isRelationInRoundTripState(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy)
     */
    public boolean isRelationInRoundTripState(IRelationProxy pProxy)
    {
        // If any part of the relation is roundtripable, the relation is roundtripable.
        boolean retval = false;

        if (pProxy != null)
        {
            // Get the relation type string from the proxy. Either
            // the proxy has an element fulfilling the role of the link, 
            // or it just know what TYPE of element will fill this role.

            IElement pLink = pProxy.getConnection();

            String relationType = null;
            if (pLink != null)
            {
                relationType = pLink.getElementType();
            }
            else
            {
                relationType = pProxy.getConnectionElementType();
            }

            if(isElementRoundTripable(relationType))
            {
                IElement pConnection = null;
                IElement pFrom = null;
                IElement pTo = null;

                pConnection = pProxy.getConnection();
                pFrom = pProxy.getFrom();
                pTo = pProxy.getTo();

                retval = (isAppInRoundTripState(pConnection) ||
                    isAppInRoundTripState(pFrom) ||
                    isAppInRoundTripState(pTo));
            }
        }
        return retval;
    }

}
