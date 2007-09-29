package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class PostProcessRenamedFolderRefactoringElement extends SimpleRefactoringElementImplementation {

    private final FileObject folder;
    private final FileObject folderParent;
    private final String oldName;
    private final String newName;

    public PostProcessRenamedFolderRefactoringElement(FileObject folder, String newName) {
        this.folder = folder;
        this.folderParent = folder.getParent();
        this.oldName = folder.getNameExt();
        this.newName = newName;
    }

    public String getDisplayText() {
        return getText();
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public FileObject getParentFile() {
        return folder;
    }

    public PositionBounds getPosition() {
        return null;
    }

    public String getText() {
        return NbBundle.getMessage(PostProcessRenamedFolderRefactoringElement.class,
                "MSG_PostProcessFolder"); // NOI18N
    }

    public void performChange() {
        FileObject newFolder = folderParent.getFileObject(newName);
        if (newFolder != null) {
            FacesModelSet facesModelSet = FacesModelSet.getInstance(newFolder);
            if (facesModelSet != null) {
                facesModelSet.processFileDataCreated(newFolder);
            }
        }
    }
    
    public void undoChange() {
        FileObject oldFolder = folderParent.getFileObject(oldName);
        if (oldFolder != null) {
            FacesModelSet facesModelSet = FacesModelSet.getInstance(oldFolder);
            if (facesModelSet != null) {
                facesModelSet.processFileDataCreated(oldFolder);
            }
        }
    }
}
