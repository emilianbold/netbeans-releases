/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class DependenciesNode extends AbstractNode {
    /**
     * Create a dependencies node.  Note, this constructor will not cause a
     * reference to dependent projects or their artifacts to be held, and is
     * for use in the explorer view.  For editing dependencies, use the other
     * constructor.
     * 
     * @param project
     */
    public DependenciesNode (JCProject project) {
        super (DependenciesChildren.createChildren(project), Lookups.singleton(project));
        init();
    }

    public DependenciesNode (JCProject project, ResolvedDependencies rd) {
        super (DependenciesChildren.createChildren(project, rd), Lookups.fixed(project, rd));
        init();
    }

    private void init() {
        setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LIBRARIES_NODE_NAME"));
    }

    @Override
    public Image getIcon(int ignored) {
        return ImageUtilities.loadImage ("org/netbeans/modules/javacard/resources/libraries.gif");
    }

    @Override
    public Image getOpenedIcon(int ignored) {
        return getIcon(ignored);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { new AddDependencyAction() };
    }
}
