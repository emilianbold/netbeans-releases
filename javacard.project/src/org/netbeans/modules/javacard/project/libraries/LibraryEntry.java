/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.javacard.project.libraries;

import java.awt.Image;
import java.io.File;
import javax.swing.UIManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ant.AntArtifact;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Tim Boudreau
 */
public abstract class LibraryEntry {
    public abstract Image getIcon();
    public abstract String getDisplayName();
    public abstract String getPath();
    public abstract LibraryKind kind();
    
    public File toFile() {
        String p = getPath().replace('/', File.separatorChar);
        return new File (p);
    }

    public enum LibraryKind {
        PROJECT,
        JAR,
        FOLDER,
        UNRESOLVED;

        public String constant() {
            switch (this) {
                case PROJECT :
                case JAR :
                    return JavaProjectConstants.ARTIFACT_TYPE_JAR;
                case FOLDER :
                    return JavaProjectConstants.ARTIFACT_TYPE_FOLDER;
                case UNRESOLVED :
                    return null;
                default :
                    throw new AssertionError();
            }
        }

        Image icon() {
            switch (this) {
                case JAR :
                    return ImageUtilities.loadImage (
                            "org/netbeans/modules/javacard/resources/jar.png"); //NOI18N
                case UNRESOLVED :
                    Image result = ImageUtilities.loadImage (
                            "org/netbeans/modules/javacard/resources/" + //NOI18N
                            "libraries.gif"); //NOI18N
                    Image badge = ImageUtilities.loadImage(
                            "org/netbeans/modules/javacard/resources/" + //NOI18N
                            "brokenProjectBadge.png"); //NOI18N
                    return ImageUtilities.mergeImages(result, badge, 8, 8);
                case FOLDER :
                    return ImageUtilities.icon2Image(UIManager.getIcon(
                            "Tree.closedIcon")); //NOI18N
                case PROJECT :
                    //Project will provide its own
                    return null;
                default :
                    throw new AssertionError();
            }
        }
    }

    public static class LE2 extends LibraryEntry {
        private String reference;
        private AntArtifact artifact;
        private LibraryKind kind;
        LE2 (AntArtifact artifact, String reference, LibraryKind kind) {
            this.artifact = artifact;
            this.reference = reference;
            this.kind = kind;
        }

        @Override
        public Image getIcon() {
            return kind.icon();
        }

        @Override
        public String getDisplayName() {
            return artifact == null ? reference == null ? "missing" : reference : artifact.getArtifactFile().getName();
        }

        @Override
        public String getPath() {
            return artifact == null ? null : artifact.getArtifactFile().getPath();
        }

        @Override
        public LibraryKind kind() {
            return kind;
        }
    }

    private static class LE extends LibraryEntry {
        private final LibraryKind kind;
        private String path;
        LE(LibraryKind kind, String path) {
            this.kind = kind;
            this.path = path;
        }

        public Image getIcon() {
            if (kind != LibraryKind.PROJECT) {
                return kind().icon();
            } else {
                File f = new File (path.replace( '/', File.separatorChar)); //NOI18N
                assert f.exists();
                FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(f));
                Project p = FileOwnerQuery.getOwner(fo);
                ProjectInformation info = p == null ? null : p.getLookup().lookup(ProjectInformation.class);
                if (info != null) {
                    return ImageUtilities.icon2Image(info.getIcon());
                }
                return LibraryKind.UNRESOLVED.icon();
            }
        }
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDisplayName() {
            String pth = path.replace(File.separatorChar, '/'); //NOI18N
            int ix = pth.lastIndexOf('/'); //NOI18N
            if (ix > 0 && ix < pth.length() - 1) {
                pth = pth.substring(ix);
            }
            return pth;
        }

        public LibraryKind kind() {
            return kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LE other = (LE) obj;
            if (this.kind != other.kind) {
                return false;
            }
            if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + this.kind.hashCode();
            hash = 23 * hash + (this.path != null ? this.path.hashCode() : 0);
            return hash;
        }
    }
}
