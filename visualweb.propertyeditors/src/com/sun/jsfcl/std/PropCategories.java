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
package com.sun.jsfcl.std;

import java.util.HashMap;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.CategoryDescriptor;

public class PropCategories {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(PropCategories.class);

    protected static HashMap CategoryHash;

    public static final CategoryDescriptor GENERAL = new CategoryDescriptor(
        bundle.getMessage("gen"), bundle.getMessage("genCatDesc"), true); //NOI18N

    public static final CategoryDescriptor APPEARANCE = new CategoryDescriptor(
        bundle.getMessage("appear"), bundle.getMessage("appearCatDesc"), true); //NOI18N

    public static final CategoryDescriptor DATA = new CategoryDescriptor(
        bundle.getMessage("data"), bundle.getMessage("dataCatDesc"), true); //NOI18N

    public static final CategoryDescriptor EVENTS = new CategoryDescriptor(
        bundle.getMessage("ev"), bundle.getMessage("evCatDesc"), true); //NOI18N

    public static final CategoryDescriptor JAVASCRIPT = new CategoryDescriptor(
        bundle.getMessage("js"), bundle.getMessage("jsCatDesc"), false); //NOI18N

    public static final CategoryDescriptor ADVANCED = new CategoryDescriptor(
        bundle.getMessage("adv"), bundle.getMessage("advCatDesc"), false); //NOI18N

    public static final CategoryDescriptor INTERNAL = new CategoryDescriptor(
        bundle.getMessage("intern"), bundle.getMessage("internCatDesc"), false); //NOI18N

    static {
        /*
         * EAT: Although this code was more fun to write and provides for easy addition
         * of constants, I did not feel that losing references to the variables was a good
         * thing.
                 String[] descriptorFields = {
            "GENERAL",
            "APPEARANCE",
            "DATA",
            "EVENTS",
            "JAVASCRIPT",
            "ADVANCED",
            "INTERNAL"
                 };
                 CategoryDescriptor category;

                 CategoryHash = new HashMap();
                 for (int i=0; i < descriptorFields.length; i++) {
            category = null;
            try {
         category = (CategoryDescriptor) PropCategories.class.getField(descriptorFields[i]).get(null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            CategoryHash.put(category.getName(), new Object[] {descriptorFields[i], category});
                 }
         */

        CategoryHash = new HashMap();
        CategoryHash.put(GENERAL.getName().toLowerCase(), new Object[] {
            "GENERAL", GENERAL}); //NOI18N
        CategoryHash.put(APPEARANCE.getName().toLowerCase(), new Object[] {
            "APPEARANCE", APPEARANCE}); //NOI18N
        CategoryHash.put(DATA.getName().toLowerCase(), new Object[] {
            "DATA", DATA}); //NOI18N
        CategoryHash.put(EVENTS.getName().toLowerCase(), new Object[] {
            "EVENTS", EVENTS}); //NOI18N
        CategoryHash.put(JAVASCRIPT.getName().toLowerCase(), new Object[] {
            "JAVASCRIPT", JAVASCRIPT}); //NOI18N
        CategoryHash.put(ADVANCED.getName().toLowerCase(), new Object[] {
            "ADVANCED", ADVANCED}); //NOI18N
        CategoryHash.put(INTERNAL.getName().toLowerCase(), new Object[] {
            "INTERNAL", INTERNAL}); //NOI18N
    }

    public static CategoryDescriptor getCategoryDescriptor(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])CategoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            pair = new Object[] {
                null, new CategoryDescriptor(categoryName, "")}; //NOI18N
            CategoryHash.put(categoryName, pair);
        }
        return (CategoryDescriptor)pair[1];
    }

    public static String getCategoryDescriptorConstantName(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])CategoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            return null;
        }
        return (String)pair[0];
    }

    // craigmcc - Provide a default CategoryDescriptor[] for
    // beaninfo classes for components that just want the
    // standard categorization.
    private static CategoryDescriptor defaultCategoryDescriptors[] = {
        GENERAL, APPEARANCE, DATA, EVENTS, JAVASCRIPT, ADVANCED, INTERNAL};

    public static CategoryDescriptor[] getDefaultCategoryDescriptors() {
        return defaultCategoryDescriptors;
    }

}
