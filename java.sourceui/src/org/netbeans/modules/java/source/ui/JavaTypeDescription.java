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

package org.netbeans.modules.java.source.ui;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.java.ui.Icons;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * 
 * @todo Resolve with TypeDescription
 *
 * @author Petr Hrebejk
 */
public class JavaTypeDescription extends TypeDescriptor {
        
    private static final String EMPTY_STRING = ""; // NOI18N
    
    private Icon icon;
    
    private final JavaTypeProvider.CacheItem cacheItem;
    
    private final ElementHandle<TypeElement> handle;
    private String simpleName;
    private String outerName;
    private String packageName;

    public JavaTypeDescription(JavaTypeProvider.CacheItem cacheItem, final ElementHandle<TypeElement> handle ) {
       this.cacheItem = cacheItem;
       this.handle = handle; 
       init();
    }
    
    public void open() {        
        final FileObject root = cacheItem.getRoot();
        if (root == null) {
            final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",handle.getQualifiedName());
            StatusDisplayer.getDefault().setStatusText(message);
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final ClasspathInfo ci = ClasspathInfo.create(root);
        if ( cacheItem.isBinary() ) {            
            final JavaSource js = JavaSource.create( ci );
            final ElementHandle<TypeElement> eh = handle;
            final Element[] el = new Element[1];
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController info) {
                        el[0] = eh.resolve (info);
                        if (!ElementOpen.open(ci, el[0])) {
                            final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",eh.getQualifiedName());
                            StatusDisplayer.getDefault().setStatusText(message);
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }

                }, true);
            }
            catch( IOException e ) {
                Logger.getLogger(JavaTypeDescription.class.getName()).info("Source not found: " + eh.getBinaryName());
                Exceptions.printStackTrace(e);
            }                    
        }
        else {
            final FileObject file = SourceUtils.getFile(handle, ci);
            boolean opened = false;
            if (file != null) {
                opened = ElementOpen.open(file, handle);
            }
            if (!opened) {
                StringBuilder name = new StringBuilder ();
                if (packageName != null) {
                    name.append(packageName);
                    name.append('.');           //NOI18N
                }
                if (outerName != null) {
                    name.append(outerName);
                }
                else {
                    name.append(simpleName);
                }
                final String message = NbBundle.getMessage(JavaTypeDescription.class, "LBL_JavaTypeDescription_nosource",name.toString());
                StatusDisplayer.getDefault().setStatusText(message);
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public String getSimpleName() {
        return simpleName;
    }
    
    public String getOuterName() {
        return outerName;
    }

    public FileObject getFileObject() {
        return cacheItem.getRoot();
    }

    public String getTypeName() {
        StringBuilder sb = new StringBuilder( simpleName );
        if( outerName != null  ) {
            sb.append(" in ").append( outerName );
        }
        return sb.toString();
    }
    
    public String getContextName() {
        StringBuilder sb = new StringBuilder();
        sb.append( " (").append( packageName == null ? "Default Package" : packageName).append(")");
        return sb.toString();
                
        
    }
    
    public String getProjectName() {
        String projectName = cacheItem.getProjectName();
        return projectName == null ? "" : projectName; // NOI18N        
    }
    
    public Icon getProjectIcon() {        
        return cacheItem.getProjectIcon();
    }
    
    public ElementHandle<TypeElement> getHandle() {
        return handle;
    }
    
    private void init() {
        final String typeName = this.handle.getBinaryName();
        int lastDot = typeName.lastIndexOf('.'); // NOI18N
        int lastDollar = typeName.lastIndexOf('$'); // NOI18N
        if ( lastDot == -1 ) {
            if ( lastDollar == -1 ) {
                simpleName = typeName;
            }
            else {
                simpleName = typeName.substring(lastDollar + 1);
                outerName = typeName.substring(0, lastDollar ).replace( '$', '.');  //NOI18N;
            }
        }
        else {
            packageName = typeName.substring( 0, lastDot );
            
            if ( lastDollar == -1 ) {
                simpleName = typeName.substring( lastDot + 1 ).replace( '$', '.');  //NOI18N
            }
            else {
                simpleName = typeName.substring(lastDollar + 1);
                outerName = typeName.substring(lastDot + 1, lastDollar ).replace( '$', '.');  //NOI18N;
            }
                        
        }
        icon = Icons.getElementIcon (handle.getKind(), null);
    }

    public String toString() {
        
        StringBuilder sb = new StringBuilder( simpleName );
        if( outerName != null  ) {
            sb.append(" in ").append( outerName );
        }
        sb.append( " (").append( packageName == null ? "Default Package" : packageName).append(")");
        if (cacheItem.getProjectName() != null ) {
            sb.append( " [").append( cacheItem.getProjectName()).append("]");
        }
        
        return sb.toString();
    }

//    
//    //public int compareTo( JavaTypeDescription td ) {
//    public int compareTo( TypeDescriptor descriptor ) {
//        if (descriptor instanceof JavaTypeDescription) {
//            JavaTypeDescription td = (JavaTypeDescription)descriptor;
//           int cmpr = compareStrings( simpleName, td.simpleName );
//           if ( cmpr != 0 ) {
//               return cmpr;
//           }
//           cmpr = compareStrings( outerName, td.outerName );
//           if ( cmpr != 0 ) {
//               return cmpr;
//           }
//           return compareStrings( packageName, td.packageName );
//        } else {
//           int cmpr = compareStrings(simpleName, descriptor.getTypeName());
//           if (cmpr != 0) {
//               return cmpr;
//           }
//           return compareStrings(outerName, descriptor.getPackageName());
//        }
//    }
    
    public synchronized Icon getIcon() {
        return icon;
    }
        
//    private int compareStrings(String s1, String s2) {
//        if( s1 == null ) {
//            s1 = EMPTY_STRING;
//        }
//        if ( s2 == null ) {
//            s2 = EMPTY_STRING;
//        }
//        return s1.compareTo( s2 );
//    }

    public int getOffset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
