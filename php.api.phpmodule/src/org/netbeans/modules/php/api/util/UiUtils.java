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

package org.netbeans.modules.php.api.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.api.ui.SearchPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Miscellaneous UI utilities.
 * @author Tomas Mysik
 */
public final class UiUtils {
    /**
     * SFS path where all the PHP options can be found.
     */
    public static final String OPTIONS_PATH = "org-netbeans-modules-php-project-ui-options-PHPOptionsCategory"; // NOI18N

    private UiUtils() {
    }

    /**
     * Display a dialog with the message and then open IDE PHP options.
     * @param message message to display before IDE options are opened
     * @see #invalidScriptProvided(String, String)
     */
    public static void invalidScriptProvided(String message) {
        invalidScriptProvided(message, null);
    }

    /**
     * Display a dialog with the message and then open IDE options.
     * @param message message to display before IDE options are opened
     * @param optionsSubcategory IDE options subcategory to open (suitable e.g. for frameworks)
     * @see #invalidScriptProvided(String)
     */
    public static void invalidScriptProvided(String message, String optionsSubcategory) {
        Parameters.notNull("message", message);

        informAndOpenOptions(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE), optionsSubcategory);
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Opens IDE options, PHP General category.
     * @param exc {@link ExecutionException} thrown
     * @see #processExecutionException(ExecutionException, String)
     */
    public static void processExecutionException(ExecutionException exc) {
        processExecutionException(exc, null);
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Opens IDE options, PHP &lt;subcategory> category or General category if no <code>subcategory</code> given.
     * @param exc {@link ExecutionException} thrown
     * @param optionsSubcategory IDE options subcategory to open (suitable e.g. for frameworks)
     * @see #processExecutionException(ExecutionException)
     */
    public static void processExecutionException(ExecutionException exc, final String optionsSubcategory) {
        Parameters.notNull("exc", exc);

        final Throwable cause = exc.getCause();
        assert cause != null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                informAndOpenOptions(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UiUtils.class, "MSG_ExceptionDuringRunScript", cause.getLocalizedMessage()), NotifyDescriptor.ERROR_MESSAGE),
                        optionsSubcategory);
            }
        });
    }

    /**
     * Display Options dialog with PHP > General panel preselected.
     * @see #showOptions(String)
     * @since 1.20
     */
    public static void showGeneralOptions() {
        showOptions(null);
    }

    /**
     * Display Options dialog with PHP > &lt;subcategory> panel preselected.
     * @param optionsSubcategory PHP Options subcategory to be opened, can be <code>null</code> (then, the General panel is opened)
     * @see #showGeneralOptions()
     * @since 1.20
     */
    public static void showOptions(String optionsSubcategory) {
        String path = OPTIONS_PATH;
        if (StringUtils.hasText(optionsSubcategory)) {
            path += "/" + optionsSubcategory; // NOI18N
        }
        OptionsDisplayer.getDefault().open(path);
    }

    private static void informAndOpenOptions(NotifyDescriptor descriptor, String optionsSubcategory) {
        assert descriptor != null;

        DialogDisplayer.getDefault().notify(descriptor);
        showOptions(optionsSubcategory);
    }

    /**
     * Utility class for searching which is done in a separate thread so the UI is not blocked.
     */
    public static final class SearchWindow {
        private SearchWindow() {
        }

        /**
         * Open a serch window, start searching (in a separate thread) and display the results.
         * @param support {@link SearchWindowSupport search window support}
         * @return selected item (can be <code>null</code>) if user clicks OK button, <code>null</code> otherwise
         */
        public static String search(SearchWindowSupport support) {
            Parameters.notNull("support", support);

            SearchPanel panel = SearchPanel.create(support);
            if (panel.open()) {
                return panel.getSelectedItem();
            }
            return null;
        }

        public interface SearchWindowSupport {
            /**
             * Detector which runs in a separate thread and its results are displayed to a user.
             * @return list of search result
             */
            List<String> detect();
            /**
             * Get the title of the window.
             * @return the title of the window
             */
            String getWindowTitle();
            /**
             * Get the title of the list of items.
             * @return the title of the list of items
             */
            String getListTitle();
            /**
             * Get the "important" part (e.g. "PHPUnit script") of message that is displayed during running of a {@link #detect() detect} method.
             * @return the "important" part (e.g. "PHPUnit script") of message that is displayed during running of a {@link #detect() detect} method
             */
            String getPleaseWaitPart();
            /**
             * Get message that is displayed when no items are found.
             * @return message that is displayed when no items are found
             */
            String getNoItemsFound();
        }
    }
}
