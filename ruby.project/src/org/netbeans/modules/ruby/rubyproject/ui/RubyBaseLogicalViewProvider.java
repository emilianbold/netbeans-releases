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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.rubyproject.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.bundler.BundlerSupport;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Base for Ruby project's logical view providers.
 */
public abstract class RubyBaseLogicalViewProvider implements LogicalViewProvider {

    private final RubyBaseProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private List<ChangeListener> changeListeners;
    protected final BundlerSupport bundlerSupport;

    protected RubyBaseLogicalViewProvider(
            final RubyBaseProject project,
            final UpdateHelper updateHelper,
            final PropertyEvaluator evaluator,
            final ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = updateHelper;
        assert updateHelper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
        this.bundlerSupport = new BundlerSupport(project);
    }

    public final RubyBaseProject getProject() {
        return project;
    }

    public final PropertyEvaluator getEvaluator() {
        return evaluator;
    }

    public final ReferenceHelper getRefHelper() {
        return resolver;
    }

    public final UpdateHelper getUpdateHelper() {
        return helper;
    }

    protected abstract Node findWithPathFinder(Node root, FileObject target);

    public synchronized void addChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            this.changeListeners = new ArrayList<ChangeListener>();
        }
        this.changeListeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            return;
        }
        this.changeListeners.remove(l);
    }

    protected Node findWithPathFinder1(final Node root, final FileObject target) {
        TreeRootNode.PathFinder pf2 = root.getLookup().lookup(TreeRootNode.PathFinder.class);
        if (pf2 != null) {
            Node n = pf2.findPath(root, target);
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    public final Node findPath(Node root, Object target) {
        Project _project = root.getLookup().lookup(Project.class);
        if (_project == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject targetFO = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(targetFO);
            if (!_project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }

            Node[] rootChildren = root.getChildren().getNodes(true);
            for (int i = 0; i < rootChildren.length; i++) {
                Node n = findWithPathFinder(rootChildren[i], targetFO);
                if (n != null) {
                    return n;
                }
                DataObject dObj = rootChildren[i].getLookup().lookup(DataObject.class);
                if (dObj == null) {
                    continue;
                }
                FileObject childFO = dObj.getPrimaryFile();
                if (targetFO.equals(childFO)) {
                    return rootChildren[i];
                }
            }
        }

        return null;
    }
}
