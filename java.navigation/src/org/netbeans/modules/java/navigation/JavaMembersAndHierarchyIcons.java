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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation;

import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.util.Utilities;
import java.util.EnumSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
final class JavaMembersAndHierarchyIcons {
    public static final Icon INHERITED_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/filterHideInherited.png")); // NOI18N
    
    public static final Icon SUPER_TYPE_HIERARCHY_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/supertypehierarchy.gif")); // NOI18N

    public static final Icon SUB_TYPE_HIERARCHY_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/subtypehierarchy.gif")); // NOI18N

    public static final Icon EXPAND_ALL_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/expandall.gif")); // NOI18N
    
    public static final Icon FQN_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/fqn.gif")); // NOI18N
    
    public static final Icon CLASS_ICON = ElementIcons.getElementIcon(ElementKind.CLASS,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon INNER_CLASS_ICON = ElementIcons.getElementIcon(ElementKind.CLASS,
            EnumSet.of(Modifier.PUBLIC));
    
    public static final Icon INTERFACE_ICON = ElementIcons.getElementIcon(ElementKind.INTERFACE,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon INNER_INTERFACE_ICON = ElementIcons.getElementIcon(ElementKind.INTERFACE,
            EnumSet.of(Modifier.PUBLIC));

    public static final Icon CONSTRUCTOR_ICON = ElementIcons.getElementIcon(ElementKind.CONSTRUCTOR,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon METHOD_ICON = ElementIcons.getElementIcon(ElementKind.METHOD,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon FIELD_ICON = ElementIcons.getElementIcon(ElementKind.FIELD,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon ENUM_ICON = ElementIcons.getElementIcon(ElementKind.ENUM,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon ENUM_CONSTANTS_ICON = ElementIcons.getElementIcon(ElementKind.ENUM_CONSTANT,
            EnumSet.of(Modifier.PUBLIC));
    public static final Icon PACKAGE_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/package.gif")); // NOI18N
    public static final Icon PRIVATE_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/private.gif")); // NOI18N
    public static final Icon PROTECTED_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/protected.gif")); // NOI18N
    public static final Icon PUBLIC_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/public.gif")); // NOI18N
    public static final Icon STATIC_ICON = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/java/navigation/resources/static.gif")); // NOI18N
}
