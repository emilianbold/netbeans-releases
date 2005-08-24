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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.actions.EditAction;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

// XXX is there some way to hook into Check/Validate XML action
// so that layer-specific errors can be reported too?
// (e.g. duplicated folders...)

// XXX maybe rather display raw FS as a subnode, and another subnode for FS in system FS context?

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
        handle.setAutosave(true);
    }
    
    private static Node getRootNode(LayerUtils.LayerHandle handle) {
        FileObject layer = handle.getLayerFile();
        FileSystem fs = badge(handle.layer(), createClasspath(layer), layer);
        try {
            return DataObject.find(fs.getRoot()).getNodeDelegate();
        } catch (DataObjectNotFoundException e) {
            assert false : e;
            return Node.EMPTY;
        }
    }
    
    /**
     * Add badging support to the plain layer.
     */
    private static FileSystem badge(final FileSystem base, final ClassPath cp, final FileObject layer) {
        class BadgingMergedFileSystem extends MultiFileSystem {
            private final BadgingSupport status;
            public BadgingMergedFileSystem() {
                super(new FileSystem[] {base});
                status = new BadgingSupport(this);
                status.addFileStatusListener(new FileStatusListener() {
                    public void annotationChanged(FileStatusEvent ev) {
                        fireFileStatusChanged(ev);
                    }
                });
                status.setClasspath(cp);
                // XXX listening?
                // XXX loc/branding suffix?
            }
            public FileSystem.Status getStatus() {
                return status;
            }
            public String getDisplayName() {
                return FileUtil.getFileDisplayName(layer);
            }
        }
        return new BadgingMergedFileSystem();
        /* XXX loc/branding suffix possibilities:
        Matcher m = Pattern.compile("(.*" + "/)?[^_/.]+(_[^/.]+)?(\\.[^/]+)?").matcher(u);
        assert m.matches() : u;
        suffix = m.group(2);
        if (suffix == null) {
            suffix = "";
        }
        status.setSuffix(suffix);
         */
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

    /**
     * Make a runtime classpath indicative of what is accessible from a sample resource.
     */
    private static ClassPath createClasspath(FileObject resource) {
        ClassPath srcCP = ClassPath.getClassPath(resource, ClassPath.SOURCE);
        if (srcCP == null) {
            return null;
        }
        FileObject[] roots = srcCP.getRoots();
        ClassPath source = ClassPathSupport.createClassPath(roots);
        if (roots.length == 1) {
            // XXX exec cp doesn't work too well in practice. Use LayerUtils.getPlatformJarsFor*Project
            // when that also supports netbeans.org modules. That will enable us to display proper menu
            // names etc. for menus defined in some infrastructure module, which is nice.
            ClassPath execCP = ClassPath.getClassPath(roots[0], ClassPath.EXECUTE);
            if (execCP != null) {
                return ClassPathSupport.createProxyClassPath(new ClassPath[] {source, execCP});
            }
        }
        return source;
    }

}
