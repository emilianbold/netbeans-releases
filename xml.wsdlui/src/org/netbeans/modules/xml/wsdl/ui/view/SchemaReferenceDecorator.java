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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.Collection;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * The ExternalReferenceDecorator for schema components.
 *
 * @author  Nathan Fiedler
 */
public class SchemaReferenceDecorator extends AbstractReferenceDecorator {
    /** The customizer that created this decorator. */
    private ImportSchemaCustomizer customizer;
    /** Used to generate unique namespace prefixes. */
    private int prefixCounter;

    /**
     * Creates a new instance of SchemaReferenceDecorator.
     *
     * @param  customizer  provides information about the edited component.
     */
    public SchemaReferenceDecorator(ImportSchemaCustomizer customizer) {
        this.customizer = customizer;
    }

    public String validate(ExternalReferenceNode node) {
        if (node.hasModel()) {
            Model model = node.getModel();
            if (model == null) {
                // If it is supposed to have a model, it must not be null.
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_ReferenceDecorator_NoModel");
            }
            
            if (model.getState() == Model.State.NOT_WELL_FORMED) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_ReferenceDecorator_NoModel");
            }
            
            //For wsdl, imported schema's should have a namespace.
            if (model instanceof SchemaModel) {
                String tns = Utility.getTargetNamespace((SchemaModel) model);
                if (tns == null) {
                    return NbBundle.getMessage(SchemaReferenceDecorator.class,
                    "LBL_ReferenceDecorator_NoNamespace");
                }
            }
            
            Model componentModel = customizer.getComponentModel();
            if (model.equals(componentModel)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_ReferenceDecorator_SameModel");
            }

            // It had better be a schema model, but check anyway.
            if (componentModel instanceof SchemaModel) {
                SchemaModel sm = (SchemaModel) componentModel;
                Schema schema = sm.getSchema();
                if (schema != null) {
                    Collection<SchemaModelReference> references =
                            schema.getSchemaReferences();
                    // Ensure the selected document is not already among the
                    // set that have been included.
                    for (SchemaModelReference ref : references) {
                        try {
                            SchemaModel otherModel = ref.resolveReferencedModel();
                            if (model.equals(otherModel)) {
                                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                                        "LBL_ReferenceDecorator_AlreadyRefd");
                            }
                        } catch (CatalogModelException cme) {
                            // Ignore that one as it does not matter.
                        }
                    }
                }
            }
        }
        String ns = node.getNamespace();
        String namespace = customizer.getTargetNamespace();
        if (customizer.mustNamespaceDiffer()) {
            // This is an import, which must have no namespace, or a
            // different one than the customized component.
            if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                    namespace.equals(ns)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                "LBL_ReferenceDecorator_SameNamespace");
            }
        } else {
            // This is an include or redefine, which must have no namespace,
            // or the same one as the customized component.
            if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                    !namespace.equals(ns)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_ReferenceDecorator_DifferentNamespace");
            }
        }
        return null;
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return customizer.createExternalReferenceNode(original);
    }

    protected String generatePrefix(Model model) {
        WSDLModel wModel = customizer.getModel();
        if (wModel != null) {
            return NameGenerator.getInstance().generateNamespacePrefix(
                    null, wModel, prefixCounter++);
        }
        return "";
    }

    public Utilities.DocumentTypesEnum getDocumentType() {
        return Utilities.DocumentTypesEnum.schema;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        if (validate(node) != null) {
            return "<s>" + name + "</s>";
        }
        return name;
    }

    public String getNamespace(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            return Utility.getTargetNamespace(sm);
        }
        return null;
    }
}
