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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.beans.PropertyChangeEvent;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.BoundaryFacet;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LengthFacet;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalSimpleTypeNode;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.schema.WhitespaceNode;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.AdvancedFacetProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.AdvancedEnumerationProperty;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.SimpleTypeCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AdvancedGlobalSimpleTypeNode extends GlobalSimpleTypeNode
{
	/**
	 *
	 *
	 */
	public AdvancedGlobalSimpleTypeNode(SchemaUIContext context,
			SchemaComponentReference<GlobalSimpleType> reference,
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
				return new SimpleTypeCustomizer<GlobalSimpleType>(getReference());
			}
		};
	}
	
	
	@Override
	protected Sheet createSheet()
	{
		Sheet sheet = super.createSheet();
		Sheet.Set set = sheet.get(Sheet.PROPERTIES);
		GlobalSimpleType gst = getReference().get();
		SimpleTypeDefinition typeDef = gst.getDefinition();
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
			GlobalSimpleType component = getReference().get();
			if (source == component.getDefinition())
			{
				((RefreshableChildren) getChildren()).refreshChildren();
			}
			if (source == component || source == component.getDefinition())
			{
				updatePropertiesSet();
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
			GlobalSimpleType component = getReference().get();
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
			else if(def instanceof SimpleTypeRestriction)
			{
				SimpleTypeRestriction str = (SimpleTypeRestriction)def;
				if(str.getBase() != null && str.getBase().get() == evt.getSource())
				{
					((RefreshableChildren) getChildren()).refreshChildren();
				}
			}
		}
	}
	
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if(!isValid()) return;
        Object source = event.getSource();
        GlobalSimpleType component = getReference().get();
        SimpleTypeDefinition def = component.getDefinition();
        if (source == component && event.getPropertyName().equals(
                GlobalSimpleType.DEFINITION_PROPERTY))
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
		String gstName = getSuperDefinitionName();
		if(gstName==null) return super.getHtmlDisplayName();

		String retValue = getDefaultDisplayName();
		SimpleTypeDefinition definition = getReference().get().getDefinition();
		String supertypeLabel = null;
		
		if(definition instanceof SimpleTypeRestriction)
		{
			supertypeLabel = NbBundle.getMessage(AdvancedGlobalSimpleTypeNode.class,
					"LBL_SimpleTypeChildren_RestrictionOf",gstName);
		}
		if(definition instanceof List)
		{
			supertypeLabel = NbBundle.getMessage(AdvancedGlobalSimpleTypeNode.class,
					"LBL_SimpleTypeChildren_ListOf",gstName);
		}
		if(supertypeLabel!=null)
		{
			retValue = retValue+"<font color='#999999'> ("+supertypeLabel+")</font>";
		}
		return applyHighlights(retValue);
	}
}
