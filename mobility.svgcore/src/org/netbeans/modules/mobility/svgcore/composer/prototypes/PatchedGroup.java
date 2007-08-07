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

import com.sun.perseus.model.*;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.Group;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedGroup extends Group implements PatchedTransformableElement {
    private String    m_idBackup  = null;
    private SVGObject m_svgObject = null; 
    
    public static boolean isWrapper(Node node) {
        return node != null &&
            node instanceof PatchedGroup &&
            SVGFileModel.isWrapperId( ((PatchedGroup) node).getId());
    }
    
    public PatchedGroup(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    public void attachSVGObject(SVGObject svgObject) {
        m_svgObject = svgObject;
    }
    
    public SVGObject getSVGObject() {
        return m_svgObject;
    }
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id         = null;
        } else {
            id = m_idBackup;
        }
    }
    
    public ElementNode newInstance(final DocumentNode doc) {
        return new PatchedGroup(doc);
    }    
    
    //Fix for Perseus bug
    public SVGRect getScreenBBox() {
        SVGRect bBox = super.getScreenBBox();
        if (bBox == null) {
            ModelNode child = getFirstChildNode();
            if (child != null && child instanceof SVGLocatableElement) {
                bBox = ((SVGLocatableElement) child).getScreenBBox();
            }
        }
        return bBox;
    }

    //Fix for Perseus bug
    public SVGRect getBBox() {
        SVGRect bBox = super.getBBox();
        if (bBox == null) {
            ModelNode child = getFirstChildNode();
            if (child != null && child instanceof SVGLocatableElement) {
                bBox = ((SVGLocatableElement) child).getBBox();
            }
        }
        return bBox;
    }
}
