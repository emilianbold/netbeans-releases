/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.List;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A simple node factory that creates instances of
 * <code>SchemaComponentNodeChildren</code> to present a structural hierarchy
 * of schema nodes.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class CategorizedSchemaNodeFactory extends SchemaNodeFactory
{
	/**
	 *
	 *
	 */
	public CategorizedSchemaNodeFactory(SchemaModel model, Lookup lookup)
	{
		this(model,null,lookup);
	}
	
	
	/**
	 *
	 *
	 * @param	filters
	 *			A list of schema component types to show, or null if all
	 *			children should be shown
	 */
	public CategorizedSchemaNodeFactory(SchemaModel model,
			List<Class<? extends SchemaComponent>> filters, Lookup lookup)
	{
		super(model,lookup);
		this.filters=filters;
	}
	
	
	/**
	 *
	 *
	 */
	public List<Class<? extends SchemaComponent>> getChildFilters()
	{
		return filters;
	}
	
	
	/**
	 *
	 *
	 */
	@Override
	public <C extends SchemaComponent> Children createChildren(
			SchemaComponentReference<C> reference)
	{
		return new CategorizedChildren<C>(getContext(),reference,
				getChildFilters());
	}
	

        public Node createPrimitiveTypesNode() {
            return new PrimitiveSimpleTypesNode(getContext());
        }
	
	
	
	////////////////////////////////////////////////////////////////////////////
	// Node factory methods
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 *
	 *
	 */
	@Override
	protected Node createAnnotationNode(
			SchemaComponentReference<Annotation> reference)
	{
		// Create our "custom" node and children instead of the default
		Children children=new AnnotationChildren(getContext(), reference);
		return new AdvancedAnnotationNode(getContext(),reference,children);
	}
	
	/**
	 *
	 *
	 */
	@Override
	protected Node createAllNode(
			SchemaComponentReference<All> reference)
	{
		// Create our "custom" node instead of the default
		Children children=createChildren(reference);
		return new AdvancedAllNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createSequenceNode(
			SchemaComponentReference<Sequence> reference)
	{
		Children children = new SequenceChildren<Sequence>(
				getContext(), reference);
		return new AdvancedSequenceNode(getContext(), reference,
				children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createChoiceNode(
			SchemaComponentReference<Choice> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedChoiceNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createGlobalAttributeGroupNode(
			SchemaComponentReference<GlobalAttributeGroup> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedGlobalAttributeGroupNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createGlobalGroupNode(
			SchemaComponentReference<GlobalGroup> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedGlobalGroupNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createGlobalAttributeNode(
			SchemaComponentReference<GlobalAttribute> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedGlobalAttributeNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createLocalAttributeNode(
			SchemaComponentReference<LocalAttribute> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedLocalAttributeNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	@Override
	protected Node createGlobalElementNode(
			SchemaComponentReference<GlobalElement> reference)
	{
		Children children = new ElementChildren<GlobalElement>
				(getContext(),reference);
		AdvancedGlobalElementNode result=
				new AdvancedGlobalElementNode(getContext(),reference,children);
		return result;
	}
	
	
	/**
	 *
	 *
	 */
	@Override
	protected Node createLocalElementNode(
			SchemaComponentReference<LocalElement> reference)
	{
		Children children = new ElementChildren<LocalElement>
				(getContext(),reference);
		Node result=
				new AdvancedLocalElementNode(getContext(),reference,children);
		return result;
	}
	
	
	@Override
    protected Node createElementReferenceNode(
            SchemaComponentReference<ElementReference> reference) {
		Children children = new ReferenceChildren<ElementReference>
				(getContext(),reference);
		Node result=
				new AdvancedElementReferenceNode(getContext(),reference,children);
		return result;
    }

	@Override
    protected Node createAttributeGroupReferenceNode(
            SchemaComponentReference<AttributeGroupReference> reference) {
		Children children = new ReferenceChildren<AttributeGroupReference>
				(getContext(),reference);
		Node result=
				new AdvancedAttributeGroupReferenceNode(getContext(),reference,children);
		return result;
    }

	@Override
    protected Node createGroupReferenceNode(
            SchemaComponentReference<GroupReference> reference) {
		Children children = new ReferenceChildren<GroupReference>
				(getContext(),reference);
		Node result=
				new AdvancedGroupReferenceNode(getContext(),reference,children);
		return result;
    }

	@Override
    protected Node createAttributeReferenceNode(
            SchemaComponentReference<AttributeReference> reference) {
		Children children = new ReferenceChildren<AttributeReference>
				(getContext(),reference);
		Node result=
				new AdvancedAttributeReferenceNode(getContext(),reference,children);
		return result;
    }

    /**
	 *
	 *
	 */
	@Override
	protected Node createGlobalComplexTypeNode(
			SchemaComponentReference<GlobalComplexType> reference)
	{
		
		Children children = new ComplexTypeChildren<GlobalComplexType>
				(getContext(),reference);
		AdvancedGlobalComplexTypeNode result=
				new AdvancedGlobalComplexTypeNode(getContext(),reference,children);
		return result;
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createLocalComplexTypeNode(
			SchemaComponentReference<LocalComplexType> reference)
	{
		Children children = new ComplexTypeChildren<LocalComplexType>
				(getContext(),reference);
		return new AdvancedLocalComplexTypeNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createGlobalSimpleTypeNode(
			SchemaComponentReference<GlobalSimpleType> reference)
	{
		Children children = new SimpleTypeChildren<GlobalSimpleType>
				(getContext(),reference);
		return new AdvancedGlobalSimpleTypeNode(
				getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createLocalSimpleTypeNode(
			SchemaComponentReference<LocalSimpleType> reference)
	{
		Children children = new SimpleTypeChildren<LocalSimpleType>
				(getContext(),reference);
		return new AdvancedLocalSimpleTypeNode(
				getContext(),reference,children);
	}
	
	
    protected Node createEnumerationNode(SchemaComponentReference<Enumeration> reference) {
		Children children = createChildren(reference);
		return new AdvancedEnumerationNode(getContext(),reference,children);
    }

	/**
	 *
	 *
	 */
	protected Node createKeyNode(
			SchemaComponentReference<Key> reference)
	{
		Children children = createChildren(reference);
		return new AdvancedKeyNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createKeyRefNode(
			SchemaComponentReference<KeyRef> reference)
	{
		Children children = createChildren(reference);
		return new AdvancedKeyRefNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createUniqueNode(
			SchemaComponentReference<Unique> reference)
	{
		Children children = createChildren(reference);
		return new AdvancedUniqueNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createSchemaNode(
			SchemaComponentReference<Schema> reference)
	{
		Children children=createChildren(reference);
		return new AdvancedSchemaNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createImportNode(
			SchemaComponentReference<Import> reference)
	{
		Children children = new ReferencedSchemaModelChildren<Import>
				(getContext(),reference);
		return new AdvancedImportNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createIncludeNode(
			SchemaComponentReference<Include> reference)
	{
		Children children = new ReferencedSchemaModelChildren<Include>
				(getContext(),reference);
		return new AdvancedIncludeNode(getContext(),reference,children);
	}
	
	
	/**
	 *
	 *
	 */
	protected Node createRedefineNode(
			SchemaComponentReference<Redefine> reference)
	{
		Children children = new ReferencedSchemaModelChildren<Redefine>
				(getContext(),reference);
		return new AdvancedRedefineNode(getContext(),reference,children);
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////
	
	private List<Class<? extends SchemaComponent>> filters;

}
