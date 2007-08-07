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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedTransformableElement;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public final class SVGObject {
    private final SceneManager        m_sceneMgr;
    private final SVGLocatableElement m_elem;
    public final Transform            m_initialTransform;
    private       SVGObjectOutline    m_outline = null;
    private       boolean             m_isDeleted = false;

    private       float               m_translateDx = 0;
    private       float               m_translateDy = 0;
    private       float               m_skewX       = 0;
    private       float               m_skewY       = 0;
    private       float               m_scale       = 1;
    private       float               m_rotate      = 0;
    
    private       float               m_tempTranslateDx = 0;
    private       float               m_tempTranslateDy = 0;
    private       float               m_tempSkewX       = 0;
    private       float               m_tempSkewY       = 0;
    private       float               m_tempScale       = 1;
    private       float               m_tempRotate      = 0;
   
    private static final String ATTR_TRANSFORM = "transform"; //NOI18N
    
    public SVGObject(SceneManager sceneMgr, SVGLocatableElement elem) {
        assert sceneMgr != null;
        assert elem != null;
        
        m_sceneMgr         = sceneMgr;
        m_elem             = elem;
        m_initialTransform = new Transform( m_elem instanceof PatchedTransformableElement ? 
            ((PatchedTransformableElement) m_elem).getTransform() : null);
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
    
    public SVGRect getSafeBBox() {
        return PerseusController.getSafeBBox(m_elem);
    }
/*    
    public synchronized SVGRect getSVGScreenBBox() {
        return PerseusController.getSafeScreenBBox(m_elem);
    }
*/    
    public Rectangle getScreenBBox() {
        return getOutline().getScreenBoundingBox();
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
            assert rect != null;
            m_sceneMgr.getScreenManager().repaint( rect, overlap);
        }
    }
    
    public synchronized SVGObjectOutline getOutline() {
        if ( m_outline == null) {
            m_outline = new SVGObjectOutline(this);
        }
        return m_outline;
    }
    
    public SVGMatrix getParentTransformation() {
        Node            node       = m_elem.getParentNode();
        List<SVGMatrix> transforms = null;
        
        
        while(node != null) {
            if ( (node instanceof PatchedTransformableElement) ) {
                Transform temp = ((PatchedTransformableElement) node).getTransform();
                if (temp != null) {
                    if (transforms == null) {
                        transforms = new ArrayList<SVGMatrix>();
                    }
                    transforms.add( new Transform(temp));
                }
            }
            node = node.getParentNode();
       }
        SVGMatrix total = null;
        
        if (transforms != null) {
            total = transforms.get(0);
            for (int i = 1; i < transforms.size(); i++) {
                total = total.mMultiply(transforms.get(i));
            }
        }

        return total;
    }
    
    public void translate(final float dx, final float dy, final boolean isRelative) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                float _dx = dx,
                      _dy = dy;
                
                try {
                    Transform txf = (Transform) getParentTransformation();
                    
                    if ( txf != null) {
                        txf = (Transform) txf.inverse();
                        float [] point  = new float[2];
                        txf.transformPoint( new float[] {dx, dy}, point);
                        _dx = point[0] - txf.getComponent(4);
                        _dy = point[1] - txf.getComponent(5);
                    } 
                    
                    //System.out.println("Dx=" + _dx + ", Dy=" + _dy);
                    
                    if ( isRelative) {
                        m_tempTranslateDx += _dx;
                        m_tempTranslateDy += _dy;
                    } else {
                        m_tempTranslateDx = _dx;
                        m_tempTranslateDy = _dy;
                    }
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
                    m_tempRotate = rotate;
                    applyUserTransform();
                } catch (Exception ex) {
                    System.err.println("Rotate operation failed!");
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
    }

    public void skew(final float skewX, final float skewY ) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    m_tempSkewX = skewX;
                    m_tempSkewY = skewY;
                    applyUserTransform();
                } catch (Exception ex) {
                    System.err.println("Skew operation failed!");
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
    }
    
    public void moveToTop() {
        String id = getElementId();
        getPerseusController().moveToTop(m_elem);
        getFileModel().moveToTop(id);
        repaint();
    }

    public void moveToBottom() {
        String id = getElementId();
        getPerseusController().moveToBottom(m_elem);
        getFileModel().moveToBottom(id);
        repaint();
    }

    public void moveForward() {
        String id = getElementId();
        getPerseusController().moveForward(m_elem);
        getFileModel().moveForward(id);
        repaint();
    }

    public void moveBackward() {
        String id = getElementId();
        getPerseusController().moveBackward(m_elem);
        getFileModel().moveBackward(id);
        repaint();
    }
    
    public void delete() {
        if ( !m_isDeleted) {
            //ask for repaint before the object is removed,
            //the bounding box shall not be avaiable after delete
            repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            
            m_isDeleted = true;

            String id = getElementId();
                    
            getPerseusController().delete(m_elem);
            getFileModel().deleteElement(id);
        } else {
            System.err.println("Already deleted!");
        }
    }
    
    public void applyTextChanges() {
        if (m_elem != null && m_elem instanceof PatchedTransformableElement) {
        String transform = getTransformAsText((PatchedTransformableElement) m_elem);
        m_sceneMgr.getDataObject().getModel().setAttribute(getElementId(), ATTR_TRANSFORM, transform);
        }
    }
    
    public void commitChanges() {
        m_translateDx = getCurrentTranslateX();
        m_translateDy = getCurrentTranslateY();
        m_scale       = getCurrentScale();
        m_rotate      = getCurrentRotate();
        m_skewX       = getCurrentSkewX();
        m_skewY       = getCurrentSkewY();
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempSkewX = m_tempSkewY = 0;
        m_tempScale = 1;
        m_tempRotate = 0;
        applyUserTransform();
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);        
    }
    
    public void rollbackChanges() {
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);          
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempSkewX = m_tempSkewY = 0;
        m_tempScale = 1;
        m_tempRotate = 0;
        applyUserTransform();
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);        
    }
    
    public float [] getCurrentTranslate() {
        return new float [] { getCurrentTranslateX(), getCurrentTranslateY() };
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

    public float getCurrentSkewX() {
        return m_skewX + m_tempSkewX;
    }

    public float getCurrentSkewY() {
        return m_skewY + m_tempSkewY;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "SVGObject( tag="); //NOI18N
        sb.append( m_elem.getLocalName());
        sb.append( " id="); //NOI18N
        sb.append( m_elem.getId());
        sb.append( " isDeleted="); //NOI18N
        sb.append( m_isDeleted);
        sb.append( " translateX="); //NOI18N
        sb.append( m_translateDx);
        sb.append( " translateY="); //NOI18N
        sb.append( m_translateDy);
        sb.append( " skewX="); //NOI18N
        sb.append( m_skewX);
        sb.append( " skewY="); //NOI18N
        sb.append( m_skewY);
        sb.append( " scale="); //NOI18N
        sb.append( m_scale);
        sb.append( " rotate="); //NOI18N
        sb.append( m_rotate);
        sb.append(")"); //NOI18N

        return sb.toString();
    }
    
    protected void applyUserTransform() {
        if (m_elem instanceof PatchedTransformableElement) {
            getScreenManager().incrementChangeTicker();
            PatchedTransformableElement pg = (PatchedTransformableElement) m_elem;
            SVGRect rect = PerseusController.getSafeBBox(m_elem);
            
            if (rect != null) {
                float [] temp = new float[] {
                    rect.getX() + rect.getWidth() / 2,
                    rect.getY() + rect.getHeight() / 2
                };
                float []  rotatePivot = new float[2];
                m_initialTransform.transformPoint( temp, rotatePivot);
                //System.out.println("Rotate pivot: [" + rotatePivot[0] + "," + rotatePivot[1] + "]");

                Transform txf = new Transform( 1, 0, 0, 1, 0, 0);
                txf.mTranslate(getCurrentTranslateX() + rotatePivot[0],
                    getCurrentTranslateY() + rotatePivot[1]);
                
                float skew;
                skew = getCurrentSkewX();
                if ( skew != 0 && skew > -90 && skew < 90) {
                    Transform skewTxf = new Transform( 1, 0, (float) Math.tan(Math.toRadians(skew)), 1, 0, 0);
                    txf.mMultiply(skewTxf);
                }
                skew = getCurrentSkewY();
                if ( skew != 0 && skew > -90 && skew < 90) {
                    Transform skewTxf = new Transform( 1, (float) Math.tan(Math.toRadians(skew)), 0, 1, 0, 0);
                    txf.mMultiply(skewTxf);
                }
                
                txf.mScale(getCurrentScale());
                txf.mRotate(getCurrentRotate());
                txf.mTranslate( -rotatePivot[0], -rotatePivot[1]);
                
                txf.mMultiply(m_initialTransform);
                pg.setTransform(txf);
            } else {
                System.err.println("Null BBox for " + pg);
            }
        }
    }

            
    protected final PerseusController getPerseusController() {
        return m_sceneMgr.getPerseusController();
    }
    
    protected final SVGFileModel getFileModel() {
        return m_sceneMgr.getDataObject().getModel();
    }
    
    public static boolean areSame(SVGObject [] arr1,SVGObject [] arr2) {
        if (arr1 == arr2) {
            return true;
        } else if (arr1 == null || arr2 == null) {
            return false;
        } else if (arr1.length != arr2.length) {
            return false;
        } else {
            for (int i = 0; i < arr1.length; i++) {
                if ( arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static String getTransformAsText(PatchedTransformableElement telem) {
        Transform     tfm = telem.getTransform();
        StringBuilder sb  = new StringBuilder();

        if (tfm != null) {
            sb.append("matrix("); //NOI18N
            for (int i = 0; i < 5; i++) {
                sb.append( tfm.getComponent(i));
                sb.append(',');
            }
            sb.append(tfm.getComponent(5));
            sb.append(")"); //NOI18N
        }
        return sb.toString();
    }    
}
