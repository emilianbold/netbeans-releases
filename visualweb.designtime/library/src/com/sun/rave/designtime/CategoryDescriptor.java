/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
