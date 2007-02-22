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
import javax.swing.Icon;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.Lookup;

/**
 * A Category represents a visual component within a XAM model editor.
 *
 * @author Nathan Fiedler
 */
public interface Category extends Lookup.Provider {

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
     * Invoked when the category component has been made invisible.
     */
    void componentHidden();

    /**
     * Invoked when the category component has been made visible.
     */
    void componentShown();

    /**
     * Returns the user interface component for this category.
     *
     * @return  the user interface component.
     */
    java.awt.Component getComponent();

    /**
     * Returns the user-oriented description of this category, for use in
     * tooltips in the usre interface.
     *
     * @return  the human-readable description of this category.
     */
    String getDescription();

    /**
     * Returns the display icon of this category.
     *
     * @return  icon for this category.
     */
    Icon getIcon();

    /**
     * Returns the display title of this category.
     *
     * @return  title for this category.
     */
    String getTitle();

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
     * Shows the component in the category interface, expanding trees
     * and selecting nodes as necessary.
     *
     * @param  component  XAM component to be shown.
     */
    void showComponent(Component component);
}
