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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.io.IOException;
import java.util.List;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.xam.Component;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class NodesFactory {
    
    private static NodesFactory mInstance = null;
    
    private NodesFactory() {
        
    }
    
    public static NodesFactory getInstance() {
        if(mInstance == null) {
            mInstance = new NodesFactory();
        }
        
        return mInstance;
    }
    
    public Node create(Component component) {
        if (component instanceof Definitions) {
            return new DefinitionsNode((Definitions) component);
        } else if (component instanceof Message) {
            return new MessageNode((Message) component);
        } else if (component instanceof Binding) {
            return new BindingNode((Binding) component);
        } else if (component instanceof PortType) {
            return new PortTypeNode((PortType) component);
        } else if (component instanceof Service) {
            return new ServiceNode((Service) component);
        } else if (component instanceof Types) {
            return new TypesNode((Types) component);
            
        } else if (component instanceof Import) {
            Definitions defs = ((Import) component).getModel().getDefinitions();
            Import imp = (Import) component;
            String location = imp.getLocation(); 
            Node node = null;
            if(location != null) {
                if(location.toLowerCase().endsWith(Constants.XSD_EXT)) {
                    node = new XSDImportNode(imp);
                } else if(location.toLowerCase().endsWith(Constants.WSDL_EXT)) {
                    node = new WSDLImportNode(imp);
                }
                
                if(node == null) {
                    
                    List list = defs.getModel().findSchemas(imp.getNamespace());
                    if (list != null && list.size() > 0) {
                        if (list.size() > 0) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception("Found more than one schemas for the targetnamespace"));
                        }
                        
                        node = new XSDImportNode(imp);
                    } else {
                        
                        List<WSDLModel> models = defs.getModel().findWSDLModel(imp.getNamespace());
                        
                        if (models != null && ! models.isEmpty()) {
                            
                        } else {
                            node = new ImportNode(Children.LEAF, imp);
                        }
                    }
                }
                
                
            } else {
                node = new ImportNode(Children.LEAF, imp);
            }
            
            return node;
        } else if(component instanceof BindingOperation) {
            return new BindingOperationNode((BindingOperation) component);
        } else if(component instanceof BindingInput) {
            return new BindingOperationInputNode((BindingInput) component);
        } else if(component instanceof BindingOutput) {
            return new BindingOperationOutputNode((BindingOutput) component);
        } else if(component instanceof BindingFault) {
            return new BindingOperationFaultNode((BindingFault) component);
        } else if(component instanceof Part) {
            return new PartNode((Part) component);
        } else if(component instanceof Input) {
            return new OperationInputNode((Input) component);
        } else if(component instanceof Output) {
            return new OperationOutputNode((Output) component);
        } else if(component instanceof Fault) {
            return new OperationFaultNode((Fault) component);
        } else if(component instanceof NotificationOperation) {
            Operation operation = (Operation) component;
            return new NotificationOperationNode(operation);
        } else if (component instanceof OneWayOperation) {
            Operation operation = (Operation) component;
            return new OneWayOperationNode(operation);
        } else if (component instanceof SolicitResponseOperation) {
            Operation operation = (Operation) component;
            return new SolicitResponseOperationNode(operation);
        } else if (component instanceof RequestResponseOperation) {
            Operation operation = (Operation) component;
            return new RequestResponseOperationNode(operation);
        } else if(component instanceof Port) {
            return new PortNode((Port) component);
        } else if (component instanceof WSDLSchema) {
            return createSchemaNode((WSDLSchema) component);
        } else if(component instanceof Documentation) {
            return new DocumentationNode((Documentation) component);
        } else if (component instanceof SchemaComponent) {
            // Those instances other than Schema, handled above.
            return createSchemaNode((SchemaComponent) component);
        } else if (component instanceof PartnerLinkType) {
            return new PartnerLinkTypeNode((PartnerLinkType) component);
        } else if (component instanceof ExtensibilityElement) {
            return new ExtensibilityElementNode((ExtensibilityElement) component);
        }
        return null;
    }

    /**
     * Creates node tree to represent the given schema component.
     *
     * @param  component  schema component.
     * @return  node of schema component.
     */
    private static Node createSchemaNode(SchemaComponent component) {
        SchemaModel model = component.getModel();
        SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                model, Lookup.EMPTY);
        if (component instanceof Schema) {
            return factory.createRootNode();
        }
        return factory.createNode(component);
    }

    /**
     * Creates node tree to represent the given WSDL schema.
     *
     * @param  component  WSDL schema.
     * @return  node of WSDL schema.
     */
    private static Node createSchemaNode(WSDLSchema component) {
        SchemaModel model = component.getSchemaModel();
        SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                model, Lookup.EMPTY);
        Node node = factory.createRootNode();
        return new SchemaRootNode(node, component);
    }

    /**
     * Wrapper for the schema root node to allow it to be deleted.
     *
     * @author  Nathan Fiedler
     */
    private static class SchemaRootNode extends FilterNode {
        /** The WSDL schema component. */
        private WSDLSchema component;

        public SchemaRootNode(Node node, WSDLSchema component) {
            super(node);
            this.component = component;
        }

        @Override
        public boolean canDestroy() {
            WSDLModel model = component.getModel();
            return model != null && model.getModelSource().isEditable();
        }

        public void destroy() throws IOException {
            SchemaComponentNode scn = (SchemaComponentNode) getOriginal().
                    getCookie(SchemaComponentNode.class);
            if (scn != null) {
                // Let the schema node do its cleanup.
                scn.destroy();
            }
            // Remove the schema root from the WSDL model.
            WSDLModel model = component.getModel();
            Types types = model.getDefinitions().getTypes();
            model.startTransaction();
            types.removeExtensibilityElement(component);
                model.endTransaction();
            super.destroy();
        }
    }
}
