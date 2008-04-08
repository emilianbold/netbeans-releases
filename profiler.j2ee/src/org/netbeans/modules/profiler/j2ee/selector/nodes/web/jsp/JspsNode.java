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

package org.netbeans.modules.profiler.j2ee.selector.nodes.web.jsp;

import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.j2ee.WebProjectUtils;
import org.netbeans.modules.profiler.j2ee.ui.Utils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.profiler.selector.spi.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.spi.nodes.GreedySelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.ProjectNode;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorChildren;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class JspsNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<JspsNode> {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(JspsNode parent) {
            List<SelectorNode> components = new ArrayList<SelectorNode>();

            Project project = ((ProjectNode) parent.getParent()).getProject();
            Collection<FileObject> fos = WebProjectUtils.getDocumentBaseFileObjects(project, true);

            for (FileObject fo : fos) {
                enumerateJSPs(fo, components, parent);
            }

            return components;
        }

        private void enumerateJSPs(FileObject fo, List<SelectorNode> components, JspsNode parent) {
            Set<JSPFolderNode> folders = new HashSet<JSPFolderNode>();
            Set<JSPNode> jsps = new HashSet<JSPNode>();

            for (FileObject child : fo.getChildren()) {
                if (child.isFolder()) {
                    JSPFolderNode folderNode = new JSPFolderNode(child, parent);

                    if (!folderNode.isLeaf()) {
                        folders.add(folderNode);
                    }
                } else {
                    if (child.getExt().equalsIgnoreCase("jsp")) {
                        // NOI18N
                        jsps.add(new JSPNode(child, parent));
                    }
                }
            }

            List<JSPFolderNode> foldersList = new ArrayList<JSPFolderNode>(folders);
            List<JSPNode> jspsList = new ArrayList<JSPNode>(jsps);

            Collections.sort(foldersList, JSPFolderNode.COMPARATOR);
            components.addAll(foldersList);
            Collections.sort(jspsList, JSPNode.COMPARATOR);
            components.addAll(jspsList);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String JSPS_STRING = NbBundle.getMessage(JspsNode.class, "JspsNode_JspsString"); // NOI18N
                                                                                                          // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of JspsNode */
    public JspsNode(ContainerNode parent) {
        super(JSPS_STRING, Utils.PACKAGE_ICON, parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }
}
