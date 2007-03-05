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
package com.sun.rave.web.ui.model.list;

/**
 *
 * @author avk
 */
public class ListItem {

    Object valueObject;
    String label;
    String value;
    String description = null;
    boolean selected = false;
    boolean disabled = false;
    boolean title = false;

    public ListItem(String label) {
        this.label = label;
        this.valueObject = label;
    }

    public ListItem(Object realValue, String label) {
        this.label = label;
        this.valueObject = realValue;
    }

    public ListItem(Object realValue, String label, boolean disabled) {
        this.label = label;
        this.valueObject = realValue;
        this.disabled = disabled;
    }
    public ListItem(Object realValue, String label, String description,
            boolean disabled) {
        this.label = label;
        this.valueObject = realValue;
        this.description = description;
        this.disabled = disabled;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public Object getValueObject() {
        return valueObject;
    }
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    public void setTitle(boolean title) {
        this.title = title;
    }
    
    public boolean isTitle() {
        return title;
    }
   
}
