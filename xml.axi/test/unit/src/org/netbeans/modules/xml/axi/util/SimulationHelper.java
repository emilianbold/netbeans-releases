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
package org.netbeans.modules.xml.axi.util;

import java.io.IOException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.refactoring.RefactoringManager;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 *
 * @author Samaresh
 */
public class SimulationHelper {
    
    private AXIModel model;
    
    /**
     * Creates a new instance of SimulationHelper
     */
    public SimulationHelper(AXIModel model) {
        this.model = model;
    }
    
    public Attribute dropAttributeOnElement(Element e, String name) {
        AXIDocument doc = model.getRoot();
        model.startTransaction();
        Attribute a = model.getComponentFactory().createAttribute();
        a.setName(name);
        e.addAttribute(a);
        model.endTransaction();
        return a;
    }
    
    public Element dropGlobalElement(String name) {
        AXIDocument doc = model.getRoot();
        model.startTransaction();
        Element e = model.getComponentFactory().createElement();
        e.setName(name);
        doc.addElement(e);
        model.endTransaction();
        return e;
    }
    
    public ContentModel dropGlobalComplexType(String name) {
        AXIDocument doc = model.getRoot();
        model.startTransaction();
        ContentModel cm = model.getComponentFactory().createComplexType();
        cm.setName(name);
        doc.addContentModel(cm);
        model.endTransaction();
        return cm;
    }
    
    public Element dropElement(AXIContainer parent, String name) {
        model.startTransaction();
        Element element = model.getComponentFactory().createElement();
        element.setName(name);
        parent.addElement(element);
        model.endTransaction();
        return element;
    }
    
    public void setElementType(Element e, AXIType type) {
        model.startTransaction();
        e.setType(type);
        model.endTransaction();
    }
    
    public void delete(AXIComponent child) {
        model.startTransaction();
        child.getParent().removeChild(child);
        model.endTransaction();
    }    
    
    public void clearAll() {
        model.startTransaction();
        model.getRoot().removeAllChildren();
        model.endTransaction();
    }    
        
    public Compositor dropCompositor(AXIContainer parent, CompositorType type) {
        model.startTransaction();
        Compositor c = getCompositor(type);
        parent.addCompositor(c);
        model.endTransaction();
        return c;
    }
    
    public Element dropElementOnCompositor(Compositor c, String name) {
        model.startTransaction();
        Element e = model.getComponentFactory().createElement();
        e.setName(name);
        c.addElement(e);
        model.endTransaction();
        return e;
    }

    public void dropChildAtIndex(AXIComponent parent, AXIComponent child, int i) {
        model.startTransaction();
        parent.addChildAtIndex(child, i);
        model.endTransaction();
    }

    public void setCompositorType(Compositor c, CompositorType type) {
        model.startTransaction();
        c.setType(type);
        model.endTransaction();
    }
    
    private Compositor getCompositor(CompositorType type) {
        Compositor c = null;
        switch(type) {
            case SEQUENCE:
                c = model.getComponentFactory().createSequence();
                break;
            case CHOICE:
                c = model.getComponentFactory().createChoice();
                break;
            case ALL:
                c = model.getComponentFactory().createAll();
                break;
        }
        
        return c;
    }
    
    /**
     * Checks if a component is part of an AXI model.
     * Returns true if it has a valid parent and model, false otherwise.
     */
    public boolean inModel(AXIComponent c) {
	//for everything else, both parent and model should be valid
        return ( (c.getParent() != null) && (c.getModel() != null) && (c.getPeer() != null));
    }
    
    public boolean refactorRename(AXIContainer container, String name) {
        assert(container.getParent() instanceof AXIDocument);
        NamedReferenceable ref = null;
        SchemaComponent comp = container.getPeer();
        if (comp instanceof NamedReferenceable) {
            ref = NamedReferenceable.class.cast(comp);
        }
        
        RenameRequest request  = new RenameRequest((Nameable)ref, name);
        request.setScopeLocal();
        try {
            SchemaModel sm = model.getSchemaModel();
            RefactoringManager.getInstance().execute(request, false);
            model.sync();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }    

}
