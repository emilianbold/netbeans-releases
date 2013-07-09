/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.util.Collection;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.team.ui.common.MyProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.util.treelist.LeafNode;
import org.netbeans.modules.team.ui.util.treelist.TreeListNode;
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
}
