/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.schema.ui.nodes;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class SchemaNodeFactory extends Object {

    /**
     *
     *
     */
    public SchemaNodeFactory(SchemaModel model, Lookup lookup)
    {
        super();
		context=createContext(model,lookup);
    }

	/**
	 * Creates the SchemaUIContext.  Subclasses can override this method to
	 * customize the SchemaUIContext instance.
	 *
	 */
	protected SchemaUIContext createContext(SchemaModel model, Lookup lookup)
	{
		return new SchemaUIContext(model,this,lookup);
	}


	/**
	 * Returns the context object used by this factory.  All nodes created by
	 * this factory will share this context object.
	 *
	 */
	public SchemaUIContext getContext()
	{
		return context;
	}

	/**
	 * Convenience method to create a "root" node for representing the schema.
	 * This method is a convenience for calling <code>createNode()</code> and 
	 * passing it a reference to the <code>Schema</code> component.
	 *
	 */
	public SchemaNode createRootNode()
	{
//		SchemaComponentReference<Schema> reference=
//			SchemaComponentReference.create(
//				getContext().getModel().getSchema());
            Schema schema = getContext().getModel().getSchema();
            if (schema != null) {
                    return (SchemaNode)createNode(schema);
            }
            //
            return null;
	}




	////////////////////////////////////////////////////////////////////////////
	// Primary factory methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a node for the specified schema component
	 *
	 */
	public Node createNode(SchemaComponent component)
	{
		CreateSchemaComponentNodeVisitor cscnv = new CreateSchemaComponentNodeVisitor();
		return cscnv.createNode(this,component);
	}

        /**
         * Create the node to represent the set of built-in simple types
         * (referred to as "primitive" types).
         *
         * @return  container node for the primitive simple types.
         */
        public abstract Node createPrimitiveTypesNode();

	/**
	 * Creates the children object for the specified component reference. The
	 * defalut implementation returns <code>Children.LEAF</code>, meaning that
	 * any nodes created via <code>createNode()</code> will be lead nodes with
	 * no sub-structure.  Subclasses should override this method to return
	 * more functional children objects.<p>
	 *
	 * Note, this method is only used by convention by methods in this class;
	 * subclasses are free to create and use any children object in the 
	 * various node factory methods.  This method provides a way to override
	 * the default children created by this class, but its use by particular
	 * node factory methods is not guaranteed.
	 *
	 * @param	parent
	 *			The parent node of the about-to-be created node for which this
	 *			method will return the children object.  Note, this node is
	 *			<em>not</em> the node with which the return children object
	 *			will be associated.
	 * @param	reference
	 *			The schema component reference associated with the about-to-be-
	 *			created node.
	 */
	public <C extends SchemaComponent> Children createChildren(
			SchemaComponentReference<C> reference)
	{
		return Children.LEAF;
	}


	////////////////////////////////////////////////////////////////////////////
	// Node factory methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 */
	protected Node createAllNode(
		SchemaComponentReference<All> reference)
	{
		Children children=createChildren(reference);
		return new AllNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createAnnotationNode(
		SchemaComponentReference<Annotation> reference)
	{
		Children children=createChildren(reference);
		return new AnnotationNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createAnyNode(
		SchemaComponentReference<AnyElement> reference)
	{
		Children children=createChildren(reference);
		return new AnyNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createAnyAttributeNode(
		SchemaComponentReference<AnyAttribute> reference)
	{
		Children children=createChildren(reference);
		return new AnyAttributeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createAttributeGroupReferenceNode(
		SchemaComponentReference<AttributeGroupReference> reference)
	{
		Children children=createChildren(reference);
		return new AttributeGroupReferenceNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createChoiceNode(
		SchemaComponentReference<Choice> reference)
	{
		Children children=createChildren(reference);
		return new ChoiceNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createComplexContentNode(
		SchemaComponentReference<ComplexContent> reference)
	{
		Children children=createChildren(reference);
		return new ComplexContentNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createComplexContentRestrictionNode(
		SchemaComponentReference<ComplexContentRestriction> reference)
	{
		Children children=createChildren(reference);
		return new ComplexContentRestrictionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createComplexExtensionNode(
		SchemaComponentReference<ComplexExtension> reference)
	{
		Children children=createChildren(reference);
		return new ComplexExtensionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createDocumentationNode(
		SchemaComponentReference<Documentation> reference)
	{
		Children children=createChildren(reference);
		return new DocumentationNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createAppInfoNode(
		SchemaComponentReference<AppInfo> reference)
	{
		Children children=createChildren(reference);
		return new AppInfoNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createEnumerationNode(
		SchemaComponentReference<Enumeration> reference)
	{
		Children children=createChildren(reference);
		return new EnumerationNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createFieldNode(
		SchemaComponentReference<Field> reference)
	{
		Children children=createChildren(reference);
		return new FieldNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createFractionDigitsNode(
		SchemaComponentReference<FractionDigits> reference)
	{
		Children children=createChildren(reference);
		return new FractionDigitsNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalAttributeNode(
		SchemaComponentReference<GlobalAttribute> reference)
	{
		Children children=createChildren(reference);
		return new GlobalAttributeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalAttributeGroupNode(
		SchemaComponentReference<GlobalAttributeGroup> reference)
	{
		Children children=createChildren(reference);
		return new GlobalAttributeGroupNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalComplexTypeNode(
		SchemaComponentReference<GlobalComplexType> reference)
	{
		Children children=createChildren(reference);
		return new GlobalComplexTypeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalElementNode(
		SchemaComponentReference<GlobalElement> reference)
	{
		Children children=createChildren(reference);
		return new GlobalElementNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalGroupNode(
		SchemaComponentReference<GlobalGroup> reference)
	{
		Children children=createChildren(reference);
		return new GlobalGroupNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGlobalSimpleTypeNode(
		SchemaComponentReference<GlobalSimpleType> reference)
	{
		Children children=createChildren(reference);
		return new GlobalSimpleTypeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createGroupReferenceNode(
		SchemaComponentReference<GroupReference> reference)
	{
		Children children=createChildren(reference);
		return new GroupReferenceNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createImportNode(
		SchemaComponentReference<Import> reference)
	{
		Children children=createChildren(reference);
		return new ImportNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createIncludeNode(
		SchemaComponentReference<Include> reference)
	{
		Children children=createChildren(reference);
		return new IncludeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createKeyNode(
		SchemaComponentReference<Key> reference)
	{
		Children children=createChildren(reference);
		return new KeyNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createKeyRefNode(
		SchemaComponentReference<KeyRef> reference)
	{
		Children children=createChildren(reference);
		return new KeyRefNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createLengthNode(
		SchemaComponentReference<Length> reference)
	{
		Children children=createChildren(reference);
		return new LengthNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createListNode(
		SchemaComponentReference<List> reference)
	{
		Children children=createChildren(reference);
		return new ListNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createLocalAttributeNode(
		SchemaComponentReference<LocalAttribute> reference)
	{
		Children children=createChildren(reference);
		return new LocalAttributeNode(getContext(),reference,children);
	}

	/**
	 *
	 *
	 */
	protected Node createAttributeReferenceNode(
		SchemaComponentReference<AttributeReference> reference)
	{
		Children children=createChildren(reference);
		return new AttributeReferenceNode(getContext(),reference,children);
	}

	/**
	 *
	 *
	 */
	protected Node createLocalComplexTypeNode(
		SchemaComponentReference<LocalComplexType> reference)
	{
		Children children=createChildren(reference);
		return new LocalComplexTypeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createLocalElementNode(
		SchemaComponentReference<LocalElement> reference)
	{
		Children children=createChildren(reference);
		return new LocalElementNode(getContext(),reference,children);
	}
	
	/**
	 *
	 *
	 */
	protected Node createElementReferenceNode(
		SchemaComponentReference<ElementReference> reference)
	{
		Children children=createChildren(reference);
		return new ElementReferenceNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createLocalSimpleTypeNode(
		SchemaComponentReference<LocalSimpleType> reference)
	{
		Children children=createChildren(reference);
		return new LocalSimpleTypeNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMaxExclusiveNode(
		SchemaComponentReference<MaxExclusive> reference)
	{
		Children children=createChildren(reference);
		return new MaxExclusiveNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMaxInclusiveNode(
		SchemaComponentReference<MaxInclusive> reference)
	{
		Children children=createChildren(reference);
		return new MaxInclusiveNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMaxLengthNode(
		SchemaComponentReference<MaxLength> reference)
	{
		Children children=createChildren(reference);
		return new MaxLengthNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMinInclusiveNode(
		SchemaComponentReference<MinInclusive> reference)
	{
		Children children=createChildren(reference);
		return new MinInclusiveNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMinExclusiveNode(
		SchemaComponentReference<MinExclusive> reference)
	{
		Children children=createChildren(reference);
		return new MinExclusiveNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createMinLengthNode(
		SchemaComponentReference<MinLength> reference)
	{
		Children children=createChildren(reference);
		return new MinLengthNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createNotationNode(
		SchemaComponentReference<Notation> reference)
	{
		Children children=createChildren(reference);
		return new NotationNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createPatternNode(
		SchemaComponentReference<Pattern> reference)
	{
		Children children=createChildren(reference);
		return new PatternNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createRedefineNode(
		SchemaComponentReference<Redefine> reference)
	{
		Children children=createChildren(reference);
		return new RedefineNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSchemaNode(
		SchemaComponentReference<Schema> reference)
	{
		Children children=createChildren(reference);
		return new SchemaNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSequenceNode(
		SchemaComponentReference<Sequence> reference)
	{
		Children children=createChildren(reference);
		return new SequenceNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSelectorNode(
		SchemaComponentReference<Selector> reference)
	{
		Children children=createChildren(reference);
		return new SelectorNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSimpleContentNode(
		SchemaComponentReference<SimpleContent> reference)
	{
		Children children=createChildren(reference);
		return new SimpleContentNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSimpleContentRestrictionNode(
		SchemaComponentReference<SimpleContentRestriction> reference)
	{
		Children children=createChildren(reference);
		return new SimpleContentRestrictionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSimpleExtensionNode(
		SchemaComponentReference<SimpleExtension> reference)
	{
		Children children=createChildren(reference);
		return new SimpleExtensionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createSimpleTypeRestrictionNode(
		SchemaComponentReference<SimpleTypeRestriction> reference)
	{
		Children children=createChildren(reference);
		return new SimpleTypeRestrictionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createTotalDigitsNode(
		SchemaComponentReference<TotalDigits> reference)
	{
		Children children=createChildren(reference);
		return new TotalDigitsNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createUnionNode(
		SchemaComponentReference<Union> reference)
	{
		Children children=createChildren(reference);
		return new UnionNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createUniqueNode(
		SchemaComponentReference<Unique> reference)
	{
		Children children=createChildren(reference);
		return new UniqueNode(getContext(),reference,children);
	}


	/**
	 *
	 *
	 */
	protected Node createWhitespaceNode(
		SchemaComponentReference<Whitespace> reference)
	{
		Children children=createChildren(reference);
		return new WhitespaceNode(getContext(),reference,children);
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private SchemaUIContext context;
}
