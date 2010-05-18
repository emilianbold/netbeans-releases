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
