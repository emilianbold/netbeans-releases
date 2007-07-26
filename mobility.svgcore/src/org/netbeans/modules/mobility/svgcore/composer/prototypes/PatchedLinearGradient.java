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
import com.sun.perseus.model.LinearGradient;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedLinearGradient extends LinearGradient implements PatchedTransformableElement {
    private String m_idBackup = null;
    
    public PatchedLinearGradient(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }
    
    public void attachSVGObject(SVGObject obj) {
        
    }

    public SVGObject getSVGObject() {
        return null;
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
        return new PatchedLinearGradient(doc);
    }    
}
