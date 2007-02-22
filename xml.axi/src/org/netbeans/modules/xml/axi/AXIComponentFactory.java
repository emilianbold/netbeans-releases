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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.axi;

import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel.ContentModelType;
import org.netbeans.modules.xml.axi.impl.AnyAttributeProxy;
import org.netbeans.modules.xml.axi.impl.AnyElementProxy;
import org.netbeans.modules.xml.axi.impl.AttributeImpl;
import org.netbeans.modules.xml.axi.impl.AttributeProxy;
import org.netbeans.modules.xml.axi.impl.AttributeRef;
import org.netbeans.modules.xml.axi.impl.CompositorProxy;
import org.netbeans.modules.xml.axi.impl.ElementImpl;
import org.netbeans.modules.xml.axi.impl.ElementProxy;
import org.netbeans.modules.xml.axi.impl.ElementRef;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Factory class to help create various AXI components.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIComponentFactory {
        
    /**
     * Creates a new instance of AXIComponentFactory.
     */
    AXIComponentFactory(AXIModel model) {
        this.model = model;
    }
    
    public long getComponentCount() {
        return elementCount + attributeCount +
               compositorCount + contentModelCount +
               proxyComponentCount + 1;
    }
    
    /**
     * Creates a copy of the original.
     */
    public AXIComponent copy(AXIComponent original) {
        AXICopier copier = new AXICopier(model);
        return copier.copy(original);
    }
    
    /**
     * Creates a new Element.
     */
    public Element createElement() {
        elementCount++;
        return new ElementImpl(model);
    }
    
    /**
     * Creates a new Element.
     */
    public Element createElement(SchemaComponent component) {
        elementCount++;
        return new ElementImpl(model, component);
    }
    
    /**
     * Creates a new Element reference.
     */
    public Element createElementReference(Element referent) {
        elementCount++;
        return new ElementRef(model, referent);
    }
    
    /**
     * Creates a new Element reference.
     */
    public Element createElementReference(SchemaComponent component, Element referent) {
        elementCount++;
        return new ElementRef(model, component, referent);
    }
    
    /**
     * Creates a new Attribute.
     */
    public Attribute createAttribute() {
        attributeCount++;
        return new AttributeImpl(model);
    }
        
    /**
     * Creates a new Attribute.
     */
    public Attribute createAttribute(SchemaComponent component) {
        attributeCount++;
        return new AttributeImpl(model, component);
    }
    
    /**
     * Creates a new Attribute reference.
     */
    public Attribute createAttributeReference(Attribute referent) {
        attributeCount++;
        return new AttributeRef(model, referent);
    }
    
    /**
     * Creates a new Attribute reference.
     */
    public Attribute createAttributeReference(SchemaComponent component, Attribute referent) {
        attributeCount++;
        return new AttributeRef(model, component, referent);
    }
    
    /**
     * Creates a new instance of compositor.
     */
    public Compositor createCompositor(CompositorType type) {
        compositorCount++;
        Compositor compositor = new Compositor(model, type);
        return compositor;
    }
    
    /**
     * Creates a new instance Sequence compositor.
     */
    public Compositor createSequence() {
        compositorCount++;
        return new Compositor(model, CompositorType.SEQUENCE);
    }
    
    /**
     * Creates a new instance Sequence compositor.
     */
    public Compositor createSequence(SchemaComponent component) {
        compositorCount++;
        Compositor compositor = new Compositor(model, component);
        return compositor;
    }
    
    /**
     * Creates a new instance Choice compositor.
     */
    public Compositor createChoice() {
        compositorCount++;
        return new Compositor(model, CompositorType.CHOICE);
    }
    
    /**
     * Creates a new instance Choice compositor.
     */
    public Compositor createChoice(SchemaComponent component) {
        compositorCount++;
        Compositor compositor = new Compositor(model, component);
        return compositor;
    }
    
    /**
     * Creates a new instance All compositor.
     */
    public Compositor createAll() {
        compositorCount++;
        return new Compositor(model, CompositorType.ALL);
    }
    
    /**
     * Creates a new instance All compositor.
     */
    public Compositor createAll(SchemaComponent component) {
        compositorCount++;
        Compositor compositor = new Compositor(model, component);
        return compositor;
    }
    
    /**
     * Creates a new instance AnyElement.
     */
    public AnyElement createAnyElement() {
        compositorCount++;
        return new AnyElement(model);
    }
    
    /**
     * Creates a new instance AnyElement.
     */
    public AnyElement createAnyElement(SchemaComponent component) {
        compositorCount++;
        return new AnyElement(model, component);
    }
    
    /**
     * Creates a new instance AnyAttribute.
     */
    public AnyAttribute createAnyAttribute() {
        compositorCount++;
        return new AnyAttribute(model);
    }
    /**
     * Creates a new instance AnyAttribute.
     */
    public AnyAttribute createAnyAttribute(SchemaComponent component) {
        compositorCount++;
        return new AnyAttribute(model, component);
    }
    
    /**
     * Creates a ComplexType.
     */
    public ContentModel createComplexType() {
        contentModelCount++;
        return new ContentModel(model, ContentModelType.COMPLEX_TYPE);
    }
    
    /**
     * Creates a Group.
     */
    public ContentModel createGroup() {
        contentModelCount++;
        return new ContentModel(model, ContentModelType.GROUP);
    }
    
    /**
     * Creates an AttributeGroup.
     */
    public ContentModel createAttributeGroup() {
        contentModelCount++;
        return new ContentModel(model, ContentModelType.ATTRIBUTE_GROUP);
    }
    
    /**
     * Creates a ContentModel.
     */
    public ContentModel createContentModel(ContentModelType type) {
        contentModelCount++;
        return new ContentModel(model, type);
    }
    
    /**
     * Creates a ContentModel.
     */
    public ContentModel createContentModel(SchemaComponent component) {
        contentModelCount++;
        return new ContentModel(model, component);
    }
    
    public AXIComponent createProxy(AXIComponent original) {
        proxyComponentCount++;
        return new ProxyComponentFactory().createProxy(original);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("elementCount: " + elementCount + "\n");
        buffer.append("attributeCount: " + attributeCount + "\n");
        buffer.append("compositorCount: " + compositorCount + "\n");
        buffer.append("contentModelCount: " + contentModelCount + "\n");
        buffer.append("proxyComponentCount: " + proxyComponentCount + "\n");
        return buffer.toString();
    }
    
    /**
     * Creates a proxy for an AXIComponent.
     */
    private class ProxyComponentFactory extends DefaultVisitor {
        
        private AXIComponent proxyComponent;
        
        ProxyComponentFactory() {
        }
        
        AXIComponent createProxy(AXIComponent original) {            
            original.accept(this);
            return proxyComponent;
        }
        
        public void visit(Element element) {
            proxyComponent = new ElementProxy(model, element);
        }
        
        public void visit(AnyElement element) {
            proxyComponent = new AnyElementProxy(model, element);
        }
        
        public void visit(Attribute attribute) {
            proxyComponent = new AttributeProxy(model, attribute);
        }
        
        public void visit(AnyAttribute attribute) {
            proxyComponent = new AnyAttributeProxy(model, attribute);
        }
        
        public void visit(Compositor compositor) {
            proxyComponent = new CompositorProxy(model, compositor);
        }        
    }
    
    /**
     * Creates a copy of the specified AXIComponent.
     */
    private class AXICopier extends DefaultVisitor {
        
        private AXIComponent copiedComponent;
        private AXIModel model;
        
        /**
         * Creates a new instance of AXICopier
         */
        public AXICopier(AXIModel model) {
            this.model = model;
        }
        
        public AXIComponent copy(AXIComponent original) {
            //if proxy, create a new proxy, initialize and return
            if(original.getComponentType() == ComponentType.PROXY) {
                AXIComponentFactory f = model.getComponentFactory();
                copiedComponent = f.createProxy(original.getSharedComponent());
                assert(copiedComponent != null);
                return copiedComponent;
            }
            
            //visit so that it'll get created
            original.accept(this);
            assert(copiedComponent != null);
            return copiedComponent;
        }
        
        public void visit(Element element) {
            if(element instanceof ElementRef) {
                ElementRef ref = (ElementRef)element;
                copiedComponent = model.getComponentFactory().
                        createElementReference(ref.getReferent());
                ((Element)copiedComponent).setMaxOccurs(element.getMaxOccurs());
                ((Element)copiedComponent).setMinOccurs(element.getMinOccurs());
                return;
            }
            copiedComponent = model.getComponentFactory().
                    createElement();
            ((Element)copiedComponent).setAbstract(element.getAbstract());
            ((Element)copiedComponent).setBlock(element.getBlock());
            ((Element)copiedComponent).setDefault(element.getDefault());
            ((Element)copiedComponent).setFinal(element.getFinal());
            ((Element)copiedComponent).setForm(element.getForm());
            ((Element)copiedComponent).setFixed(element.getFixed());
            ((Element)copiedComponent).setMaxOccurs(element.getMaxOccurs());
            ((Element)copiedComponent).setMinOccurs(element.getMinOccurs());
            ((Element)copiedComponent).setName(element.getName());
            ((Element)copiedComponent).setNillable(element.getNillable());
        }
        
        public void visit(AnyElement element) {
            copiedComponent = model.getComponentFactory().
                    createAnyElement();
            ((AnyElement)copiedComponent).setProcessContents(element.getProcessContents());
            ((AnyElement)copiedComponent).setTargetNamespace(element.getTargetNamespace());
        }
        
        public void visit(Attribute attribute) {
            if(attribute instanceof AttributeRef) {
                AttributeRef ref = (AttributeRef)attribute;
                copiedComponent = model.getComponentFactory().
                        createAttributeReference(ref.getReferent());
                ((Attribute)copiedComponent).setFixed(attribute.getFixed());
                ((Attribute)copiedComponent).setDefault(attribute.getDefault());
                ((Attribute)copiedComponent).setUse(attribute.getUse());
                return;
            }
            copiedComponent = model.getComponentFactory().
                    createAttribute();
            ((Attribute)copiedComponent).setDefault(attribute.getDefault());
            ((Attribute)copiedComponent).setFixed(attribute.getFixed());
            ((Attribute)copiedComponent).setForm(attribute.getForm());
            ((Attribute)copiedComponent).setUse(attribute.getUse());
            ((Attribute)copiedComponent).setName(attribute.getName());
        }
        
        public void visit(AnyAttribute attribute) {
            copiedComponent = model.getComponentFactory().
                    createAnyAttribute();
            ((AnyAttribute)copiedComponent).setProcessContents(attribute.getProcessContents());
            ((AnyAttribute)copiedComponent).setTargetNamespace(attribute.getTargetNamespace());
        }
        
        public void visit(Compositor compositor) {
            copiedComponent = model.getComponentFactory().
                    createCompositor(compositor.getType());
            ((Compositor)copiedComponent).setMaxOccurs(compositor.getMaxOccurs());
            ((Compositor)copiedComponent).setMinOccurs(compositor.getMinOccurs());
        }
        
        public void visit(ContentModel contentModel) {
            copiedComponent = model.getComponentFactory().
                    createContentModel(contentModel.getType());
        }        
    }
        
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    /////////////////////////////////////////////////////////////////////
    private AXIModel model;
    private long elementCount;
    private long attributeCount;
    private long compositorCount;
    private long contentModelCount;
    private long proxyComponentCount;
}
