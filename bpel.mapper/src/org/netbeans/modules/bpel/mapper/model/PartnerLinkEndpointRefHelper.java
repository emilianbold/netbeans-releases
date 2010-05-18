/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.model.DefaultBpelModelUpdater.TreePathInfo;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.ServiceRef;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.openide.util.NbBundle;

/**
 * @author Alex Petrov (Alexey.Petrov@Sun.COM)
 */
public class PartnerLinkEndpointRefHelper {
    public static final String
        SERVICE_REF_NAME = "service-ref", // NOI18N
        ENDPOINT_REFERENCE = "EndpointReference", // NOI18N
        ENDPOINT_REFERENCE_TYPE = "EndpointReferenceType", // NOI18N
        NAME = "name", // NOI18N
        COMPOUND_NAME_SEPARATOR = "_", // NOI18N
        VARIABLE_NAME_SUBSTITUTE_CHAR = "-", // NOI18N
        PREFIX_GENERATED = "generated"; // NOI18N

    private static final String[] VARIABLE_NAME_FORBIDDEN_CHARS = {"."};

    private static Logger mLog = Logger.getLogger(
        PartnerLinkEndpointRefHelper.class.getName());

    private Assign updatedAssign;
    private BpelModelUpdater bpelModelUpdater;
    private Copy newGeneratedCopy;

    void setBpelModelUpdater(BpelModelUpdater bpelModelUpdater) {
        this.bpelModelUpdater = bpelModelUpdater;
    }

    void setUpdatedAssign(Assign updatedAssign) {
        this.updatedAssign = updatedAssign;
    }

    boolean isAvailable() {
        return (updatedAssign != null) && (bpelModelUpdater != null);
    }

    private SchemaComponent getEPRSchemaComponent(Object eprObjectRef) {
        if (eprObjectRef == null ) {
            return null;
        }

        if (eprObjectRef instanceof SchemaComponent) {
            return (SchemaComponent)eprObjectRef;
        }

        SchemaComponent sc = null;
        if (eprObjectRef instanceof Variable) {
            SchemaReference<? extends SchemaComponent> scRef = ((Variable)eprObjectRef).getElement();
            if (scRef == null) {
                scRef = ((Variable)eprObjectRef).getType();
            }
            return scRef == null ? null : scRef.get();
        }

        if (eprObjectRef instanceof BpelMapperPseudoComp) {
            return ((BpelMapperPseudoComp)eprObjectRef).getType();
        }
        return null;
    }

    From handlePartnerLinkEndpointRef(From from, XPathModel xPathModel,
        TreePathInfo tpInfo, TreePath rightTreePath, Object eprObjectRef) {
        if ((! isAvailable()) ||
            (from == null) || (xPathModel == null) || (tpInfo == null) ||
            (eprObjectRef == null)) {
            return null;
        }

        SchemaComponent scEndpointRef = getEPRSchemaComponent(eprObjectRef);
        if (scEndpointRef == null) {
            return null;
        }

        newGeneratedCopy = null;
        
        try {
            String rightNodeType = scEndpointRef instanceof GlobalType
                    ? ((GlobalType)scEndpointRef).getName()
                    : scEndpointRef.getPeer().getAttribute(
                Variable.TYPE);
            if (rightNodeType.endsWith(ENDPOINT_REFERENCE_TYPE)) {
                Variable newGeneratedVar = getNewGeneratedVariable(tpInfo, scEndpointRef);

                tpInfo.varDecl = newGeneratedVar;
                tpInfo.schemaCompList.clear();
                tpInfo.schemaCompList.addFirst(scEndpointRef);

                tpInfo.part = null;

                PartnerLink partnerLink = tpInfo.pLink;
                Roles roles = tpInfo.roles;

                tpInfo.pLink = null;
                tpInfo.roles = null;

                From modifiedFrom = bpelModelUpdater.populateFrom(from, xPathModel,
                    tpInfo, rightTreePath);

                addExtension(modifiedFrom, 
                    new XPathBpelVariable(newGeneratedVar, null).getExpressionString(),
                    scEndpointRef);

                generateNewCopy(partnerLink, roles, newGeneratedVar);

                return modifiedFrom;
            }
        } catch(Exception e) {/* just ignore */
            mLog.log(Level.WARNING, e.getMessage() == null ? 
                e.getClass().getName() : e.getMessage());
        }
        return null;
    }

    private Variable getNewGeneratedVariable(TreePathInfo tpInfo,
        SchemaComponent scEndpointReference) {
        if ((tpInfo == null) || (tpInfo.pLink == null)) return null;

        BpelModel bpelModel = bpelModelUpdater.getDesignContext().getBpelModel();
        BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
        Variable newGeneratedVar = elementBuilder.createVariable();

        VariableContainer variableContainer = getVariableContainer();
        variableContainer.insertVariable(newGeneratedVar, 0);

        List<Object> names = new ArrayList<Object>(2);
        names.add(tpInfo.pLink);
        names.add(tpInfo.pLink.MY_ROLE);
        try {
            String varName = getGeneratedObjectName(names, getExistingVarNames());

            // fix for issue 174240 - change forbidden characters in a variable name
            for (String forbiddenChar : VARIABLE_NAME_FORBIDDEN_CHARS) {
                if (varName.indexOf(forbiddenChar) > -1) {
                    varName = varName.replace(forbiddenChar, VARIABLE_NAME_SUBSTITUTE_CHAR);
                }
            }

            newGeneratedVar.setName(varName);
            newGeneratedVar.setElement(getGeneratedVariableElement(newGeneratedVar));

            addExtension(newGeneratedVar,
                new XPathBpelVariable(newGeneratedVar, null).getExpressionString(),
                scEndpointReference);
        } catch (Exception e) {
            mLog.log(Level.WARNING, e.getMessage() == null ?
                e.getClass().getName() : e.getMessage());
            return null;
        }
        return newGeneratedVar;
    }

    private String getGeneratedObjectName(List combinedNames, List<String> existingObjectNames) {
        if ((combinedNames == null) || (combinedNames.size() < 1)) return null;

        String baseName = getCompoundGeneratedName(combinedNames);

        int i = -1;
        while (true) {
            String varName = i < 0 ? baseName : baseName +
                COMPOUND_NAME_SEPARATOR + i;
            if (! existingObjectNames.contains(varName)) {
                return varName;
            }
            ++i;
            if (i > 1024) return null; // to prevent from an endless loop
        }
    }

    private List<String> getExistingVarNames() {
        VariableContainer variableContainer = getVariableContainer();
        Variable[] vars = variableContainer.getVariables();
        List<String> existingVarNames = new ArrayList<String>();
        if ((vars != null) && (vars.length > 0)) {
            for (Variable var : vars) {
                if (var == null) continue;
                String varName = var.getVariableName();
                if ((varName != null) && (varName.length() > 0)) {
                    existingVarNames.add(varName);
                }
            }
        }
        return existingVarNames;
    }

    private void generateNewCopy(PartnerLink partnerLink, Roles roles,
        Variable newGeneratedVar) {
        BpelModel bpelModel = updatedAssign.getBpelModel();
        BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
        try {
            newGeneratedCopy = elementBuilder.createCopy();

            From from = elementBuilder.createFrom();
            BpelReference<PartnerLink> pLinkRef = from.createReference(partnerLink, 
                PartnerLink.class);
            from.setPartnerLink(pLinkRef);
            if (roles != null) {
                from.setEndpointReference(roles);
            }
            newGeneratedCopy.setFrom(from);

            To to = elementBuilder.createTo();
            BpelReference<VariableDeclaration> varRef = to.createReference(
                newGeneratedVar, VariableDeclaration.class);
            to.setVariable(varRef);
            newGeneratedCopy.setTo(to);
        } catch (Exception e) {
            mLog.log(Level.WARNING, e.getMessage() == null ?
                e.getClass().getName() : e.getMessage());
        }
    }

    void insertNewGeneratedCopy() {
        if ((! isAvailable()) || (newGeneratedCopy == null)) return;

        updatedAssign.insertAssignChild(newGeneratedCopy, 0);

        // show a result info dialog
        String
            newGeneratedVarName =
                newGeneratedCopy.getTo().getVariable().get().getVariableName(),
            updatedAssignName = updatedAssign.getName(),
            infoMsg = NbBundle.getMessage (PartnerLinkEndpointRefHelper.class,
                "INFO_MSG_FINISH_HANDLING_OF_PARTNER_LINK_ENDPOINT_REF",
                new String[] {newGeneratedVarName, updatedAssignName}),
            dialogTitle = NbBundle.getMessage (PartnerLinkEndpointRefHelper.class,
                "DIALOG_TITLE_FINISH_HANDLING_OF_PARTNER_LINK_ENDPOINT_REF");

        JOptionPane.showMessageDialog(null, infoMsg, dialogTitle,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private VariableContainer getVariableContainer() {
        BpelModel bpelModel = bpelModelUpdater.getDesignContext().getBpelModel();
        Process process = bpelModel.getProcess();
        VariableContainer variableContainer = process.getVariableContainer();

        if (variableContainer == null) {
            BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
            variableContainer = elementBuilder.createVariableContainer();
            process.setVariableContainer(variableContainer);
            variableContainer = process.getVariableContainer();
        }
        return variableContainer;
    }

    private SchemaReference<GlobalElement> getGeneratedVariableElement(Variable var) {
        if (var == null) return null;

        BpelModel bpelModel = var.getBpelModel();
        try {
            checkServiceReferenceImport();
            bpelModel.getProcess().getNamespaceContext().addNamespace(
                ServiceRef.SERVICE_REF_NS);

            Collection<SchemaModel> schemaModels = SchemaReferenceBuilder.getSchemaModels(
                bpelModel, ServiceRef.SERVICE_REF_NS, false, true);
            SchemaReference<GlobalElement> schemaElementRef = null;
            if ((schemaModels != null) && (schemaModels.size() > 0)) {
                Iterator<SchemaModel> schemaIterator = schemaModels.iterator();
                while (schemaIterator.hasNext()) {
                    SchemaModel schemaModel = schemaIterator.next();
                    schemaElementRef = getSchemaElementServiceReference(var, schemaModel);
                    if (schemaElementRef != null) {
                        return schemaElementRef;
                    }
                }
            }
        } catch (InvalidNamespaceException ine) {
            mLog.log(Level.WARNING, ine.getMessage() == null ?
                ine.getClass().getName() : ine.getMessage());
        } catch (Exception e) {
            mLog.log(Level.SEVERE, e.getMessage() == null ?
                e.getClass().getName() : e.getMessage());
        }
        return null;
    }

    private SchemaReference<GlobalElement> getSchemaElementServiceReference(Variable var,
        SchemaModel schemaModel) {
        if ((var == null) || (schemaModel == null)) return null;

        String targetNS = schemaModel.getSchema().getTargetNamespace();
        if ((targetNS != null) && (targetNS.equals(ServiceRef.SERVICE_REF_NS))) {
            Collection<GlobalElement> globalElements =
                schemaModel.getSchema().getElements();
            Iterator<GlobalElement> elementIterator = globalElements.iterator();
            while (elementIterator.hasNext()) {
                GlobalElement globalElement = elementIterator.next();
                if (globalElement.getName().contains(SERVICE_REF_NAME)) {
                    SchemaReference<GlobalElement> schemaElementRef =
                        var.createSchemaReference(globalElement, GlobalElement.class);
                    return schemaElementRef;
                }
            }
        }
        return null;
    }

    private void checkServiceReferenceImport() {
        BpelModel bpelModel = updatedAssign.getBpelModel();
        BPELElementsBuilder bpelBuilder = bpelModel.getBuilder();
        Process process = bpelModel.getProcess();

        Import[] imports = process.getImports();
        if ((imports != null) && (imports.length > 0)) {
            for (Import imprt : imports) {
                if (imprt == null) continue;

                String importNS = imprt.getNamespace();
                if ((importNS != null) && (importNS.equals(ServiceRef.SERVICE_REF_NS))) {
                    return; // Service Reference import has been found
                }
            }
        }
        // add the required Service Refeference import
        try {
            Import importServiceRef = bpelBuilder.createImport();
            importServiceRef.setNamespace(ServiceRef.SERVICE_REF_NS);
            importServiceRef.setLocation(ServiceRef.SERVICE_REF_LOCATION);
            importServiceRef.setImportType(Import.SCHEMA_IMPORT_TYPE);

            process.addImport(importServiceRef);
        } catch (Exception e) {
            mLog.log(Level.WARNING, e.getMessage() == null ?
                e.getClass().getName() : e.getMessage());
        }
    }

    private void addExtension(ExtensibleElements extElement, String parentPath,
        SchemaComponent scEndpointReference) throws InvalidNamespaceException, VetoException {
        extElement.getNamespaceContext().addNamespace(Extensions.EDITOR2_EXT_URI);

        BPELElementsBuilder elementBuilder = extElement.getBpelModel().getBuilder();

        Editor extEditor = elementBuilder.createExtensionEntity(Editor.class);
        extElement.addExtensionEntity(Editor.class, extEditor);

        PseudoComp pseudoComp = elementBuilder.createExtensionEntity(PseudoComp.class);
        extEditor.addExtension(pseudoComp);

        pseudoComp.setSource(Source.FROM);
        pseudoComp.setParentPath(parentPath);

        //scEndpointReference or SchemaType or SchemaElement
        assert scEndpointReference instanceof TypeContainer
                || scEndpointReference instanceof GlobalType;
        SchemaReference<? extends GlobalType> schemaTypeRef = null;
        if (scEndpointReference instanceof TypeContainer) {
            schemaTypeRef =
                pseudoComp.createSchemaReference(
                ((TypeContainer) scEndpointReference).getType().get(),
                GlobalType.class);
        } else {
            schemaTypeRef =
                pseudoComp.createSchemaReference(
                (GlobalType)scEndpointReference,
                GlobalType.class);
        }

        pseudoComp.setType(schemaTypeRef);

        String prefix =
            extElement.getBpelModel().getProcess().getNamespaceContext().addNamespace(
            BpelEntity.WS_ADDRESSING_2004_08_NS_URI);

        pseudoComp.setName(new QName(BpelEntity.WS_ADDRESSING_2004_08_NS_URI,
            ENDPOINT_REFERENCE, prefix));
    }

    public static String getCompoundGeneratedName(List namedElements) {
        if ((namedElements == null) || (namedElements.size() < 1)) return null;

        StringBuilder buf = new StringBuilder(PREFIX_GENERATED);
        for (Object objNamedElement : namedElements) {
            if (objNamedElement == null) continue;

            String name = objNamedElement instanceof NamedElement ?
                ((NamedElement) objNamedElement).getName() : objNamedElement.toString();
            if ((name != null) && (name.length() > 0)) {
                buf.append(COMPOUND_NAME_SEPARATOR);
                buf.append(name);
            }
        }
        return buf.toString();
    }
}