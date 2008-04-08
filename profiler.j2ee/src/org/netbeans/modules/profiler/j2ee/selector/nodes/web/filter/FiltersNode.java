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

package org.netbeans.modules.profiler.j2ee.selector.nodes.web.filter;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.profiler.j2ee.WebProjectUtils;
import org.netbeans.modules.profiler.j2ee.ui.Utils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.spi.nodes.GreedySelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class FiltersNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<FiltersNode> {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Set<ClassIndex.SearchScope> scope = new HashSet<ClassIndex.SearchScope>();

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Children() {
            scope.add(ClassIndex.SearchScope.SOURCE);
            scope.add(ClassIndex.SearchScope.DEPENDENCIES);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(final FiltersNode parent) {
            final Set<SelectorNode> filters = new HashSet<SelectorNode>();

            try {
                Project project = parent.getProject();
                final ClasspathInfo cpInfo = ProjectUtilities.getClasspathInfo(project);

                Collection<FileObject> dds = WebProjectUtils.getDeploymentDescriptorFileObjects(project, true);

                for (FileObject dd : dds) {
                    enumerateFilters(parent, cpInfo, filters, dd);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return new ArrayList<SelectorNode>(filters);
        }

        private void enumerateFilters(final FiltersNode parent, final ClasspathInfo cpInfo,
                                      final Collection<SelectorNode> filters, FileObject dd)
                               throws IOException, IllegalArgumentException {
            final WebApp webApp = DDProvider.getDefault().getDDRoot(dd);

            final Map<String, String> filter2class = new HashMap<String, String>();

            for (Filter filter : webApp.getFilter()) {
                filter2class.put(filter.getFilterName(), filter.getFilterClass());
            }

            JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                             throws Exception {
                        for (FilterMapping mapping : webApp.getFilterMapping()) {
                            TypeElement type = controller.getElements().getTypeElement(filter2class.get(mapping.getFilterName()));

                            if (type != null) {
                                String urlMapping = mapping.getUrlPattern();

                                if (urlMapping == null) {
                                    String servletName = mapping.getServletName();

                                    for (ServletMapping servletMapping : webApp.getServletMapping()) {
                                        if (servletName.equals(servletMapping.getServletName())) {
                                            urlMapping = servletMapping.getUrlPattern();

                                            break;
                                        }
                                    }
                                }

                                if (urlMapping != null) {
                                    filters.add(new FilterNode(cpInfo, type, mapping.getFilterName(), urlMapping, parent));
                                }
                            }
                        }
                    }
                }, true);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String FILTERS_STRING = NbBundle.getMessage(FiltersNode.class, "FiltersNode_FiltersString"); // NOI18N
                                                                                                                      // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ServletsNode */
    public FiltersNode(ContainerNode parent) {
        super(FILTERS_STRING, Utils.PACKAGE_ICON, parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }
}
