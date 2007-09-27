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
package org.netbeans.modules.mobility.svgcore.view.svg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteFactory;
import org.netbeans.modules.xml.multiview.AbstractMultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Pavel Benes
 */
public final class SVGViewMultiViewElement extends AbstractMultiViewElement {
    private static final long serialVersionUID = 7526471457562007148L;        
    
    private transient SVGViewTopComponent svgView = null;

    public SVGViewMultiViewElement(SVGDataObject obj) {
        super(obj);
    }
    
    public void componentActivated() {
        if (svgView != null) {
            svgView.componentActivated();
        }
    }

    public void componentDeactivated() {        
        if (svgView != null) {
            svgView.componentDeactivated();
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        if (svgView != null) {
            svgView.componentClosed();
        }
    }

    public void componentHidden() {
        if (svgView != null) {
            svgView.componentHidden();
            getDataObject().setMultiViewElement(null);
        }
    }

    public void componentOpened() {
        super.componentOpened();
        getDataObject().getModel().attachToOpenedDocument();
        if (svgView != null) {
            svgView.componentOpened();
        }
    }

    public void componentShowing() {
        svgView.onShow();
        getDataObject().setMultiViewElement(this);
        dObj.setLastOpenView( SVGDataObject.SVG_VIEW_INDEX);
    }

    /*    
    public Action[] getActions() {
        return dObj.getNodeDelegate().getActions(false);
    }
     */    
    
    public Lookup getLookup() {
        Lookup palette = null;

        try {
            palette = Lookups.singleton( SVGPaletteFactory.getPalette());
        } catch( IOException e) {
            SceneManager.error("Palette could not be created.", e); //NOI18N
        }

        Lookup [] lookup;
        if (palette == null) {
            lookup = new Lookup[] { svgView.getLookup(), dObj.getNodeDelegate().getLookup()}; 
        } else {
            lookup = new Lookup[] { svgView.getLookup(), palette, dObj.getNodeDelegate().getLookup()}; 
        }
        return new ProxyLookup( lookup);
    }

    public JComponent getToolbarRepresentation() {
        getVisualRepresentation();
        return svgView.getToolbar();
    }

    public synchronized JComponent getVisualRepresentation() {
        if (svgView == null) {
            svgView = new SVGViewTopComponent((SVGDataObject) dObj);
            
        }
        return svgView;
    }
    
    private SceneManager getSceneManager() {
        return getDataObject().getSceneManager();
    }
    
    private SVGDataObject getDataObject() {
        return (SVGDataObject) this.dObj;
    }
    
    private void readObject(ObjectInputStream in) {
        try {
            in.defaultReadObject();
            getSceneManager().deserialize(in);        
        } catch( Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeObject(ObjectOutputStream out) {
        try {
            out.defaultWriteObject();
            getSceneManager().serialize(out);
        } catch( Exception e) {
            e.printStackTrace();
        }    
    }    
}
