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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * This class contains the model element that was notified.  The Drawing Area creates
 * this object, puts the ChangedModelElement.  Listeners should interpret the changed
 * model element and use AddNotifiedElement to tell the drawing area to notify all presentation
 * elements of this model element of relevant changes.
 *
 * @author sumitabhk
 */
public class NotificationTargets implements INotificationTargets
{
    private int      m_Kind                  = ModelElementChangedKind.MECK_UNKNOWN;
    private IElement m_ChangedModelElement   = null;
    private IElement m_SecondaryModelElement = null;
    private ETList < IPresentationElement > m_PresentationElements = new ETArrayList< IPresentationElement >();
    
    /**
     *
     */
    public NotificationTargets()
    {
    }
    
    /**
     * Retrieves the kind of event that trigger this change.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#getKind()
     */
    public int getKind()
    {
        return m_Kind;
    }
    
    /**
     * Sets the kind of event that trigger this change.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#setKind(int)
     */
    public void setKind(int value)
    {
        m_Kind = value;
    }
    
    /**
     * Retrieves the model element that has changed.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#getChangedModelElement()
     */
    public IElement getChangedModelElement()
    {
        return m_ChangedModelElement;
    }
    
    /**
     * Sets the model element that has changed.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#setChangedModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setChangedModelElement(IElement value)
    {
        m_ChangedModelElement = value;
    }
    
    /**
     * Retrieves the secondary model element that has changed.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#getSecondaryChangedModelElement()
     */
    public IElement getSecondaryChangedModelElement()
    {
        return m_SecondaryModelElement;
    }
    
    /**
     * Sets the secondary model element that has changed.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#setSecondaryChangedModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setSecondaryChangedModelElement(IElement value)
    {
        m_SecondaryModelElement = value;
    }
    
    /**
     * Retrieves a list of presentation elements to notify.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#getPresentationElementsToNotify()
     */
    public ETList < IPresentationElement > getPresentationElementsToNotify()
    {
        return m_PresentationElements;
    }
    
    /**
     * Sets a list of presentation elements to notify.
     *
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#setPresentationElementsToNotify(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[])
     */
    public void setPresentationElementsToNotify(ETList < IPresentationElement > value)
    {
        m_PresentationElements = value;
    }
    
    /**
     * Adds an IPresentationElement to our list of elements to notify (ChangedModelElement).
     *
     * @param pElementToNotify The presentation to notify.
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#addNotifiedElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
     */
    public void addNotifiedElement(IPresentationElement pElementToNotify)
    {
        if(pElementToNotify != null)
        {
            ETList < IPresentationElement > elements = getPresentationElementsToNotify();
            if(elements != null)
            {
                if(elements.contains(pElementToNotify) == false)
                {
                    elements.add(pElementToNotify);
                }
            }
        }
    }
    
    /**
     * Adds these IPresentationElements to our list of elements to notify
     * (ChangedModelElement).
     *
     * @param pElementsToNotify The presentation elements to notify.
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#addNotifiedElements(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[])
     */
    public void addNotifiedElements(ETList < IPresentationElement > pElementsToNotify)
    {
        if(pElementsToNotify != null)
        {
            for (Iterator < IPresentationElement > iter = pElementsToNotify.iterator(); iter.hasNext();)
            {
                IPresentationElement curElement = iter.next();
                if(curElement != null)
                {
                    addNotifiedElement(curElement);
                }
            }
        }
    }
    
    /**
     * Adds the IPresentationElements of this IElement to our list of elements
     * to notify (ChangedModelElement)
     *
     * @param pDiagram The diagram where the presentation elements should live.
     * @param pElementToGetPEsFrom The IElement for which we grab the
     *                             presentation elements from and add to our
     *                             list.
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets#addElementsPresentationElements(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void addElementsPresentationElements(IDiagram pDiagram,
            IElement pElementToGetPEsFrom)
    {
        if((pDiagram != null) && (pElementToGetPEsFrom != null))
        {
            ETList < IPresentationElement > presElements = pDiagram.getAllItems2(pElementToGetPEsFrom);
            if(presElements != null)
            {             
                addNotifiedElements(presElements);
            }
        }
    }
    
}
