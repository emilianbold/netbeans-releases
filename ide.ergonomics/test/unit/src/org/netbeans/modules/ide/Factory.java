package org.netbeans.modules.ide;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

public final class Factory implements ProjectFactory {

    public boolean isProject(FileObject projectDirectory) {
        return false;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        return null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }
}
