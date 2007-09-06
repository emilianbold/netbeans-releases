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
 */   
/*
 * SVGAnimationRasterizer.java
 *
 * Created on November 30, 2005, 10:53 AM
 */

package org.netbeans.modules.mobility.svgcore.export;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.util.SVGConstants;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import javax.microedition.m2g.SVGImage;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.export.ComponentGroup.ComponentWrapper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author  Pavel Benes
 */
public abstract class SVGRasterizerPanel extends JPanel implements AnimationRasterizer.Params {
    protected final    SVGDataObject       m_dObj;
    protected final    String              m_elementId;
    protected final    J2MEProject         m_project;
    protected final    Dimension           m_dim;
    protected          double              m_ratio;      
    protected          int                 m_overrideWidth = -1;
    protected          int                 m_overrideHeight = -1;
    protected volatile boolean             m_updateInProgress = false;
    protected volatile SVGImage            m_svgImage;
    private            SVGLocatableElement m_exportedElement = null;            
    
    protected class SVGRasterizerComponentGroup extends ComponentGroup {
        public SVGRasterizerComponentGroup( Object ... comps) {
            super(comps);
        }
        public void refresh(JComponent source) {
            updateImage(source, true);
        }
    };
    
    protected SVGRasterizerPanel( SVGDataObject dObj, String elementId) throws IOException, BadLocationException {
        m_dObj      = dObj;
        m_elementId = elementId;
        
        FileObject primaryFile = m_dObj.getPrimaryFile();
        
        Project p = FileOwnerQuery.getOwner (primaryFile);
        if (p != null && p instanceof J2MEProject){
            m_project = (J2MEProject) p;
        } else {
            m_project = null;
        }

        if (m_elementId == null && isInProject()) {
            m_dim = ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, null);
        } else {
            getSVGImage();
            m_dim = new Dimension( m_svgImage.getViewportWidth(), m_svgImage.getViewportHeight());
        }
    }
    
    protected ComponentGroup createTimeGroup( JSpinner spinner, final JSlider slider, boolean isStart) {
        final float duration = m_dObj.getSceneManager().getAnimationDuration();
        int p = Math.round(duration * 100);
        slider.setMinimum( 0);
        slider.setMaximum( p);
        ComponentWrapper sliderWrapper;
        
        if (!isStart) {
            slider.setInverted(true);
            sliderWrapper = new ComponentGroup.SliderWrapper(slider) {
                public float getValue() {
                    return duration - super.getValue();
                }

                public void setValue(float value) {
                    super.setValue(duration - value);
                }
            };
        } else {
            sliderWrapper = ComponentWrapper.wrap(slider);
        }
       
        final SpinnerNumberModel model = new SpinnerNumberModel( (double) (isStart ? 0 : duration), 0.0, duration, 1.0);
        spinner.setModel( model);
        return new SVGRasterizerComponentGroup( spinner, sliderWrapper);
    }
    
    protected ComponentGroup createCompressionGroup(JComboBox combo, JSpinner spinner) {
        spinner.setModel( new SpinnerNumberModel( 0, 0, 99, 1));
        spinner.setValue( new Integer( AnimationRasterizer.DEFAULT_COMPRESSION.getRate()));
        combo.setSelectedItem(AnimationRasterizer.DEFAULT_COMPRESSION);
        
        return new SVGRasterizerComponentGroup( createComboWrapper(combo), spinner);
    }
    
    protected final boolean isInProject() {
        return m_project != null;
    }
    
    protected ComponentGroup.ComponentWrapper createComboWrapper( JComboBox combo) {
        return  new ComponentGroup.ComponentWrapper(combo) {
            public float getValue() {
                Object obj = ((JComboBox) m_delegate).getSelectedItem();
                return ((AnimationRasterizer.CompressionLevel) obj).getRate();
            }

            public void setValue(float value) {
                int q = Math.round(value);
                ((JComboBox) m_delegate).setSelectedItem(AnimationRasterizer.CompressionLevel.getLevel(q));
            }
        };
    }
   
    protected static float roundTime(float f) {
        return Math.round( f * 100) / 100.0f;
    }
    
    public final void setImageWidth(int w) {
        m_overrideWidth = w;
    }

    public final void setImageHeight(int h) {
        m_overrideHeight = h;
    }
    
    public float getEndTime(){
        return 0;
    }
    
    public float getFramesPerSecond(){
        return 1;        
    }
    
    public double getRatio(){
        return m_ratio;
    }
    
    private void loadSVGImage() {
        try {
            m_svgImage = m_dObj.getModel().parseSVGImage();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public final synchronized SVGImage getSVGImage() throws IOException, BadLocationException {
        if ( m_svgImage == null) {            
            //TODO Revisit, update of the model will probably cause deadlock
            if ( SwingUtilities.isEventDispatchThread()) {
                Thread th = new Thread() {
                    public void run() {
                        loadSVGImage();
                    }
                };
                th.start();
                try {
                    th.join();
                } catch( InterruptedException e) {}
            } else {
                loadSVGImage();
            }
            assert m_svgImage != null;
            
            if ( m_elementId != null) {
                SVGSVGElement svg = (SVGSVGElement) m_svgImage.getDocument().getDocumentElement();
                SVGElement elem = PerseusController.hideAllButSubtree((ModelNode) svg, m_elementId);
                if (elem != null && elem instanceof SVGLocatableElement) {
                    m_exportedElement = (SVGLocatableElement) elem;
                    SVGRect bBox = m_exportedElement.getBBox();
                    SVGMatrix screenCTM = m_exportedElement.getScreenCTM();
                    float [][] coords = SVGObjectOutline.transformRectangle(bBox, (Transform) screenCTM, new float[4][2]);
                    Rectangle rect1 = SVGObjectOutline.getShapeBoundingBox(coords);
                    
                    // svg -> screen
                    SVGMatrix svgCTM = svg.getScreenCTM();

                    // element -> svg -> screen
                    SVGMatrix eltCTM = m_exportedElement.getScreenCTM();

                    // screen -> svg
                    SVGMatrix svgICTM = svgCTM.inverse();

                    // elt-> svg matrix
                    SVGMatrix eltToSvg = svgICTM.mMultiply(eltCTM);
            
                    coords = SVGObjectOutline.transformRectangle(m_exportedElement.getBBox(),
                            (Transform) eltToSvg, new float[4][2]);
                    Rectangle rect2 = SVGObjectOutline.getShapeBoundingBox(coords);
                    
                    bBox = new Box(rect2.x - 1, rect2.y - 1, rect2.width + 2, rect2.height + 2);
                            
                    svg.setRectTrait(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, bBox);
                    m_svgImage.setViewportWidth( Math.round(bBox.getWidth()));
                    m_svgImage.setViewportHeight( Math.round(bBox.getHeight()));
                }
            }
        }
        return m_svgImage;
    }
    
    public final J2MEProject getProject() {
        return m_project;
    }
    
    public final String getElementId() {
        return m_elementId;
    }
    
    protected static ComboBoxModel createImageTypeComboBoxModel() {
        ComboBoxModel model = new DefaultComboBoxModel( AnimationRasterizer.ImageType.values());
        return model;
    }    
    
    protected abstract void updateImage(JComponent source, boolean isOutputChanged);
}
    
    
