package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public final class TestFactory extends ProjectOpenedHook
implements ProjectFactory, Project, ProjectInformation {

    static FileObject recognize;
    int closed;
    int opened;
    int listenerCount;

    public TestFactory() {
        super();
    }

    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.equals(recognize);
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        return isProject(projectDirectory) ? new TestFactory() : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    public FileObject getProjectDirectory() {
        return recognize;
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
}
