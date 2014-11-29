/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.openide.util.RequestProcessor;

/**
 * npm library provider.
 * 
 * The clients of this provider are expected to use {@link #findLibraries}
 * method to search libraries matching given search term. The search
 * for the libraries is performed asynchronously. Hence, this method
 * returns {@code null} when it is called for the first time for the given
 * search term. The clients should register property change listeners
 * on the provider to be notified when the result of the search is available.
 * The property change events fired by the provider will have the property
 * name set to the search term and the new value to the result of the search.
 * The new value may be set to {@code null} when the search failed for
 * some reason (the new value is set to an empty array when the result
 * of the search is empty).
 * 
 * @author Jan Stola
 */
public class LibraryProvider {
    /** Library providers for individual projects. */
    private static final Map<Project,LibraryProvider> providers = new WeakHashMap<>();
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(LibraryProvider.class.getName(), 3);
    /** Project for which the libraries should be provided. */
    private final Project project;
    /** Cache of the search results. It maps the search term to the search result. */
    private final Map<String,WeakReference<Library[]>> cache =
            Collections.synchronizedMap(new HashMap<String,WeakReference<Library[]>>());
    /** Property change support. */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Creates a new {@code LibraryProvider} for the given project.
     * 
     * @param project project for which the libraries should be provided.
     */
    private LibraryProvider(Project project) {
        this.project = project;
    }

    /**
     * Returns library provider for the given project.
     * 
     * @param project project for which the library provider should be returned.
     * @return library provider for the given project.
     */
    public static synchronized LibraryProvider forProject(Project project) {
        LibraryProvider provider = providers.get(project);
        if (provider == null) {
            provider = new LibraryProvider(project);
            providers.put(project, provider);
        }
        return provider;
    }

    /**
     * Adds a property change listener to this provider. The listener
     * is notified whenever a new search result is available.
     * 
     * @param listener listener to register.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from this provider.
     * 
     * @param listener listener to unregister.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Finds the libraries matching the given search term. This method returns
     * {@code null} when the result of the search is not present in the cache
     * already. It starts the corresponding search in this case and reports
     * its result by firing a property change event with the property name
     * equal to the given search term. The result of the search can be obtained
     * through the new value property of the event or by another invocation
     * of this method. The first approach is recommended as it allows
     * to recognize that the search failed. The new value property of the
     * event is set to {@code null} in such case.
     * 
     * @param searchTerm search term.
     */
    public Library[] findLibraries(String searchTerm) {
        WeakReference<Library[]> reference = cache.get(searchTerm);
        Library[] result = null;
        if (reference != null) {
            result = reference.get();
        }
        if (result == null) {
            SearchTask task = new SearchTask(searchTerm);
            RP.post(task);
        }
        return result;
    }

    /**
     * Updates the cache with the result of the search.
     * 
     * @param searchTerm search term.
     * @param libraries libraries matching the search term.
     */
    void updateCache(String searchTerm, Library[] libraries) {
        if (libraries != null) {
            WeakReference<Library[]> reference = new WeakReference<>(libraries);
            cache.put(searchTerm, reference);
        }
        propertyChangeSupport.firePropertyChange(searchTerm, null, libraries);
    }

    /**
     * Search task - a task that performs one search for libraries matching
     * the given search term.
     */
    private class SearchTask implements Runnable {
        /** Search term. */
        private final String searchTerm;

        /**
         * Creates a new {@code SearchTask} for the given search term.
         * 
         * @param searchTerm search term.
         */
        SearchTask(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        @Override
        public void run() {
            NpmExecutable executable = NpmExecutable.getDefault(project, false);
            if (executable != null) {
                String result = executable.search(searchTerm);
                Library[] libraries = result == null ? null : parseSearchResult(result);
                updateCache(searchTerm, libraries);
            }
        }

        /**
         * Parses the output of npm search call.
         * 
         * @param searchResult output of the npm search call.
         * @return libraries/packages returned by the search.
         */
        private Library[] parseSearchResult(String searchResult) {
            String[] lines = searchResult.split("\n"); // NOI18N
            String header = lines[0];
            int descriptionIndex = header.indexOf("DESCRIPTION"); // NOI18N
            int authorIndex = header.indexOf("AUTHOR"); // NOI18N
            int versionIndex = header.indexOf("VERSION"); // NOI18N
            int keywordsIndex = header.indexOf("KEYWORDS"); // NOI18N
            List<Library> libraryList = new LinkedList<>();
            String description = ""; // NOI18N
            for (int i=lines.length-1; i>=1; i--) {
                String line = lines[i];
                String name = line.substring(0,descriptionIndex).trim();
                int length = line.length();
                String descriptionPart = length < authorIndex
                        ? line.substring(descriptionIndex).trim()
                        : line.substring(descriptionIndex, authorIndex).trim();
                description = descriptionPart + "\n" + description; // NOI18N
                if (!"".equals(name)) { // NOI18N
                    String versionName = length < keywordsIndex
                            ? line.substring(versionIndex).trim()
                            : line.substring(versionIndex, keywordsIndex).trim();
                    String keywords = length < keywordsIndex
                            ? "" // NOI18N
                            : line.substring(keywordsIndex).trim();
                    Library library = new Library(name);
                    library.setDescription(description.trim());
                    if (!keywords.isEmpty()) {
                        library.setKeywords(keywords.split(" ")); // NOI18N
                    }
                    Library.Version version = new Library.Version(library, versionName);
                    library.setLatestVersion(version);
                    libraryList.add(0, library);
                    description = ""; // NOI18N
                }
            }
            return libraryList.toArray(new Library[libraryList.size()]);
        }

    }

}
