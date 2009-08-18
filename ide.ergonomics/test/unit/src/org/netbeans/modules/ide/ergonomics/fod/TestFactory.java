package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class, position=29998)
public final class TestFactory extends ProjectOpenedHook
implements ProjectFactory, Project, ProjectInformation, SubprojectProvider, LogicalViewProvider {

    static Set<FileObject> recognize = new HashSet<FileObject>();
    static Set<Project> subprojects = new HashSet<Project>();
    static IOException ex;
    int closed;
    int opened;
    int listenerCount;
    final FileObject dir;

    public TestFactory() {
        dir = null;
    }

    private TestFactory(FileObject dir) {
        this.dir = dir;
    }

    public boolean isProject(FileObject projectDirectory) {
        return recognize.contains(projectDirectory);
    }

    public Project loadProject(FileObject pd, ProjectState state) throws IOException {
        IOException e = ex;
        if (e != null) {
            ex = null;
            throw e;
        }
        return isProject(pd) ? new TestFactory(pd) : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    public FileObject getProjectDirectory() {
        return dir;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public String getName() {
        return "x";
    }

    public String getDisplayName() {
        return "y";
    }

    public Icon getIcon() {
        return null;
    }

    public Project getProject() {
        return this;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerCount++;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerCount--;
    }

    @Override
    protected void projectOpened() {
        opened++;
    }

    @Override
    protected void projectClosed() {
        closed++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestFactory) {
            return super.equals(obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Set<? extends Project> getSubprojects() {
        return subprojects;
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public Node createLogicalView() {
        AbstractNode an = new AbstractNode(new Children.Array());
        an.setName("xyz");
        an.setDisplayName("Name xyz");

        an.getChildren().add(new Node[]{ new AbstractNode(Children.LEAF), new AbstractNode(Children.LEAF) });
        an.getChildren().getNodeAt(0).setName("a");
        an.getChildren().getNodeAt(1).setName("b");
        return an;
    }

    public Node findPath(Node root, Object target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
