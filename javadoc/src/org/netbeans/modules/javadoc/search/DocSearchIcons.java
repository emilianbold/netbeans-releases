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

package org.netbeans.modules.javadoc.search;

import java.util.EnumSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.source.UiUtils;

import org.openide.util.Utilities;

/** <DESCRIPTION>

 @author Petr Hrebejk
*/
final class DocSearchIcons extends Object {

    public static final int ICON_NOTRESOLVED = 0;
    public static final int ICON_PACKAGE = ICON_NOTRESOLVED + 1 ;
    public static final int ICON_CLASS = ICON_PACKAGE + 1 ;
    public static final int ICON_INTERFACE = ICON_CLASS + 1;
    public static final int ICON_ENUM = ICON_INTERFACE + 1;
    public static final int ICON_ANNTYPE = ICON_ENUM + 1;
    public static final int ICON_EXCEPTION = ICON_ANNTYPE + 1;
    public static final int ICON_ERROR = ICON_EXCEPTION + 1;
    public static final int ICON_CONSTRUCTOR = ICON_ERROR + 1;
    public static final int ICON_METHOD = ICON_CONSTRUCTOR + 1;
    public static final int ICON_METHOD_ST = ICON_METHOD + 1;
    public static final int ICON_VARIABLE = ICON_METHOD_ST + 1;
    public static final int ICON_VARIABLE_ST = ICON_VARIABLE + 1;
    public static final int ICON_NOT_FOUND = ICON_VARIABLE_ST + 1;
    public static final int ICON_WAIT = ICON_NOT_FOUND + 1;

    private static final Icon[] icons = new Icon[ ICON_WAIT + 1 ];

    static {
        try {
            final EnumSet<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
            final EnumSet<Modifier> modsSt = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
            icons[ ICON_NOTRESOLVED ] = new ImageIcon (Utilities.loadImage("org/netbeans/modules/javadoc/resources/pending.gif")); // NOI18N
            icons[ ICON_PACKAGE ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/comments/resources/package.gif")); // NOI18N                                    
            icons[ ICON_CLASS ] = UiUtils.getElementIcon(ElementKind.CLASS, mods);
            icons[ ICON_INTERFACE ] = UiUtils.getElementIcon(ElementKind.INTERFACE, mods);
            icons[ ICON_ENUM ] = UiUtils.getElementIcon(ElementKind.ENUM, mods);
            icons[ ICON_ANNTYPE ] = UiUtils.getElementIcon(ElementKind.ANNOTATION_TYPE, mods);
            icons[ ICON_EXCEPTION ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/exception.gif")); // NOI18N
            icons[ ICON_ERROR ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/error.gif")); // NOI18N
            icons[ ICON_CONSTRUCTOR ] = UiUtils.getElementIcon(ElementKind.CONSTRUCTOR, mods);
            icons[ ICON_METHOD ] = UiUtils.getElementIcon(ElementKind.METHOD, mods);
            icons[ ICON_METHOD_ST ] = UiUtils.getElementIcon(ElementKind.METHOD, modsSt);
            icons[ ICON_VARIABLE ] = UiUtils.getElementIcon(ElementKind.FIELD, mods);
            icons[ ICON_VARIABLE_ST ] = UiUtils.getElementIcon(ElementKind.FIELD, modsSt);
            icons[ ICON_NOT_FOUND ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/notFound.gif")); // NOI18N
            icons[ ICON_WAIT ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/wait.png")); // NOI18N
        }
        catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    static Icon getIcon( int index ) {
        return icons[ index ];
    }

}
