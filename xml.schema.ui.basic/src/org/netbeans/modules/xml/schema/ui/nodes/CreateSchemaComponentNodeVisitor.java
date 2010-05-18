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

package org.netbeans.modules.xml.schema.ui.nodes;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.FractionDigits;
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
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.MaxLength;
import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.MinInclusive;
import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.Selector;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.openide.nodes.Node;

/**
 *
 * @author Ajit Bhate
 */
public class CreateSchemaComponentNodeVisitor extends DefaultSchemaVisitor
{
	private Node result;
	private SchemaNodeFactory factory;
	/** Creates a new instance of CreateSchemaComponentNodeVisitor */
	public CreateSchemaComponentNodeVisitor()
	{
	}

	public <C extends SchemaComponent> Node createNode(SchemaNodeFactory factory,
		C component)
	{
		this.result = null;
		this.factory = factory;
		component.accept(this);
		return result;
	}

	public void visit(LocalSimpleType type)
	{
		SchemaComponentReference<LocalSimpleType> reference = 
				SchemaComponentReference.create(type);
		result = getFactory().createLocalSimpleTypeNode(reference);
	}

	public void visit(Union u)
	{
		SchemaComponentReference<Union> reference = 
				SchemaComponentReference.create(u);
		result = getFactory().createUnionNode(reference);
	}

	public void visit(AnyElement any)
	{
		SchemaComponentReference<AnyElement> reference = 
				SchemaComponentReference.create(any);
		result = getFactory().createAnyNode(reference);
	}

	public void visit(Enumeration e)
	{
		SchemaComponentReference<Enumeration> reference = 
				SchemaComponentReference.create(e);
		result = getFactory().createEnumerationNode(reference);
	}

	public void visit(AppInfo appinfo)
	{
		SchemaComponentReference<AppInfo> reference = 
				SchemaComponentReference.create(appinfo);
		result = getFactory().createAppInfoNode(reference);
	}

	public void visit(AttributeGroupReference agr)
	{
		SchemaComponentReference<AttributeGroupReference> reference = 
				SchemaComponentReference.create(agr);
		result = getFactory().createAttributeGroupReferenceNode(reference);
	}

	public void visit(GlobalAttributeGroup gag)
	{
		SchemaComponentReference<GlobalAttributeGroup> reference = 
				SchemaComponentReference.create(gag);
		result = getFactory().createGlobalAttributeGroupNode(reference);
	}

	public void visit(KeyRef kr)
	{
		SchemaComponentReference<KeyRef> reference = 
				SchemaComponentReference.create(kr);
		result = getFactory().createKeyRefNode(reference);
	}

	public void visit(GlobalSimpleType gst)
	{
		SchemaComponentReference<GlobalSimpleType> reference = 
				SchemaComponentReference.create(gst);
		result = getFactory().createGlobalSimpleTypeNode(reference);
	}

	public void visit(Include include)
	{
		SchemaComponentReference<Include> reference = 
				SchemaComponentReference.create(include);
		result = getFactory().createIncludeNode(reference);
	}

	public void visit(MinInclusive mi)
	{
		SchemaComponentReference<MinInclusive> reference = 
				SchemaComponentReference.create(mi);
		result = getFactory().createMinInclusiveNode(reference);
	}

	public void visit(Import im)
	{
		SchemaComponentReference<Import> reference = 
				SchemaComponentReference.create(im);
		result = getFactory().createImportNode(reference);
	}

	public void visit(Choice choice)
	{
		SchemaComponentReference<Choice> reference = 
				SchemaComponentReference.create(choice);
		result = getFactory().createChoiceNode(reference);
	}

	public void visit(Unique u)
	{
		SchemaComponentReference<Unique> reference = 
				SchemaComponentReference.create(u);
		result = getFactory().createUniqueNode(reference);
	}

	public void visit(MaxLength ml)
	{
		SchemaComponentReference<MaxLength> reference = 
				SchemaComponentReference.create(ml);
		result = getFactory().createMaxLengthNode(reference);
	}

	public void visit(Redefine rd)
	{
		SchemaComponentReference<Redefine> reference = 
				SchemaComponentReference.create(rd);
		result = getFactory().createRedefineNode(reference);
	}

	public void visit(SimpleContentRestriction scr)
	{
		SchemaComponentReference<SimpleContentRestriction> reference = 
				SchemaComponentReference.create(scr);
		result = getFactory().createSimpleContentRestrictionNode(reference);
	}

	public void visit(LocalElement le)
	{
		SchemaComponentReference<LocalElement> reference = 
				SchemaComponentReference.create(le);
		result = getFactory().createLocalElementNode(reference);
	}

	public void visit(Selector s)
	{
		SchemaComponentReference<Selector> reference = 
				SchemaComponentReference.create(s);
		result = getFactory().createSelectorNode(reference);
	}

	public void visit(Annotation ann)
	{
		SchemaComponentReference<Annotation> reference = 
				SchemaComponentReference.create(ann);
		result = getFactory().createAnnotationNode(reference);
	}

	public void visit(ComplexExtension ce)
	{
		SchemaComponentReference<ComplexExtension> reference = 
				SchemaComponentReference.create(ce);
		result = getFactory().createComplexExtensionNode(reference);
	}

	public void visit(FractionDigits fd)
	{
		SchemaComponentReference<FractionDigits> reference = 
				SchemaComponentReference.create(fd);
		result = getFactory().createFractionDigitsNode(reference);
	}

	public void visit(SimpleExtension se)
	{
		SchemaComponentReference<SimpleExtension> reference = 
				SchemaComponentReference.create(se);
		result = getFactory().createSimpleExtensionNode(reference);
	}

	public void visit(Whitespace ws)
	{
		SchemaComponentReference<Whitespace> reference = 
				SchemaComponentReference.create(ws);
		result = getFactory().createWhitespaceNode(reference);
	}

	public void visit(LocalComplexType type)
	{
		SchemaComponentReference<LocalComplexType> reference = 
				SchemaComponentReference.create(type);
		result = getFactory().createLocalComplexTypeNode(reference);
	}

	public void visit(TotalDigits td)
	{
		SchemaComponentReference<TotalDigits> reference = 
				SchemaComponentReference.create(td);
		result = getFactory().createTotalDigitsNode(reference);
	}

	public void visit(MaxExclusive me)
	{
		SchemaComponentReference<MaxExclusive> reference = 
				SchemaComponentReference.create(me);
		result = getFactory().createMaxExclusiveNode(reference);
	}

	public void visit(SimpleContent sc)
	{
		SchemaComponentReference<SimpleContent> reference = 
				SchemaComponentReference.create(sc);
		result = getFactory().createSimpleContentNode(reference);
	}

	public void visit(AnyAttribute anyAttr)
	{
		SchemaComponentReference<AnyAttribute> reference = 
				SchemaComponentReference.create(anyAttr);
		result = getFactory().createAnyAttributeNode(reference);
	}

	public void visit(GlobalAttribute ga)
	{
		SchemaComponentReference<GlobalAttribute> reference = 
				SchemaComponentReference.create(ga);
		result = getFactory().createGlobalAttributeNode(reference);
	}

	public void visit(All all)
	{
		SchemaComponentReference<All> reference = 
				SchemaComponentReference.create(all);
		result = getFactory().createAllNode(reference);
	}

	public void visit(ComplexContentRestriction ccr)
	{
		SchemaComponentReference<ComplexContentRestriction> reference = 
				SchemaComponentReference.create(ccr);
		result = getFactory().createComplexContentRestrictionNode(reference);
	}

	public void visit(GroupReference gr)
	{
		SchemaComponentReference<GroupReference> reference = 
				SchemaComponentReference.create(gr);
		result = getFactory().createGroupReferenceNode(reference);
	}

	public void visit(Key key)
	{
		SchemaComponentReference<Key> reference = 
				SchemaComponentReference.create(key);
		result = getFactory().createKeyNode(reference);
	}

	public void visit(List l)
	{
		SchemaComponentReference<List> reference = 
				SchemaComponentReference.create(l);
		result = getFactory().createListNode(reference);
	}

	public void visit(Pattern p)
	{
		SchemaComponentReference<Pattern> reference = 
				SchemaComponentReference.create(p);
		result = getFactory().createPatternNode(reference);
	}

	public void visit(Documentation d)
	{
		SchemaComponentReference<Documentation> reference = 
				SchemaComponentReference.create(d);
		result = getFactory().createDocumentationNode(reference);
	}

	public void visit(MinExclusive me)
	{
		SchemaComponentReference<MinExclusive> reference = 
				SchemaComponentReference.create(me);
		result = getFactory().createMinExclusiveNode(reference);
	}

	public void visit(MinLength ml)
	{
		SchemaComponentReference<MinLength> reference = 
				SchemaComponentReference.create(ml);
		result = getFactory().createMinLengthNode(reference);
	}

	public void visit(Schema s)
	{
		SchemaComponentReference<Schema> reference = 
				SchemaComponentReference.create(s);
		result = getFactory().createSchemaNode(reference);
	}

	public void visit(ElementReference er)
	{
		SchemaComponentReference<ElementReference> reference = 
				SchemaComponentReference.create(er);
		result = getFactory().createElementReferenceNode(reference);
	}

	public void visit(AttributeReference r)
	{
		SchemaComponentReference<AttributeReference> reference = 
				SchemaComponentReference.create(r);
		result = getFactory().createAttributeReferenceNode(reference);
	}

	public void visit(GlobalComplexType gct)
	{
		SchemaComponentReference<GlobalComplexType> reference = 
				SchemaComponentReference.create(gct);
		result = getFactory().createGlobalComplexTypeNode(reference);
	}

	public void visit(Sequence s)
	{
		SchemaComponentReference<Sequence> reference = 
				SchemaComponentReference.create(s);
		result = getFactory().createSequenceNode(reference);
	}

	public void visit(MaxInclusive mi)
	{
		SchemaComponentReference<MaxInclusive> reference = 
				SchemaComponentReference.create(mi);
		result = getFactory().createMaxInclusiveNode(reference);
	}

	public void visit(SimpleTypeRestriction str)
	{
		SchemaComponentReference<SimpleTypeRestriction> reference = 
				SchemaComponentReference.create(str);
		result = getFactory().createSimpleTypeRestrictionNode(reference);
	}

	public void visit(LocalAttribute la)
	{
		SchemaComponentReference<LocalAttribute> reference = 
				SchemaComponentReference.create(la);
		result = getFactory().createLocalAttributeNode(reference);
	}

	public void visit(Notation notation)
	{
		SchemaComponentReference<Notation> reference = 
				SchemaComponentReference.create(notation);
		result = getFactory().createNotationNode(reference);
	}

	public void visit(ComplexContent cc)
	{
		SchemaComponentReference<ComplexContent> reference = 
				SchemaComponentReference.create(cc);
		result = getFactory().createComplexContentNode(reference);
	}

	public void visit(GlobalElement ge)
	{
		SchemaComponentReference<GlobalElement> reference = 
				SchemaComponentReference.create(ge);
		result = getFactory().createGlobalElementNode(reference);
	}

	public void visit(Length length)
	{
		SchemaComponentReference<Length> reference = 
				SchemaComponentReference.create(length);
		result = getFactory().createLengthNode(reference);
	}

	public void visit(GlobalGroup gd)
	{
		SchemaComponentReference<GlobalGroup> reference = 
				SchemaComponentReference.create(gd);
		result = getFactory().createGlobalGroupNode(reference);
	}

	public void visit(Field f)
	{
		SchemaComponentReference<Field> reference = 
				SchemaComponentReference.create(f);
		result = getFactory().createFieldNode(reference);
	}

	protected SchemaNodeFactory getFactory()
	{
		return factory;
	}
	
}
