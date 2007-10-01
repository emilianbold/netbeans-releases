/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/**
 * The static options for hierarchy and members pop up windows.
 * <p>
 * The options are currently not persisted (probably should be). Also note that
 * some options are shared between hierarchy andmembers windows.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaMembersAndHierarchyOptions {
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

    private static int subTypeHierarchyDepth = 1;

    private JavaMembersAndHierarchyOptions() {
    }

    /**
     * @return Value of property showInherited.
     */
    public static boolean isShowInherited() {
        return showInherited;
    }

    /**
     * @param showInherited New value of property showInherited.
     */
    public static void setShowInherited(boolean showInherited) {
        JavaMembersAndHierarchyOptions.showInherited = showInherited;
    }

    /**
     * @return
     */
    public static boolean isShowSubTypeHierarchy() {
        return showSubTypeHierarchy;
    }

    /**
     * @param showSubTypeHierarchy
     */
    public static void setShowSubTypeHierarchy(boolean showSubTypeHierarchy) {
        JavaMembersAndHierarchyOptions.showSubTypeHierarchy = showSubTypeHierarchy;
        if (showSubTypeHierarchy) {
            JavaMembersAndHierarchyOptions.showSuperTypeHierarchy = false;
        }
    }

    /**
     * @return
     */
    public static boolean isShowSuperTypeHierarchy() {
        return showSuperTypeHierarchy;
    }

    /**
     * @param showSuperTypeHierarchy
     */
    public static void setShowSuperTypeHierarchy(boolean showSuperTypeHierarchy) {
        JavaMembersAndHierarchyOptions.showSuperTypeHierarchy = showSuperTypeHierarchy;
        if (showSuperTypeHierarchy) {
            JavaMembersAndHierarchyOptions.showSubTypeHierarchy = false;
        }
    }


    /**
     * @return Value of property caseSensitive.
     */
    public static boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * @param caseSensitive New value of property caseSensitive.
     */
    public static void setCaseSensitive(boolean caseSensitive) {
        JavaMembersAndHierarchyOptions.caseSensitive = caseSensitive;
    }

    /**
     * @return Value of property showInner.
     */
    public static boolean isShowFQN() {
        return JavaMembersAndHierarchyOptions.showFQN;
    }

    /**
     * @param showFQN New value of property showFQN.
     */
    public static void setShowFQN(boolean showFQN) {
        JavaMembersAndHierarchyOptions.showFQN = showFQN;
    }

    /**
     * @return Value of property showInner.
     */
    public static boolean isShowInner() {
        return JavaMembersAndHierarchyOptions.showInner;
    }

    /**
     * @param showInner New value of property showInner.
     */
    public static void setShowInner(boolean showInner) {
        JavaMembersAndHierarchyOptions.showInner = showInner;
    }

    /**
     * @return Value of property showConstructors.
     */
    public static boolean isShowConstructors() {
        return JavaMembersAndHierarchyOptions.showConstructors;
    }

    /**
     * @param showConstructors New value of property showConstructors.
     */
    public static void setShowConstructors(boolean showConstructors) {
        JavaMembersAndHierarchyOptions.showConstructors = showConstructors;
    }

    /**
     * @return Value of property showMethods.
     */
    public static boolean isShowMethods() {
        return JavaMembersAndHierarchyOptions.showMethods;
    }

    /**
     * @param showMethods New value of property showMethods.
     */
    public static void setShowMethods(boolean showMethods) {
        JavaMembersAndHierarchyOptions.showMethods = showMethods;
    }

    /**
     * @return Value of property showFields.
     */
    public static boolean isShowFields() {
        return JavaMembersAndHierarchyOptions.showFields;
    }

    /**
     * @param showFields New value of property showFields.
     */
    public static void setShowFields(boolean showFields) {
        JavaMembersAndHierarchyOptions.showFields = showFields;
    }

    /**
     * @return
     */
    public static boolean isShowEnumConstants() {
        return showEnumConstants;
    }

    /**
     * @param showEnumConstants
     */
    public static void setShowEnumConstants(boolean showEnumConstants) {
        JavaMembersAndHierarchyOptions.showEnumConstants = showEnumConstants;
    }

    /**
     * @return Value of property showPublicOnly.
     */
    public static boolean isShowProtected() {
        return JavaMembersAndHierarchyOptions.showProtected;
    }

    /**
     * @param showProtected
     */
    public static void setShowProtected(boolean showProtected) {
        JavaMembersAndHierarchyOptions.showProtected = showProtected;
    }

    /**
     * @return Value of property showPackage.
     */
    public static boolean isShowPackage() {
        return JavaMembersAndHierarchyOptions.showPackage;
    }

    /**
     * @param showPackage
     */
    public static void setShowPackage(boolean showPackage) {
        JavaMembersAndHierarchyOptions.showPackage = showPackage;
    }

    /**
     * @return Value of property showPrivate.
     */
    public static boolean isShowPrivate() {
        return JavaMembersAndHierarchyOptions.showPrivate;
    }

    /**
     * @param showPrivate New value of property showPrivate.
     */
    public static void setShowPrivate(boolean showPrivate) {
        JavaMembersAndHierarchyOptions.showPrivate = showPrivate;
    }

    /**
     * @return Value of property showStatic.
     */
    public static boolean isShowStatic() {
        return JavaMembersAndHierarchyOptions.showStatic;
    }

    /**
     * @param showStatic New value of property showStatic.
     */
    public static void setShowStatic(boolean showStatic) {
        JavaMembersAndHierarchyOptions.showStatic = showStatic;
    }

    static int getSubTypeHierarchyDepth() {
        return subTypeHierarchyDepth;
    }

    static void setSubTypeHierarchyDepth(int subTypeHierarchyDepth) {
        JavaMembersAndHierarchyOptions.subTypeHierarchyDepth = subTypeHierarchyDepth;
    }

}
