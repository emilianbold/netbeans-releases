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
 * SaveAsImage.java
 *
 * Created on November 22, 2005, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dialog;
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
public final class SaveAsImageAction extends CookieAction{
    
    /** Creates a new instance of SaveAsImage */
    public SaveAsImageAction() {
    }

    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    protected void performAction(Node[] n) {
        SVGDataObject doj = (SVGDataObject) n[0].getLookup().lookup(SVGDataObject.class);
        if (doj != null){       
            try {
                SVGImageRasterizerPanel panel = new SVGImageRasterizerPanel(doj, null);
                DialogDescriptor        dd    = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_ImageExport"));

                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                SaveAnimationAsImageAction.setDialogMinimumSize(dlg);
                dlg.setVisible(true);

                if (dd.getValue() == DialogDescriptor.OK_OPTION){
                    AnimationRasterizer.export(doj, (AnimationRasterizer.Params) panel);
                }
            } catch( Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(SaveAsImageAction.class, "LBL_ExportAction");
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
