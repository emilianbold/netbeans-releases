/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.IndexableDataAbstractSetCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.openide.loaders.DataObject;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpPDatabindingCodeSupport {

    /**
     * Types of BinderProviders provideres a
     */
    public static enum ProviderType {

        DataField,
        ImageItem,
        Item,
        //   List,
        StringItem,
        TextField
    };

    /**
     * Types of BinderFeatures provideres a
     */
    public static enum FeatureType {

        DataField_FeatureInputDateTime,
        DataField_FEATURE_DATETIME,
        DataField_FEATURE_INPUTMODE,
        ImageItem_FEATURE_IMAGE,
        Item_FEATURE_LABEL,
        //    List,
        StringItem_FEATURE_TEXT,
        TextField_FeatureText
    };

    private MidpPDatabindingCodeSupport() {
    }

    public static String getCodeProviderNama(ProviderType type) {
        switch (type) {
            case DataField:
                return "new DateFieldBindingProvider()"; //NOI18N

            case ImageItem:
                return "new ImageItemBindingProvider()"; //NIOI18N

            case Item:
                return "new ItemBindingProvider()"; //NOI18N
            //  case List:
            //      return "ListBindingProvider()"; //NOI18N

            case StringItem:
                return "new StringItemBindingProvider()"; //NOI18N

            case TextField:
                return "new TextFieldBindingProvider()"; //NOI18N    

        }
        throw new IllegalArgumentException();
    }

    public static String getCodeFeatureName(FeatureType type) {

        final DataObject dao = ActiveViewSupport.getDefault().getActiveView().getContext().getDataObject();
        IOSupport.getDataObjectInteface(dao).getEditorDocument();

        switch (type) {
            case DataField_FEATURE_DATETIME:
                return "DateFieldBindingProvider.FEATURE_DATETIME"; //NOI18N

            case DataField_FEATURE_INPUTMODE:
                return "DateFieldBindingProvider.FEATURE_INPUTMODE"; //NOI18N

            case DataField_FeatureInputDateTime:
                return "new DateFieldBindingProvider.FeatureInputDateTime(false)"; //NIOI18N

            case ImageItem_FEATURE_IMAGE:
                return "ImageItemBindingProvider.FEATURE_IMAGE"; //NOI18N
            //   case List:
            //       return "ListBindingProvider()"; //NOI18N

            case Item_FEATURE_LABEL:
                return "ItemBindingProvider.FEATURE_LABEL"; //NOI18N

            case StringItem_FEATURE_TEXT:
                return "StringItemBindingProvider.FEATURE_TEXT"; //NOI18N   

            case TextField_FeatureText:
                return "new TextFieldBindingProvider.FeatureText(false)"; //NOI18N

        }
        throw new IllegalArgumentException();
    }

    public static Presenter createDataBinderRegisterCodePresenter(final String bindedProperty) {
        assert bindedProperty != null;
        return new CodeClassInitHeaderFooterPresenter() {

            @Override
            public void generateClassInitializationHeader(MultiGuardedSection section) {
            }

            @Override
            public void generateClassInitializationFooter(MultiGuardedSection section) {

                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector != null) {
                    String codeAccess = CodeReferencePresenter.generateAccessCode(connector.getParentComponent());
                    String directAccess = CodeReferencePresenter.generateDirectAccessCode(connector.getParentComponent());
                    section.getWriter().write("DataBinder.registerDataSet(" + codeAccess + ", \"" + directAccess + "\");"); //NOI18N

                }

            }
        };
    }

    public static Presenter createDataBinderBindCodePresenter(final String bindedProperty,
            final MidpPDatabindingCodeSupport.ProviderType providerType,
            final MidpPDatabindingCodeSupport.FeatureType featureType) {
        assert bindedProperty != null;
        assert providerType != null;
        assert featureType != null;

        return new CodeClassInitHeaderFooterPresenter() {

            @Override
            public void generateClassInitializationHeader(MultiGuardedSection section) {
            }

            @Override
            public void generateClassInitializationFooter(MultiGuardedSection section) {
                DesignComponent connecter = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connecter == null) {
                    return;
                }
                if (connecter != null) {
                    StringBuffer code = new StringBuffer();
                    code.append("\n"); //NOI18N
                    code.append("DataBinder.bind(").append("\""); //NOI18N
                    code.append(getExpression(connecter)).append("\", "); //NOI!8N
                    code.append(MidpPDatabindingCodeSupport.getCodeProviderNama(providerType)).append(", "); //NOI18N
                    code.append(CodeReferencePresenter.generateAccessCode(getComponent())).append(", "); //NOI!8N
                    code.append(MidpPDatabindingCodeSupport.getCodeFeatureName(featureType)).append(");"); //NOI18N
                    section.getWriter().write(code.toString());
                }

            }
        };
    }

    public synchronized static String generateCodeDatabindingEventSource(DesignComponent component, MultiGuardedSection section) {

        Collection<? extends MidpEventSourceCodeGenPresenter> presenters = DocumentSupport.gatherAllPresentersOfClass(component.getDocument(), MidpEventSourceCodeGenPresenter.class);

        for (MidpEventSourceCodeGenPresenter presenter : presenters) {
            presenter.generateCodeRegistry(section);
        }
        Collection<String> indexNames = new HashSet<String>();
        Collection<DesignComponent> connectors = MidpDatabindingSupport.getAllConnectors(component.getDocument());
        for (DesignComponent connector : connectors) {
            PropertyValue value = connector.readProperty(DataSetConnectorCD.PROP_INDEX_NAME);
            if (value != PropertyValue.createNull()) {
                indexNames.add((String) value.getPrimitiveValue());
            }
        }
        for (String indexName : indexNames) {
            section.getWriter().write("if (){");
            for (MidpEventSourceCodeGenPresenter presenter : presenters) {
                if (presenter.isValid(component, indexName)) {
                    presenter.generateCodeNext(section);
                }
            }
            section.getWriter().write("} else {");
            for (MidpEventSourceCodeGenPresenter presenter : presenters) {
                presenter.generateCodeNext(section);
            }
            section.getWriter().write("}");
        }
        return null;
    }

    public static Presenter createEventSourceCodeGenPresenter(final String bindedProperty,
            final String getterMethodName) {

        return new MidpEventSourceCodeGenPresenter() {

            public void generateCodeRegistry(MultiGuardedSection section) {
                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return;
                }
                section.getWriter().write("DataBinder.writeValue(\"" + getExpression(connector) + "\"," //NOI18N
                        + CodeReferencePresenter.generateAccessCode(getComponent()) + "." + getterMethodName + ");\n"); //NOI18N
                DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
                if (!registry.isInHierarchy(IndexableDataAbstractSetCD.TYPEID, connector.getParentComponent().getType())) {
                    return;
                }
            }

            public boolean isValid(DesignComponent eventSource, String indexName) {

                if (eventSource.getType() != CommandEventSourceCD.TYPEID) {
                    return false;
                }

                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return false;
                }
                PropertyValue value = connector.readProperty(DataSetConnectorCD.PROP_INDEX_NAME);
                if (value != PropertyValue.createNull() && value.getPrimitiveValue().equals(indexName)) {
                    return true;
                }
//                DesignComponent command = connector.readProperty(DataSetConnectorCD.PROP_UPDATE_COMMAND).getComponent();
//                if (command != null && command == eventSource.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent()) {
//                    return true;
//                }

                return false;
            }

            @Override
            public void generateCodeNext(MultiGuardedSection section) {
                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector.readProperty(DataSetConnectorCD.PROP_NEXT_COMMAND) != PropertyValue.createNull()) {
                    section.getWriter().write("NxtCommand set\n");
                }
            }

            @Override
            public void generateCodePrevious(MultiGuardedSection section) {
                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector.readProperty(DataSetConnectorCD.PROP_PREVIOUS_COMMAND) != PropertyValue.createNull()) {
                    section.getWriter().write("PreviousCommand set\n");
                }
            }
        };

    }

    public static Collection<Presenter> createDatabindingPresenters(String bindedProperty,
            String methodString,
            MidpPDatabindingCodeSupport.ProviderType providedType,
            MidpPDatabindingCodeSupport.FeatureType featureType) {
        assert bindedProperty != null;
        assert methodString != null;
        assert providedType != null;
        assert featureType != null;

        String fqnName = null;

        switch (providedType) {
            case DataField:
                fqnName = "org.netbeans.microedition.databinding.lcdui.DateFieldBindingProvider"; //NOI18N
                break;
            case ImageItem:
                fqnName = "org.netbeans.microedition.databinding.lcdui.ImageItemBindingProvider"; //NOI18N
                break;
            case Item:
                fqnName = "org.netbeans.microedition.databinding.lcdui.ItemBindingProvider"; //NOI18N
                break;
            //  case List:
            //  return "ListBindingProvider()"; //NOI18N
            case StringItem:
                fqnName = "org.netbeans.microedition.databinding.lcdui.StringItemBindingProvider"; //NOI18N
                break;
            case TextField:
                fqnName = "org.netbeans.microedition.databinding.lcdui.TextFieldBindingProvider"; //NOI18N  

        }
        String[] fqnNames = new String[]{fqnName};
        return Arrays.asList(
                MidpPDatabindingCodeSupport.createDataBinderRegisterCodePresenter(bindedProperty),
                MidpPDatabindingCodeSupport.createDataBinderBindCodePresenter(bindedProperty, providedType, featureType),
                MidpPDatabindingCodeSupport.createEventSourceCodeGenPresenter(bindedProperty, methodString),
                MidpCodePresenterSupport.createAddImportDatabindingPresenter(bindedProperty, fqnNames));
    }

    private static String getExpression(DesignComponent connector) {
        StringBuffer buffer = new StringBuffer();
        //buffer.append((String) connector.getParentComponent().readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue());
        //buffer.append(".");//NOI28N
        buffer.append((String) connector.readProperty(DataSetConnectorCD.PROP_EXPRESSION).getPrimitiveValue());
        return buffer.toString();

    }
}