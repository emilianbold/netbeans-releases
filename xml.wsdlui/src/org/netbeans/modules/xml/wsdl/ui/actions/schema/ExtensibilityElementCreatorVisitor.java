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

/*
 * NodeChildrenCreatorVisitor.java
 *
 * Created on March 28, 2006, 6:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.actions.schema;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.AbstractXSDVisitor;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfiguratorFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class ExtensibilityElementCreatorVisitor extends AbstractXSDVisitor {
    
    private WSDLComponent mComponent;
    
    private WSDLModel mModel;
    
    private Definitions mDefinitions;
    
    private Stack<WSDLComponent> mStack = new Stack<WSDLComponent>();
    
    private ExtensibilityElement mRootExtensibilityElement;
    
    
    /** Creates a new instance of ExtensibilityElementCreatorVisitor */
    public ExtensibilityElementCreatorVisitor(WSDLComponent component) {
        this.mComponent = component;
        //push the component to stack so when we visit
        //schema element we can add extensibilty element
        //crated for schema element to it
        push(this.mComponent);

        this.mModel = component.getModel();
        this.mDefinitions = this.mModel.getDefinitions();
        
    }
    
    public ExtensibilityElement getRootExtensibilittElement() {
        return this.mRootExtensibilityElement;
    }
    
    @Override  
    public void visit(LocalAttribute la) {
        Attribute.Use use = la.getUseEffective();
        if(use.equals(Attribute.Use.REQUIRED)) {
            NamedComponentReference<GlobalSimpleType>  lstRef = la.getType();
            if(lstRef != null)  {
                visitAttribute(la, la.getName(), lstRef.get());
            } else {
                visitAttribute(la, la.getName(), la.getInlineType());
            }
        }
    }
    
    @Override   
    public void visit(AttributeReference reference) {
        Attribute.Use use = reference.getUseEffective();
        if(use.equals(Attribute.Use.REQUIRED)) {
            NamedComponentReference<GlobalAttribute> ga = reference.getRef();
            if(ga != null) {
                visit(ga.get());
            }
        }
    }
    
    @Override   
    public void visit(GlobalAttribute ga) {
        NamedComponentReference<GlobalSimpleType>  gstRef = ga.getType();
        if(gstRef != null)  {
            visitAttribute(ga, ga.getName(), gstRef.get());
        } else {
            visitAttribute(ga, ga.getName(), ga.getInlineType());
        }
    }
    
    @Override   
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> aGroup = agr.getGroup();
        if(aGroup != null) {
            visit(aGroup.get());
        }
    }
    
    
    @Override   
    public void visit(GlobalAttributeGroup gag) {
        List<SchemaComponent> children = gag.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
        
        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof  LocalAttribute) {
                visit((LocalAttribute) sc);
            } else if(sc instanceof AttributeReference) {
                visit((AttributeReference) sc );
            } else if(sc instanceof  AttributeGroupReference) {
                visit((AttributeGroupReference) sc);
            }
        }
        
    }
    
//  public void visit(AllElement ae) {
//  Occur.ZeroOne oc = ae.getMinOccursEffective();
//  if(oc != null && oc.equals(Occur.ZeroOne.ONE)) {
//  String namespace = ae.getSchemaModel().getSchema().getTargetNamespace();
//  visit(ae, ae.getName(), namespace);
//  }
//  }
//  
//  public void visit(AllElementReference allElementReference) {
//  Occur.ZeroOne oc = allElementReference.getMinOccursEffective();
//  if(oc != null && oc.equals(Occur.ZeroOne.ONE)) {
//  NamedComponentReference<GlobalElement> geRef = allElementReference.getRef();
//  if(geRef != null && geRef.get() != null) {
//  visit(geRef.get());
//  }
//  }
//  }
    
    @Override  
    public void visit(ElementReference er) {
        int minOccurs = er.getMinOccursEffective();
        //if this is top level object or min occur is > 0 then visit
        //top leaving meaning this is the entry point for
        //visitor and stack have the wsdl component for 
        //which we are creating extensibility element
        if(mStack.size() == 1 || minOccurs > 0) {
            NamedComponentReference<GlobalElement> geRef = er.getRef();
            if(geRef != null && geRef.get() != null) {
                visit(geRef.get());
            }
        }
    }
    
    @Override   
    public void visit(LocalElement le) {
        int minOccurs = le.getMinOccursEffective();
        //if this is top level object or min occur is > 0 then visit
        if(mStack.size() == 1 || minOccurs > 0) {
            String namespace = Utility.getTargetNamespace(le.getModel());
            visit(le, le.getName(), namespace);
        }
    }
    
    @Override    
    public void visit(GlobalElement ge) {
        String namespace = Utility.getTargetNamespace(ge.getModel());
        visit(ge, ge.getName(), namespace);
    }
    
    @Override   
    public void visit(All all) {
        Collection<LocalElement> allElements = all.getElements();
        Iterator<LocalElement> it = allElements.iterator();
        while(it.hasNext()) {
            LocalElement element = it.next();
            visit(element);
        }
        
        Collection<ElementReference> elementRefs = all.getElementReferences();
        Iterator<ElementReference> itER = elementRefs.iterator();
        while(itER.hasNext()) {
            ElementReference element = itER.next();
            visit(element);
        }
    }
    
    @Override
    public void visit(AnyAttribute anyAttr) {
//      Node node = NodeFactory.getInstance().createNode(anyAttr);
//      if(node != null) {
//      addChild((TreeNode) node);
//      }
    }
    
    @Override
    public void visit(AnyElement any) {
//      Node node = NodeFactory.getInstance().createNode(any);
//      if(node != null) {
//      addChild((TreeNode) node);
//      }
    }
    
    
    
    @Override
    public void visit(Choice choice) {
        List<SchemaComponent> children =  choice.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
        
        while(it.hasNext()) {
            SchemaComponent comp = it.next();
            if(comp instanceof AnyElement) {
                visit((AnyElement) comp);
            } else if(comp instanceof Choice) {
                visit((Choice) comp);
            } else if(comp instanceof ElementReference) {
                visit((ElementReference) comp);
            } else if(comp instanceof GroupReference) {
                visit((GroupReference) comp);
            } else if(comp instanceof LocalElement) {
                visit((LocalElement) comp);
            } else if(comp instanceof Sequence) {
                visit((Sequence) comp);
            } 
        }
    }
    
    
//  public void visit(GroupAll ga) {
//  List<SchemaComponent> children = ga.getChildren();
//  Iterator<SchemaComponent> it = children.iterator();
//  
//  while(it.hasNext()) {
//  SchemaComponent sc = it.next();
//  if(sc instanceof  AllElement) {
//  visit((AllElement) sc);
//  } else if(sc instanceof  AllElementReference) {
//  visit((AllElementReference) sc);
//  } 
//  }
//  }
//  
//  public void visit(GroupChoice gc) {
//  List<SchemaComponent> children = gc.getChildren();
//  Iterator<SchemaComponent> it = children.iterator();
//  
//  while(it.hasNext()) {
//  SchemaComponent sc = it.next();
//  if(sc instanceof AnyElement) {
//  visit((AnyElement) sc);
//  } else if(sc instanceof  Choice) {
//  visit((Choice) sc);
//  } else if(sc instanceof ElementReference) {
//  visit((ElementReference) sc);
//  } else if(sc instanceof  GroupReference) {
//  visit((GroupReference) sc);
//  }  else if(sc instanceof  LocalElement) {
//  visit((LocalElement) sc);
//  } else if(sc instanceof  Sequence) {
//  visit((Sequence)sc);
//  }
//  
//  }
//  }
    
    @Override
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> gg = gr.getRef();
        if(gg != null) {
            visit(gg.get());
        }
    }
    
//  public void visit(GroupSequence gs) {
//  visit(gs.getContent());
//  }
    
    
    @Override
    public void visit(SimpleContent sc) {
        
    }
    
    @Override
    public void visit(SimpleContentRestriction scr) {
        
    }
    
    @Override
    public void visit(SimpleExtension se) {
        
    }
    
    @Override
    public void visit(SimpleTypeRestriction str) {
        
    }
    
    private void visit(TypeContainer ge, String elementName, String namespace) {
        String prefix = createNamespacePrefix(namespace);
        
        ExtensibilityElement element = createExtensibilityElement(elementName, prefix, namespace, peek());
        addExtensibilityElement(peek(), element);
        ExtensibilityElementConfigurator configurator = ExtensibilityElementConfiguratorFactory.getDefault().getExtensibilityElementConfigurator(new QName(namespace, elementName));
        if (configurator != null) {
            String attributeName = configurator.getDisplayAttributeName(element, element.getQName());
            if (attributeName != null && element.getAttribute(attributeName) == null) {
                String keyValuePrefix = null;
                if ((keyValuePrefix = configurator.getAttributeUniqueValuePrefix(element, element.getQName(), attributeName)) != null) {
                    
                    boolean isInTransaction = Utility.startTransaction(element.getModel());
                    element.setAttribute(attributeName, 
                            NameGenerator.generateUniqueValueForKeyAttribute(element, attributeName, element.getQName(), keyValuePrefix));
                    Utility.endTransaction(element.getModel(), isInTransaction);
                }
            }
        }
        if(mRootExtensibilityElement == null) {
            mRootExtensibilityElement = element;
        }
        push(element);
        if(ge.getType() != null) {
            GlobalType gt = ge.getType().get();
            if(gt != null) {
                visit(gt);
            }
        } else {
            LocalType lt = ge.getInlineType();
            visit(lt);
        }
        
        pop();
    }
    
    private void visitAttribute(Attribute attr, String attrName, SimpleType simpleType) {
        String defaultValue = attr.getDefault();
        String fixedValue = attr.getFixed();
        ExtensibilityElement exElement = (ExtensibilityElement) peek();
        String attrVal = null;
        
        //Already has a value.
        if (exElement.getAttribute(attrName) != null)
            return;
        
        if(defaultValue != null) {
            attrVal = defaultValue;
        } else if(fixedValue != null) {
            attrVal = fixedValue;
        } else {
            String targetNs = Utility.getTargetNamespace(attr.getModel());
            QName elementQName = new QName(targetNs, exElement.getQName().getLocalPart());
            ExtensibilityElementConfigurator configurator = 
                ExtensibilityElementConfiguratorFactory.getDefault().getExtensibilityElementConfigurator(elementQName);
            if(simpleType != null) {
                //for boolean type we show true/false drop down
                String simpleTypeName = null;
                if (simpleType instanceof GlobalSimpleType) {
                    simpleTypeName = ((GlobalSimpleType) simpleType).getName();
                }
                SchemaModel primitiveTypesModel =
                        SchemaModelFactory.getDefault().getPrimitiveTypesModel();
                
                if (simpleType.getModel() == primitiveTypesModel &&
                        "boolean".equals(simpleTypeName)) {//NOI18N
                    //
                    if (configurator != null) {
                        attrVal = configurator.getDefaultValue(exElement, exElement.getQName(), attrName);
                    }
                    if (attrVal == null) {
                        attrVal = "true";//NOI18N
                        addAttributeToExtensibilityElement(exElement, attrName, attrVal);
                        return;
                    }
                } else if(simpleType.getDefinition() instanceof  SimpleTypeRestriction) {
                    //if attribute has enumeration facet
                    //then use the first enumeration value
                    SimpleTypeRestriction sr = (SimpleTypeRestriction) simpleType.getDefinition();
                    Collection enumerations = sr.getEnumerations();
                    if(enumerations != null && enumerations != null) {
                        if (configurator != null) {
                            attrVal = configurator.getDefaultValue(exElement, exElement.getQName(), attrName);
                        }
                        if (attrVal == null) {
                            Iterator enuIter = enumerations.iterator();
                            if(enuIter.hasNext()) {
                                Enumeration facet = (Enumeration) enuIter.next();
                                attrVal = facet.getValue();
                                addAttributeToExtensibilityElement(exElement, attrName, attrVal);
                                return;
                            }
                        }
                    } 
                    
                }
            }
            
            if(attrVal == null) {
                if (configurator == null || (attrVal = configurator.getDefaultValue(exElement, exElement.getQName(), attrName)) == null) {
                    attrVal = NbBundle.getMessage(ExtensibilityElementCreatorVisitor.class, "REQUIRED_PROPERTY_DEFAULT_VALUE");
                }
            }
            
            
        }
        addAttributeToExtensibilityElement(exElement, attrName, attrVal);
        
        
    }
    
    private String createNamespacePrefix(String targetNamespace) {
        //(1) see if a prefix for schema targetNamespace
        //needs to be added
        String prefix = Utility.getNamespacePrefix(targetNamespace, this.mModel);
        if(prefix == null) {
            prefix = NameGenerator.getInstance().generateNamespacePrefix(null, this.mDefinitions);
            
            boolean inTransaction = Utility.startTransaction(mModel);
            ((AbstractDocumentComponent)this.mDefinitions).addPrefix(prefix, targetNamespace);
            Utility.endTransaction(mModel, inTransaction);
        }
        
        return prefix;
    }
    
    private ExtensibilityElement createExtensibilityElement(String elementName, 
            String prefix, 
            String targetNamespace,
            WSDLComponent parent) {
        QName qName = null;
        
        if(prefix != null) {
            qName = new QName(targetNamespace, elementName, prefix);
        } else {
            qName = new QName(targetNamespace, elementName);
        }
        
        //create extensibility element
        //set all its attribute as defined in schema element
        ExtensibilityElement exElement = (ExtensibilityElement) this.mModel.getFactory().create(parent, qName);
        
        return exElement;
        
    }
    
    private void addExtensibilityElement(WSDLComponent parent, ExtensibilityElement child) {
        boolean inTransaction = Utility.startTransaction(mModel);
        
        final ExtensibilityElement element = child;
        parent.addExtensibilityElement(child);
        
        Utility.endTransaction(mModel, inTransaction);
        Runnable runnable = new Runnable() {
        
            public void run() {
                ActionHelper.selectNode(element);
            }
        
        };
        SwingUtilities.invokeLater(runnable);
        
    }
    
    private void addAttributeToExtensibilityElement(ExtensibilityElement exElement, String attrName, String attrVal) {
        boolean inTransaction = Utility.startTransaction(mModel);
        exElement.setAttribute(attrName, attrVal);
        Utility.endTransaction(mModel, inTransaction);
    }
    
    private void push(WSDLComponent currentComponent) {
        mStack.push(currentComponent);
    }
    
    private void pop() {
        if(!this.mStack.empty()) {
            this.mStack.pop();
        }
    }
    
    private WSDLComponent peek() {
        if(!this.mStack.empty()) {
            return this.mStack.peek();
        }
        
        return null;
    }
}
