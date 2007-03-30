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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public abstract class DescriptionGroupImpl extends JSFConfigComponentImpl implements DescriptionGroup {
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList();
    { 
        SORTED_ELEMENTS.add(JSFConfigQNames.DESCRIPTION.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.DISPLAY_NAME.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.ICON.getLocalName());
    }
            
    public DescriptionGroupImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public List<Description> getDescriptions() {
        return getChildren(Description.class);
    }
    
    public void addDescription(Description description) {
        appendChild(DESCRIPTION, description);
    }
    
    public void addDescription(int index, Description description) {
        insertAtIndex(DESCRIPTION, description, index, NavigationCase.class);
    }
    
    public void removeDescription(Description description) {
        removeChild(DESCRIPTION, description);
    }
    
    public List<DisplayName> getDisplayNames() {
        return getChildren(DisplayName.class);
    }
    
    public void addDisplayName(DisplayName displayName) {
        appendChild(DISPLAY_NAME, displayName);
    }
    
    public void addDisplayName(int index, DisplayName displayName) {
        insertAtIndex(DISPLAY_NAME, displayName, index, NavigationCase.class);
    }
    
    public void removeDisplayName(DisplayName displayName) {
        removeChild(DISPLAY_NAME, displayName);
    }
    
    public List<Icon> getIcons() {
        return getChildren(Icon.class);
    }
    
    public void addIcon(Icon icon) {
        appendChild(ICON, icon);
    }
    
    public void addIcon(int index, Icon icon) {
        insertAtIndex(ICON, icon, index, NavigationCase.class);
    }
    
    public void removeIcon(Icon icon) {
        removeChild(ICON, icon);
    }
    
}
