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
 *
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

package org.netbeans.modules.mobility.project.ui;
import org.netbeans.api.project.*;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;


/**
 * Support for creating logical views.
 * @author Petr Hrebejk, Adam Sotona
 */
public class J2MEPhysicalViewProvider implements LogicalViewProvider {
        
    protected final ReferenceHelper refHelper;
    protected final ProjectConfigurationsHelper pcp;
    protected final AntProjectHelper helper;
    protected final J2MEProject project;
    J2MEProjectRootNode rootNode;
    
    public J2MEPhysicalViewProvider(Project project, AntProjectHelper helper, ReferenceHelper refHelper, ProjectConfigurationsHelper pcp) {
        this.project = (J2MEProject)project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.refHelper = refHelper;
        assert refHelper != null;
        this.pcp = pcp;
        assert pcp != null;
    }
    
    public Node createLogicalView() {
        try {
            return rootNode=new J2MEProjectRootNode(project);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            return Node.EMPTY;
        }
    }
    
    public Node findPath(final Node root, final Object target) {
        if ( project == null ) {
            return null;
        }
        if ( target instanceof FileObject ) {
            final FileObject fo = (FileObject)target;
            final Project owner = FileOwnerQuery.getOwner( fo );
            if ( !project.equals( owner ) ) {
                return null; // Don't waste time if project does not own the fo
            }
            
            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
