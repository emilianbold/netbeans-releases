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

package com.sun.rave.designtime;

import java.beans.FeatureDescriptor;

/**
 * <p>A CategoryDescriptor describes a category for a property.  A PropertyDescriptor may include a
 * CategoryDescriptor using the named attribute: Constants.PropertyDescriptor.CATEGORY, or literally
 * "category".  Properties will be displayed on the property sheet grouped with their associated
 * category.  Use <code>java.beans.PropertyDescriptor.setValue(Constants.PropertyDescriptor.CATEGORY,
 * someCatDesc)</code> to associate a category with a particular property.</p>
 *
 * <p>You can also specify the desired display order of categories on the property sheet by
 * providing an array of CategoryDescriptors in the BeanDescriptor.  Use
 * <code>java.beans.BeanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,
 * new CategoryDescriptor[] { ... });</code> to specify the order.</p>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see java.beans.PropertyDescriptor
 * @see java.beans.BeanDescriptor
 */
public class CategoryDescriptor extends FeatureDescriptor {

    /**
     * Constructs a new CategoryDescriptor with no settings.
     */
    public CategoryDescriptor() {}

    /**
     * Constructs a new CategoryDescriptor with the specified name.
     *
     * @param name The String name for the new CategoryDescriptor
     */
    public CategoryDescriptor(String name) {
        setName(name);
    }

    /**
     * Constructs a new CategoryDescriptor with the specified name and description.
     *
     * @param name The String name for the new CategoryDescriptor
     * @param description The String description for the new CategoryDescriptor
     */
    public CategoryDescriptor(String name, String description) {
        setName(name);
        setShortDescription(description);
    }

   /**
    * Constructs a new CategoryDescriptor with the specified name, description and default expansion
    * state.
    *
    * @param name The String name for the new CategoryDescriptor
    * @param description The String description for the new CategoryDescriptor
    * @param expandByDefault The initial state for the 'expandByDefault' property of the new
    *        CategoryDescriptor.  If expandByDefault is true, the category will appear expanded
    *        in the property sheet, and collapsed if false.
    */
    public CategoryDescriptor(String name, String description, boolean expandByDefault) {
        setName(name);
        setShortDescription(description);
        setExpandByDefault(expandByDefault);
    }

    /**
     * Storage field for the 'expandByDefault' property.
     */
    protected boolean expandByDefault = true;

    /**
     * Sets the expandByDefault property.  If expandByDefault is true, this category will appear
     * expanded in the property sheet, or collapsed if false.
     *
     * @param expandByDefault <code>true</code> to expand the category, <code>false</code> to
     *        collapse it by default.
     */
    public void setExpandByDefault(boolean expandByDefault) {
        this.expandByDefault = expandByDefault;
    }

    /**
     * Returns the state of the expandByDefault property.  If expandByDefault is true, this category
     * will appear expanded in the property sheet, or collapsed if false.
     *
     * @return <code>true</code> if the category should be expanded, or <code>false</code> for
     *         collapsed.
     */
    public boolean isExpandByDefault() {
        return expandByDefault;
    }

    public boolean equals(Object o) {
        if (o instanceof CategoryDescriptor) {
            CategoryDescriptor cd = (CategoryDescriptor)o;
            return cd == this ||
                (cd.getName() == null && getName() == null ||
                cd.getName() != null && cd.getName().equals(getName()));
        }
        return false;
    }
}
