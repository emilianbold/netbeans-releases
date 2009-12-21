package org.netbeans.modules.versioning.diff;

import java.io.IOException;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class EditorSaveCookie implements SaveCookie {

    private final EditorCookie editorCookie;
    private final String name;

    public EditorSaveCookie(EditorCookie editorCookie, FileObject fileObj) {
        this(editorCookie, getName(fileObj));
    }

    public EditorSaveCookie(EditorCookie editorCookie, String name) {
        super();
        if (editorCookie == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        this.editorCookie = editorCookie;
        this.name = name;
    }

    public void save() throws IOException {
        editorCookie.saveDocument();
    }

    private static String getName(FileObject fileObj) {
        return (fileObj != null) ? FileUtil.getFileDisplayName(fileObj) : null;
    }

    @Override
    public String toString() {
        return (name != null) ? name : super.toString();
    }
}
