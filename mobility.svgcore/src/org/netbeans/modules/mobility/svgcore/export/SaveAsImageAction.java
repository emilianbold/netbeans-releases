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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author suchys
 */
public class SaveAsImageAction extends CookieAction{
    
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
            J2MEProject project = null;
            final FileObject primaryFile = doj.getPrimaryFile ();
            Project p = FileOwnerQuery.getOwner (primaryFile);
            if (p != null && p instanceof J2MEProject){
                project = (J2MEProject) p;
            }
                
            SVGRasterizerPanel panel = new SVGRasterizerPanel(ScreenSizeHelper.getCurrentDeviceScreenSize(doj.getPrimaryFile(), null), project != null);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_ImageExport"));
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            int imageWidth = panel.getImageWidth();
            int imageHeigth = panel.getImageHeigth();
            boolean forAllConfig = panel.isForAllConfigurations();

            if (dd.getValue() == DialogDescriptor.OK_OPTION){
                ImageRasterizerHelper.export(doj.getPrimaryFile(), project, imageWidth, imageHeigth, 0.0f, 0.0f, 1, forAllConfig);
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
