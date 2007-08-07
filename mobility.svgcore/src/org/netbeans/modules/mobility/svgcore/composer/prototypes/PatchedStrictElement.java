/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.StrictElement;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedStrictElement extends StrictElement implements PatchedElement {
    private String    m_idBackup = null;
    private SVGObject m_svgObject = null; 
    
    public PatchedStrictElement(final DocumentNode doc,
                         final String localName,
                         final String namespaceURI,
                         final String[] requiredTraits,
                         final String[][] requiredTraitsNS) {
        super(doc, localName, namespaceURI, requiredTraits, requiredTraitsNS);
    }
    
    public void attachSVGObject(SVGObject obj) {
        m_svgObject = obj;
    }

    public SVGObject getSVGObject() {
        return m_svgObject;
    }
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id       = null;
        } else {
            id = m_idBackup;
        }
    }
            
    public ElementNode newInstance(final DocumentNode doc) {
        return new PatchedStrictElement(doc, getLocalName(), 
                getNamespaceURI(), getRequiredTraits(), getRequiredTraitsNS());
    }    
}
