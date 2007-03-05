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
import javax.faces.context.FacesContext;

/**
 *
 * @author avk
 */
public interface SelectorManager {
      /**
     * JSF standard method from UIComponent
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     */
    public String getClientId(FacesContext context);

    /**
     * JSF standard method from UIComponent
     * @return true if the component is disabled
     */
    public boolean isDisabled();

        /**
     * Get the JS onchange event handler
     * @return A string representing the JS event handler
     */
    public String getOnChange();

    /**
     * Get the tab index for the component
     * @return the tabindex
     */
    public int getTabIndex();
    
       /**
     * Returns true if the component allows multiple selections
     * @return true if the component allows multiple selections
     */
    public boolean isMultiple();
    
    /**
     * Returns true if the component is readonly
     * @return true if the component is readonly
     */
    public boolean isReadOnly(); 
    
    /**
     * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle();
    
    /**
     * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public String getStyleClass();
}
