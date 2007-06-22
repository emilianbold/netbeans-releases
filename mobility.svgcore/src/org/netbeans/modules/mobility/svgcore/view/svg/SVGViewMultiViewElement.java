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

import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.xml.multiview.AbstractMultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Pavel Benes
 */
public class SVGViewMultiViewElement extends AbstractMultiViewElement {
    private static final long serialVersionUID = 7526471457562007148L;        
    
    private final SVGViewTopComponent svgView;
    private final int                 index;

    public SVGViewMultiViewElement(SVGDataObject obj, int index) {
        super(obj);
        this.index = index;
        svgView = SVGViewTopComponent.findInstance(obj);
    }
    
    public void componentActivated() {
    }

    public void componentDeactivated() {        
    }
    
    public void componentClosed() {
        super.componentClosed();
        svgView.componentClosed();
    }

    public void componentHidden() {
        svgView.componentHidden();
    }

    public void componentOpened() {
        super.componentOpened();
        svgView.componentOpened();
    }

    public void componentShowing() {
        svgView.onShow();
        dObj.setLastOpenView( index );
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
        return svgView.getToolbar();
    }

    public JComponent getVisualRepresentation() {
        return svgView;
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
