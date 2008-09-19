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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.List;

import org.netbeans.modules.xml.schema.model.SchemaModel;
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
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.xam.Component;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

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
    
    public Node create(Component comp) {
        if (comp instanceof WSDLComponent) {
            return create((WSDLComponent)comp);
        }
        return null;
    }
    
    public Node create(WSDLComponent component) {
        return new WSDLCreatorVisitor().createNode(component);
    }

    /**
     * Creates node tree to represent the given WSDL schema.
     *
     * @param  component  WSDL schema.
     * @return  node of WSDL schema.
     */
    private Node createSchemaNode(WSDLSchema component) {
        SchemaModel model = component.getSchemaModel();
        
        DataObject dobj = ActionHelper.getDataObject(component.getModel());
        Lookup lookup = new ProxyLookup(new Lookup[] {
                Lookups.exclude(dobj.getNodeDelegate().getLookup(), new Class[] {
                    Node.class,
                    DataObject.class,
                })
        });
        SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                model, lookup);
        Node node = factory.createRootNode();
        Node schemaRootNode = new EmbeddedSchemaNode(node, component, new InstanceContent());
        return schemaRootNode;
    }
    
    
    public Node createFilteredDefinitionNode(Definitions def, List<Class<? extends WSDLComponent>> filters) {
        return new DefinitionsNode(def, filters);
    }
    
    
    private class WSDLCreatorVisitor implements WSDLVisitor{
        
        Node result;
        
        public <C extends WSDLComponent> Node createNode(C component)
        {
            this.result = null;
            component.accept(this);
            return result;
        }

        public void visit(Definitions definition) {
            result = new DefinitionsNode(definition);
        }

        public void visit(Types types) {
            result = new TypesNode(types);
        }

        public void visit(Documentation doc) {
            result = new DocumentationNode(doc);
        }

        public void visit(Import imp) {
            Definitions defs = imp.getModel().getDefinitions();
            String location = imp.getLocation(); 
            if(location != null) {
                if(location.toLowerCase().endsWith(Constants.XSD_EXT)) {
                    result = new XSDImportNode(imp);
                } else if(location.toLowerCase().endsWith(Constants.WSDL_EXT)) {
                    result = new WSDLImportNode(imp);
                }
                
                if(result == null) {
                    
                    List list = defs.getModel().findSchemas(imp.getNamespace());
                    if (list != null && list.size() > 0) {
                        if (list.size() > 0) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception("Found more than one schemas for the targetnamespace"));
                        }
                        
                        result = new XSDImportNode(imp);
                    } else {
                        
                        List<WSDLModel> models = defs.getModel().findWSDLModel(imp.getNamespace());
                        
                        if (models != null && ! models.isEmpty()) {
                            
                        } else {
                            result = new ImportNode(new WSDLChildFactory(NodesFactory.getInstance(), imp), imp);
                        }
                    }
                }
                
                
            } else {
                result = new ImportNode(new WSDLChildFactory(NodesFactory.getInstance(), imp), imp);
            }
        }

        public void visit(Message message) {
            result = new MessageNode(message);
        }

        public void visit(Part part) {
            result = new PartNode(part);
        }

        public void visit(PortType portType) {
            result = new PortTypeNode(portType);
        }

        public void visit(OneWayOperation op) {
            result = new OneWayOperationNode(op);
        }

        public void visit(RequestResponseOperation op) {
            result = new RequestResponseOperationNode(op);
        }

        public void visit(NotificationOperation op) {
            result = new NotificationOperationNode(op);
        }

        public void visit(SolicitResponseOperation op) {
            result = new SolicitResponseOperationNode(op);
        }

        public void visit(Input in) {
            result = new OperationInputNode(in);
        }

        public void visit(Output out) {
            result = new OperationOutputNode(out);
        }

        public void visit(Binding binding) {
            result = new BindingNode(binding);
        }

        public void visit(BindingInput bi) {
            result = new BindingOperationInputNode(bi);
        }

        public void visit(BindingOutput bo) {
            result = new BindingOperationOutputNode(bo);
        }

        public void visit(BindingOperation bop) {
            result = new BindingOperationNode(bop);
        }

        public void visit(BindingFault bf) {
            result = new BindingOperationFaultNode(bf);
        }

        public void visit(Service service) {
            result = new ServiceNode(service);
        }

        public void visit(Port port) {
            result = new PortNode(port);
        }

        public void visit(Fault fault) {
            result = new OperationFaultNode(fault);
        }

        public void visit(ExtensibilityElement ee) {
            if (ee instanceof WSDLSchema) {
                result = createSchemaNode((WSDLSchema) ee);
            } else if (ee instanceof PartnerLinkType) {
                result = new PartnerLinkTypeNode((PartnerLinkType) ee);
            } else {
                result = new ExtensibilityElementNode<ExtensibilityElement>(ee);
            }
        }
        
    }

}
