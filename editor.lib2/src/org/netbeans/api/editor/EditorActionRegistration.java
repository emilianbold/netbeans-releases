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

package org.netbeans.api.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registration of an editor action so that it's automatically added into the list
 * of editor actions even without being explicitly created by <code>BaseKit.createActions()</code>.
 * <br/>
 * The corresponding annotation processor will build a xml-layer entry file
 * in the corresponding <i>/Editors/&lt;mime-type&gt;/Actions</code> folder.
 *
 * @since 1.10
 * @author Miloslav Metelka
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface EditorActionRegistration {

    /**
     * Name of the action that will appear as <code>Action.NAME</code> attribute's value.
     * <br/>
     * The Swing's text package actions use convention of lowercase letters with hyphens
     * e.g. "caret-end-word" - see String constants in {@link javax.swing.text.DefaultEditorKit}.
     *
     * @return value of Action.NAME attribute.
     */
    String name();

    /**
     * Mime type for which the action will be registered.
     * <br/>
     * It implies the target folder of the registration <i>/Editors/&lt;mime-type&gt;/Actions</code>.
     *
     * @return mime-type of the action registration (for example "text/x-java"
     *  or empty string which means that the action will be registered as global (for all mime-types).
     */
    String mimeType() default "";

    /**
     * Resource specification for small icon.
     *
     * @return icon resource specification or default (empty string which means no icon).
     */
    String iconResource() default "";

    /**
     * Short description bundle key of the action being registered.
     * Several constructions are allowed:<ul>
     *   <li>
     *      Leave the annotation's default value which means that the short description
     *      will be searched in a bundle located in the same package as the action's class
     *      by a key equal to action's name (value of Action.NAME property).
     *   </li>
     *   <li>
     *      Value starting with a hash "#key" searches in a bundle in the same package
     *      as the action's class.
     *   </li>
     *   <li>
     *      "bundle#key" allows specification of both bundle and a corresponding key.
     *   </li>
     *   <li>
     *      Empty string "" means that nothing will be generated and
     *      the value of Action.SHORT_DESCRIPTION property will be null.
     *   </li>
     * </ul>
     */
    String shortDescription() default "BY_ACTION_NAME";

    /**
     * Menu text bundle key of the registered action.
     * If an empty string is used (the default) it will be set to the same value
     * like action's short description.
     * <br/>
     * Value starting with a hash "#key" searches in a bundle in the same package
     * as the action's class.
     * <br/>
     * "bundle#key" allows specification of both bundle and a corresponding key.
     */
    String menuText() default "";

    /**
     * Popup menu text bundle key of the registered action.
     * If an empty string is used (the default) it will be set to the same value
     * like menu text.
     * <br/>
     * Value starting with a hash "#key" searches in a bundle in the same package
     * as the action's class.
     * <br/>
     * "bundle#key" allows specification of both bundle and a corresponding key.
     */
    String popupText() default "";

    /**
     * Path of this action in main menu e.g. "Edit".
     */
    String menuPath() default "";

    /**
     * Integer position of the main menu item among the other menu items.
     * <br/>
     * The default Integer.MAX_VALUE value means no menu representation.
     */
    int menuPosition() default Integer.MAX_VALUE;

    /**
     * Path of this action in popup menu e.g. "" for appearance right in the context menu
     * or a corresponding path for nested submenu appearance.
     */
    String popupPath() default "";

    /**
     * Integer position of the popup menu item among the other popup menu (or submenu) items.
     * <br/>
     * The default Integer.MAX_VALUE value means no popup menu representation.
     */
    int popupPosition() default Integer.MAX_VALUE;

    /**
     * Integer position of this action in editor toolbar.
     * <br/>
     * The default Integer.MAX_VALUE value means no toolbar representation.
     */
    int toolBarPosition() default Integer.MAX_VALUE;

    /**
     * Boolean key in preferences that corresponds to action's selected state.
     * <br/>
     * If set to non-empty string the action will be represented by a check-box
     * in menu and popup menu and the corresponding key will be set in
     * global mime-lookup <code>MimeLookup.getLookup(MimePath.EMPTY)</code>.
     */
    String preferencesKey() default "";

}
