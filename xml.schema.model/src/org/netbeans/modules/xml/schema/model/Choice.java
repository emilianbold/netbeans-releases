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

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * This interface represents a choice outside a definition of a group.
 * @author Chris Webster
 */
public interface Choice extends ComplexExtensionDefinition,
ComplexTypeDefinition, SequenceDefinition, LocalGroupDefinition, SchemaComponent {
        public static final String MAX_OCCURS_PROPERTY  = "maxOccurs"; // NOI18N
	public static final String MIN_OCCURS_PROPERTY  = "minOccurs"; // NOI18N
	public static final String CHOICE_PROPERTY          = "choice"; // NOI18N
        public static final String GROUP_REF_PROPERTY       = "groupReference"; // NOI18N
        public static final String SEQUENCE_PROPERTY        = "sequence"; // NOI18N
        public static final String ANY_PROPERTY             = "any"; // NOI18N
        public static final String LOCAL_ELEMENT_PROPERTY   = "localElememnt"; // NOI18N
	public static final String ELEMENT_REFERENCE_PROPERTY = "elementReference"; // NOI18N
              
	Collection<Choice> getChoices();
	void addChoice(Choice choice);
	void removeChoice(Choice choice);
	
	Collection<GroupReference> getGroupReferences();
	void addGroupReference(GroupReference ref);
	void removeGroupReference(GroupReference ref);
	
	Collection<Sequence> getSequences();
	void addSequence(Sequence seq);
	void removeSequence(Sequence seq);
	
	Collection<AnyElement> getAnys();
	void addAny(AnyElement any);
	void removeAny(AnyElement any);
	
	Collection<LocalElement> getLocalElements();
	void addLocalElement(LocalElement element);
	void removeLocalElement(LocalElement element);
	
	Collection<ElementReference> getElementReferences();
	void addElementReference(ElementReference element);
	void removeElementReference(ElementReference element);
	
	/**
	 * return ability to set min and max occurs if appropriate, null 
	 * otherwise. This method
	 * should only be used after insertion into the model. 
	 */ 
	public Cardinality getCardinality();
    
}
