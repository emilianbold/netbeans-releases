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

package org.netbeans.modules.php.spi.phpmodule;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.util.HelpCtx;

/**
 * Provides support for extending a PHP module properties (via Project Properties dialog).
 * For Reading and storing properties, {@link PhpModule#getPreferences(Class, boolean)} can be used.
 *
 * @author Tomas Mysik
 * @since 1.26
 */
public abstract class PhpModuleCustomizerExtender {

    /**
     * Returns the display name of this extender. This method
     * is meant to return a shorter name then
     * {@link PhpFramewor Provider#getName()} (which is used if {@code null} is returned).
     * @return display name of the category, can be {@code null}.
     */
    public abstract String getDisplayName();

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     *
     * @param  listener a listener.
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public abstract void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     *
     * @return a component that provides configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    public abstract JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     *
     * @return a help context; can be <code>null</code>.
     */
    public abstract HelpCtx getHelp();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     * <p>
     * If it returns <code>false</code>, check {@link #getErrorMessage() error message}, it
     * should not be <code>null</code>.
     *
     * @return <code>true</code> if the configuration is valid, <code>false</code> otherwise.
     * @see #getErrorMessage()
     */
    public abstract boolean isValid();

    /**
     * Get error message or <code>null</code> if the {@link #getComponent component} is {@link #isValid() valid}.
     * @return error message or <code>null</code> if the {@link #getComponent component} is {@link #isValid() valid}
     * @see #isValid()
     */
    public abstract String getErrorMessage();

    /**
     * Called to extend properties of the given PHP module. This method
     * is called only if user clicks on the OK button; also, it cannot be called
     * if {@link #isValid()} is <code>false</code>.
     * <i>This method should be as fast as possible.</i>
     *
     * @param  phpModule the PHP module which properties are to be extended; never <code>null</code>
     * @see #isValid()
     */
    public abstract void save(PhpModule phpModule);
}
