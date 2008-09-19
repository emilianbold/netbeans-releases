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
 * Support for "Attach ..." dialog. Represents one type of attaching.
 *
 * @author   Jan Jancura
 */
public abstract class AttachType {

    /**
     * Provides display name of this Attach Type. Is used as one choice in
     * ComboBox.
     *
     * @return display name of this Attach Type
     */
    public abstract String getTypeDisplayName ();

    /**
     * Returns visual customizer for this Attach Type. Customizer can
     * optionally implement {@link Controller} interface. In that case please
     * notice the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()} and consider extending
     * {@link #getController()} method in case you need to provide
     * false validity in some cases.
     *
     * @return visual customizer for this Attach Type
     */
    public abstract JComponent getCustomizer ();

    /**
     * Return the implementation of {@link Controller} interface.<br/>
     * In cases when it's not desired to implement {@link Controller} interface
     * by the JComponent returned from {@link #getCustomizer()} method, because
     * of the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}, an explicit implementation
     * can be returned by overriding this method.
     * The default implementation returns the result of {@link #getCustomizer()}
     * if that implements the {@link Controller} interface, or <code>null</code>
     * otherwise.
     *
     * @return Controller implementation or <code>null</code>.
     * @since 2.14
     */
    public Controller getController() {
        JComponent c = getCustomizer();
        if (c instanceof Controller) {
            return ((Controller) c);
        } else {
            return null;
        }
    }

}