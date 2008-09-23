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

package org.netbeans.spi.debugger.ui;

import javax.swing.JComponent;

/**
 * Support for "New Breakpoint" dialog and Breakpoint Customizer. Represents
 * one breakpoint type.
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointType {

    /**
     * Display name of cathegory of this breakpoint type. Cathegory typically
     * represents one debugger language.
     *
     * @return display name of cathegory of this breakpoint type
     */
    public abstract String getCategoryDisplayName ();

    /**
     * Return display name of this breakpoint type (like "Line Breakppoint").
     *
     * @return display name of this breakpoint type
     */
    public abstract String getTypeDisplayName ();

    /**
     * Returns visual customizer for this breakpoint type. Customizer can 
     * optionally implement {@link Controller} interface. In that case please
     * notice the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()} and consider extending
     * {@link #getController()} method in case you need to provide
     * false validity in some cases.
     *
     * @return visual customizer for this breakpoint type
     */
    public abstract JComponent getCustomizer ();

    /**
     * Return the implementation of {@link Controller} interface.<br/>
     * In cases when it's not desired to implement {@link Controller} interface
     * by the JComponent returned from {@link #getCustomizer()} method, because
     * of the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}, an explicit implementation
     * can be returned by overriding this method.
     * The default implementation returns  <code>null</code>, in which case
     * the customizer component is cast to {@link Controller}.
     *
     * @return Controller implementation or <code>null</code>.
     * @since 2.14
     */
    public Controller getController() {
        return null;
    }

    /**
     * Should return true of this breakpoint type should be default one in 
     * the current context. Default breakpoint type is selected one when the
     * New Breakpoint dialog is opened.
     *
     * @return true of this breakpoint type should be default
     */
    public abstract boolean isDefault ();
}