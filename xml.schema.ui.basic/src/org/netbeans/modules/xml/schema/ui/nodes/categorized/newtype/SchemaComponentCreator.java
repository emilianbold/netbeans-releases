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

/*
 * SchemaComponentCreator.java
 *
 * Created on April 20, 2006, 4:12 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype;

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
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
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
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;

/**
 *
 * @author Ajit Bhate
 */
public class SchemaComponentCreator extends DeepSchemaVisitor
{	
	protected enum Operation { VERIFY_ADD, ADD, REMOVE , SHOW_CUSTOMIZER};
	
	// operation to perform
	private Operation operation;
	// parent
	private SchemaComponent parent;
	// child
	private SchemaComponent child;
	
	// container where child can be added
	private SchemaComponent container;
	
	// customizer for child
	private Customizer customizer;
	
	// indicates if child can be added to container
	// used to prevent overwriting of existing definitions, or honor mutual
	// exclusivity of certain components
	private boolean addAllowed = true;

	//levels to visit in findContainer
	private int visitLevels = 1;
	private int currentLevel = 0;
	
	/** Creates a new instance of SchemaComponentCreator */
	public SchemaComponentCreator()
	{
	}
	
	/**
	 * Creates a customizer for newly created component, if needed.
	 * @param child Child component which customizer to create.
	 * @param parent The parent under which child is to be added
	 * @return The customizer for child component.
	 * Null if child does not have customizer.
	 */
	public Customizer createCustomizer(SchemaComponent child, 
			SchemaComponent parent)
	{
		//init fields
		Customizer result = null;
		this.operation = Operation.SHOW_CUSTOMIZER;
		this.parent = parent;
		this.child = null;
		setCustomizer(null);
		// process
		child.accept(this);
		result = getCustomizer();
		//reset fields
		setCustomizer(null);
		this.parent = null;
		return result;
	}
	
	/**
	 * Finds container for child to be added under given parent.
	 * @param parent The parent under which child is to be added
	 * @param child Child component to be added
	 * @return The schema component under parent (can be same as parent)
	 * where child can be added. Null if child cant be added.
	 */
	public SchemaComponent findContainer(SchemaComponent parent, SchemaComponent child)
	{
		//init fields
		SchemaComponent result = null;
		this.operation = Operation.VERIFY_ADD;
		this.parent = null;
		this.child = child;
		this.container = null;
		setCustomizer(null);
		// process
		parent.accept(this);
		result = this.container;
		//reset fields
		this.container = null;
		return result;
	}
	
	/**
	 * Adds child component directly under given parent.
	 * @param parent The parent under which child is to be added
	 * @param child Child component to be added
	 */
	public void add(SchemaComponent parent, SchemaComponent child)
	{
		//init fields
		this.operation = Operation.ADD;
		this.parent = parent;
		this.child = null;
		this.container = null;
		setCustomizer(null);
		// process
		child.accept(this);
		//reset fields
		this.parent = null;
		this.operation = null;
	}
	
	// if operation is null just checks if component can be added
	protected void visitChildren(SchemaComponent sc)
	{
		if(getOperation() == Operation.VERIFY_ADD)
		{
			if(sc.canPaste(getChild()) && isAddAllowed())
			{
				container = sc;
				return;
			}
			if(getCurrentLevel()<getVisitLevels())
			{
				setCurrentLevel(getCurrentLevel()+1);
				setAddAllowed(true);
				super.visitChildren(sc);
				setCurrentLevel(getCurrentLevel()-1);
			}
		}
		else if(getOperation() == Operation.ADD)
		{
			getParent().getModel().addChildComponent(getParent(), sc,-1);
		}
		else if(getOperation() == Operation.REMOVE)
		{
			getParent().getModel().removeChildComponent(sc);
		}
	}
	
	protected Operation getOperation()
	{
		return operation;
	}
	
	protected SchemaComponent getParent()
	{
		return parent;
	}
	
	protected void setParent(SchemaComponent parent)
	{
		this.parent = parent;
	}

	protected SchemaComponent getChild()
	{
		return child;
	}

	private int getVisitLevels()
	{
		return visitLevels;
	}

	/**
	 * This api sets the levels for deep visiting during finding container
	 * operation. Default level is 1, which will not do schema deep visiting.
	 * If set to 2, the children of the component will be tested.
	 */
	protected void setVisitLevels(int visitLevels)
	{
		if(visitLevels<1)
			throw new IllegalArgumentException("visit levels must be positive integer");
		this.visitLevels = visitLevels;
	}

	private int getCurrentLevel()
	{
		return currentLevel;
	}

	private void setCurrentLevel(int currentLevel)
	{
		this.currentLevel = currentLevel;
	}
	
	/**
	 * This api checks if given child component can be added to the container.
	 * Subclass implementations should invoke setAddAllowed to allow/prevent
	 * addition of child, depending upon current definition of container
	 */
	protected boolean isAddAllowed()
	{
		return addAllowed;
	}

	/**
	 * This api sets the canAdd flag, which is indicator of whether given child 
	 * component can be added to the container.
	 * Subclass implementations should pass in true to allow and false to prevent
	 * addition of child to current container, depending upon current definition.
	 */
	protected void setAddAllowed(boolean addAllowed)
	{
		this.addAllowed = addAllowed;
	}

	public static SchemaComponent createComponent(SchemaComponentFactory factory,
			Class<? extends SchemaComponent> type)
	{
		SchemaComponent result = null;
		if(All.class.isAssignableFrom(type))
		{
			result = factory.createAll();
		}
		else if(Annotation.class.isAssignableFrom(type))
		{
			result = factory.createAnnotation();
		}
		else if(AnyElement.class.isAssignableFrom(type))
		{
			result = factory.createAny();
		}
		else if(AnyAttribute.class.isAssignableFrom(type))
		{
			result = factory.createAnyAttribute();
		}
		else if(AppInfo.class.isAssignableFrom(type))
		{
			result = factory.createAppInfo();
		}
		else if(AttributeGroupReference.class.isAssignableFrom(type))
		{
			result = factory.createAttributeGroupReference();
		}
		else if(Choice.class.isAssignableFrom(type))
		{
			result = factory.createChoice();
		}
		else if(ComplexContent.class.isAssignableFrom(type))
		{
			result = factory.createComplexContent();
		}
		else if(ComplexContentRestriction.class.isAssignableFrom(type))
		{
			result = factory.createComplexContentRestriction();
		}
		else if(ComplexExtension.class.isAssignableFrom(type))
		{
			result = factory.createComplexExtension();
		}
		else if(Documentation.class.isAssignableFrom(type))
		{
			result = factory.createDocumentation();
		}
		else if(Enumeration.class.isAssignableFrom(type))
		{
			result = factory.createEnumeration();
		}
		else if(Field.class.isAssignableFrom(type))
		{
			result = factory.createField();
		}
		else if(FractionDigits.class.isAssignableFrom(type))
		{result = factory.createFractionDigits();
		}
		else if(GlobalAttribute.class.isAssignableFrom(type))
		{
			result = factory.createGlobalAttribute();
		}
		else if(GlobalAttributeGroup.class.isAssignableFrom(type))
		{
			result = factory.createGlobalAttributeGroup();
		}
		else if(GlobalComplexType.class.isAssignableFrom(type))
		{
			result = factory.createGlobalComplexType();
		}
		else if(GlobalElement.class.isAssignableFrom(type))
		{result = factory.createGlobalElement();
		}
		else if(GlobalSimpleType.class.isAssignableFrom(type))
		{
			result = factory.createGlobalSimpleType();
		}
		else if(GlobalGroup.class.isAssignableFrom(type))
		{
			result = factory.createGroupDefinition();
		}
		else if(GroupReference.class.isAssignableFrom(type))
		{
			result = factory.createGroupReference();
		}
		else if(Import.class.isAssignableFrom(type))
		{
			result = factory.createImport();
		}
		else if(Include.class.isAssignableFrom(type))
		{
			result = factory.createInclude();
		}
		else if(Key.class.isAssignableFrom(type))
		{
			result = factory.createKey();
		}
		else if(KeyRef.class.isAssignableFrom(type))
		{
			result = factory.createKeyRef();
		}
		else if(Length.class.isAssignableFrom(type))
		{
			result = factory.createLength();
		}
		else if(List.class.isAssignableFrom(type))
		{
			result = factory.createList();
		}
		else if(LocalAttribute.class.isAssignableFrom(type))
		{
			result = factory.createLocalAttribute();
		}
		else if(AttributeReference.class.isAssignableFrom(type))
		{
			result = factory.createAttributeReference();
		}
		else if(LocalComplexType.class.isAssignableFrom(type))
		{
			result = factory.createLocalComplexType();
		}
		else if(LocalElement.class.isAssignableFrom(type))
		{
			result = factory.createLocalElement();
		}
		else if(ElementReference.class.isAssignableFrom(type))
		{
			result = factory.createElementReference();
		}
		else if(LocalSimpleType.class.isAssignableFrom(type))
		{
			result = factory.createLocalSimpleType();
		}
		else if(MaxExclusive.class.isAssignableFrom(type))
		{
			result = factory.createMaxExclusive();
		}
		else if(MaxInclusive.class.isAssignableFrom(type))
		{
			result = factory.createMaxInclusive();
		}
		else if(MaxLength.class.isAssignableFrom(type))
		{
			result = factory.createMaxLength();
		}
		else if(MinInclusive.class.isAssignableFrom(type))
		{
			result = factory.createMinInclusive();
		}
		else if(MinExclusive.class.isAssignableFrom(type))
		{
			result = factory.createMinExclusive();
		}
		else if(MinLength.class.isAssignableFrom(type))
		{
			result = factory.createMinLength();
		}
		else if(Notation.class.isAssignableFrom(type))
		{
			result = factory.createNotation();
		}
		else if(Pattern.class.isAssignableFrom(type))
		{
			result = factory.createPattern();
		}
		else if(Redefine.class.isAssignableFrom(type))
		{
			result = factory.createRedefine();
		}
		else if(Schema.class.isAssignableFrom(type))
		{
			result = factory.createSchema();
		}
		else if(Sequence.class.isAssignableFrom(type))
		{
			result = factory.createSequence();
		}
		else if(Selector.class.isAssignableFrom(type))
		{
			result = factory.createSelector();
		}
		else if(SimpleContent.class.isAssignableFrom(type))
		{
			result = factory.createSimpleContent();
		}
		else if(SimpleContentRestriction.class.isAssignableFrom(type))
		{
			result = factory.createSimpleContentRestriction();
		}
		else if(SimpleExtension.class.isAssignableFrom(type))
		{
			result = factory.createSimpleExtension();
		}
		else if(SimpleTypeRestriction.class.isAssignableFrom(type))
		{
			result = factory.createSimpleTypeRestriction();
		}
		else if(TotalDigits.class.isAssignableFrom(type))
		{
			result = factory.createTotalDigits();
		}
		else if(Union.class.isAssignableFrom(type))
		{
			result = factory.createUnion();
		}
		else if(Unique.class.isAssignableFrom(type))
		{
			result = factory.createUnique();
		}
		else if(Whitespace.class.isAssignableFrom(type))
		{
			result = factory.createWhitespace();
		}
		return result;
	}

	protected Customizer getCustomizer()
	{
		return customizer;
	}

	protected void setCustomizer(Customizer customizer)
	{
		this.customizer = customizer;
	}

}
