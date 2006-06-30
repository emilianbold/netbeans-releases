/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Lets user pick a localized display name for a given layer file.
 * @author Jesse Glick
 */
public class PickNameAction extends CookieAction {
    
    private static FileObject findFile(Node[] activatedNodes) {
        return ((DataObject) activatedNodes[0].getCookie(DataObject.class)).getPrimaryFile();
    }
    
    private static NbModuleProject findProject(FileObject f) {
        URL location = (URL) f.getAttribute("WritableXMLFileSystem.location"); // NOI18N
        if (location == null) {
            return null;
        }
        NbModuleProject p = (NbModuleProject) FileOwnerQuery.getOwner(URI.create(location.toExternalForm()));
        assert p != null : location;
        return p;
    }
    
    private static String findBundlePath(NbModuleProject p) {
        FileObject src = p.getSourceDirectory();
        ManifestManager mm = ManifestManager.getInstance(p.getManifest(), false);
        String bundlePath = mm.getLocalizingBundle();
        if (bundlePath != null && bundlePath.endsWith(".properties") && src.getFileObject(bundlePath) != null) {
            return bundlePath;
        } else {
            return null;
        }
    }
    
    protected void performAction(Node[] activatedNodes) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(PickNameAction.class, "PickNameAction_dialog_label"),
                NbBundle.getMessage(PickNameAction.class, "PickNameAction_dialog_title"));
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        FileObject f = findFile(activatedNodes);
        NbModuleProject p = findProject(f);
        String bundlePath = findBundlePath(p);
        try {
            FileObject properties = p.getSourceDirectory().getFileObject(bundlePath);
            EditableProperties ep = Util.loadProperties(properties);
            ep.setProperty(f.getPath(), name);
            Util.storeProperties(properties, ep);
            f.setAttribute("SystemFileSystem.localizingBundle", bundlePath.substring(0, bundlePath.length() - ".properties".length()).replace('/', '.')); // NOI18N
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        FileObject f = findFile(activatedNodes);
        if (f == null) {
            return false;
        }
        NbModuleProject p = findProject(f);
        if (p == null) {
            return false;
        }
        return findBundlePath(p) != null;
    }

    public String getName() {
        return NbBundle.getMessage(PickIconAction.class, "LBL_pick_name");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {DataObject.class};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
