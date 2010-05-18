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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.navigation;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.prefs.Preferences;

import org.openide.util.NbPreferences;

/**
 * The static options for hierarchy and members pop up windows.
 * <p>
 * The options are currently not persisted (probably should be). Also note that
 * some options are shared between hierarchy andmembers windows.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaMembersAndHierarchyOptions {
    
    private static final Preferences getPreferences() {
        return NbPreferences.forModule(JavaMembersAndHierarchyOptions.class);
    }
    
    private static final String PROP_caseSensitive = "caseSensitive";
    private static final String PROP_showInherited = "showInherited";
    private static final String PROP_showSuperTypeHierarchy = "showSuperTypeHierarchy";
    private static final String PROP_showSubTypeHierarchy = "showSubTypeHierarchy";
    private static final String PROP_showInner = "showInner";
    private static final String PROP_showFQN = "showFQN";
    private static final String PROP_showConstructors = "showConstructors";
    private static final String PROP_showMethods = "showMethods";
    private static final String PROP_showFields = "showFields";
    private static final String PROP_showEnumConstants = "showEnumConstants";
    private static final String PROP_showProtected = "showProtected";
    private static final String PROP_showPackage = "showPackage";
    private static final String PROP_showPrivate = "showPrivate";
    private static final String PROP_showStatic = "showStatic";
    private static final String PROP_lastBoundsX = "lastBoundsX";
    private static final String PROP_lastBoundsY = "lastBoundsY";
    private static final String PROP_lastBoundsWidth = "lastBoundsWidth";
    private static final String PROP_lastBoundsHeight = "lastBoundsHeight";
    private static final String PROP_membersDividerLocation = "membersDividerLocation";
    private static final String PROP_hierarchyDividerLocation = "hierarchyDividerLocation";
    
    /**
     */
    private static boolean caseSensitive = false;

    /**
     */
    private static boolean showInherited = false;

    /**
     */
    private static boolean showSuperTypeHierarchy = true;

    /**
     */
    private static boolean showSubTypeHierarchy = false;

    /**
     */
    private static boolean showInner = false;

    /**
     */
    private static boolean showFQN = false;

    /**
     */
    private static boolean showConstructors = true;

    /**
     */
    private static boolean showMethods = true;

    /**
     */
    private static boolean showFields = true;

    /**
     */
    private static boolean showEnumConstants = true;

    /**
     */
    private static boolean showProtected = true;

    /**
     */
    private static boolean showPackage = true;

    /**
     */
    private static boolean showPrivate = true;

    /**
     */
    private static boolean showStatic = true;
    
    private static Rectangle lastBounds;
    static
    {
        Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();        
        lastBounds = new Rectangle(((dimensions.width / 2) - 410), ((dimensions.height / 2) - 300), 820, 600);
    }
    
    private static int membersDividerLocation = 350;
    
    private static int hierarchyDividerLocation = 350;   

    private static int subTypeHierarchyDepth = 1;

    private JavaMembersAndHierarchyOptions() {
    }

    /**
     * @return Value of property showInherited.
     */
    public static boolean isShowInherited() {
        return getPreferences().getBoolean(PROP_showInherited, showInherited);
    }

    /**
     * @param showInherited New value of property showInherited.
     */
    public static void setShowInherited(boolean showInherited) {
        getPreferences().putBoolean(PROP_showInherited, showInherited);
    }

    /**
     * @return
     */
    public static boolean isShowSubTypeHierarchy() {
        return getPreferences().getBoolean(PROP_showSubTypeHierarchy, showSubTypeHierarchy);
    }

    /**
     * @param showSubTypeHierarchy
     */
    public static void setShowSubTypeHierarchy(boolean showSubTypeHierarchy) {
        getPreferences().putBoolean(PROP_showSubTypeHierarchy, showSubTypeHierarchy);
        if (showSubTypeHierarchy) {
            getPreferences().putBoolean(PROP_showSuperTypeHierarchy, false);
        }
    }

    /**
     * @return
     */
    public static boolean isShowSuperTypeHierarchy() {
        return getPreferences().getBoolean(PROP_showSuperTypeHierarchy, showSuperTypeHierarchy);
    }

    /**
     * @param showSuperTypeHierarchy
     */
    public static void setShowSuperTypeHierarchy(boolean showSuperTypeHierarchy) {
        getPreferences().putBoolean(PROP_showSuperTypeHierarchy, showSuperTypeHierarchy);
        if (showSuperTypeHierarchy) {
            getPreferences().putBoolean(PROP_showSubTypeHierarchy, false);
        }
    }


    /**
     * @return Value of property caseSensitive.
     */
    public static boolean isCaseSensitive() {
        return getPreferences().getBoolean(PROP_caseSensitive, caseSensitive);
    }

    /**
     * @param caseSensitive New value of property caseSensitive.
     */
    public static void setCaseSensitive(boolean caseSensitive) {
        getPreferences().putBoolean(PROP_caseSensitive, caseSensitive);
    }

    /**
     * @return Value of property showInner.
     */
    public static boolean isShowFQN() {
        return getPreferences().getBoolean(PROP_showFQN, showFQN);
    }

    /**
     * @param showFQN New value of property showFQN.
     */
    public static void setShowFQN(boolean showFQN) {
        getPreferences().putBoolean(PROP_showFQN, showFQN);
    }

    /**
     * @return Value of property showInner.
     */
    public static boolean isShowInner() {
        return getPreferences().getBoolean(PROP_showInner, showInner);
    }

    /**
     * @param showInner New value of property showInner.
     */
    public static void setShowInner(boolean showInner) {
        getPreferences().putBoolean(PROP_showInner, showInner);
    }

    /**
     * @return Value of property showConstructors.
     */
    public static boolean isShowConstructors() {
        return getPreferences().getBoolean(PROP_showConstructors, showConstructors);
    }

    /**
     * @param showConstructors New value of property showConstructors.
     */
    public static void setShowConstructors(boolean showConstructors) {
        getPreferences().putBoolean(PROP_showConstructors, showConstructors);
    }

    /**
     * @return Value of property showMethods.
     */
    public static boolean isShowMethods() {
        return getPreferences().getBoolean(PROP_showMethods, showMethods);
    }

    /**
     * @param showMethods New value of property showMethods.
     */
    public static void setShowMethods(boolean showMethods) {
        getPreferences().putBoolean(PROP_showMethods, showMethods);
    }

    /**
     * @return Value of property showFields.
     */
    public static boolean isShowFields() {
        return getPreferences().getBoolean(PROP_showFields, showFields);
    }

    /**
     * @param showFields New value of property showFields.
     */
    public static void setShowFields(boolean showFields) {
        getPreferences().putBoolean(PROP_showFields, showFields);
    }

    /**
     * @return
     */
    public static boolean isShowEnumConstants() {
        return getPreferences().getBoolean(PROP_showEnumConstants, showEnumConstants);
    }

    /**
     * @param showEnumConstants
     */
    public static void setShowEnumConstants(boolean showEnumConstants) {
        getPreferences().putBoolean(PROP_showEnumConstants, showEnumConstants);
    }

    /**
     * @return Value of property showPublicOnly.
     */
    public static boolean isShowProtected() {
        return getPreferences().getBoolean(PROP_showProtected, showProtected);
    }

    /**
     * @param showProtected
     */
    public static void setShowProtected(boolean showProtected) {
       getPreferences().putBoolean(PROP_showProtected, showProtected);
    }

    /**
     * @return Value of property showPackage.
     */
    public static boolean isShowPackage() {
        return getPreferences().getBoolean(PROP_showPackage, showPackage);
    }

    /**
     * @param showPackage
     */
    public static void setShowPackage(boolean showPackage) {
        getPreferences().putBoolean(PROP_showPackage, showPackage);
    }

    /**
     * @return Value of property showPrivate.
     */
    public static boolean isShowPrivate() {
        return getPreferences().getBoolean(PROP_showPrivate, showPrivate);
    }

    /**
     * @param showPrivate New value of property showPrivate.
     */
    public static void setShowPrivate(boolean showPrivate) {
        getPreferences().putBoolean(PROP_showPrivate, showPrivate);
    }

    /**
     * @return Value of property showStatic.
     */
    public static boolean isShowStatic() {
        return getPreferences().getBoolean(PROP_showStatic, showStatic);
    }

    /**
     * @param showStatic New value of property showStatic.
     */
    public static void setShowStatic(boolean showStatic) {
        getPreferences().putBoolean(PROP_showStatic, showStatic);
    }
    
    public static Rectangle getLastBounds() {
        int x = getPreferences().getInt(PROP_lastBoundsX, lastBounds.x);
        int y = getPreferences().getInt(PROP_lastBoundsY, lastBounds.y);
        int width = getPreferences().getInt(PROP_lastBoundsWidth, lastBounds.width);
        int height = getPreferences().getInt(PROP_lastBoundsHeight, lastBounds.height);
        
        return new Rectangle(x, y, width, height);
    }
    
    public static void setLastBounds(Rectangle lastBounds) {
        if (lastBounds != null) {
            getPreferences().putInt(PROP_lastBoundsX, lastBounds.x);
            getPreferences().putInt(PROP_lastBoundsY, lastBounds.y);
            getPreferences().putInt(PROP_lastBoundsWidth, lastBounds.width);
            getPreferences().putInt(PROP_lastBoundsHeight, lastBounds.height);
        }
    }
    
    public static int getMembersDividerLocation() {
        return getPreferences().getInt(PROP_membersDividerLocation, membersDividerLocation);
    }
    
    public static void setMembersDividerLocation(int membersDividerLocation) {
        getPreferences().putInt(PROP_membersDividerLocation, membersDividerLocation);
    }
    
    public static int getHierarchyDividerLocation() {
        return getPreferences().getInt(PROP_hierarchyDividerLocation, hierarchyDividerLocation);
    }
    
    public static void setHierarchyDividerLocation(int hierarchyDividerLocation) {
        getPreferences().putInt(PROP_hierarchyDividerLocation, hierarchyDividerLocation);
    }
    
    static int getSubTypeHierarchyDepth() {
        return subTypeHierarchyDepth;
    }

    static void setSubTypeHierarchyDepth(int subTypeHierarchyDepth) {
        JavaMembersAndHierarchyOptions.subTypeHierarchyDepth = subTypeHierarchyDepth;
    }
}
