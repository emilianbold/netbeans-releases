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

package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;


public class GeneralOrdering extends NamedElement implements IGeneralOrdering
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering#getAfter()
     */
    public IEventOccurrence getAfter()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "after", IEventOccurrence.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering#setAfter(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setAfter(IEventOccurrence occ)
    {
        new ElementConnector<IGeneralOrdering>()
            .setSingleElementAndConnect( this, occ, "after",
                new IBackPointer<IEventOccurrence>()
                {
                    public void execute(IEventOccurrence eOcc)
                    {
                        eOcc.addAfterOrdering(GeneralOrdering.this);
                    }
                },
                new IBackPointer<IEventOccurrence>()
                {
                    public void execute(IEventOccurrence eOcc)
                    {
                        eOcc.removeAfterOrdering(GeneralOrdering.this);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering#getBefore()
     */
    public IEventOccurrence getBefore()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "before", IEventOccurrence.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering#setBefore(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setBefore(IEventOccurrence occ)
    {
        new ElementConnector<IGeneralOrdering>()
            .setSingleElementAndConnect( this, occ, "before",
                new IBackPointer<IEventOccurrence>()
                {
                    public void execute(IEventOccurrence eOcc)
                    {
                        eOcc.addBeforeOrdering(GeneralOrdering.this);
                    }
                },
                new IBackPointer<IEventOccurrence>()
                {
                    public void execute(IEventOccurrence eOcc)
                    {
                        eOcc.removeBeforeOrdering(GeneralOrdering.this);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:GeneralOrdering", doc, node);
    }
}