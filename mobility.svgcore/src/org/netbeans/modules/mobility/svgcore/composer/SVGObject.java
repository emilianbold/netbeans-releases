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
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.util.Exceptions;
import org.w3c.dom.svg.SVGLocatableElement;
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
    
    public String getElementId() {
        String id = m_elem.getId();
        assert id != null : "Null ID of SVGElement " + m_elem;
        return id;
    }       

    public synchronized SVGRect getInitialScreenBBox() {
        SVGLocatableElement elem = getSVGElement();
        
        PatchedGroup pg  = null;
        Transform    tfm = null;
        
        if (elem instanceof PatchedGroup) {
            pg = (PatchedGroup) elem;
            tfm = pg.getTransform();
            pg.setTransform(null);
        } 
        
        SVGRect rect = PerseusController.getSafeScreenBBox(elem);
        
        if (tfm != null) {
            pg.setTransform(tfm);
        }
        
        return rect;
    }
    
    public synchronized SVGRect getSVGScreenBBox() {
        return PerseusController.getSafeScreenBBox(m_elem);
    }
    
    public Rectangle getScreenBBox() {
        SVGRect bBox = getSVGScreenBBox();
        if (bBox != null) {
            return new Rectangle( (int) bBox.getX(), (int) bBox.getY(),
                                  (int) bBox.getWidth(), (int) bBox.getHeight());
        } else {
            return null;
        }
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
            Rectangle rect = getScreenBBox();
            if (rect != null) {
                m_sceneMgr.getScreenManager().repaint( rect, overlap);
            }
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
            String id = getElementId();
            getPerseusController().moveToTop(m_elem);
            getFileModel().moveToTop(id);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveToTop failed");
            e.printStackTrace();
        }
    }

    public void moveToBottom() {
        try {
            String id = getElementId();
            getPerseusController().moveToBottom(m_elem);
            getFileModel().moveToBottom(id);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveToBottom failed");
            e.printStackTrace();
        }
    }

    public void moveForward() {
        try {
            String id = getElementId();
            getPerseusController().moveForward(m_elem);
            getFileModel().moveForward(id);
            repaint();
        } catch(Exception e) {
            System.err.println("MoveForward failed");
            e.printStackTrace();
        }
    }

    public void moveBackward() {
        try {
            String id = getElementId();
            getPerseusController().moveBackward(m_elem);
            getFileModel().moveBackward(id);
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

            String id = getElementId();
                    
            getPerseusController().delete(m_elem);
            try {
                getFileModel().deleteElement(id);
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
        String id = getElementId();
        
        m_elem = getPerseusController().wrapElement(this);
        getFileModel().wrapElement(id, ((PatchedGroup)m_elem).getText(false));
    }
        
    public static boolean isWrapperID(String id) {
        return id != null && id.startsWith("w_");
    }
            
    protected void applyUserTransform() {
        if (isWrappedObject()) {
            getOutline().setDirty();
            PatchedGroup pg = (PatchedGroup) m_elem;
            SVGRect rect = PerseusController.getSafeBBox(pg);
            if (rect != null) {
                pg.setTransform(null);
                float rotatePivotX = rect.getX() + rect.getWidth() / 2;
                float rotatePivotY = rect.getY() + rect.getHeight() / 2;

                Transform txf = new Transform( 1, 0, 0, 1, 0, 0);
                txf.mTranslate(getCurrentTranslateX() + rotatePivotX,
                    getCurrentTranslateY() + rotatePivotY);
                txf.mScale(getCurrentScale());
                txf.mRotate(getCurrentRotate());
                txf.mTranslate( -rotatePivotX, -rotatePivotY);

                pg.setUserTransform(txf);
            } else {
                System.err.println("Null BBox for " + pg);
            }
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
            
    protected final PerseusController getPerseusController() {
        return m_sceneMgr.getPerseusController();
    }
    
    protected final SVGFileModel getFileModel() {
        return m_sceneMgr.getDataObject().getModel();
    }
}
