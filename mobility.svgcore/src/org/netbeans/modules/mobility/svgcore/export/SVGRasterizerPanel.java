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

import com.sun.perseus.model.ModelNode;
import java.awt.Dimension;
import java.io.IOException;
import javax.microedition.m2g.SVGImage;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author  Pavel Benes
 */
public abstract class SVGRasterizerPanel extends javax.swing.JPanel implements AnimationRasterizer.Params {
    protected static final String SVG_VIEW_BOX_ATTRIBUTE = "viewBox";  
    
    protected final    SVGDataObject  m_dObj;
    protected final    String         m_elementId;
    protected final    J2MEProject    m_project;
    protected final    Dimension      m_dim;
    protected          double         m_ratio;      
    protected          int            overrideWidth = -1;
    protected          int            overrideHeight = -1;
    protected volatile boolean        m_updateInProgress = false;
    protected volatile SVGImage       m_svgImage;
        
    
    protected class SVGRasterizerComponentGroup extends ComponentGroup {
        public SVGRasterizerComponentGroup( Object [] comps) {
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
            if ( m_exportedElement != null) {
                SVGRect screenBBox = m_exportedElement.getScreenBBox();
                m_dim = new Dimension( (int) screenBBox.getWidth(), (int) screenBBox.getHeight());
            } else {
                m_dim = new Dimension( m_svgImage.getViewportWidth(), m_svgImage.getViewportHeight());
            }
        }
    }
    
    protected ComponentGroup createTimeGroup(  JSpinner spinner, JSlider slider,boolean isStart) {
        float duration = m_dObj.getSceneManager().getAnimationDuration();
        int p = Math.round(duration * 100);
        slider.setMinimum( 0);
        slider.setMaximum( p);
        slider.setValue(isStart ? 0 : p);        
        spinner.setModel( new SpinnerNumberModel( (double) (isStart ? 0 : duration), 0.0, duration, 1.0));
        return new SVGRasterizerComponentGroup( new JComponent[] { spinner, slider});
    }
    
    protected ComponentGroup createCompressionGroup(JComboBox combo, JSpinner spinner) {
        spinner.setModel( new SpinnerNumberModel( 0, 0, 99, 1));
        spinner.setValue( new Integer( AnimationRasterizer.COMPRESSION_LEVELS[AnimationRasterizer.DEFAULT_COMPRESSION_QUALITY]));
        combo.setSelectedIndex(AnimationRasterizer.DEFAULT_COMPRESSION_QUALITY);
        
        return new SVGRasterizerComponentGroup( new Object[] {
            createComboWrapper(combo),
            spinner
        });
    }
    
    protected final boolean isInProject() {
        return m_project != null;
    }
    
    protected ComponentGroup.ComponentWrapper createComboWrapper( JComboBox combo) {
        return  new ComponentGroup.ComponentWrapper(combo) {
            public float getValue() {
                int index = ((JComboBox) m_delegate).getSelectedIndex();
                return (float) AnimationRasterizer.COMPRESSION_LEVELS[index];
            }

            public void setValue(float value) {
                int i;
                int q = Math.round(value);
                for (i = 0; i < AnimationRasterizer.COMPRESSION_LEVELS.length; i++) {
                    if (AnimationRasterizer.COMPRESSION_LEVELS[i] >= q) {
                        ((JComboBox) m_delegate).setSelectedIndex(i);
                        return;
                    } 
                }
                System.err.println("Invalid value");
            }
        };
    }

    protected static String getSizeText( int size) {
        if ( size < 1024) {
            return size + " Bytes";
        } else if ( size < 1024 * 1024) {
            return (Math.round(size / 102.4) / 10.0) + " KBytes";
        } else {
            return (Math.round(size / (102.4 * 1024)) / 10.0) + " MBytes";
        }
    }
    
    protected static float roundTime(float f) {
        return Math.round( f * 100) / 100.0f;
    }
    
    public final void setImageWidth(int w) {
        overrideWidth = w;
    }

    public final void setImageHeight(int h) {
        overrideHeight = h;
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
    
    private SVGLocatableElement m_exportedElement = null;
    
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
                ModelNode svg = (ModelNode) m_svgImage.getDocument().getDocumentElement();
                SVGElement elem = PerseusController.hideAllButSubtree(svg, m_elementId);
                if (elem != null && elem instanceof SVGLocatableElement) {
                    m_exportedElement = (SVGLocatableElement) elem;
                    SVGRect bBox = m_exportedElement.getBBox();
                    System.out.println("BBox: " + bBox);
                    ((SVGSVGElement) svg).setRectTrait("viewBox", bBox);
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
    
    
