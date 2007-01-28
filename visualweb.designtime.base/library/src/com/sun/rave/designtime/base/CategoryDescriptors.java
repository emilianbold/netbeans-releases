/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.base;

import com.sun.rave.designtime.CategoryDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Defines the component property categories used by Creator design-time
 * implementations.
 */

public class CategoryDescriptors {
    
    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("com.sun.rave.designtime.base.CategoryDescriptorsBundle", // NOI18N
            Locale.getDefault(),
            CategoryDescriptors.class.getClassLoader());
    
    public static final CategoryDescriptor ACCESSIBILITY = new CategoryDescriptor(
            bundle.getString("accessibility"), bundle.getString("accessibilityCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor ADVANCED = new CategoryDescriptor(
            bundle.getString("adv"), bundle.getString("advCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor APPEARANCE = new CategoryDescriptor(
            bundle.getString("appear"), bundle.getString("appearCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor BEHAVIOR = new CategoryDescriptor(
            bundle.getString("behavior"), bundle.getString("behaviorCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor DATA = new CategoryDescriptor(
            bundle.getString("data"), bundle.getString("dataCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor EVENTS = new CategoryDescriptor(
            bundle.getString("ev"), bundle.getString("evCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor GENERAL = new CategoryDescriptor(
            bundle.getString("gen"), bundle.getString("genCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor INTERNAL = new CategoryDescriptor(
            bundle.getString("intern"), bundle.getString("internCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor JAVASCRIPT = new CategoryDescriptor(
            bundle.getString("js"), bundle.getString("jsCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor LAYOUT = new CategoryDescriptor(
            bundle.getString("layout"), bundle.getString("layoutCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor NAVIGATION = new CategoryDescriptor(
            bundle.getString("navigation"), bundle.getString("navigationCatDesc"), false); //NOI18N
    
    private static CategoryDescriptor defaultCategoryDescriptors[] = {
        GENERAL, APPEARANCE, LAYOUT, DATA, EVENTS, NAVIGATION, BEHAVIOR, ACCESSIBILITY,
        JAVASCRIPT, ADVANCED, INTERNAL
    };
    
    protected static HashMap categoryHash;
    
    public static CategoryDescriptor getCategoryDescriptor(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])categoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            pair = new Object[] {
                null, new CategoryDescriptor(categoryName, "")}; //NOI18N
            categoryHash.put(categoryName, pair);
        }
        return (CategoryDescriptor)pair[1];
    }

    public static String getCategoryDescriptorConstantName(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])categoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            return null;
        }
        return (String)pair[0];
    }
    
    /**
     * <p>Return an array of <code>CategoryDescriptor</code> instances
     * describing the property categories supported by this library.</p>
     */
    public static CategoryDescriptor[] getDefaultCategoryDescriptors() {
        return defaultCategoryDescriptors;
    }
    
    
}
