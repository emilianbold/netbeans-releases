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
    
    /**
     * Allow cleanup.
     */
    void close();
}
