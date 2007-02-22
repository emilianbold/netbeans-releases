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

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.JToolBar;
import org.openide.util.Lookup;

/**
 * A CategoryPane manages a set of Category instances, displaying one
 * category at any given time, and providing a means of selecting the
 * category to be shown.
 *
 * @author Nathan Fiedler
 */
public interface CategoryPane {
    /** Property name for the selected Category. */
    public static final String PROP_CATEGORY = "category";

    /**
     * Adds the Category to this pane. All of the available categories
     * must be added before the populateToolbar() method is invoked.
     *
     * @param  category  Category to be added.
     */
    void addCategory(Category category);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param  listener  property change listener to add.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener for a specific property.
     *
     * @param  name      name of property to listen to.
     * @param  listener  property change listener to add.
     */
    void addPropertyChangeListener(String name, PropertyChangeListener listener);
    
    /**
     * Returns the currently selected Category.
     *
     * @return  currently seleced Category, or null if none.
     */
    Category getCategory();

    /**
     * Returns the user interface component for this category pane.
     *
     * @return  the user interface component.
     */
    Component getComponent();

    /**
     * Returns the search component for this category pane. This component
     * should be made visible when the Find action is invoked, by calling
     * <code>setVisible(true)</code>.
     *
     * @return  search component.
     */
    SearchComponent getSearchComponent();

    /**
     * Add components to the given toolbar to permit selecting the current
     * category. Note that all categories should have already been added
     * to this pane via the add(Category) method.
     *
     * @param  toolbar  toolbar component to be populated.
     */
    void populateToolbar(JToolBar toolbar);

    /**
     * Remove a PropertyChangeListener from the listener list.
     *
     * @param  listener  property change listener to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param  name      name of property to listen to.
     * @param  listener  property change listener to remove.
     */
    void removePropertyChangeListener(String name, PropertyChangeListener listener);

    /**
     * Change the selected Category to the one given. Notifies property change
     * listeners of the change in selection (property name "category").
     *
     * @param  category  Category to be selected (may not be null).
     */
    void setCategory(Category category);
}
