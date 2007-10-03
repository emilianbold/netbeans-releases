/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.beans.BeanInfo;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents classpath items of various types. Can be used in the model of
 * classpath editing controls.
 *
 * @author  phrebejk
 */
public final class VisualClassPathItem {
    
    /** Types of the classpath elements. */
    public static enum Type {
        JAR, LIBRARY, ARTIFACT, CLASSPATH
    };
    
    public static final String PATH_IN_WAR_LIB = "WEB-INF/lib"; //NOI18N
    public static final String PATH_IN_EAR = "/"; //NOI18N
    public static final String PATH_IN_EAR_NONE = null;
    
    private static final String RESOURCE_ICON_JAR =
            "org/netbeans/modules/j2ee/earproject/ui/resources/jar.gif"; //NOI18N
    private static final String RESOURCE_ICON_LIBRARY =
            "org/netbeans/modules/j2ee/earproject/ui/resources/libraries.gif"; //NOI18N
    private static final String RESOURCE_ICON_ARTIFACT =
            "org/netbeans/modules/j2ee/earproject/ui/resources/projectDependencies.gif"; //NOI18N
    private static final String RESOURCE_ICON_CLASSPATH =
            "org/netbeans/modules/j2ee/earproject/ui/resources/referencedClasspath.gif"; //NOI18N
    
    private static final Icon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
    private static Icon iconFolder;
    private static final Icon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
    private static final Icon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
    private static final Icon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );
    
    private final Type type;
    private final Object cpElement;
    private String raw;
    private final String eval;
    private String pathInEAR;
    private String origPathInEAR;
    
    VisualClassPathItem(Object cpElement, Type type, String raw, String eval, String pathInEAR) {
        this.cpElement = cpElement;
        this.type = type;
        this.raw = raw;
        this.eval = eval;
        this.pathInEAR = pathInEAR;
        this.origPathInEAR = pathInEAR;
        
        // check cpElement parameter
        if (cpElement != null) {
            switch ( getType() ) {
                case JAR:
                    if (!(cpElement instanceof File)) {
                        throw new IllegalArgumentException("File instance must be " + // NOI18N
                                "passed as object for Type.JAR. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case LIBRARY:
                    if (!(cpElement instanceof Library)) {
                        throw new IllegalArgumentException("Library instance must be " + // NOI18N
                                "passed as object for Type.LIBRARY. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case ARTIFACT:
                    if (!(cpElement instanceof AntArtifact)) {
                        throw new IllegalArgumentException("AntArtifact instance must be " + // NOI18N
                                "passed as object for Type.ARTIFACT. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case CLASSPATH:
                    if (!(cpElement instanceof String)) {
                        throw new IllegalArgumentException("String instance must be " + // NOI18N
                                "passed as object for Type.CLASSPATH. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type " + // NOI18N
                            "passed. Was: "+getType()); // NOI18N
            }
        }
    }
    
    public String getPathInEAR() {
        return pathInEAR;
    }
    
    public void setPathInEAR(String path) {
        pathInEAR = path;
    }
    
    public String getOrigPathInEAR() {
        return origPathInEAR;
    }
    
    public Object getObject() {
        return cpElement;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public String getEvaluated() {
        return eval == null ? getRaw() : eval;
    }
    
    public boolean canDelete() {
        return getType() != Type.CLASSPATH;
    }
    
    public Icon getIcon() {
        if (getObject() == null) {
            // Otherwise get an NPE for a broken project.
            return null;
        }
        
        switch(getType()) {
            case JAR:
                if (((File) getObject()).isDirectory()) {
                    return getFolderIcon();
                } else {
                    return ICON_JAR;
                }
            case LIBRARY:
                return ICON_LIBRARY;
            case ARTIFACT:
                return ICON_ARTIFACT;
            case CLASSPATH:
                return ICON_CLASSPATH;
            default:
                return null;
        }
        
    }
    
    @Override
    public String toString() {
        switch ( getType() ) {
            case JAR:
                if (getObject() != null) {
                    return getEvaluated();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_FILE", getFileRefName(getEvaluated()));
                }
            case LIBRARY:
                if (getObject() != null) {
                    return ((Library)this.getObject()).getDisplayName();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_LIBRARY", getLibraryName(getRaw()));
                }
            case ARTIFACT:
                if (getObject() != null) {
                    return getEvaluated();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_PROJECT", getProjectName(getEvaluated()));
                }
            case CLASSPATH:
                return getEvaluated();
            default:
                assert true : "Unknown item type"; // NOI18N
                return getEvaluated();
        }
    }
    
    private String getProjectName(String id) {
        // something in the form of "${reference.project-name.id}"
        return id.matches("\\$\\{reference\\..*\\.id\\}") // NOI18N
                ? id.substring(12, id.indexOf('.', 12)) : id;
    }
    
    private String getLibraryName(String id) {
        // something in the form of "${libs.junit.classpath}"
        return id.substring(7, id.indexOf(".classpath")); // NOI18N
    }
    
    private String getFileRefName(String id) {
        // something in the form of "${file.reference.smth.jar}"
        return id.substring(17, id.length()-1);
    }
    
    @Override
    public int hashCode() {
        int hash = getType().ordinal();
        switch ( getType() ) {
            case ARTIFACT:
                if (getObject() != null) {
                    AntArtifact aa = (AntArtifact)getObject();
                    hash += aa.getType().hashCode();
                    hash += aa.getScriptLocation().hashCode();
                    hash += aa.getArtifactLocations()[0].hashCode();
                } else {
                    hash += getRaw().hashCode();
                }
                break;
            default:
                if (getObject() != null) {
                    hash += getObject().hashCode();
                } else {
                    hash += getRaw().hashCode();
                }
                break;
        }
        return hash;
    }
    
    @Override
    public boolean equals( Object object ) {
        if ( !( object instanceof VisualClassPathItem ) ) {
            return false;
        }
        VisualClassPathItem vcpi = (VisualClassPathItem)object;
        
        if ( getType() != vcpi.getType() ) {
            return false;
        }
        
        switch ( getType() ) {
            case ARTIFACT:
                AntArtifact aa2 = (AntArtifact) vcpi.getObject();
                AntArtifact aa1 = (AntArtifact) getObject();
                if (aa1 != null && aa2 != null) {
                    if ( aa1.getType() != aa2.getType() ) {
                        return false;
                    }
                    
                    if ( !aa1.getScriptLocation().equals( aa2.getScriptLocation() ) ) {
                        return false;
                    }
                    
                    if ( !aa1.getArtifactLocations()[0].equals( aa2.getArtifactLocations()[0] ) ) {
                        return false;
                    }
                    
                    return true;
                } else {
                    return getRaw().equals(vcpi.getRaw());
                }
            default:
                if (getObject() != null) {
                    return getObject().equals(vcpi.getObject());
                } else {
                    return getRaw().equals(vcpi.getRaw());
                }
        }
        
    }
    
    static VisualClassPathItem createArtifact(AntArtifact artifact, String raw, String pathInEAR, String eval) {
        return new VisualClassPathItem(artifact, VisualClassPathItem.Type.ARTIFACT, raw, eval, pathInEAR);
    }
    
    static VisualClassPathItem createArtifact(AntArtifact artifact, String raw, String pathInEAR) {
        String eval = artifact != null ? artifact.getArtifactLocations()[0].toString() : null;
        return createArtifact(artifact, raw, pathInEAR, eval);
    }
    
    static VisualClassPathItem createArtifact(AntArtifact antArtifact) {
        return createArtifact(antArtifact, null, VisualClassPathItem.PATH_IN_EAR);
    }
    
    static VisualClassPathItem createJAR(File jarFile, String raw, String pathInEAR, String eval) {
        return new VisualClassPathItem(jarFile, VisualClassPathItem.Type.JAR,
                raw, eval, pathInEAR);
    }
    
    static VisualClassPathItem createJAR(File jarFile, String raw, String pathInEAR) {
        return createJAR(jarFile, raw, pathInEAR, jarFile.getPath());
    }
    
    static VisualClassPathItem createJAR(File jarFile) {
        return createJAR(jarFile, null, VisualClassPathItem.PATH_IN_EAR);
    }
    
    static VisualClassPathItem createLibrary(Library library) {
        return createLibrary(library, VisualClassPathItem.PATH_IN_EAR);
    }
    
    static VisualClassPathItem createLibrary(Library library, String pathInEar) {
        String libraryName = library.getName();
        return new VisualClassPathItem(library, Type.LIBRARY, "${libs." + libraryName + ".classpath}", // NOI18N
                libraryName, pathInEar);
    }
    
    public static VisualClassPathItem createClassPath(String wellKnownPath, String eval) {
        return new VisualClassPathItem(wellKnownPath,
                VisualClassPathItem.Type.CLASSPATH,
                wellKnownPath,
                eval,
                PATH_IN_EAR_NONE);
    }
    
    private static Icon getFolderIcon() {
        if ( iconFolder == null ) {
            FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
            DataFolder dataFolder = DataFolder.findFolder( root );
            iconFolder = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );
        }
        
        return iconFolder;
    }
    
    public String getToolTipText() {
        String toolTipText = null;
        switch (getType()) {
            case JAR:
                toolTipText = ((File) cpElement).getAbsolutePath();
                break;
            case LIBRARY:
                toolTipText = VisualClasspathSupport.getLibraryString((Library) cpElement);
                break;
            case ARTIFACT:
                final AntArtifact artifact = (AntArtifact) cpElement;
                final FileObject fos[] = artifact.getArtifactFiles();
                if (fos.length > 0) {
                    final FileObject f = fos[0];
                    toolTipText = f == null ? artifact.getArtifactLocations()[0].getPath() : f.getPath();
                }
                break;
            case CLASSPATH:
                toolTipText = (String) cpElement;
                break;
            default:
                toolTipText = null;
        }
        return toolTipText;
    }
    
    public String getCompletePathInArchive(final boolean original) {
        String full = getEvaluated();
        int lastSlash = full.lastIndexOf('/');
        String trimmed = null;
        trimmed = (lastSlash != -1) ? full.substring(lastSlash+1) : full;
        String path = original ? getOrigPathInEAR() : getPathInEAR();
        return (null != path && path.length() > 1)
                ? path + '/' + trimmed : trimmed;
    }
    
    public String getCompletePathInArchive() {
        return getCompletePathInArchive(false);
    }
    
}
