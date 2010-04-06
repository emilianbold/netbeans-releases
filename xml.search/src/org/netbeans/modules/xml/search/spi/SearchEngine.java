/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.search.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchEvent;
import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.api.SearchPattern;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.25
 */
public interface SearchEngine {

    /**
     * Performs search for the given option.
     * @throws SearchException if the search resulted in an unexpected error
     * @param option the given option for search
     */
    void search(SearchOption option) throws SearchException;

    /**
     * Returns true if search is applicable for root.
     * @param root where search will be perfromed
     * @return true if search is applicable for root
     */
    boolean isApplicable(Object root);

    /**
     * Addes search listener.
     * @param listener the given listener to be added
     */
    void addSearchListener(SearchListener listener);

    /**
     * Removes all search listeners.
     */
    void removeSearchListeners();

    /**
     * Returns the display name of engine.
     * @return the display name of engine
     */
    String getDisplayName();

    /**
     * Returns the short description of engine.
     * @return the short description of engine
     */
    String getShortDescription();

    // ----------------------------------------------------
    public abstract class Adapter implements SearchEngine {

        public Adapter() {
            removeSearchListeners();
        }

        public Object[] getTargets() {
            return new Object[]{};
        }

        public synchronized void addSearchListener(SearchListener listener) {
            mySearchListeners.add(listener);
        }

        public synchronized void removeSearchListeners() {
            mySearchListeners = new ArrayList<SearchListener>();
        }

        protected final synchronized void fireSearchStarted(SearchOption option) throws SearchException {
            try {
                createSearchPattern(option);
            } catch (PatternSyntaxException e) {
                throw new SearchException(e.getMessage());
            }
            SearchEvent event = new SearchEvent.Adapter(option, null);

            for (SearchListener listener : mySearchListeners) {
                listener.searchStarted(event);
            }
        }

        protected final synchronized void fireSearchFound(SearchElement element) {
            SearchEvent event = new SearchEvent.Adapter(null, element);

            for (SearchListener listener : mySearchListeners) {
                listener.searchFound(event);
            }
        }

        protected final synchronized void fireSearchFinished(SearchOption option) {
            SearchEvent event = new SearchEvent.Adapter(option, null);

            for (SearchListener listener : mySearchListeners) {
                listener.searchFinished(event);
            }
        }

        protected final boolean accepts(String text) {
            return mySearchPattern.accepts(text);
        }

        private void createSearchPattern(SearchOption option) {
            mySearchPattern = new SearchPattern(option.getText(), option.getSearchMatch(), option.isCaseSensitive());
        }

        private SearchPattern mySearchPattern;
        private List<SearchListener> mySearchListeners;
    }
}
