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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.io.File;
import java.net.URI;
import java.awt.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/** Represents classpath items of various types. Can be used in the model
 * of classpath editing controls.
 *
 * @author  phrebejk
 */
public final class VisualClassPathItem {
    
    // Types of the classpath elements
    public static final int TYPE_JAR = 0;
    public static final int TYPE_LIBRARY = 1;
    public static final int TYPE_ARTIFACT = 2;
    public static final int TYPE_CLASSPATH = 3;
    public static final int TYPE_FOLDER = 4;
    
    final private static String RESOURCE_ICON_JAR = "org/netbeans/modules/project/support/customizer/resources/jar.gif"; //NOI18N
    final private static String RESOURCE_ICON_FOLDER = "org/netbeans/modules/project/support/customizer/resources/folder.gif"; //NOI18N
    final private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/project/support/customizer/resources/libraries.gif"; //NOI18N
    final private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/project/support/customizer/resources/projectDependencies.gif"; //NOI18N
    final private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/project/support/customizer/resources/project.gif"; //NOI18N
    final private static String RESOURCE_ICON_BROKEN = "org/netbeans/modules/project/support/customizer/resources/brokenProjectBadge.gif"; //NOI18N
    
    private static Icon ICON_JAR;
    private static Icon ICON_LIBRARY;
    private static Icon ICON_ARTIFACT;
    private static Icon ICON_CLASSPATH;
    private static Icon ICON_FOLDER;
    private static Icon ICON_BROKEN_JAR;
    private static Icon ICON_BROKEN_LIBRARY;
    private static Icon ICON_BROKEN_ARTIFACT;
    private static Icon ICON_BROKEN_CLASSPATH;
    private static Icon ICON_BROKEN_FOLDER;
    
    static {
        Image i;
        Image broken = ImageUtilities.loadImage(RESOURCE_ICON_BROKEN);
        
        i = ImageUtilities.loadImage(RESOURCE_ICON_JAR);
        ICON_JAR = new ImageIcon(i);
        ICON_BROKEN_JAR = new ImageIcon(ImageUtilities.mergeImages(i, broken, 8, 0));
        
        i = ImageUtilities.loadImage(RESOURCE_ICON_LIBRARY);
        ICON_LIBRARY = new ImageIcon(i);
        ICON_BROKEN_LIBRARY = new ImageIcon(ImageUtilities.mergeImages(i, broken, 8, 0));
        
        i = ImageUtilities.loadImage(RESOURCE_ICON_ARTIFACT);
        ICON_ARTIFACT = new ImageIcon(i);
        ICON_BROKEN_ARTIFACT = new ImageIcon(ImageUtilities.mergeImages(i, broken, 8, 0));
        
        i = ImageUtilities.loadImage(RESOURCE_ICON_CLASSPATH);
        ICON_CLASSPATH = new ImageIcon(i);
        ICON_BROKEN_CLASSPATH = new ImageIcon(ImageUtilities.mergeImages(i, broken, 8, 0));
        
        i = ImageUtilities.loadImage(RESOURCE_ICON_FOLDER);
        ICON_FOLDER = new ImageIcon(i);
        ICON_BROKEN_FOLDER = new ImageIcon(ImageUtilities.mergeImages(i, broken, 8, 0));
    }
    
    private int type;
    private Object cpElement;
    private String display;
    private String rawText;
    private URI uri;
    private boolean extra = false;
    
    
    public static VisualClassPathItem create( final Library library ) {
        if ( library == null ) {
            throw new IllegalArgumentException( "library must not be null" ); // NOI18N
        }
        final String libraryName = library.getName();
        return new VisualClassPathItem( library, TYPE_LIBRARY, "${libs."+libraryName+".classpath}", library.getDisplayName()); //NOI18N
        
    }
    
    public static VisualClassPathItem create( final AntArtifact artifact, final URI artifactURI) {
        if ( artifactURI == null ) {
            throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
        }
        if ( artifact == null ) {
            throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
        }
        String location;
        try {
            location = FileUtil.normalizeFile(new File(artifact.getScriptLocation().getParentFile().toURI().resolve(artifactURI))).getPath();
        } catch (Exception e) {
            location = artifactURI.getPath();
        }
        return new VisualClassPathItem(artifact, artifactURI, TYPE_ARTIFACT, null, location);
    }
    
    public static VisualClassPathItem create( final File file ) {
        if ( file == null ) {
            throw new IllegalArgumentException( "file must not be null" ); // NOI18N
        }
        return new VisualClassPathItem( file, isJar(file.getName()) ? TYPE_JAR : TYPE_FOLDER, null, file.getPath() );
    }
    
    private static boolean isJar(String s) {
        if (s == null) return false;
        s = s.toLowerCase();
        return s.endsWith(".jar") || s.endsWith(".zip"); //NOI18N
    }
    
    public void setExtra(boolean extra) {
        this.extra = extra;
    }
    
    public boolean isExtra() {
        return extra;
    }
    
    public VisualClassPathItem( AntArtifact cpElement, URI uri, int type, String rawText, String display ) {
        this(cpElement, type, rawText, display);
        this.uri = uri;
    }
    
    public VisualClassPathItem( Object cpElement, int type, String rawText, String display ) {
        this.cpElement = cpElement;
        this.type = type;
        this.display = display;
        this.rawText = rawText;
    }
    
    public Object getElement() {
        return cpElement;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public int getType() {
        return type;
    }
    
    public String getDisplayName() {
        return display;
    }
    
    public String getRawText() {
        return rawText;
    }
    
    public boolean canDelete() {
        return getType() != TYPE_CLASSPATH;
    }
    
    public Icon getIcon() {
        
        switch( getType() ) {
            case TYPE_JAR:
                return getElement() != null ? ICON_JAR : ICON_BROKEN_JAR;
            case TYPE_FOLDER:
                return getElement() != null ? ICON_FOLDER : ICON_BROKEN_FOLDER;
            case TYPE_LIBRARY:
                return getElement() != null ? ICON_LIBRARY : ICON_BROKEN_LIBRARY;
            case TYPE_ARTIFACT:
                return getElement() != null ? ICON_ARTIFACT : ICON_BROKEN_ARTIFACT;
            case TYPE_CLASSPATH:
                return getElement() != null ? ICON_CLASSPATH : ICON_BROKEN_CLASSPATH;
            default:
                return null;
        }
        
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public int hashCode() {
        
        int hash = getType();
        
        switch ( getType() ) {
            case TYPE_ARTIFACT:
                final AntArtifact aa = (AntArtifact)getElement();
                if (aa == null) {
                    if (rawText != null)
                        hash += rawText.hashCode();
                    break;
                }
                
                hash += aa.getType().hashCode();
                hash += aa.getScriptLocation().hashCode();
                hash += uri.hashCode();
                break;
            default:
                final Object element = getElement();
                if (element != null)
                    hash += element.hashCode();
                break;
        }
        
        return hash;
    }
    
    public boolean equals( final Object object ) {
        //fix for 98455 - obvious case is missing
        if (this == object)
            return true;
        
        if ( !( object instanceof VisualClassPathItem ) ) {
            return false;
        }
        final VisualClassPathItem vcpi = (VisualClassPathItem)object;
        
        if ( getType() != vcpi.getType() ) {
            return false;
        }
        
        switch ( getType() ) {
            case TYPE_ARTIFACT:
                final AntArtifact aa1 = (AntArtifact)getElement();
                final AntArtifact aa2 = (AntArtifact)vcpi.getElement();
                if (aa1 == null  ||  aa2 == null) {
                    if (getRawText() != null  &&  vcpi.getRawText() != null)
                        if (getRawText().equals(vcpi.getRawText()))
                            return true;
                    return false;
                }
                
                if ( aa1.getType() != aa2.getType() ) {
                    return false;
                }
                
                if ( !aa1.getScriptLocation().equals( aa2.getScriptLocation() ) ) {
                    return false;
                }
                
                if ( !getURI().equals( vcpi.getURI() ) ) {
                    return false;
                }
                
                return true;
            default:
                final Object element = getElement();
                final Object cElement = vcpi.getElement();
                //Special case when the library is missing but we want compare values
                if (cElement == null && element == null && vcpi.getRawText().equals(getRawText()))
                    return true;
                //And another special case when library was removed from manager
                if (element == null && cElement instanceof Library  &&
                    toString().equals(vcpi.toString()) && vcpi.getRawText().equals(getRawText()))
                        return true;
                    
                if (element == null)
                    return false;
                
                
                return element.equals( vcpi.getElement() );
        }
        
    }
    
}
