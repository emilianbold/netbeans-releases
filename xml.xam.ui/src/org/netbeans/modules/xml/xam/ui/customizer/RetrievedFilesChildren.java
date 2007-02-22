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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.customizer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Ajit Bhate
 */
public class RetrievedFilesChildren extends Children.Keys {
    private CatalogWriteModel cwm;
    private ExternalReferenceDecorator decorator;

    /** Creates a new instance of Children */
    public RetrievedFilesChildren(CatalogWriteModel cwm,
            ExternalReferenceDecorator decorator) {
        super();
        this.cwm = cwm;
        this.decorator = decorator;
    }

    protected Node[] createNodes(Object key) {
        if (key == WaitNode.WAIT_KEY) {
            return WaitNode.createNode();
        } else if (key instanceof CatalogEntry) {
            CatalogEntry entry = (CatalogEntry) key;
            try {
                ModelSource modelSource = cwm.getModelSource(new URI(entry.getSource()));
                if (modelSource == null) {
                    return new Node[0];
                }
                FileObject fobj = (FileObject) modelSource.
                        getLookup().lookup(FileObject.class);
                if (fobj == null) {
                    return new Node[0];
                }
                DataObject dobj = DataObject.find(fobj);
                if (dobj == null) {
                    return new Node[0];
                }
                ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
                if (cookie == null) {
                    return new Node[0];
                }
                Model model = cookie.getModel();
                if (model == null) {
                    return new Node[0];
                }
                String targetNS = decorator.getNamespace(model);
                RetrievedFileNode childNode = new RetrievedFileNode(entry,
                        targetNS, Children.LEAF, decorator);
                childNode.setDisplayName(entry.getSource());
                Children.Array children = new Children.Array();
                children.add(new Node[] { childNode });
                return new Node[] {
                    new RetrievedFileNode(entry, targetNS, children, decorator)
                };
            } catch (URISyntaxException urise) {
            } catch (CatalogModelException cme) {
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioe) {
            }
        }
        return new Node[0];
    }

    protected void addNotify() {
        setKeys(WaitNode.getKeys());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                setKeys(cwm.getCatalogEntries());
            }
        });
    }

    public static class RetrievedFileNode extends AbstractNode
            implements ExternalReferenceNode {
        private boolean valid;
        private String location;
        private String targetNS;
        private ExternalReferenceDecorator decorator;

        /**
         * Creates a new instance of RetrievedFileNode.
         */
        RetrievedFileNode(CatalogEntry entry, String targetNS,
                org.openide.nodes.Children children,
                ExternalReferenceDecorator decorator) {
            super(children);
            this.location = entry.getSource();
            this.valid = entry.isValid();
            this.targetNS = targetNS;
            this.decorator = decorator;
            setName(targetNS);
            if (targetNS == null) {
                setDisplayName(NbBundle.getMessage(RetrievedFileNode.class,
                        "LBL_NoTargetNamespace"));
            }
            setIconBaseWithExtension(
                    "org/netbeans/modules/xml/xam/ui/customizer/Schema_File.png"); // NOI18N
        }

        public String getLocation() {
            return location;
        }

        public String getNamespace() {
            return targetNS;
        }

        public boolean isValid() {
            return valid;
        }

        public String getHtmlDisplayName() {
            String name = super.getHtmlDisplayName();
            if (isValid()) {
                if (decorator != null) {
                    if (name == null) {
                        name = getDisplayName();
                    }
                    name = decorator.getHtmlDisplayName(name, this);
                }
            } else {
                return "<s>" + name == null ? getDisplayName() : name + "</s>";
            }
            return name;
        }

        public Model getModel() {
            return null;
        }

        public boolean hasModel() {
            return false;
        }
    }
}
