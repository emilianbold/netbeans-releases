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

import java.util.Iterator;
import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;
import javax.faces.context.FacesContext;

import com.sun.rave.web.ui.event.TabActionListener;

/**
 * Defines a Tab component. Each Tab is intended to be specified as a child
 * of a Tabs component.
 *
 * @author Sean Comerford
 */
public class Tab extends TabBase implements NamingContainer {
    
    /** Default constructor */
    public Tab() {
        super();
        
        this.addActionListener(new TabActionListener());        
    }
    
    /** Construct a tab with the given label */
    public Tab(String label) {
        this();
        setText(label);        
    }
    
    /**
     * This method returns true if this Tab has any Tab children or false if it
     * has no children or no children that are Tab instances.
     *
     * @return Whether or not any of this Tab's children are Tab components.
     */
    public boolean hasTabChildren() {
        if (getChildCount() > 0) {
            Iterator i = getChildren().iterator();
            
            while (i.hasNext()) {
                UIComponent kid = (UIComponent) i.next();
                
                if (kid instanceof Tab) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Holds value of property extraStyles.
     */
    private String extraStyles;

    /**
     * Getter for property extraStyles.  This is a private (but publically 
     * exposed method) used internally by the component library
     * Do not modify it's value
     * @return Value of property extraStyles.
     */
    public String getExtraStyles() {

        return this.extraStyles;
    }

    /**
     * Setter for property extraStyles.  This is a private (but publically 
     * exposed method) used internally by the component library
     * Do not modify it's value     * @param extraStyles New value of property \
     * extraStyles.
     */
    public void setExtraStyles(String extraStyles) {

        this.extraStyles = extraStyles;
    }

    // <RAVE>
    public void processDecodes(FacesContext context) {
        Iterator iter = this.getChildren().iterator();
        boolean isSelected = this.getId().equals(getSelectedTabId(this));
        while (iter.hasNext()) {
            Object child = iter.next();
            if (child instanceof Tab) {
                ((Tab) child).processDecodes(context);
            } else if ((child instanceof UIComponent) && isSelected) {
                ((UIComponent) child).processDecodes(context);
            }
        }
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
    }
    
    String getSelectedTabId(Tab tab) {
        while (tab.getParent() instanceof Tab) {
            tab = (Tab) tab.getParent();
        }
        TabSet tabSet = (TabSet) tab.getParent();
        return tabSet.getSelected();
    }
    // </RAVE>
    
}
