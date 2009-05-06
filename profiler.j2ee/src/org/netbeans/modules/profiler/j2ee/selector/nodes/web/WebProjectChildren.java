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

package org.netbeans.modules.profiler.j2ee.selector.nodes.web;

import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.j2ee.selector.nodes.web.filter.FiltersNode;
import org.netbeans.modules.profiler.j2ee.selector.nodes.web.jsp.JspsNode;
import org.netbeans.modules.profiler.j2ee.selector.nodes.web.listener.ListenersNode;
import org.netbeans.modules.profiler.j2ee.selector.nodes.web.servlet.ServletsNode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.profiler.j2ee.WebProjectUtils;
import org.netbeans.modules.profiler.j2ee.selector.nodes.ProjectNode;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class WebProjectChildren extends SelectorChildren<ProjectNode> {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Project project;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public WebProjectChildren(final Project project) {
        this.project = project;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected List<SelectorNode> prepareChildren(ProjectNode parent) {
        List<SelectorNode> contents = new ArrayList<SelectorNode>();
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            if (provider.getServerInstanceID() != null && !provider.getServerInstanceID().equals("DEV-NULL")) { // NOI18N
                SelectorNode content = new ServletsNode(parent);

                if (!content.isLeaf()) {
                    contents.add(content);
                }

                content = new FiltersNode(parent);

                if (!content.isLeaf()) {
                    contents.add(content);
                }

                content = new ListenersNode(parent);

                if (!content.isLeaf()) {
                    contents.add(content);
                }

                content = new JspsNode(parent);

                if (!content.isLeaf()) {
                    contents.add(content);
                }
            } else {
                Logger.getLogger(WebProjectChildren.class.getName()).warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/profiler/j2ee/selector/Bundle").getString("ROOT_METHODS_NOT_AVAILABLE"));
            }
        }

        return contents;
    }
}
