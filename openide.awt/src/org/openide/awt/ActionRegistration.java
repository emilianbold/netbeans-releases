/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.awt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.ActionMap;

/** Registers an action under associated identifier specified by separate
 * {@link ActionID} annotation on the same element. Here is few usage examples:
 * <ul>
 *   <li>{@link Actions#alwaysEnabled(java.awt.event.ActionListener, java.lang.String, java.lang.String, boolean) always enabled action}</li>
 *   <li>{@link Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean) callback action}</li>
 *   <li>{@link Actions#context(java.lang.Class, boolean, boolean, org.openide.util.ContextAwareAction, java.lang.String, java.lang.String, java.lang.String, boolean)  context aware action} </li>
 * </ul>
 * 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 7.26
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ActionRegistration {
    /** Display name. Usually prefixed with '#' to reference value from a 
     * <code>Bundle.properties</code> file in the same package.
     * @return display name for the action
     */
    String displayName();
    
    /** 
     * Provides the JMenuItem text if one wants to use other than 
     * the name of the action returned by {@link #displayName()}.
     * 
     * @return display name for the action
     * 
     * @see displayName
     * @see Actions#connect(javax.swing.JMenuItem, javax.swing.Action, boolean) 
     * @since 7.35
     */    
    String menuText() default "";
    
    /** 
     * Provides the JMenuItem popup text if one wants to use other   
     * than the name of the action returned by {@link #displayName()}.
     * 
     * @return display name for the action in a popup menu
     * 
     * @see displayName
     * @see Actions#connect(javax.swing.JMenuItem, javax.swing.Action, boolean) 
     * @since 7.35
     */
    String popupText() default "";
    
    /** Path to image representing the action's icon.
     * @return "org/myproject/mypkg/Icon.png"
     */
    String iconBase() default "";
    /** Shall the action's icon be visible in menu?
     * @return true or false
     */
    boolean iconInMenu() default true;
    /** Shall this action be associated with a particular key in an
     * {@link ActionMap}? E.g. behave like {@link Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean)} one?
     * @return the value of the key to seek in currently selected {@link ActionMap}
     */
    String key() default "";
    /** Shall the action be performed outside of AWT thread.
     * @return false, if the action shall run synchronously
     */
    boolean asynchronous() default false;
    /** Shall the action work on last selection when it was enabled?
     */
    boolean surviveFocusChange() default false;
}
