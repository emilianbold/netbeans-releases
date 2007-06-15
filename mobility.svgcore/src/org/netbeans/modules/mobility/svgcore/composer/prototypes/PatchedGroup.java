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

import com.sun.perseus.j2d.Transform;
import com.sun.perseus.model.*;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.Group;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.w3c.dom.Node;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedGroup extends Group implements PatchedElement {
    private static final byte WRAPPER_UNKNOWN = 0;
    private static final byte WRAPPER_NO      = 1;
    private static final byte WRAPPER_YES     = 2;

    //TODO HACK - revisit
    public static SceneManager s_sceneMgr = null;
    
    private String    m_idBackup  = null;
    private byte      m_wrapperState = WRAPPER_UNKNOWN;
    private SVGObject m_svgObject = null; 
    private boolean   m_isChanged = false;
    
    public static PatchedGroup getWrapper(Node node) {
        if (node != null && node instanceof PatchedGroup) {
            PatchedGroup pg = (PatchedGroup) node;
            return pg.isWrapper() ? pg : null;
        } else {
            return null;
        }
    }
    
    public PatchedGroup(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    public void attachSVGObject(SVGObject svgObject) {
        m_svgObject = svgObject;
        m_wrapperState = WRAPPER_YES;
    }
    
    public SVGObject getSVGObject() {
        checkWrapper();
        return m_svgObject;
    }
    
    private synchronized void checkWrapper() {
        if ( m_wrapperState == WRAPPER_UNKNOWN) {
            if ( SVGObject.isWrapperID(id)) {
                assert s_sceneMgr != null : "SceneManager reference not set!";
                m_wrapperState = WRAPPER_YES;
                m_svgObject = new SVGObject(s_sceneMgr, this);
            } else {
                m_wrapperState = WRAPPER_NO;
            }
        }
    }
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id         = null;
        } else {
            id = m_idBackup;
        }
    }
        
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
        System.out.println("Changing the group: " + id);
    }
    
    private int [] m_path;
    
    public void setPath(int [] path) {
        m_path = path;
    }
    
    public int [] getPath() {
        return m_path;
    }
    
    /*
    SVGConstants.SVG_TRANSFORM_ATTRIBUTE
    SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE
            
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == traitName
            || 
            SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == traitName
            
            
       if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_RULE_ATTRIBUTE == traitName
            || 
            SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_DISPLAY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VISIBILITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_COLOR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_OPACITY_ATTRIBUTE == traitName)    
           
SVGConstants.SVG_ID_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REQUIRED_FEATURES_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REQUIRED_EXTENSIONS_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_SYSTEM_LANGUAGE_ATTRIBUTE == traitName) 
    */
    
    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append( "<g ");
        sb.append("id=\"");
        sb.append(id);
        sb.append("\"");
        
        Transform tfm = getTransform();
        if (tfm != null) {
            sb.append(" transform=\"matrix(");
            for (int i = 0; i < 5; i++) {
                sb.append( tfm.getComponent(i));
                sb.append(',');
            }
            sb.append(tfm.getComponent(5));
            sb.append(")\"");            
        }
        sb.append(">");
        return sb.toString();
    }
        
    public ElementNode newInstance(final DocumentNode doc) {
        return new PatchedGroup(doc);
    }    
}
