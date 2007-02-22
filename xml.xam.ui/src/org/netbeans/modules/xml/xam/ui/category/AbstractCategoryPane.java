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

package org.netbeans.modules.xml.xam.ui.category;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract implementation of CategoryPane, used by concrete implementations.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractCategoryPane implements CategoryPane {
    /** Property change support for firing events to listeners. */
    protected PropertyChangeSupport changeSupport;
    /** The currently selected (visible) Category. */
    private Category currentCategory;

    /**
     * Creates a new instance of AbstractCategoryPane.
     */
    public AbstractCategoryPane() {
        changeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(name, listener);
    }

    public Category getCategory() {
        return currentCategory;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(name, listener);
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        Category oldcat = currentCategory;
        currentCategory = category;
        changeSupport.firePropertyChange(PROP_CATEGORY, oldcat, category);
    }
}
