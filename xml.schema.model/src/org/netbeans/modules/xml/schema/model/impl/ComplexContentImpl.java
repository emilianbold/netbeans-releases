/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * ComplexContentImpl.java
 *
 * Created on October 6, 2005, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public class ComplexContentImpl extends SchemaComponentImpl implements ComplexContent{
    
    /** Creates a new instance of ComplexContentImpl */
    protected ComplexContentImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.COMPLEX_CONTENT, model));
    }
    
    public ComplexContentImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return ComplexContent.class;
	}
    
    public void setMixed(Boolean mixed) {
        setAttribute(MIXED_PROPERTY, SchemaAttributes.MIXED, mixed);
    }
    
    public Boolean isMixed() {
//        return Boolean.parseBoolean(getAttribute(SchemaAttributes.MIXED));
        String s = getAttribute(SchemaAttributes.MIXED);
        return s == null ? null : Boolean.valueOf(s);
    }
    
    public void setLocalDefinition(ComplexContentDefinition definition) {
        if(definition == null){
            throw new IllegalArgumentException("ComplexContentDefinition is null");
        }
        List<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(ComplexContentDefinition.class, LOCAL_DEFINITION_PROPERTY, definition, list);
    }
    
    public ComplexContentDefinition getLocalDefinition() {
        Collection<ComplexContentDefinition> elements = getChildren(ComplexContentDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public boolean getMixedEffective() {
        Boolean v = isMixed();
        return v == null ? getMixedDefault() : v;
    }

    /*
     * The effective mixed be the appropriate case among the following:
     * 1.1 If the mixed [attribute] is present on <complexContent>, then its ·actual value·;
     * 1.2 If the mixed [attribute] is present on <complexType>, then its ·actual value·;
     * 1.3 otherwise false.
     */
    public boolean getMixedDefault() {
        //TODO check getParent().getMixedDefault()
        return false;
    }
}
