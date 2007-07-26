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
    //private static final byte WRAPPER_UNKNOWN = 0;
    //private static final byte WRAPPER_NO      = 1;
    //private static final byte WRAPPER_YES     = 2;
    
    private String    m_idBackup  = null;
    //private byte      m_wrapperState = WRAPPER_UNKNOWN;
    private SVGObject m_svgObject = null; 
    //private boolean   m_isChanged = false;
    
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
        //m_wrapperState = WRAPPER_YES;
    }
    
    public SVGObject getSVGObject() {
        //checkWrapper();
        return m_svgObject;
    }
    
    /*
    private synchronized void checkWrapper() {
        if ( m_wrapperState == WRAPPER_UNKNOWN) {
            if ( SVGFileModel.isWrapperId(id)) {
                assert s_sceneMgr != null : "SceneManager reference not set!";
                m_wrapperState = WRAPPER_YES;
                m_svgObject = new SVGObject(s_sceneMgr, this);
            } else {
                m_wrapperState = WRAPPER_NO;
            }
        }
    }
    */
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id         = null;
        } else {
            id = m_idBackup;
        }
    }
        
    /*
    public boolean isWrapper() {
        checkWrapper();
        return m_wrapperState == WRAPPER_YES;
    }
    
    public boolean isChanged() {
        return m_isChanged;
    }
    
    public void setChanged(boolean isChanged) {
        m_isChanged = isChanged;
    }
    
    public void setUserTransform(Transform txf) {
        super.setTransform(txf);
        setChanged(true);
        //System.out.println("Changing the group: " + id);
    }
    
    public void _applyChangesToText() {
        String transform = getTransformAsText();
        s_sceneMgr.getDataObject().getModel().setAttribute(getId(), ATTR_TRANSFORM, transform);
    }
    
    public String getText(boolean onlyAttrs) {
        StringBuilder sb = new StringBuilder();
        if (!onlyAttrs) {
            sb.append( "<g ");
        }
        sb.append("id=\"");
        sb.append(id);
        sb.append("\"");
        
        String transform = getTransformAsText();
        if (transform != null && transform.length() > 0) {
            sb.append(" " + ATTR_TRANSFORM + "=\"");
            sb.append(transform);
            sb.append("\"");            
        }
        if (!onlyAttrs) {        
            sb.append(">");
        }
        return sb.toString();
    }
    */
    
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
    
    /*
    private String getTransformAsText() {
        Transform     tfm = getTransform();
        StringBuilder sb  = new StringBuilder();

        if (tfm != null) {
            sb.append("matrix(");
            for (int i = 0; i < 5; i++) {
                sb.append( tfm.getComponent(i));
                sb.append(',');
            }
            sb.append(tfm.getComponent(5));
            sb.append(")");
        }
        return sb.toString();
    } */   
}
