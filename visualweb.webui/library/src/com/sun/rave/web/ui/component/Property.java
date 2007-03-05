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
package com.sun.rave.web.ui.component;

import com.sun.rave.web.ui.util.ComponentUtilities;
import java.util.Iterator;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *
 * @author  Ken Paulsen
 */
public class Property extends PropertyBase implements ComplexComponent {

    public static final String CONTENT_FACET = "content"; //NOI18N
    public static final String HELPTEXT_FACET = "helpText"; //NOI18N
    public static final String LABEL_FACET = "label"; //NOI18N

    /**
     *	Constructor.
     */
    public Property() {
        super();
    }

    /**
     *	<p> This method calculates the proper <code>UIComponent</code> that
     *	    should be used when the label property is used with this
     *	    component.</p>
     *
     *	<p> This method provides the implementation for
     *	    {@link com.sun.rave.web.ui.component.ComplexComponent}</p>
     *
     *	@param	context	    The <code>FacesContext</code>.
     *
     *	@return The <code>id</code> of the label target.
     */
    public String getPrimaryElementID(FacesContext context) {
        // Check for "content" Facet first
        UIComponent contentFacet = (UIComponent) getFacets().get("content");
        
        // The field component is the one that is labelled
        UIComponent labeledComponent = null;
        
        if (contentFacet == null) {
            // If there is no facet, assume that the content is specified
            // as a child of this component. Search for a
            // required EditableValueHolderamong the children
            labeledComponent = findLabeledComponent(this, true);
        } else {
            // If a facet has been specified, see if the facet is a required
            // EditableValueHolder or search for a required EditableValueHolder
            // among the children of the facet component
            labeledComponent = findLabeledComponent(contentFacet, false);
        }
        
        if(labeledComponent != null) {
            // Return an absolute path (relative is harder to calculate)
            // NOTE: Label component does not fully support relative anyway,
            // NOTE: the ":" I'm adding isn't necessary... however, it doesn't
            // NOTE: hurt and if Label ever does support relative paths, the
            // NOTE: ":" prefix is needed to specify a full path.
            // NOTE:
            // NOTE: Don't use ComplexComponent here, the Label component will.
            return ":" + labeledComponent.getClientId(context); // NOI18N
        }
        return null;
    }
    
    /**
     *	<p> This method checks the component, children, and facets to see if
     *	    any of them are <code>EditableValueHolder</code>s.  The first one
     * found is returned, null
     *	    otherwise.</p>
     *
     *	@param	comp	The <code>UIComponent</code> to check.
     *	@param	skip	Flag indicating the initial component should be ignored
     *
     *	@return	The first <code>EditableValueHolder</code>, null if not found.
     */
    private static UIComponent findLabeledComponent(UIComponent comp, boolean skip) {
        if (!skip) {
            // Check to see if comp is an EditableValueHolder
            if (comp instanceof EditableValueHolder) {
                return comp;
            }
        }
        
        // Next check children and facets
        Iterator it = comp.getFacetsAndChildren();
        while (it.hasNext()) {
            comp = findLabeledComponent((UIComponent) it.next(), false);
            if (comp != null) {
                return comp;
            }
        }
        
        // Not found
        return null;
    }
    
    
    /**
     * Return the a component that represents the content of the property.
     * If a facet called <code>content</code> does not exist <code>null</code>
     * is returned.
     */
    public UIComponent getContentComponent() {
        return getFacet(CONTENT_FACET);
    }
    
    /**
     * Return the component that implements help text.
     * If a facet named <code>helpText</code> is found
     * that component is returned. Otherwise a <code>HelpInline</code>
     * component is returned. It is assigned the id</br>
     * <code>getId() + "_helpText"</code></br>
     * <p>
     * If the facet is not defined then the returned <code>HelpInline</code>
     * component is re-intialized every time this method is called.
     * </p>
     * <p>
     * If <code>getHelpeText</code> returns null, null is returned.
     * </p>
     *
     * @return a help text facet component
     */
    public UIComponent getHelpTextComponent() {
        
        UIComponent component = getFacet(HELPTEXT_FACET);
        if (component != null) {
            return component;
        }
        
        String helpText = getHelpText();
        if (helpText == null) {
            return null;
        }
        
        HelpInline helpInline = new HelpInline();
        helpInline.setText(helpText);
        helpInline.setId(ComponentUtilities.createPrivateFacetId(this, HELPTEXT_FACET));
        helpInline.setParent(this);
        helpInline.setType("field"); //NOI18N
        
        return helpInline;
        
    }
    
    /**
     * Return the component that implements a label.
     * If a facet named <code>label</code> is found
     * that component is returned. Otherwise a <code>Label</code> component
     * is returned. It is assigned the id</br>
     * <code>getId() + "_label"</code></br>
     * <p>
     * If the facet is not defined then the returned <code>Label</code>
     * component is re-intialized every time this method is called.
     * </p>
     *
     * @return a label facet component
     */
    public UIComponent getLabelComponent() {
        
        UIComponent component = getFacet(LABEL_FACET);
        if (component != null) {
            return component;
        }
        
        if (getLabel() == null) {
            return null;
        }
        
        component = ComponentUtilities.getPrivateFacet(this, LABEL_FACET, true);
        
        if (component == null) {
            Label label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(
                    this, LABEL_FACET));
            label.setText(getLabel());
            String id = getPrimaryElementID(FacesContext.getCurrentInstance());
            label.setFor(id);
            ComponentUtilities.putPrivateFacet(this, LABEL_FACET, label);
            component = label;
        }
        
        return component;
    }
}
