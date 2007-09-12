/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bpel.properties.importchooser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.ExNamespaceContext;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A base import creator for a BPEL.
 *
 * @author  nk160297
 * @author  Nathan Fiedler
 */
public abstract class BpelImportCreator extends ExternalReferenceCreator<Process> {
    private ExternalReferenceDecorator decorator;
    
    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  defs  component to contain the import(s).
     */
    public BpelImportCreator(Process process) {
        super(process, null);
    }
    
    public void applyChanges() throws IOException {
        try {
            super.applyChanges();
            Process process = getModelComponent();
            BpelModel model = process.getBpelModel();
            List<Node> nodes = getSelectedNodes();
            for (Node node : nodes) {
                Import imp = model.getBuilder().createImport();
                //
                String namespace = getNamespace(node);
                if (mustNamespaceDiffer()) {
                    imp.setNamespace(namespace);
                }
                imp.setLocation(getLocation(node));
                switch (getImportType()) {
                    case schema:
                        imp.setImportType(Import.SCHEMA_IMPORT_TYPE);
                        break;
                    case wsdl:
                        imp.setImportType(Import.WSDL_IMPORT_TYPE);
                        break;
                }
                //
                // Save the prefix.
                ExNamespaceContext nsCont = process.getNamespaceContext();
                if (nsCont != null) {
                    String oldPrefix = nsCont.getPrefix(namespace);
                    String newPrefix = null;
                    if (node instanceof ExternalReferenceDataNode) {
                        newPrefix = ((ExternalReferenceDataNode) node).getPrefix();
                    }
                    //
                    if (oldPrefix == null || !oldPrefix.equals(newPrefix)) {
                        if (newPrefix == null || newPrefix.length() == 0) {
                            nsCont.addNamespace(namespace);
                        } else {
                            nsCont.addNamespace(newPrefix, namespace);
                        }
                    }
                }
                //
                process.addImport(imp);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            IOException ioEx = new IOException();
            ioEx.initCause(ex);
            throw ioEx;
        }
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // return new HelpCtx(ImportWSDLCreator.class);
    }
    
    protected String getTargetNamespace(Model model) {
        return Util.getTargetNamespace(model);
    }
    
    protected Map<String, String> getPrefixes(Model model) {
        if (model != null) {
            assert model instanceof BpelModel;
            BpelModel bpelModel = (BpelModel)model;
            return ((AbstractDocumentComponent)bpelModel.getProcess()).getPrefixes();
        }
        return new HashMap();
    }
    
    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new ReferenceDecorator(getModelComponent(),
                    this, getImportType());
        }
        return decorator;
    }
    
    public abstract Utilities.DocumentTypesEnum getImportType();
    
    public boolean mustNamespaceDiffer() {
        return true;
    }
    
    protected String referenceTypeName() {
          return NbBundle.getMessage(BpelImportCreator.class,
              "LBL_WSDLFileImportDialog_Type");
    }
    
}
