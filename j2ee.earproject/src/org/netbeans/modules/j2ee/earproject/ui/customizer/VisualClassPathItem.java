/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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

/** Represents classpath items of various types. Can be used in the model
 * of classpath editing controls.
 *
 * @author  phrebejk
 */
public class VisualClassPathItem {
            
    // Types of the classpath elements
    public static final int TYPE_JAR = 0;
    public static final int TYPE_LIBRARY = 1;
    public static final int TYPE_ARTIFACT = 2;
    public static final int TYPE_CLASSPATH = 3;

    public static final String PATH_IN_WAR_LIB = "WEB-INF/lib"; //NOI18N
    public static final String PATH_IN_WAR_APPLET = "/"; //NOI18N
    public static final String PATH_IN_WAR_NONE = null;
    
    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/j2ee/earproject/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/j2ee/earproject/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/j2ee/earproject/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/j2ee/earproject/ui/resources/j2seProject.gif"; //NOI18N
    
    private static Icon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
    private static Icon ICON_FOLDER = null; 
    private static Icon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
    private static Icon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
    private static Icon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );
    
    
    private int type;
    private Object cpElement;
    private String raw;
    private String eval;
    private String pathInWAR;

    VisualClassPathItem( Object cpElement, int type, String raw, String eval, String pathInWAR ) {
        this.cpElement = cpElement;
        this.type = type;
        this.raw = raw;
        this.eval = eval;
        this.pathInWAR = pathInWAR;

        // check cpElement parameter
        if (cpElement != null) {
            switch ( getType() ) {
                case TYPE_JAR:
                    if (!(cpElement instanceof File)) {
                        throw new IllegalArgumentException("File instance must be " + // NOI18N
                            "passed as object for TYPE_JAR. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case TYPE_LIBRARY:
                    if (!(cpElement instanceof Library)) {
                        throw new IllegalArgumentException("Library instance must be " + // NOI18N
                            "passed as object for TYPE_LIBRARY. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case TYPE_ARTIFACT:
                    if (!(cpElement instanceof AntArtifact)) {
                        throw new IllegalArgumentException("AntArtifact instance must be " + // NOI18N
                            "passed as object for TYPE_ARTIFACT. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                case TYPE_CLASSPATH:
                    if (!(cpElement instanceof String)) {
                        throw new IllegalArgumentException("String instance must be " + // NOI18N
                            "passed as object for TYPE_CLASSPATH. Was: "+cpElement.getClass()); // NOI18N
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type " + // NOI18N
                        "passed. Was: "+getType()); // NOI18N
            }
        }
    }

    public String getPathInWAR () {
        return pathInWAR;
    }
    
    public void setPathInWAR (String path) {
        pathInWAR = path;
    }
    
    public Object getObject() {
        return cpElement;
    }
    
    public int getType() {
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
        return getType() != TYPE_CLASSPATH;
    }
    
    public Icon getIcon() {
        if (getObject() == null) {
            // Otherwise get an NPE for a broken project.
            return null;
        }
        
        
        switch( getType() ) {
            case TYPE_JAR:
                if ( ((File)getObject()).isDirectory() ) {
                    return getFolderIcon();
                }
                else {
                    return ICON_JAR;
                }
            case TYPE_LIBRARY:
                return ICON_LIBRARY;
            case TYPE_ARTIFACT:
                return ICON_ARTIFACT;
            case TYPE_CLASSPATH:
                return ICON_CLASSPATH;
            default:
                return null;
        }
         
    }

    public String toString() {
        switch ( getType() ) {
            case TYPE_JAR:
                if (getObject() != null) {
                    return getEvaluated();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_FILE", getFileRefName(getEvaluated()));
                }
            case TYPE_LIBRARY:
                if (getObject() != null) {
                    return ((Library)this.getObject()).getDisplayName();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_LIBRARY", getLibraryName(getRaw()));
                }
            case TYPE_ARTIFACT:
                if (getObject() != null) {
                    return getEvaluated();
                } else {
                    return NbBundle.getMessage(VisualClassPathItem.class, "LBL_MISSING_PROJECT", getProjectName(getEvaluated()));
                }
            case TYPE_CLASSPATH:
                return getEvaluated();
            default:
                assert true : "Unknown item type"; // NOI18N
                return getEvaluated();
        }
    }
            
    private String getProjectName(String ID) {
        // something in the form of "${reference.project-name.id}"
        return ID.substring(12, ID.indexOf(".", 12)); // NOI18N
    }
    
    private String getLibraryName(String ID) {
        // something in the form of "${libs.junit.classpath}"
        return ID.substring(7, ID.indexOf(".classpath")); // NOI18N
    }
    
    private String getFileRefName(String ID) {
        // something in the form of "${file.reference.smth.jar}"
        return ID.substring(17, ID.length()-1);
    }
            
    public int hashCode() {
        
        int hash = getType();
        
        switch ( getType() ) {
            case TYPE_ARTIFACT:
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
    
    public boolean equals( Object object ) {
        
        if ( !( object instanceof VisualClassPathItem ) ) {
            return false;
        }
        VisualClassPathItem vcpi = (VisualClassPathItem)object;
        
        if ( getType() != vcpi.getType() ) {
            return false;
        }
        
        switch ( getType() ) {
            case TYPE_ARTIFACT:
                if (getObject() != null) {
                    AntArtifact aa1 = (AntArtifact)getObject();
                    AntArtifact aa2 = (AntArtifact)vcpi.getObject();

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
    
    public static VisualClassPathItem create (Library library, String pathInWar) {
        String libraryName = library.getName();
        return new VisualClassPathItem(library,TYPE_LIBRARY,
            "${libs."+libraryName+".classpath}",libraryName, pathInWar); // NOI18N
    }

    public static VisualClassPathItem create (File archiveFile, String pathInWar) {
        return new VisualClassPathItem( archiveFile,
                    VisualClassPathItem.TYPE_JAR,
                    null,
                    archiveFile.getPath(),
                    pathInWar);
    }

    public static VisualClassPathItem create (AntArtifact artifact, String pathInWar) {
        return new VisualClassPathItem( artifact,
                    VisualClassPathItem.TYPE_ARTIFACT,
                    null,
                    artifact.getArtifactLocations()[0].toString(),
                    pathInWar);
    }
    
    public static VisualClassPathItem create (String wellKnownPath, String path) {
        return new VisualClassPathItem( wellKnownPath,
                    VisualClassPathItem.TYPE_CLASSPATH,
                    wellKnownPath,
                    path,
                    PATH_IN_WAR_NONE);
    }

    private static Icon getFolderIcon() {
        
        if ( ICON_FOLDER == null ) {
            FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
            DataFolder dataFolder = DataFolder.findFolder( root );
            ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );            
        }
        
        return ICON_FOLDER;
   
    }

    public String getToolTipText() {
        String toolTipText = null;
        switch (getType()) {
            case TYPE_JAR:
                toolTipText = ((File) cpElement).getAbsolutePath();
                break;
            case TYPE_LIBRARY:
                toolTipText = VisualClasspathSupport.getLibraryString((Library) cpElement);
                break;
            case TYPE_ARTIFACT:
                final AntArtifact artifact = (AntArtifact) cpElement;
                final FileObject fos[] = artifact.getArtifactFiles();
                if (fos.length > 0) {
                    final FileObject f = fos[0];
                    toolTipText = f == null ? artifact.getArtifactLocations()[0].getPath() : f.getPath();
                }
                break;
            case TYPE_CLASSPATH:
                toolTipText = (String) cpElement;
                break;
            default:
                toolTipText = null;
        }
        return toolTipText;
    }

    public String getCompletePathInArchive() {
        String full = getEvaluated();
        int lastSlash = full.lastIndexOf('/');
        String trimmed = null;
        if (lastSlash != -1)
            trimmed = full.substring(lastSlash+1);
        else
            trimmed = full;
        String path = getPathInWAR();
        if (null != path && path.length() > 1)
            return path+"/"+trimmed;
        else
            return trimmed;
    }

}
