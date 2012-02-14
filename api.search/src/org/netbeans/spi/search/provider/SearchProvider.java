/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.spi.search.provider;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;

/**
 * Search provider can register complex search feature to the IDE.
 *
 * @author jhavlin
 */
public abstract class SearchProvider {

    /**
     * Create presenter for this search provider.
     *
     * @param replaceMode True if the presenter will be used in Replace dialog,
     * false if it will be used in Find dialog.
     * @return New presenter that will be used in the search dialog.
     */
    public abstract Presenter createPresenter(boolean replaceMode);

    /**
     * If replace is supported, this dialog will be shown in replace dialog.
     *
     * @return True if replace is supported by this type of search, false
     * otherwise.
     */
    public abstract boolean isReplaceSupported();

    /**
     * Tells whether this search provider is enabled. It can depend on operating
     * system, type of opened projects, or available databases.
     *
     * @return True if this provider is enabled at the moment, false otherwise.
     */
    public abstract boolean isEnabled();

    /**
     * Get help ID for this type of search. Can return null if it is not
     * available.
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * Presenter for search provider. This class is used to show panel and start
     * the search task.
     */
    public static abstract class Presenter {

        /**
         * Returns a new JComponent that contains controls for setting search
         * options. It will be shown as a tab in search dialog.
         *
         * This method will be called only once for each presenter.
         *
         * You should update inner state of this presenter from the component,
         * or store reference to created component in this presenter, so that
         * the current settings can be obtained from the form when a new search
         * is started, e.i. when {@link #composeSearch()} is called.
         */
        public abstract JComponent createForm();

        /**
         * Performs search considering current settings in the panel that was
         * returned by {@link #createForm()}
         *
         * @return A new search composition.
         */
        public abstract SearchComposition<?> composeSearch();

        /**
         * Test that the current settings specified in associated form are
         * usable for searching.
         *
         * @return True if search can start with the current settings, false
         * otherwise.
         */
        public abstract boolean isUsable();

        /**
         * Add change listener to the form. This listener should be notified
         * when usability of the search settings changes.
         *
         * @param cl Change listener that should be notified when usability of
         * the form changes.
         */
        public abstract void addUsabilityChangeListener(ChangeListener cl);

        /**
         * Method called when the dialog is closed. It should release allocated
         * resources. The default implementation does nothing.
         */
        public void clean() {
        }
    }
}
