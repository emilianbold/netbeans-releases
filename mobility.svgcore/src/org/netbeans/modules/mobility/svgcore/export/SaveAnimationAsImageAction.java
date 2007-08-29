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
 * SaveAnimationAsImage.java
 *
 * Created on November 22, 2005, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Pavel Benes, suchys
 */
public class SaveAnimationAsImageAction extends CookieAction {
    
    /** Creates a new instance of SaveAnimationAsImage */
    public SaveAnimationAsImageAction() {
    }

    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
        
    public static void setDialogMinimumSize(final Dialog dlg) {
        dlg.pack();
        dlg.setSize( dlg.getPreferredSize());
        
        dlg.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = dlg.getWidth();
                int h = dlg.getHeight();
                final Dimension minSize = dlg.getPreferredSize();

                int _w = Math.max( w, minSize.width);
                int _h = Math.max( h, minSize.height);

                if ( w != _w || h != _h) {
                    dlg.setSize( new Dimension(_w, _h));
                }
            }
        });
    }
    
    protected void performAction(Node[] n) {
        SVGDataObject doj = (SVGDataObject) n[0].getLookup().lookup(SVGDataObject.class);
        if (doj != null){   
            try {
                SVGAnimationRasterizerPanel panel = new SVGAnimationRasterizerPanel(doj);
                DialogDescriptor            dd    = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExport"));

                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                setDialogMinimumSize( dlg);
                dlg.setVisible(true);

                if (dd.getValue() == DialogDescriptor.OK_OPTION){
                    AnimationRasterizer.export(doj, panel);
                }
            } catch( Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(SaveAnimationAsImageAction.class, "LBL_ExportAnimationAction"); //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }    

    protected int mode() {
        return CookieAction.MODE_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[] {
            SVGDataObject.class
        };
    }

    protected boolean asynchronous() {
        return false;
    }
}
