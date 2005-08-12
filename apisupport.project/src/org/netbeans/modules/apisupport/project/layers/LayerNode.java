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

import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.TreeDocumentRoot;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Displays view of layer.
 * @author Jesse Glick
 */
public final class LayerNode extends FilterNode {
    
    private final FileObject layerXML;
    
    public LayerNode(FileObject layerXML) {
        super(getRootNode(layerXML));
        this.layerXML = layerXML;
    }
    
    private static Node getRootNode(FileObject layerXML) {
        try {
            DataObject layerXMLD = DataObject.find(layerXML);
            TreeEditorCookie cookie = (TreeEditorCookie) layerXMLD.getCookie(TreeEditorCookie.class);
            if (cookie == null) {
                // Loaded by some other data loader, e.g. old apisupport?
                Util.err.log(ErrorManager.WARNING, "No TreeEditorCookie in " + layerXMLD);
                return Node.EMPTY;
            }
            FileSystem fs = new WritableXMLFileSystem(layerXML.getURL(), cookie);
            return DataObject.find(fs.getRoot()).getNodeDelegate();
        } catch (Exception e) {
            // DataObjectNotFoundException, IOException, TreeException
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return Node.EMPTY;
        }
    }
    
    public String getName() {
        return layerXML.toString();
    }
    
    public String getDisplayName() {
        return "XML Layer"; // XXX I18N
    }
    
}
