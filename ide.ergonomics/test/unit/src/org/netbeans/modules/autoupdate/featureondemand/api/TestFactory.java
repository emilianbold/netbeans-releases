package org.netbeans.modules.autoupdate.featureondemand.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public final class TestFactory implements ProjectFactory, Project {

    static FileObject recognize;

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
}
