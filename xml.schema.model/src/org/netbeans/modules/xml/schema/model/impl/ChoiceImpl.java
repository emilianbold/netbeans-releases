/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Cardinality;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class ChoiceImpl extends SchemaComponentImpl implements Choice, 
    Cardinality {
    
    /**
     *
     */
    public ChoiceImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
    
    public ChoiceImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.CHOICE,model));
    }
    
    /**
     *
     */
    public void addChoice(Choice choice) {
	appendChild(CHOICE_PROPERTY, choice);
    }
    
    /**
     *
     */
    public void removeChoice(Choice choice) {
	removeChild(CHOICE_PROPERTY, choice);
    }
    
    /**
     *
     */
    public void removeGroupReference(GroupReference ref) {
	removeChild(GROUP_REF_PROPERTY, ref);
    }
    
    /**
     *
     */
    public void addGroupReference(GroupReference ref) {
	appendChild(GROUP_REF_PROPERTY, ref);
    }
    
    /**
     *
     */
    public void removeSequence(Sequence seq) {
	removeChild(SEQUENCE_PROPERTY, seq);
    }
    
    /**
     *
     */
    public void addSequence(Sequence seq) {
	appendChild(SEQUENCE_PROPERTY, seq);
    }
    
    /**
     *
     */
    public void addAny(AnyElement any) {
	appendChild(ANY_PROPERTY, any);
    }
    
    /**
     *
     */
    public void removeAny(AnyElement any) {
	removeChild(ANY_PROPERTY, any);
    }
    
    /**
     *
     */
    public void removeLocalElement(LocalElement element) {
	removeChild(LOCAL_ELEMENT_PROPERTY, element);
    }
    
    /**
     *
     */
    public void addLocalElement(LocalElement element) {
	appendChild(LOCAL_ELEMENT_PROPERTY, element);
    }
    
    public void removeElementReference(ElementReference element) {
	removeChild(ELEMENT_REFERENCE_PROPERTY, element);
    }
    
    /**
     *
     */
    public void addElementReference(ElementReference element) {
	appendChild(ELEMENT_REFERENCE_PROPERTY, element);
    }
    
    /**
     *
     */
    public Collection<GroupReference> getGroupReferences() {
	return getChildren(GroupReference.class);
    }
    
    /**
     *
     */
    public Collection<Sequence> getSequences() {
	return getChildren(Sequence.class);
    }
    
    /**
     *
     */
    public Collection<AnyElement> getAnys() {
	return getChildren(AnyElement.class);
    }
    
    /**
     *
     */
    public Collection<LocalElement> getLocalElements() {
	return getChildren(LocalElement.class);
    }
    
    public Collection<ElementReference> getElementReferences() {
	return getChildren(ElementReference.class);
    }
    
    /**
     *
     */
    public Collection<Choice> getChoices() {
	return getChildren(Choice.class);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return Choice.class;
    }
    
    public void setMaxOccurs(String max) {
	setAttribute(MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
    
    public void setMinOccurs(Integer min) {
	setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
    public String getMaxOccurs() {
	return getAttribute(SchemaAttributes.MAX_OCCURS);
    }
    
    public Integer getMinOccurs() {
	String s = getAttribute(SchemaAttributes.MIN_OCCURS);
	return s == null ? null : Integer.valueOf(s);
    }
    
    public int getMinOccursDefault() {
	return 1;
    }
    
    public int getMinOccursEffective() {
	String s = getAttribute(SchemaAttributes.MIN_OCCURS);
	return s == null ? getMinOccursDefault() : Integer.valueOf(s).intValue();
    }
    
    public String getMaxOccursDefault() {
	return String.valueOf(1);
    }
    
    public String getMaxOccursEffective() {
	String s = getAttribute(SchemaAttributes.MAX_OCCURS);
	return s == null ? getMaxOccursDefault() : s;
    }
    
    public Cardinality getCardinality() {
	return getParent() instanceof GlobalGroup?null:this;
    }
}
