/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.logging.Logger;
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
    private       Transform           m_initialTransform;
    private       SVGObjectOutline    m_outline   = null;
    private       boolean             m_isDeleted = false;

    private       float               m_translateDx = 0;
    private       float               m_translateDy = 0;
    private       float               m_skewX       = 0;
    private       float               m_skewY       = 0;
    private       float               m_scaleX      = 1;
    private       float               m_scaleY      = 1;
    private       float               m_rotate      = 0;
    //AVKprivate       float[]             m_rotatePivot = null;
    private       ScalePivotPoint     m_scalePivot  = null;
    private       boolean             m_lanscapeUpdate = false;

    //private final static Logger LOG = Logger.getLogger(SVGObject.class.getName());

    public enum ScalePivotPoint{
        NW_CORNER,
        SE_CORNER;

        public boolean isSECorner(){
            switch (this){
                case SE_CORNER: return true;
                default:        return false;
            }
        }

        public boolean isNWCorner(){
            switch (this){
                case NW_CORNER: return true;
                default:        return false;
            }
        }

        @Override
        public String toString() {
            switch (this){
                case NW_CORNER: return "NW_CORNER";// NOI18N
                case SE_CORNER: return "SE_CORNER";// NOI18N
                default:        return "Unsupported"; // NOI18N
            }
        }
    }

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
                        m_translateDx += _dx;
                        m_translateDy += _dy;
                    } else {
                        m_translateDx = _dx;
                        m_translateDy = _dy;
                    }
                    applyUserTransform();
                } catch (Exception ex) {
                    SceneManager.error( "Translate operation failed!", ex); //NOI18N        
                }
            }            
        });
    }
    
    public void scale(final float scaleX, final float scaleY,
            final ScalePivotPoint scalePivot) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    m_scaleX = scaleX;
                    m_scaleY = scaleY;
                    m_scalePivot = scalePivot;
                    applyUserTransform();
                } catch (Exception ex) {
                    SceneManager.error( "Scale operation failed!", ex); //NOI18N        
                }
            }            
        });
    }

    public void setLandscape(int rotateAngle, float[] translate){
        m_lanscapeUpdate = true;
        rotate(rotateAngle);
        translate(translate[0], translate[1], true);
    }

    public void rotate(final float rotate) {
//AVK        rotate(rotate, null);
//AVK    }

//AVK    public void rotate(final float rotate, final float[] rotatePrivot) {
        m_sceneMgr.getPerseusController().execute(new Runnable() {
            public void run() {
                try {
                    m_rotate = rotate;
                    //AVKm_rotatePivot = rotatePrivot;
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
                    m_skewX = skewX;
                    m_skewY = skewY;
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
        SVGFileModel model = m_sceneMgr.getDataObject().getModel();

        String[] changedAttrs = prepareTextChanges();
        if (changedAttrs != null) {
            model.setAttributes(getElementId(), changedAttrs);
        }
    }
    
    public String[] prepareTextChanges() {
        String[] changedAttrs = null;
        if (m_elem != null && m_elem instanceof PatchedTransformableElement) {
            PatchedTransformableElement pte = (PatchedTransformableElement) m_elem;

            changedAttrs = pte.optimizeTransform();
            if (changedAttrs == null) {
                String transform = getTransformAsText(pte.getTransform());
                changedAttrs = new String[]{
                            SVGConstants.SVG_TRANSFORM_ATTRIBUTE, transform
                        };
            }
        }
        return changedAttrs;
    }

    public void commitChanges() {
        applyUserTransform(true);

        m_translateDx = 0;
        m_translateDy = 0;
        m_skewX = 0;
        m_skewY = 0;
        m_scaleX = 1;
        m_scaleY = 1;
        m_scalePivot = null;
        m_rotate = 0;
//AVK        m_rotatePivot = null;
        m_lanscapeUpdate = false;
        repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }

    public float [] getCurrentTranslate() {
        return new float [] { getCurrentTranslateX(), getCurrentTranslateY() };
    }
    
    public float getCurrentTranslateX() {
        return m_translateDx;
    }

    public float getCurrentTranslateY() {
        return m_translateDy;
    }

    public float getCurrentScaleX() {
        return m_scaleX;
    }

    public float getCurrentScaleY() {
        return m_scaleY;
    }

    public ScalePivotPoint getCurrentScalePivot() {
        return m_scalePivot != null ? m_scalePivot : ScalePivotPoint.NW_CORNER;
    }

    public float getCurrentRotate() {
        return (m_rotate) % 360;
    }

    public float getCurrentSkewX() {
        return m_skewX;
    }

    public float getCurrentSkewY() {
        return m_skewY;
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
        sb.append( " scalePivot="); //NOI18N
        sb.append( m_scalePivot);
        sb.append( " rotate="); //NOI18N
        sb.append( m_rotate);
//AVK        sb.append( " rotatePivot="); //NOI18N
//AVK        sb.append( m_rotatePivot ==null ? "null" : "["+m_rotatePivot[0]+","+m_rotatePivot[1]+"]" );
        sb.append( " lanscapeUpdate="); //NOI18N
        sb.append( m_lanscapeUpdate);
        sb.append(")"); //NOI18N

        return sb.toString();
    }
    
    protected void applyUserTransform() {
        applyUserTransform(false);
    }

    protected void applyUserTransform(boolean commit) {
        if (m_elem instanceof PatchedTransformableElement) {
            getScreenManager().incrementChangeTicker();
            PatchedTransformableElement pe = (PatchedTransformableElement) m_elem;
            SVGRect rect = PerseusController.getSafeBBox(m_elem);
            
            if (rect != null) {
                float []  rotatePivot = prepareRotatePivot(rect);
                float []  scalePivot = prepareScalePivot(rect);

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
                
                txf.mRotate(getCurrentRotate());
                txf.mTranslate( -rotatePivot[0], -rotatePivot[1]);
                
                txf.mTranslate( scalePivot[0], scalePivot[1]);
                txf.mScale(getCurrentScaleX(), getCurrentScaleY());
                txf.mTranslate( -scalePivot[0], -scalePivot[1]);

//                if (!m_lanscapeUpdate) {
                    txf.mMultiply(m_initialTransform);
                    pe.setTransform(txf);
                    if (commit) {
                        m_initialTransform = txf;
                    }
//                } else {
//                    Transform resultTxf = new Transform(m_initialTransform);
//                    resultTxf.mMultiply(txf);
//                    LOG.warning("         final init*tmp : " + getTransformAsText(resultTxf));
//                    pe.setTransform(resultTxf);
//                    if (commit) {
//                        m_initialTransform = resultTxf;
//                    }
//                }

            } else {
                SceneManager.log( Level.SEVERE, "Null BBox for " + pe); //NOI18N                        
            }
        }
    }

    private float[] prepareRotatePivot(SVGRect rect){
        if (!m_lanscapeUpdate) {
//AVK        float[] tempRotatePivot = m_rotatePivot != null ? m_rotatePivot : new float[]{
//AVK                    rect.getX() + rect.getWidth() / 2,
//AVK                    rect.getY() + rect.getHeight() / 2
//AVK                };
            float[] tempRotatePivot = new float[]{
                rect.getX() + rect.getWidth() / 2,
                rect.getY() + rect.getHeight() / 2
            };
            float[] rotatePivot = new float[2];
            m_initialTransform.transformPoint(tempRotatePivot, rotatePivot);
            return rotatePivot;
        } else {
            return new float[]{0f, 0f};
        }
    }

    private float[] prepareScalePivot(SVGRect rect){
        float[] tempScalePivot = getCurrentScalePivot().isNWCorner()
                ? new float[]{rect.getX(), rect.getY()}
                : new float[]{rect.getX() + rect.getWidth(),
                    rect.getY() + rect.getHeight()};

        float[] scalePivot = new float[2];
        m_initialTransform.transformPoint(tempScalePivot, scalePivot);
        return scalePivot;
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
