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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.openide.util.HelpCtx;

/**
 * An import customizer.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class ImportWSDLCustomizer extends ExternalReferenceCustomizer<Import> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  _import  component to customize.
     */
    public ImportWSDLCustomizer(Import _import) {
        super(_import, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Import _import = getModelComponent();
        if (isLocationChanged()) {
            // Save the file location.
            _import.setLocation(getEditedLocation());
        }

        String namespace = getEditedNamespace();
        if (mustNamespaceDiffer() && isNamespaceChanged()) {
            // Save the namespace.
            _import.setNamespace(namespace);
        }

        WSDLModel model = getModelComponent().getModel();
        if (mustNamespaceDiffer() && isPrefixChanged()) {
            // Save the prefix.
            String prefix = getEditedPrefix();
            if (prefix.length() > 0) {
                // Should not have to cast, but Definitions does not
                // expose the prefixes API.
                AbstractDocumentComponent def =
                        (AbstractDocumentComponent) model.getDefinitions();
                def.addPrefix(prefix, namespace);
            }
        }
    }

    protected String getReferenceLocation() {
        Import _import = getModelComponent();
        return _import.getLocation();
    }

    protected String getNamespace() {
        Import _import = getModelComponent();
        return _import.getNamespace();
    }

    protected String getPrefix() {
        Import _import = getModelComponent();
        String namespace = _import.getNamespace();
        WSDLModel model = getModelComponent().getModel();
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) model.getDefinitions();
        Map<String, String> prefixMap = def.getPrefixes();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(namespace)) {
                return entry.getKey();
            }
        }
// The alternative to casting to AbstractDocumentComponent is to attempt
// to collect the prefixes from the imports.
//        Collection<Import> imports = model.getDefinitions().getImports();
//        for (Import entry : imports) {
//            String ns = entry.getNamespace();
//            if (ns != null && ns.equals(namespace)) {
//                return entry.getNamespace();
//            }
//        }
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportWSDLCustomizer.class);
    }

    protected String getTargetNamespace(Model model) {
        return ((WSDLModel) model).getDefinitions().getTargetNamespace();
    }

    protected Map<String, String> getPrefixes(Model model) {
        WSDLModel wm = (WSDLModel) model;
        AbstractDocumentComponent def =
                (AbstractDocumentComponent) wm.getDefinitions();
        return def.getPrefixes();
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new WSDLReferenceDecorator(this);
        }
        return decorator;
    }

    protected String generatePrefix() {
        WSDLModel model = getModelComponent().getModel();
        return NameGenerator.getInstance().generateNamespacePrefix(null, model);
    }

    public boolean mustNamespaceDiffer() {
        return true;
    }
}
