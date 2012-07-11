/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.util.Collection;
import javax.swing.Action;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;

/**
 * Provides Team Dashboard relevant functionality.
 *
 * @author Tomas Stupka
 */
public interface DashboardProvider<S extends TeamServer, P> {

    public Action createLogoutAction();
    public Action createLoginAction();
    public LeafNode createMemberNode(MemberHandle user, TreeListNode parent);
    public TreeListNode createProjectLinksNode(ProjectNode pn, ProjectHandle<S, P> project);
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<S, P> project);
    public TreeListNode createMyProjectNode(ProjectHandle<S, P> p);   
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln);    

    public ProjectAccessor<S, P> getProjectAccessor();
    public MessagingAccessor<S, P> getMessagingAccessor();
    public MemberAccessor<S, P> getMemberAccessor();
    public SourceAccessor<S, P> getSourceAccessor();
    public QueryAccessor<S, P> getQueryAccessor();
    public BuildAccessor<S, P> getBuildAccessor();

    public Collection<ProjectHandle<S, P>> getMyProjects();
    
}
