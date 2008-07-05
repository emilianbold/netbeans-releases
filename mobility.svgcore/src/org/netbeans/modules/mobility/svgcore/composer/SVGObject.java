/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.j2d.Transform;
import com.sun.perseus.util.SVGConstants;
import java.awt.Rectangle;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedTransformableElement;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.w3c.dom.svg.SVGElement;
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
    private final Transform           m_initialTransform;
    private       SVGObjectOutline    m_outline   = null;
    private       boolean             m_isDeleted = false;

    private       float               m_translateDx = 0;
    private       float               m_translateDy = 0;
    private       float               m_skewX       = 0;
    private       float               m_skewY       = 0;
    private       float               m_scaleX      = 1;
    private       float               m_scaleY      = 1;
    private       float               m_rotate      = 0;
    
    private       float               m_tempTranslateDx = 0;
    private       float               m_tempTranslateDy = 0;
    private       float               m_tempSkewX       = 0;
    private       float               m_tempSkewY       = 0;
    private       float               m_tempScaleX      = 1;
    private       float               m_tempScaleY      = 1;
    private       float               m_tempRotate      = 0;

    public SVGObject(SceneManager sceneMgr, SVGLocatableElement elem) {
        assert sceneMgr != null;
        assert elem != null;
        
        m_sceneMgr         = sceneMgr;
        m_elem             = elem;
        m_initialTransform = new Transform( m_elem instanceof PatchedTransformableElement ? 
            ((PatchedTransformableElement) m_elem).getTransform() : null);
        SceneManager.log(Level.FINE, "SVGObject created: " + m_elem); //NOI18N        
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
        return PerseusController.getParentTransformation(m_elem.getParentNode());
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
                    
                    if ( isRelative) {
                        m_tempTranslateDx += _dx;
                        m_tempTranslateDy += _dy;
                    } else {
                        m_tempTranslateDx = _dx;
                        m_tempTranslateDy = _dy;
                    }
                    applyUserTransform();
                } catch (Exception ex) {
                    SceneManager.error( "Translate operation failed!", ex); //NOI18N        
                }
            }            
        });
    }
    
    public void scale(final float scaleX, final float scaleY) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    m_tempScaleX = scaleX;
                    m_tempScaleY = scaleY;
                    applyUserTransform();
                } catch (Exception ex) {
                    SceneManager.error( "Scale operation failed!", ex); //NOI18N        
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
                    SceneManager.error( "Rotate operation failed!", ex); //NOI18N        
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
                    SceneManager.error( "Skew operation failed!", ex); //NOI18N        
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
            String id = getElementId();
                    
            getFileModel().deleteElement(id,new SVGFileModel.TransactionCommand() {
                @SuppressWarnings("unchecked")
                public Object execute(Object userData) {
                    //ask for repaint before the object is removed,
                    //the bounding box shall not be avaiable after delete
                    repaint(SVGObjectOutline.SELECTOR_OVERLAP);
                    m_isDeleted = true;
                    PerseusController pc = getPerseusController();
                    
                    for (String id : (List<String>) userData) {
                        SVGElement elem = pc.getElementById(id);
                        getPerseusController().delete(elem);
                    }
                    return null;
                }
            });
        } else {
            SceneManager.log( Level.SEVERE, "SVGObject is already deleted."); //NOI18N        
        }
    }
    
    public void applyTextChanges() {
        if (m_elem != null && m_elem instanceof PatchedTransformableElement) {
            PatchedTransformableElement pte   = (PatchedTransformableElement) m_elem;
            SVGFileModel                model = m_sceneMgr.getDataObject().getModel();
            
            String [] changedAttrs = pte.optimizeTransform();
            if ( changedAttrs != null) {
                model.setAttributes(getElementId(), changedAttrs);
            } else {
                String transform = getTransformAsText(pte.getTransform());
                model.setAttribute(getElementId(), SVGConstants.SVG_TRANSFORM_ATTRIBUTE, transform);
            }
        }
    }
    
    public void commitChanges() {
        m_translateDx = getCurrentTranslateX();
        m_translateDy = getCurrentTranslateY();
        m_scaleX      = getCurrentScaleX();
        m_scaleY      = getCurrentScaleY();
        m_rotate      = getCurrentRotate();
        m_skewX       = getCurrentSkewX();
        m_skewY       = getCurrentSkewY();
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempSkewX = m_tempSkewY = 0;
        m_tempScaleX = 1;
        m_tempScaleY = 1;
        m_tempRotate = 0;
        applyUserTransform();
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);        
    }
    
    public void rollbackChanges() {
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);          
        m_tempTranslateDx = m_tempTranslateDy = 0;
        m_tempSkewX = m_tempSkewY = 0;
        m_tempScaleX = 1;
        m_tempScaleY = 1;
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

    public float getCurrentScaleX() {
        return m_scaleX * m_tempScaleX;
    }

    public float getCurrentScaleY() {
        return m_scaleY * m_tempScaleY;
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
        sb.append( " scaleX="); //NOI18N
        sb.append( m_scaleX);
        sb.append( " scaleY="); //NOI18N
        sb.append( m_scaleY);
        sb.append( " rotate="); //NOI18N
        sb.append( m_rotate);
        sb.append(")"); //NOI18N

        return sb.toString();
    }
    
    protected void applyUserTransform() {
        if (m_elem instanceof PatchedTransformableElement) {
            getScreenManager().incrementChangeTicker();
            PatchedTransformableElement pe = (PatchedTransformableElement) m_elem;
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
                
                txf.mScale(getCurrentScaleX(), getCurrentScaleY());
                txf.mRotate(getCurrentRotate());
                txf.mTranslate( -rotatePivot[0], -rotatePivot[1]);
                
                txf.mMultiply(m_initialTransform);
                pe.setTransform(txf);
            } else {
                SceneManager.log( Level.SEVERE, "Null BBox for " + pe); //NOI18N                        
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
    
    public static String getTransformAsText(SVGMatrix tfm) {
        StringBuilder sb  = new StringBuilder();

        if (tfm != null) {
            if (PerseusController.isIdentityTransform(tfm, true)) {
                sb.append("translate(");
                sb.append( tfm.getComponent(4));
                sb.append(',');
                sb.append( tfm.getComponent(5));
                sb.append(")"); //NOI18N
            } else {
                sb.append("matrix("); //NOI18N
                for (int i = 0; i < 5; i++) {
                    sb.append( tfm.getComponent(i));
                    sb.append(',');
                }
                sb.append(tfm.getComponent(5));
                sb.append(")"); //NOI18N
            }
        }
        return sb.toString();
    }    
}
