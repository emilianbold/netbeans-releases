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

package org.netbeans.modules.uml.core.metamodel.diagrams;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;


abstract public class UseCaseDiagram extends Diagram implements IUseCaseDiagram
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#buildNodePresence(java.lang.String, org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
         buildNodePresence("UML:UseCaseDiagram", doc, node);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.PresentationElement#establishNodeAttributes(org.dom4j.Element)
     */
    public void establishNodeAttributes(Element ele)
    {
        if(ele != null)
        {
            super.establishNodeAttributes(ele);
            XMLManip.setAttributeValue(ele,"IsDiagram","T");
        }
    }
}
