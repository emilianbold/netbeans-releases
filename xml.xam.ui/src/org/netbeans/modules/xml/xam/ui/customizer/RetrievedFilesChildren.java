/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.EventQueue;
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
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setKeys(cwm.getCatalogEntries());
            }
        });
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.emptySet());
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
