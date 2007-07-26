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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.xml.multiview.AbstractMultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Pavel Benes
 */
public class SVGViewMultiViewElement extends AbstractMultiViewElement {
    private static final long serialVersionUID = 7526471457562007148L;        
    
    //private final     int                 index;
    private transient SVGViewTopComponent svgView    = null;

    public SVGViewMultiViewElement(SVGDataObject obj) {
        super(obj);
//        this.index = index;
    }
    
    public void componentActivated() {
    }

    public void componentDeactivated() {        
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
        }
    }

    public void componentOpened() {
        super.componentOpened();
        if (svgView != null) {
            svgView.componentOpened();
        }
    }

    public void componentShowing() {
        svgView.onShow();
        dObj.setLastOpenView( SVGDataObject.SVG_VIEW_INDEX);
    }
    
    public Action[] getActions() {
        return dObj.getNodeDelegate().getActions(false);
    }
    
    public Lookup getLookup() {
        return new ProxyLookup( new Lookup[] {
            svgView.getLookup(),
            dObj.getNodeDelegate().getLookup()
        });
        //return dObj.getNodeDelegate().getLookup();        
    }

    public JComponent getToolbarRepresentation() {
        getVisualRepresentation();
        return svgView.getToolbar();
    }

    public synchronized JComponent getVisualRepresentation() {
        if (svgView == null) {
            svgView = new SVGViewTopComponent(getSceneManager());
            
        }
        return svgView;
    }
    
    private SceneManager getSceneManager() {
        return ((SVGDataObject) this.dObj).getSceneManager();
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
    
    /*
    private void showSVGSyntaxError(final String msg) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                dObj.openView(SVGDataObject.XML_VIEW_INDEX);
                DialogDisplayer.getDefault().notify(
                  new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE));
            }            
        });
    } 
     */       
}
