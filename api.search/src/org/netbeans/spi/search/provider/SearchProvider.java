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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * Search provider can register complex search feature to the IDE.
 * 
 * <div class="nonnormative">
 *   <p>Example:</p>
 *   <pre>
 * // use annotation to register this provider
 * {@code @}ServiceProvider(service = SearchProvider.class)
 * public class ExampleSearchProvider extends SearchProvider {
 *
 *   // Create presenter
 *   public Presenter createPresenter(boolean replaceMode) {
 *       return new BasicSearchPresenter(replaceMode);
 *   }
 *
 *   // Replacing is supported.
 *   public boolean isReplaceSupported() {
 *       return true;
 *   }
 *   
 *   // This search provider is always enabled.
 *   public boolean isEnabled() {
 *       return true; // could be disabled on some platforms
 *   }
 * }</pre>
 * </div>
 *
 * @author jhavlin
 */
public abstract class SearchProvider {

    /** Constructor for subclasses. */
    protected SearchProvider() {}

    /**
     * Create presenter for this search provider.
     *
     * @param replaceMode True if the presenter will be used in Replace dialog,
     * false if it will be used in Find dialog.
     * @return New presenter that will be used in the search dialog.
     */
    public abstract @NonNull Presenter createPresenter(boolean replaceMode);

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
    public @CheckForNull HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * Presenter for search provider. This class is used to show panel and start
     * the search task.
     * 
     * <div class="nonnormative">
     *   <p>Example:</p>
     *   <pre>
     * {@code
     * class ExampleSearchPresenter
     *       extends BasicSearchProvider.Presenter {
     *
     *   BasicSearchForm form = null;
     *   private boolean replacing;
     *
     *   public ExampleSearchPresenter(boolean replacing) {
     *       this.replacing = replacing;
     *   }

     *   public JComponent createForm() {
     *       if (form == null) {
     *           form = new Form(); // create a new with UI for specifying search settings
     *           form.setName("Example Provider");
     *       }
     *       return form;
     *   }
     *
     *   public SearchComposition<Def> composeSearch() {
     *       // here create a new search composition appropriate for settings 
     *       // specified in the form.
     *   }
     *
     *   public boolean isUsable() {
     *       return form.isUsable();
     *   }
     *
     *   public void addUsabilityChangeListener(ChangeListener cl) {
     *       form.setUsabilityChangeListener(cl);
     *   }
     *
     *   public void clean() {
     *       super.clean();
     *       form.clean();
     *   }    
     * }}</pre>
     * </div>
     */
    public static abstract class Presenter {

        private final List<ChangeListener> changeListeners =
                new CopyOnWriteArrayList<ChangeListener>();

        /** Constructor for subclasses. */
        protected Presenter() {}

        /**
         * Returns a new JComponent that contains controls for setting search
         * options. It will be shown as a tab in search dialog.
         *
         * This method will be called only once for each presenter.
         *
         * You should update inner state of this presenter from the component,
         * or store reference to created component in this presenter, so that
         * the current settings can be obtained from the form when a new search
         * is started, i.e. when {@link #composeSearch()} is called.
         */
        public abstract @NonNull JComponent createForm();

        /**
         * Performs search considering current settings in the panel that was
         * returned by {@link #createForm()}
         *
         * @return A new search composition.
         */
        public abstract @NonNull SearchComposition<?> composeSearch();

        /**
         * Test that the current settings specified in associated form are
         * usable for searching.
         *
         * @return True if search can start with the current settings, false
         * otherwise.
         */
        public abstract boolean isUsable();

        /**
         * Add change listener to the form. This listener is notified when the
         * search settings change.
         *
         * @param changeListener Change listener that is notified when values in
         * the form change.
         */
        public final void addChangeListener(
                @NonNull ChangeListener changeListener) {

            Parameters.notNull("changeListener", changeListener);
            changeListeners.add(changeListener);
        }

        /**
         * Remove listener added by {@link #addChangeListener(ChangeListener)}.
         *
         * @param changeListener Listener to remove.
         */
        public final void removeChangeListener(
                @NonNull ChangeListener changeListener) {
            Parameters.notNull("changeListener", changeListener);
            changeListeners.remove(changeListener);
        }

        /**
         * Call this method to notify all change listeners whenever a change in
         * the presenter occurs.
         */
        protected final void fireChange() {
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener cl : changeListeners) {
                cl.stateChanged(e);
            }
        }

        /**
         * Method called when the dialog is closed. It should release allocated
         * resources. The default implementation does nothing.
         */
        public void clean() {
        }
    }
}
