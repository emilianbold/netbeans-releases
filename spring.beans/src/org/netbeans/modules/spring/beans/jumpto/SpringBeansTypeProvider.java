/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.jumpto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.FileSpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides Spring bean definitions for the Go To Type dialog
 * 
 * @author Rohan Ranade
 */
public class SpringBeansTypeProvider implements TypeProvider {

    private Set<AbstractBeanTypeDescriptor> cache;
    private String lastRefreshText;
    private SearchType lastRefreshSearchType;
    private volatile boolean isCancelled = false;

    public String name() {
        return "springbeans"; // NOI18N

    }

    public String getDisplayName() {
        return NbBundle.getMessage(SpringBeansTypeProvider.class, "LBL_SpringBeansType"); // NOI18N

    }

    public void computeTypeNames(Context context, Result result) {
        assert context.getProject() == null; // Issue 136025
        
        isCancelled = false;
        boolean cacheRefresh = false;

        final String searchText = context.getText();
        final SearchType searchType = context.getSearchType();
        final Matcher matcher = getMatcher(searchText, searchType);
        if (matcher == null) {
            return;
        }

        if (lastRefreshText == null || lastRefreshSearchType == null || !searchText.startsWith(lastRefreshText) || lastRefreshSearchType != searchType || cache == null) {
            // refresh cache
            cacheRefresh = true;
            final Set<AbstractBeanTypeDescriptor> currCache = new HashSet<AbstractBeanTypeDescriptor>();
            Future<Project[]> prjHandle = OpenProjects.getDefault().openProjects();

            Project[] projects = null;
            try {
                projects = prjHandle.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (projects == null) {
                return;
            }

            if (isCancelled) {
                return;
            }
            for (Project project : projects) {
                ProjectSpringScopeProvider scopeProvider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
                if (scopeProvider == null) {
                    continue;
                }

                SpringScope scope = scopeProvider.getSpringScope();
                if (scope == null) {
                    continue;
                }

                if (isCancelled) {
                    return;
                }
                final Set<File> processed = new HashSet<File>();
                List<SpringConfigModel> models = scope.getAllConfigModels();
                for (SpringConfigModel model : models) {
                    try {
                        if (isCancelled) {
                            return;
                        }
                        model.runDocumentAction(new Action<DocumentAccess>() {

                            public void run(DocumentAccess docAccess) {
                                File file = docAccess.getFile();
                                if (processed.contains(file)) {
                                    return;
                                }
                                processed.add(file);

                                if (isCancelled) {
                                    return;
                                }
                                FileObject fo = docAccess.getFileObject();
                                FileSpringBeans fileBeans = docAccess.getSpringBeans().getFileBeans(fo);
                                List<SpringBean> beans = fileBeans.getBeans();

                                for (SpringBean bean : beans) {
                                    String id = bean.getId();
                                    if (id != null && matcher.match(id)) {
                                        currCache.add(new BeanTypeDescriptor(id, bean));
                                    }

                                    for (String name : bean.getNames()) {
                                        if (matcher.match(name)) {
                                            currCache.add(new BeanTypeDescriptor(name, bean));
                                        }
                                    }
                                }

                                for (String alias : fileBeans.getAliases()) {
                                    if (matcher.match(alias)) {
                                        currCache.add(new BeanAliasTypeDescriptor(alias, fo));
                                    }
                                }
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            if (!isCancelled) {
                cache = currCache;
                lastRefreshText = searchText;
                lastRefreshSearchType = searchType;
            }
        }

        if (cache != null) {
            ArrayList<AbstractBeanTypeDescriptor> beans = new ArrayList<AbstractBeanTypeDescriptor>(cache.size());
            for (AbstractBeanTypeDescriptor beanTypeDescriptor : cache) {
                if (cacheRefresh || matcher.match(beanTypeDescriptor.getSimpleName())) {
                    beans.add(beanTypeDescriptor);
                }
            }

            result.addResult(beans);
        }
    }

    public void cancel() {
        isCancelled = true;

    }

    public void cleanup() {
        isCancelled = false;
        cache = null;
        lastRefreshText = null;
        lastRefreshSearchType = null;
    }

    private interface Matcher {

        boolean match(String str);
    }
    
    private static Pattern camelCaseBlock = Pattern.compile("(\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\d|\\.|\\-)*)"); // NOI18N

    private Matcher getMatcher(String searchText, SearchType searchType) {
        switch (searchType) {
            case EXACT_NAME:
                return new StringMatcher(searchText, false, true);
            case CASE_INSENSITIVE_EXACT_NAME:
                return new StringMatcher(searchText, false, false);
            case PREFIX:
                return new StringMatcher(searchText, true, true);
            case CASE_INSENSITIVE_PREFIX:
                return new StringMatcher(searchText, true, false);
            case REGEXP:
                String regex = searchText + "*"; // NOI18N

                regex = regex.replace("*", ".*").replace('?', '.'); // NOI18N

                return new RegExpMatcher(regex, true);
            case CASE_INSENSITIVE_REGEXP:
                regex = searchText + "*"; // NOI18N

                regex = regex.replace("*", ".*").replace('?', '.'); // NOI18N

                return new RegExpMatcher(regex, false);
            case CAMEL_CASE:
                java.util.regex.Matcher m = camelCaseBlock.matcher(searchText);
                StringBuilder sb = new StringBuilder();
                while (m.find()) {
                    sb.append(m.group()).append("(?:\\p{javaLowerCase}|\\d|\\.|\\-)*"); // NOI18N

                }
                sb.append(".*"); // NOI18N
                return new RegExpMatcher(sb.toString(), false);
        }

        assert false; // probably a new type got added to SearchType, would need fixing on our part

        return null;
    }

    private static final class RegExpMatcher implements Matcher {

        private final String regex;
        private final boolean caseSensitive;
        private final Pattern pattern;

        public RegExpMatcher(String regex, boolean caseSensitive) {
            this.regex = regex;
            this.caseSensitive = caseSensitive;
            this.pattern = caseSensitive ? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        public boolean match(String str) {
            return pattern.matcher(str).matches();
        }
    }

    private static final class StringMatcher implements Matcher {

        private final String searchText;
        private final boolean prefix;
        private final boolean caseSensitive;

        public StringMatcher(String searchText, boolean prefix, boolean caseSensitive) {
            this.searchText = searchText;
            this.prefix = prefix;
            this.caseSensitive = caseSensitive;
        }

        public boolean match(String str) {
            int length = prefix ? searchText.length() : Math.max(str.length(), searchText.length());
            return str.regionMatches(caseSensitive, 0, searchText, 0, length);
        }
    }
}
