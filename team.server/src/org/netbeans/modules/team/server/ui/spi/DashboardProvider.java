/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.team.server.ui.spi;

import java.util.Collection;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.team.server.ui.common.MyProjectNode;
import org.netbeans.modules.team.server.ui.common.SourceListNode;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.Lookup;

/**
 * Provides Team Dashboard relevant functionality.
 *
 * @author Tomas Stupka
 * @param <P>
 */
public abstract class DashboardProvider<P> {

    public abstract LeafNode createMemberNode(MemberHandle user, TreeListNode parent);
    public abstract TreeListNode createProjectLinksNode(TreeListNode pn, ProjectHandle<P> project);
    public abstract JComponent createProjectLinksComponent(ProjectHandle<P> project);
    public abstract TreeListNode createSourceListNode(TreeListNode pn, ProjectHandle<P> project);
    public abstract MyProjectNode createMyProjectNode(ProjectHandle<P> project, boolean canOpen, boolean canBookmark, Action closeAction);
    public abstract TreeListNode createSourceNode(SourceHandle s, SourceListNode sln);

    public abstract ProjectAccessor<P> getProjectAccessor();
    public abstract MessagingAccessor<P> getMessagingAccessor();
    public abstract MemberAccessor<P> getMemberAccessor();
    public abstract SourceAccessor<P> getSourceAccessor();
    public abstract QueryAccessor<P> getQueryAccessor();
    public abstract BuilderAccessor<P> getBuilderAccessor();
    
    public RemoteMachineAccessor<P> getRemoteMachineAccessor() {
        return null;
    }

    public abstract Collection<ProjectHandle<P>> getMyProjects(); // XXX move to accessor

    public QueryAccessor<P> getQueryAccessor(Class<P> p) {
        Collection<? extends QueryAccessor> c = Lookup.getDefault().lookupAll(QueryAccessor.class);
        for (QueryAccessor a : c) {
            if(a.type().equals(p)) {
                return a;
            }
        }
        return null;
    }
    
    public BuilderAccessor<P> getBuildAccessor(Class<P> p) {
        Collection<? extends BuilderAccessor> c = Lookup.getDefault().lookupAll(BuilderAccessor.class);
        for (BuilderAccessor a : c) {
            if(a.type().equals(p)) {
                return a;
            }
        }
        return null;
    }
    
    public SourceAccessor<P> getSourceAccessor(Class<P> p) {
        Collection<? extends SourceAccessor> c = Lookup.getDefault().lookupAll(SourceAccessor.class);
        for (SourceAccessor a : c) {
            if(a.type().equals(p)) {
                return a;
            }
        }
        return null;
    }    
    
    protected RemoteMachineAccessor<P> getRemoteMachineAccessor(Class<P> p) {
        Collection<? extends RemoteMachineAccessor> c = Lookup.getDefault().lookupAll(RemoteMachineAccessor.class);
        for (RemoteMachineAccessor a : c) {
            if(a.type().equals(p)) {
                return a;
            }
        }
        return null;
    }    
}
