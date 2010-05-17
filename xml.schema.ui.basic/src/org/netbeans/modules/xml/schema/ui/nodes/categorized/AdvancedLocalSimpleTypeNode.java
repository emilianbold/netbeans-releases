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

import java.beans.PropertyChangeEvent;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.netbeans.modules.xml.schema.model.BoundaryFacet;
import org.netbeans.modules.xml.schema.model.LengthFacet;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.LocalSimpleTypeNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.schema.WhitespaceNode;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.AdvancedFacetProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.AdvancedEnumerationProperty;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.SimpleTypeCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedLocalSimpleTypeNode extends LocalSimpleTypeNode
{
	/**
	 *
	 *
	 */
	public AdvancedLocalSimpleTypeNode(SchemaUIContext context,
			SchemaComponentReference<LocalSimpleType> reference,
			Children children)
	{
		super(context,reference,children);
		
	}
	
	
	@Override
	public boolean hasCustomizer()
	{
		return isEditable();
	}

	public CustomizerProvider getCustomizerProvider()
	{
		return new CustomizerProvider()
		{

			public Customizer getCustomizer()
			{
				return new SimpleTypeCustomizer<LocalSimpleType>(getReference());
			}
		};
	}

	@Override
			protected Sheet createSheet()
	{
		Sheet sheet = super.createSheet();
		Sheet.Set set = sheet.get(Sheet.PROPERTIES);
		LocalSimpleType lst = getReference().get();
		SimpleTypeDefinition typeDef = lst.getDefinition();
		if(typeDef instanceof SimpleTypeRestriction)
		{
			SimpleTypeRestriction str = (SimpleTypeRestriction)typeDef;
			try
			{
				// facet properties use axi to find appropriate facets
				java.util.List<Class<? extends SchemaComponent>> facetTypes =
						DatatypeFactory.getDefault().
						getApplicableSchemaFacets(getReference().get());
				for(Class<? extends SchemaComponent> facetType:facetTypes)
				{
					String facetTypeName = facetType.getSimpleName();
					if(facetType == Enumeration.class)
					{
						Property facetProperty = new AdvancedEnumerationProperty(
								str, SimpleTypeRestriction.ENUMERATION_PROPERTY,
								NbBundle.getMessage(AdvancedLocalSimpleTypeNode.class,
								"PROP_"+facetTypeName+"_DisplayName"), // display name
								NbBundle.getMessage(AdvancedLocalSimpleTypeNode.class,
								"PROP_"+facetTypeName+"_ShortDescription"),	// descr
								isEditable());
						set.put(new SchemaModelFlushWrapper(getReference().get(), facetProperty));
						continue;
					}
					SchemaComponent facet = null;
					java.util.List<? extends SchemaComponent> facets = 
							str.getChildren(facetType);
					if(facets!=null && !facets.isEmpty()) facet = facets.get(0);
					Class valueType = null;
					String property = null;
					Class propEditorClass = null;
					if(BoundaryFacet.class.isAssignableFrom(facetType))
					{
						valueType = String.class;
						property = BoundaryFacet.VALUE_PROPERTY;
					}
					else if(LengthFacet.class.isAssignableFrom(facetType))
					{
						valueType = int.class;
						property = LengthFacet.VALUE_PROPERTY;
					}
					else if(Pattern.class.isAssignableFrom(facetType))
					{
						valueType = String.class;
						property = Pattern.VALUE_PROPERTY;
					}
					else if(Whitespace.class.isAssignableFrom(facetType))
					{
						valueType = Whitespace.Treatment.class;
						property = Whitespace.VALUE_PROPERTY;
						propEditorClass = WhitespaceNode.WhitespaceValueEditor.class;
					}
					if (valueType!=null && property!=null)
					{
						Property facetProperty = new AdvancedFacetProperty(
							str, facet, facetType, valueType, property,
							NbBundle.getMessage(AdvancedLocalSimpleTypeNode.class,
							"PROP_"+facetTypeName+"_DisplayName"), // display name
							NbBundle.getMessage(AdvancedLocalSimpleTypeNode.class,
							"PROP_"+facetTypeName+"_ShortDescription"),	// descr
							propEditorClass
							);
						set.put(new SchemaModelFlushWrapper(getReference().get(), facetProperty));
					}
				}
			}
			catch (NoSuchMethodException nsme)
			{
				assert false : "properties should be defined";
			}
		}
		return sheet;
	}
	
	protected NewTypesFactory getNewTypesFactory()
	{
		return new AdvancedNewTypesFactory();
	}
	
	public void childrenDeleted(ComponentEvent evt)
	{
		super.childrenDeleted(evt);
		if (isValid())
		{
			Object source = evt.getSource();
			LocalSimpleType component = getReference().get();
			if (source == component.getDefinition())
			{
				((RefreshableChildren) getChildren()).refreshChildren();
			}
			if (source == component || source == component.getDefinition())
			{
				fireDisplayNameChange(null,getDisplayName());
			}
		}
	}
	
	public void childrenAdded(ComponentEvent evt)
	{
		super.childrenAdded(evt);
		if (isValid())
		{
			Object source = evt.getSource();
			LocalSimpleType component = getReference().get();
			if (source == component.getDefinition())
			{
				((RefreshableChildren) getChildren()).refreshChildren();
			}
			if(source == component || source == component.getDefinition())
			{
				fireDisplayNameChange(null,getDisplayName());
			}
		}
	}
	
	public void valueChanged(ComponentEvent evt)
	{
		super.valueChanged(evt);
		if (isValid())
		{
			SimpleTypeDefinition def = getReference().get().getDefinition();
			if(evt.getSource() == def)
			{
				((RefreshableChildren) getChildren()).refreshChildren();
				fireDisplayNameChange(null,getDisplayName());
			}
		}
	}
	
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if(!isValid()) return;
        Object source = event.getSource();
        LocalSimpleType component = getReference().get();
        SimpleTypeDefinition def = component.getDefinition();
        if (source == component && event.getPropertyName().equals(
                LocalSimpleType.DEFINITION_PROPERTY))
        {
            updatePropertiesSet();
        }
        else if(def instanceof SimpleTypeRestriction)
        {
            if(source==def)
            {
                updatePropertiesSet();
            }
            else if(def.getChildren().contains(source) && event.getPropertyName().
                    equals(BoundaryFacet.VALUE_PROPERTY))
            {
                updatePropertiesSet();
            }
        }
    }

    protected void updatePropertiesSet()
	{
        Sheet.Set oldSet = getSheet().get(Sheet.PROPERTIES);
        if(oldSet!=null)
        {
            Sheet.Set newSet = createSheet().get(Sheet.PROPERTIES);
            getSheet().put(newSet);
            firePropertySetsChange(new Sheet.Set[]{oldSet}, new Sheet.Set[]{newSet});
        }
		Node parent = getParentNode();
		if(parent == null) return;
		SchemaComponentNode scn = (SchemaComponentNode) parent.
				getCookie(SchemaComponentNode.class);
		if(scn instanceof AdvancedLocalSimpleTypeNode)
		{
			((AdvancedLocalSimpleTypeNode)scn).updatePropertiesSet();
		}
		else if(scn instanceof AdvancedGlobalSimpleTypeNode)
		{
			((AdvancedGlobalSimpleTypeNode)scn).updatePropertiesSet();
		}
	}

	public String getHtmlDisplayName()
	{
		GlobalSimpleType gst = getSuperDefinition();
		if(gst==null) return super.getHtmlDisplayName();

		String retValue = getDefaultDisplayName();
		SimpleTypeDefinition definition = getReference().get().getDefinition();
		String supertypeLabel = null;
		
		if(definition instanceof SimpleTypeRestriction)
		{
			supertypeLabel = NbBundle.getMessage(AdvancedGlobalSimpleTypeNode.class,
					"LBL_SimpleTypeChildren_RestrictionOf",gst.getName());
		}
		if(definition instanceof List)
		{
			supertypeLabel = NbBundle.getMessage(AdvancedGlobalSimpleTypeNode.class,
					"LBL_SimpleTypeChildren_ListOf",gst.getName());
		}
		if(supertypeLabel!=null)
		{
			retValue = retValue+"<font color='#999999'> ("+supertypeLabel+")</font>";
		}
		return applyHighlights(retValue);
	}
}
