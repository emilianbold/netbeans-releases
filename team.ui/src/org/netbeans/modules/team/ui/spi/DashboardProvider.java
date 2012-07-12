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
    public TreeListNode createProjectLinksNode(ProjectNode pn, ProjectHandle<P> project);
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<P> project);
    public TreeListNode createMyProjectNode(ProjectHandle<P> project);   
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln);    

    public ProjectAccessor<S, P> getProjectAccessor();
    public MessagingAccessor<P> getMessagingAccessor();
    public MemberAccessor<P> getMemberAccessor();
    public SourceAccessor<P> getSourceAccessor();
    public QueryAccessor<P> getQueryAccessor();
    public BuildAccessor<P> getBuildAccessor();

    public Collection<ProjectHandle<P>> getMyProjects();
    public S forProject(ProjectHandle<P> project);
    
}
