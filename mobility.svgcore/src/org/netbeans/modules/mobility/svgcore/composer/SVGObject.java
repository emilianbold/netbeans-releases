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

package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.j2d.Transform;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.util.SVGConstants;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public class SVGObject {
    private final SceneManager        m_sceneMgr;
    private       SVGLocatableElement m_elem;
    private       SVGObjectOutline    m_outline       = null;

    private       float               m_translateDx = 0;
    private       float               m_translateDy = 0;
    private       float               m_scale       = 1;
    private       float               m_rotate      = 0;
    
    private       float               m_tempTranslateDx  = 0;
    private       float               m_tempTranslateDy = 0;
    private       float               m_tempScale  = 1;
    private       float               m_tempRotate = 0;
    
    public SVGObject(SceneManager sceneMgr, SVGLocatableElement elem) {
        m_sceneMgr = sceneMgr;
        m_elem     = elem;
    }

    public SceneManager getSceneManager() {
        return m_sceneMgr;
    }

    public ScreenManager getScreenManager() {
        return m_sceneMgr.getScreenManager();
    }
     
    public SVGLocatableElement getSVGElement() {
        return m_elem;
    }

    public int [] getActualSelection() {
        int [] path = m_sceneMgr.getPerseusController().getPath((ModelNode)m_elem);
        return path;
    }       
    
    public synchronized SVGRect getSVGScreenBBox() {
        SVGRect bBox = getSVGElement().getScreenBBox();
        assert bBox != null : "The element is no longer in document!";
        return bBox;
    }
    
    public Rectangle getScreenBBox() {
        SVGRect bBox = getSVGScreenBBox();
        return new Rectangle( (int) bBox.getX(), (int) bBox.getY(),
                              (int) bBox.getWidth(), (int) bBox.getHeight());
    }

    public void repaint() {
        repaint(0);
    }

    public void repaint(int x, int y, int w, int h) {
        m_sceneMgr.getScreenManager().repaint( x, y, w, h);
    }
    
    public void repaint(int overlap) {
        m_sceneMgr.getScreenManager().repaint( getScreenBBox(), overlap);
    }
    
    public synchronized SVGObjectOutline getOutline() {
        if ( m_outline == null) {
            m_outline = new SVGObjectOutline(this);
        }
        return m_outline;
    }
    
    public void translate(final float dx, final float dy) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                checkWrapped();
                m_tempTranslateDx = dx;
                m_tempTranslateDy = dy;
                applyUserTransform();
            }            
        });
    }

    public void scale(final float scale) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                checkWrapped();
                m_tempScale = scale;
                applyUserTransform();
            }            
        });
    }

    public void rotate(final float rotate) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                checkWrapped();
                m_tempRotate = rotate;
                applyUserTransform();
            }            
        });
    }

    public void commitChanges() {
        m_translateDx = getCurrentTranslateX();
        m_translateDy = getCurrentTranslateY();
        m_scale       = getCurrentScale();
        m_rotate      = getCurrentRotate();
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempScale = 1;
        m_tempRotate = 0;
        applyUserTransform();
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);        
    }
    
    public void rollbackChanges() {
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);          
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempScale = 1;
        m_tempRotate = 0;
        applyUserTransform();
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);        
    }
    
    public float getCurrentTranslateX() {
        return m_translateDx + m_tempTranslateDx;
    }

    public float getCurrentTranslateY() {
        return m_translateDy + m_tempTranslateDy;
    }

    public float getCurrentScale() {
        return m_scale * m_tempScale;
    }

    public float getCurrentRotate() {
        return (m_rotate + m_tempRotate) % 360;
    }
    
    protected synchronized boolean isWrappedObject() {
        return m_elem instanceof PatchedGroup &&
               ((PatchedGroup) m_elem).isWrapper();
    }
      
    protected void checkWrapped() {
        if (!isWrappedObject()) {
            wrapObject();
        }
        assert isWrappedObject() : "Wrapping failed";
    }
    
    protected synchronized void wrapObject() {
        Node parent = m_elem.getParentNode();
        Document doc = getOwnerDocument(parent);
        // HACK - clear all elements' ids so that the element removal is possible
        setNullIds(m_elem, true);
        parent.removeChild(m_elem);
        PatchedGroup wrapper = (PatchedGroup) doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                SVGConstants.SVG_G_TAG);
        wrapper.attachSVGObject(this);
        wrapper.appendChild(m_elem);
        // HACK - restore element ids
        setNullIds(m_elem, false);
        parent.appendChild(wrapper);
        wrapper.setWrapper(true);
        m_elem = wrapper;
    }
    
    protected void applyUserTransform() {
        if (isWrappedObject()) {
            getOutline().setDirty();
            float scale = getCurrentScale();
            Transform txf = new Transform( scale, 0, 0, scale,
                                          getCurrentTranslateX(),
                                          getCurrentTranslateY());
            txf.mRotate(getCurrentRotate());
            ((PatchedGroup)m_elem).setTransform(txf);
        }
    }
    
    protected static Document getOwnerDocument(Node elem) {
        Node parent;
        
        while( (parent=elem.getParentNode()) != null) {
            elem = parent;
        }
        return (Document) elem;
    }
    
    public static void setNullIds(SVGElement elem, boolean isNull) {
        if (elem instanceof PatchedElement) {
            ((PatchedElement) elem).setNullId(isNull);
        } else if ( elem.getId() != null) {
            System.err.println("The patched element must be used instead of " + elem.getClass().getName() + "[" + elem.getId() + "]");
        }

        SVGElement child = (SVGElement) elem.getFirstElementChild();
        while(child != null) {
            setNullIds( child, isNull);
            child = (SVGElement)child.getNextElementSibling();
        }
    }
}
