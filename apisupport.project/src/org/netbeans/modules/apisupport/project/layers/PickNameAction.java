/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

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
    
    private static String findBundlePath(NbModuleProject p, FileObject f) {
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
        String bundlePath = findBundlePath(p, f);
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
        return findBundlePath(p, f) != null;
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
