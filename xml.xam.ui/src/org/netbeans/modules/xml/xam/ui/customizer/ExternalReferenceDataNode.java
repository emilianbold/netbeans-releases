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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.io.IOException;
import org.netbeans.modules.xml.xam.Model;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.openide.filesystems.FileObject;

/**
 * Represents a collection of external references, or a single file.
 *
 * @author Ajit Bhate
 */
public class ExternalReferenceDataNode extends FilterNode
        implements ExternalReferenceNode {
    /** Controls the appearance of this node. */
    private ExternalReferenceDecorator decorator;

    public ExternalReferenceDataNode(Node original, ExternalReferenceDecorator decorator) {
        this(original, new Children(original, decorator), decorator);
    }

    public ExternalReferenceDataNode(Node original, org.openide.nodes.Children children,
            ExternalReferenceDecorator decorator) {
        super(original, children);
        this.decorator = decorator;
    }

    public String getHtmlDisplayName() {
        String name = getOriginal().getHtmlDisplayName();
        if (decorator != null) {
            if (name == null) {
                name = getDisplayName();
            }
            name = decorator.getHtmlDisplayName(name, this);
        }
        return name;
    }

    public String getNamespace() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    Model model = cookie.getModel();
                    return decorator.getNamespace(model);
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public Model getModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    return cookie.getModel();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean hasModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            // Don't check for a model, as it may not be well-formed, and
            // this method is not checking for that, just that we should
            // have a model in the normal case.
            return cookie != null;
        }
        return false;
    }

    public void setDisplayName(String s) {
        super.disableDelegation(DELEGATE_GET_DISPLAY_NAME|DELEGATE_SET_DISPLAY_NAME);
        super.setDisplayName(s);
    }

    private static class Children extends FilterNode.Children {
        /** Controls the appearance of child nodes. */
        private ExternalReferenceDecorator decorator;

        public Children(Node original, ExternalReferenceDecorator decorator) {
            super(original);
            this.decorator = decorator;
        }

        protected Node[] createNodes(Object key) {
            Node n = (Node) key;
            DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
            FileObject fobj = dobj.getPrimaryFile();
            if (fobj.isFolder() && fobj.getNameExt().equals("nbproject")) {
                // May be the NetBeans project folder, see if it contains a
                // project.xml file, in which case we can be fairly certain.
                FileObject[] files = fobj.getChildren();
                for (FileObject f : files) {
                    if (f.getNameExt().equals("project.xml")) {
                        // Ignore the nbproject folder.
                        return new Node[0];
                    }
                }
            }
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            String fname = fobj.getNameExt();
            String ext = decorator.getDocumentType().toString();
            if (fobj.isFolder() || cookie != null && fname.endsWith(ext)) {
                return super.createNodes(key);
            }
            return new Node[0];
        }

        protected Node copyNode(Node node) {
            return new ExternalReferenceDataNode(node, decorator);
        }
    }
}
