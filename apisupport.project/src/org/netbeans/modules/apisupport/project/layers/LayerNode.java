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

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.openide.ErrorManager;
import org.openide.actions.EditAction;
import org.openide.actions.FileSystemAction;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

// XXX is there some way to hook into Check/Validate XML action
// so that layer-specific errors can be reported too?
// (e.g. duplicated folders...)

/**
 * Displays view of layer.
 * @author Jesse Glick
 */
public final class LayerNode extends FilterNode {
    
    private final DataObject layerXML;
    
    public LayerNode(LayerUtils.LayerHandle handle) {
        super(getRootNode(handle));
        try {
            layerXML = DataObject.find(handle.getLayerFile());
        } catch (DataObjectNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    private static Node getRootNode(LayerUtils.LayerHandle handle) {
        FileSystem fs = handle.layer();
        try {
            return DataObject.find(fs.getRoot()).getNodeDelegate();
        } catch (DataObjectNotFoundException e) {
            assert false : e;
            return Node.EMPTY;
        }
    }
    
    public String getName() {
        return layerXML.getPrimaryFile().toString();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(LayerNode.class, "LayerNode_label");
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == EditCookie.class) {
            return layerXML.getCookie(type);
        }
        return super.getCookie(type);
    }

    public Action[] getActions(boolean context) {
        Action[] orig = super.getActions(context);
        Action[] nue = new Action[orig.length + 2];
        nue[0] = SystemAction.get(EditAction.class);
        // XXX cannot add FileSystemAction directly, as it has the wrong DataObject... I think
        // Really want *some* actions to apply to the XML file, others (New, Reorder, ...) to the root folder
        // Should we resurrect the old UI of having a special "<root folder>" subnode? kind of ugly
        // XXX should also Check XML and Validate XML after a separator!
        System.arraycopy(orig, 0, nue, 2, orig.length);
        return nue;
    }

    public Action getPreferredAction() {
        return SystemAction.get(EditAction.class);
    }

    public Image getIcon(int type) {
        // XXX refire changes too, in case a badge appears or disappears
        return layerXML.getNodeDelegate().getIcon(type);
    }
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

}
