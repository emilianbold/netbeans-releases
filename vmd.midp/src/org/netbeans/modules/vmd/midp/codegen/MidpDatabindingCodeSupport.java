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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.IndexableDataAbstractSetCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.items.DateFieldCD;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.StringItemCD;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpDatabindingCodeSupport {

//    /**
//     * Types of BinderProviders provideres a
//     */
//    public static enum ProviderType {
//
//        DataField,
//        ImageItem,
//        Item,
//        //   List,
//        StringItem,
//        TextField
//    };
    /**
     * Types of BinderFeatures provideres a
     */
    public static enum FeatureType {

        DateField_FeatureInputDateTime,
        DateField_FEATURE_DATETIME,
        DateField_FEATURE_INPUTMODE,
        ImageItem_FEATURE_IMAGE,
        Item_FEATURE_LABEL,
        //    List,
        StringItem_FEATURE_TEXT,
        TextField_FeatureText
    };

    private MidpDatabindingCodeSupport() {
    }

    private static String getCodeProviderNama(TypeID providerType) {
        String name = null;
        if (providerType == DateFieldCD.TYPEID) {
            name = "new DateFieldBindingProvider()"; //NOI18N
        } else if (providerType == ImageItemCD.TYPEID) {
            name = "new ImageItemBindingProvider()"; //NIOI18N
        } else if (providerType == ItemCD.TYPEID) {
            name = "new ItemBindingProvider()"; //NOI18N
        } else if (providerType == StringItemCD.TYPEID) {
            name = "new StringItemBindingProvider()"; //NOI18N
        } else if (providerType == TextFieldCD.TYPEID) {
            name = "new TextFieldBindingProvider()"; //NOI18N    

        }
        return name;
    }

    public static String getCodeFeatureName(FeatureType type) {

        //final DataObject dataObject = ActiveViewSupport.getDefault().getActiveView().getContext().getDataObject();
        //IOSupport.getDataObjectInteface(dataObject).getEditorDocument();

        switch (type) {
            case DateField_FEATURE_DATETIME:
                return "DateFieldBindingProvider.FEATURE_DATETIME"; //NOI18N

            case DateField_FEATURE_INPUTMODE:
                return "DateFieldBindingProvider.FEATURE_INPUTMODE"; //NOI18N

            case DateField_FeatureInputDateTime:
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

    public static String getCodeFeatureName(DesignDocument document, final TypeID type, final String propertyName) {
        DescriptorRegistry registry = document.getDescriptorRegistry();
//                if (! connector.getParentComponent().getType())) {
//                    
//                }


        if (type == DateFieldCD.TYPEID && propertyName.equals(DateFieldCD.PROP_INPUT_MODE)) {
            return "DateFieldBindingProvider.FEATURE_INPUTMODE"; //NOI18N
        } else if (type == DateFieldCD.TYPEID && propertyName.equals(DateFieldCD.PROP_DATE)) {
            return "new DateFieldBindingProvider.FeatureInputDateTime(false)"; //NIOI18N
        } else if (type == ImageItemCD.TYPEID && propertyName.equals(ImageItemCD.PROP_IMAGE)) {
            return "ImageItemBindingProvider.FEATURE_IMAGE"; //NOI18N
        } else if (registry.isInHierarchy(ItemCD.TYPEID, type) && propertyName.equals(ItemCD.PROP_LABEL)) {
            return "ItemBindingProvider.FEATURE_LABEL"; //NOI18N
        } else if (type == StringItemCD.TYPEID && propertyName.equals(StringItemCD.PROP_TEXT)) {
            return "StringItemBindingProvider.FEATURE_TEXT"; //NOI18N   
        } else if (type == TextFieldCD.TYPEID && propertyName.equals(TextFieldCD.PROP_TEXT)) {
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
            final TypeID providerType,
            final MidpDatabindingCodeSupport.FeatureType featureType) {
        assert bindedProperty != null;
        assert providerType != null;
        assert featureType != null;

        return new CodeClassInitHeaderFooterPresenter() {

            @Override
            public void generateClassInitializationHeader(MultiGuardedSection section) {
            }

            @Override
            public void generateClassInitializationFooter(MultiGuardedSection section) {
                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return;
                }
                if (connector != null) {
                    StringBuffer code = new StringBuffer();
                    code.append("\n"); //NOI18N
                    code.append("DataBinder.bind(").append("\""); //NOI18N
                    code.append(getExpression(connector, DataSetConnectorCD.PROP_EXPRESSION_READ)).append("\", "); //NOI!8N
                    code.append(MidpDatabindingCodeSupport.getCodeProviderNama(providerType)).append(", "); //NOI18N
                    code.append(CodeReferencePresenter.generateAccessCode(getComponent())).append(", "); //NOI!8N
                    code.append(MidpDatabindingCodeSupport.getCodeFeatureName(featureType)).append(");"); //NOI18N
                    code.append("\n"); //NOI18N
                    section.getWriter().write(code.toString());
                }

            }
        };
    }

    public synchronized static void generateCodeDatabindingEventSource(DesignComponent component, MultiGuardedSection section) {

        Collection<? extends MidpDatabindingEventSourceCodeGenPresenter> presenters = DocumentSupport.gatherAllPresentersOfClass(component.getDocument(), MidpDatabindingEventSourceCodeGenPresenter.class);

        for (MidpDatabindingEventSourceCodeGenPresenter presenter : presenters) {
            if (presenter.isValidRegistry(component)) {
                presenter.generateCodeRegistry(section);
            }
        }

        //Generate code for Indexable Nextr Previous command
        generateIndexableCode(section, component, DataSetConnectorCD.PROP_NEXT_COMMAND); //NOI18N
        generateIndexableCode(section, component, DataSetConnectorCD.PROP_PREVIOUS_COMMAND); //NOI18N

    }

    private static void generateIndexableCode(MultiGuardedSection section, DesignComponent component, String propertyNameCommand) {

        Map<DesignComponent, HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>>> map = getDataSetIndexMap(component, propertyNameCommand);
        for (DesignComponent dataSet : map.keySet()) {
            HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>> indexMap = map.get(dataSet);
            for (String indexName : map.get(dataSet).keySet()) {
                if (propertyNameCommand.equals(DataSetConnectorCD.PROP_NEXT_COMMAND)) {
                    section.getWriter().write("if (" + indexName + " < " + CodeReferencePresenter.generateDirectAccessCode(dataSet) + ".getSize() - 1) {\n"); //NOI18N
                    section.getWriter().write(indexName+"++;\n"); //NOI18N
                } else {
                    section.getWriter().write("if (" + indexName + " > 0) {\n"); //NOI18N
                    section.getWriter().write(indexName+"--;\n"); //NOI18N
                }
                for (Collection<MidpDatabindingEventSourceCodeGenPresenter> presenters : indexMap.values()) {
                    for (MidpDatabindingEventSourceCodeGenPresenter presenter : presenters) {
                        if (presenter.isValidIndexableCommands(component, indexName, dataSet, propertyNameCommand)) {
                            presenter.generateCodeIndexableCommands(section);
                        }
                    }
                }
                section.getWriter().write("}\n"); //NOI18N
            }
        }
    }

    private static Map<DesignComponent, HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>>> getDataSetIndexMap(DesignComponent component, String propertyNameCommand) {
        Collection<DesignComponent> connectors = MidpDatabindingSupport.getAllConnectors(component.getDocument());
        HashMap<DesignComponent, HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>>> dataSetMap = new HashMap<DesignComponent, HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>>>();
        for (DesignComponent connector : connectors) {
            String indexName = MidpDatabindingSupport.getIndexName(connector);
            Long uiComponentID = (Long) connector.readProperty(DataSetConnectorCD.PROP_COMPONENT_ID).getPrimitiveValue();
            DesignComponent uiComponent = component.getDocument().getComponentByUID(uiComponentID);
            Collection<? extends MidpDatabindingEventSourceCodeGenPresenter> presenters = null;
            if (uiComponent != null) {
                presenters = uiComponent.getPresenters(MidpDatabindingEventSourceCodeGenPresenter.class);
            }
            for (MidpDatabindingEventSourceCodeGenPresenter presenter : presenters) {
                if (presenter != null && presenter.isValidIndexableCommands(component, indexName, connector.getParentComponent(), propertyNameCommand)) {
                    if (dataSetMap.get(connector.getParentComponent()) == null) {
                        dataSetMap.put(connector.getParentComponent(), new HashMap<String, Collection<MidpDatabindingEventSourceCodeGenPresenter>>());
                    }
                    if (dataSetMap.get(connector.getParentComponent()).get(indexName) == null) {
                        dataSetMap.get(connector.getParentComponent()).put(indexName, new HashSet<MidpDatabindingEventSourceCodeGenPresenter>());
                        dataSetMap.get(connector.getParentComponent()).get(indexName).add(presenter);
                    } else {
                        dataSetMap.get(connector.getParentComponent()).get(indexName).add(presenter);
                    }
                }
            }
        }

        return dataSetMap;
    }

    public static Presenter createEventSourceCodeGenPresenter(final String bindedProperty, final String getterMethodName) {

        return new MidpDatabindingEventSourceCodeGenPresenter() {

            public void generateCodeRegistry(MultiGuardedSection section) {
                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return;
                }
                section.getWriter().write("DataBinder.writeValue(\"" + getExpression(connector, DataSetConnectorCD.PROP_EXPRESSION_WRITE) + "\"," //NOI18N
                        + CodeReferencePresenter.generateAccessCode(getComponent()) + "." + getterMethodName + ");\n"); //NOI18N
                DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
                if (!registry.isInHierarchy(IndexableDataAbstractSetCD.TYPEID, connector.getParentComponent().getType())) {
                    return;
                }
            }

            public boolean isValidRegistry(DesignComponent eventSource) {

                if (eventSource.getType() != CommandEventSourceCD.TYPEID) {
                    return false;
                }

                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return false;
                }

                DesignComponent command = connector.readProperty(DataSetConnectorCD.PROP_UPDATE_COMMAND).getComponent();
                if (command != null && command == eventSource.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent()) {
                    return true;
                }

                return false;
            }

            @Override
            public void generateCodeIndexableCommands(MultiGuardedSection section) {

                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return;
                }
                Long id = (Long) connector.readProperty(DataSetConnectorCD.PROP_COMPONENT_ID).getPrimitiveValue();
                TypeID providerType = connector.getDocument().getComponentByUID(id).getType();
                //if (propertyName.equals(DataSetConnectorCD.PROP_NEXT_COMMAND)) {

                StringBuffer code = new StringBuffer();
                code.append("DataBinder.bind(").append("\""); //NOI18N
                code.append(getExpression(connector, DataSetConnectorCD.PROP_EXPRESSION_READ)).append("\", "); //NOI!8N
                code.append(MidpDatabindingCodeSupport.getCodeProviderNama(providerType)).append(", "); //NOI18N
                code.append(CodeReferencePresenter.generateAccessCode(getComponent())).append(", "); //NOI!8N
                code.append(MidpDatabindingCodeSupport.getCodeFeatureName(connector.getDocument(), providerType, bindedProperty)).append(");\n"); //NOI18N
                section.getWriter().write(code.toString());

                //addImports(connector.getDocument(), "org.netbeans.microedition.databinding.lcdui.DateFieldBindingProvider");


            //} 
//                else if (propertyName.equals(DataSetConnectorCD.PROP_PREVIOUS_COMMAND)) {
//                    if (connector.readProperty(propertyName) != PropertyValue.createNull()) {
//                        DesignComponent c = connector.getDocument().getComponentByUID((Long) connector.readProperty(DataSetConnectorCD.PROP_COMPONENT_ID).getPrimitiveValue());
//                        section.getWriter().write(connector + "PreviousCommand set " + c + "\n");
//
//                    }
//                }
            }

            @Override
            public boolean isValidIndexableCommands(DesignComponent eventSource,
                    String indexName,
                    DesignComponent dataSet,
                    String propertyName) {
                if (eventSource.getType() != CommandEventSourceCD.TYPEID) {
                    return false;
                }

                DesignComponent connector = MidpDatabindingSupport.getConnector(getComponent(), bindedProperty);
                if (connector == null) {
                    return false;
                }

                DesignComponent command = connector.readProperty(propertyName).getComponent();
                if (command == null || command != eventSource.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent()) {
                    return false;
                }

                if (!MidpDatabindingSupport.getIndexName(connector).equals(indexName)) {
                    return false;
                }

                if (connector.getParentComponent() != dataSet) {
                    return false;
                }

                return true;

            }
        };

    }

    public static Collection<Presenter> createDatabindingPresenters(String bindedProperty,
            String methodString,
            TypeID providerType,
            MidpDatabindingCodeSupport.FeatureType featureType) {

        assert bindedProperty != null;
        assert methodString != null;
        assert providerType != null;
        assert featureType != null;


        String[] fqnNames = new String[]{getProviderType(providerType)};

        return Arrays.asList(
                MidpDatabindingCodeSupport.createDataBinderRegisterCodePresenter(bindedProperty),
                MidpDatabindingCodeSupport.createDataBinderBindCodePresenter(bindedProperty, providerType, featureType),
                MidpDatabindingCodeSupport.createEventSourceCodeGenPresenter(bindedProperty, methodString),
                MidpCodePresenterSupport.createAddImportDatabindingPresenter(bindedProperty, fqnNames));
    }

    private static String getProviderType(TypeID providerType) {
        String fqnName = null;
        if (providerType == DateFieldCD.TYPEID) {
            fqnName = "org.netbeans.microedition.databinding.lcdui.DateFieldBindingProvider"; //NOI18N
        } else if (providerType == ImageItemCD.TYPEID) {
            fqnName = "org.netbeans.microedition.databinding.lcdui.ImageItemBindingProvider"; //NOI18N
        } else if (providerType == ItemCD.TYPEID) {
            fqnName = "org.netbeans.microedition.databinding.lcdui.ItemBindingProvider"; //NOI18N
        } else if (providerType == StringItemCD.TYPEID) {
            fqnName = "org.netbeans.microedition.databinding.lcdui.StringItemBindingProvider"; //NOI18N
        } else if (providerType == TextFieldCD.TYPEID) {
            fqnName = "org.netbeans.microedition.databinding.lcdui.TextFieldBindingProvider"; //NOI18N  

        }
        return fqnName;
    }

    private static String getExpression(DesignComponent connector, String properytName) {
         
        String expression =((String) connector.readProperty(properytName).getPrimitiveValue());
        String indexName = MidpDatabindingSupport.getIndexName(connector);
        if (indexName != null) {
            String searchingString = "[" + indexName + "]"; //NOI18N
            if (expression.contains(searchingString)) { //NOI18N
                expression = expression.replace(searchingString, "[\"+" + indexName + "+\"]"); //NOI18N
            }
        }
        return expression;
    }
   
//    private static void addImports(DesignDocument document, final String fqn) {
//        DataObject dataObject = ProjectUtils.getDataObjectContextForDocument(document).getDataObject();
//        DataObjectInterface dataObjectInteface = IOSupport.getDataObjectInteface(dataObject);
//        final StyledDocument styledDocument = dataObjectInteface.getEditorDocument();
//        
//        try {
//            JavaSource.forDocument(styledDocument).runModificationTask(new CancellableTask<WorkingCopy>() {
//
//                public void cancel() {
//                }
//
//                public void run(WorkingCopy parameter) throws Exception {
//                    SourceUtils.resolveImport(parameter, new TreePath(parameter.getCompilationUnit()), fqn);
//                }
//            }).commit();
//        } catch (IOException e) {
//            Exceptions.printStackTrace(e);
//        }
//    }
}