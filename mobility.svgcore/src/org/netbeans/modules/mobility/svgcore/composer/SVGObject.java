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
import javax.swing.text.BadLocationException;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedElement;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.util.Exceptions;
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
    private       SVGObjectOutline    m_outline = null;
    private       boolean             m_isDeleted = false;

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
        readUserTransform();            
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
        int [] path = getPerseusController().getPath((ModelNode)m_elem);
        return path;
    }       
    
    public synchronized SVGRect getSVGScreenBBox() {
        SVGRect bBox = getSVGElement().getScreenBBox();
        assert bBox != null : "The element " + getSVGElement() + " is no longer in document!";
        return bBox;
    }
    
    public Rectangle getScreenBBox() {
        SVGRect bBox = getSVGScreenBBox();
        return new Rectangle( (int) bBox.getX(), (int) bBox.getY(),
                              (int) bBox.getWidth(), (int) bBox.getHeight());
    }

    public boolean isDeleted() {
        return m_isDeleted;
    }
    
    public void repaint() {
        repaint(0);
    }

    public void repaint(int x, int y, int w, int h) {
        m_sceneMgr.getScreenManager().repaint( x, y, w, h);
    }
    
    public void repaint(int overlap) {
        if ( !m_isDeleted) {
            m_sceneMgr.getScreenManager().repaint( getScreenBBox(), overlap);
        }
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
                try {
                    checkWrapped();
                    m_tempTranslateDx = dx;
                    m_tempTranslateDy = dy;
                    applyUserTransform();
                } catch (Exception ex) {
                    System.err.println("Translate operation failed!");
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
    }

    public void scale(final float scale) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    checkWrapped();
                    m_tempScale = scale;
                    applyUserTransform();
                } catch (Exception ex) {
                    System.err.println("Scale operation failed!");
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
    }

    public void rotate(final float rotate) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    checkWrapped();
                    m_tempRotate = rotate;
                    applyUserTransform();
                } catch (BadLocationException ex) {
                    System.err.println("Rotate operation failed!");
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
    }

    public void moveToTop() {
        try {
            int [] path = getActualSelection();
            getPerseusController().moveToTop(m_elem);
            getFileModel().moveToTop(path);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveToTop failed");
            e.printStackTrace();
        }
    }

    public void moveToBottom() {
        try {
            int [] path = getActualSelection();
            getPerseusController().moveToBottom(m_elem);
            getFileModel().moveToBottom(path);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveToBottom failed");
            e.printStackTrace();
        }
    }

    public void moveForward() {
        try {
            int [] path = getActualSelection();
            getPerseusController().moveForward(m_elem);
            getFileModel().moveForward(path);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveForward failed");
            e.printStackTrace();
        }
    }

    public void moveBackward() {
        try {
            int [] path = getActualSelection();
            getPerseusController().moveBackward(m_elem);
            getFileModel().moveBackward(path);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveBackWard failed");
            e.printStackTrace();
        }
    }
    
    public void delete() {
        if ( !m_isDeleted) {
            //ask for repaint before the object is removed,
            //the bounding box shall not be avaiable after delete
            repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            
            m_isDeleted = true;

            int [] path = getActualSelection();
                    
            getPerseusController().delete(m_elem);
            try {
                getFileModel().deleteElement(path);
            } catch(BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Already deleted!");
        }
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
        return PatchedGroup.getWrapper(m_elem) != null;
    }
      
    protected void checkWrapped() throws BadLocationException {
        if (!isWrappedObject()) {
            wrapObject();
        }
        assert isWrappedObject() : "Wrapping failed";
    }
    
    protected synchronized void wrapObject() throws BadLocationException {
        int [] path = getPerseusController().getPath((ModelNode) m_elem);
        
        Node parent = m_elem.getParentNode();
        Document doc = getOwnerDocument(parent);
        // HACK - clear all elements' ids so that the element removal is possible
        //TODO move to PerseusController
        PerseusController.setNullIds(m_elem, true);
        parent.removeChild(m_elem);
        PatchedGroup wrapper = (PatchedGroup) doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                SVGConstants.SVG_G_TAG);
        wrapper.attachSVGObject(this);
        wrapper.appendChild(m_elem);
        // HACK - restore element ids
        PerseusController.setNullIds(m_elem, false);
        if (m_elem instanceof PatchedElement) {
            wrapper.setPath( ((PatchedElement)m_elem).getPath());
        }
        parent.appendChild(wrapper);
        wrapper.setId(generateWrapperID());
        m_elem = wrapper;
        getFileModel().wrapElement(path, wrapper.getText());
    }
    
    //TODO revisit (use filename for inserted files)
    public static String generateWrapperID() {
        return "w_" + System.currentTimeMillis();
    }
    
    public static boolean isWrapperID(String id) {
        return id != null && id.startsWith("w_");
    }
            
    protected void applyUserTransform() {
        if (isWrappedObject()) {
            getOutline().setDirty();
            float scale = getCurrentScale();
            Transform txf = new Transform( scale, 0, 0, scale,
                                          getCurrentTranslateX(),
                                          getCurrentTranslateY());
            txf.mRotate(getCurrentRotate());
            PatchedGroup pg = (PatchedGroup) m_elem;
            pg.setUserTransform(txf);
        }
    }

    protected void readUserTransform() {
        PatchedGroup pg = PatchedGroup.getWrapper(m_elem);
        
        if (pg != null) {
            Transform txf = pg.getTransform();
            if (txf != null) {
                m_scale = txf.getComponent(0);
                m_translateDx = txf.getComponent(4);
                m_translateDy = txf.getComponent(5);
                //TODO read rotate as well
                m_rotate = 0;
            }
        }
    }
    
    protected static Document getOwnerDocument(Node elem) {
        Node parent;
        
        while( (parent=elem.getParentNode()) != null) {
            elem = parent;
        }
        return (Document) elem;
    }
        
    protected final PerseusController getPerseusController() {
        return m_sceneMgr.getPerseusController();
    }
    
    protected final SVGFileModel getFileModel() {
        return m_sceneMgr.getDataObject().getModel();
    }
}
