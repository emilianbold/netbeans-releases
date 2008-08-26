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

package org.netbeans.modules.gsf;

import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.Language;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.gsfret.navigation.Icons;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class GsfTypeDescription extends TypeDescriptor {
    private Icon icon;
    
    private final GsfTypeProvider.CacheItem cacheItem;
    
    private final ElementHandle handle;
    private String simpleName;
    private String outerName;
    private String packageName;

    public GsfTypeDescription(GsfTypeProvider.CacheItem cacheItem, final ElementHandle handle) {
       this.cacheItem = cacheItem;
       this.handle = handle; 
       init();
    }
    
    @Override
    public void open() {
        if ( cacheItem.isBinary() ) {
            final ClasspathInfo ci = ClasspathInfo.create(cacheItem.getRoot());
            Source js = Source.create( ci );
            if (js == null) {
                return;
            }
            final ElementHandle eh = handle;
            final ElementHandle[] el = new ElementHandle[1];
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController info) {
                        //el[0] = eh.resolve (info);
                        //UiUtils.open(ci, el[0]);
                        FileObject fo = info.getFileObject();
                        Source js = Source.forFileObject(fo);
                        UiUtils.open(js, eh);
                    }

                }, true);
            }
            catch( IOException e ) {
                Logger.getLogger(GsfTypeDescription.class.getName()).info("Source not found: " + eh/*.getBinaryName()*/);
                Exceptions.printStackTrace(e);
            }                    
        }
        else {
            //XXX: Why is this different? Why not UiUtils.open () is used?
            FileObject folder = packageName != null ? cacheItem.getRoot().getFileObject(packageName.replace(".", "/")) : cacheItem.getRoot(); // NOI18N
            if (folder != null) {
                FileObject[] ch = folder.getChildren();
                String name = outerName == null ? simpleName : outerName; // NOI18N
                int lastDot = name.indexOf('.'); //NOI18N
                if ( lastDot != -1 ) {
                    name = name.substring(0, lastDot );
                }
                for (FileObject fileObject : ch) {
                    if ( name.equals( fileObject.getName() ) && 
                         //"java".equals( fileObject.getExt().toLowerCase() ) ) {
                         LanguageRegistry.getInstance().isSupported(fileObject.getMIMEType())) {
                        //UiUtils.open(fileObject, handle);
                        Source js = Source.forFileObject(fileObject);
                        if (js != null) {
                            UiUtils.open(js, handle);
                        }
                    }
                }
            }
            else {
                Logger.getLogger(GsfTypeDescription.class.getName()).info("Package " + packageName +" doesn't exist in root: " + FileUtil.getFileDisplayName(cacheItem.getRoot()));
            }
        }
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    public String getOuterName() {
        // XXX TODO
        throw null;
    }
    
    @Override
    public FileObject getFileObject() {
        return cacheItem.getRoot();
    }

    @Override
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
    
    @Override
    public String getProjectName() {
        String projectName = cacheItem.getProjectName();
        return projectName == null ? "" : projectName; // NOI18N        
    }
    
    @Override
    public Icon getProjectIcon() {        
        return cacheItem.getProjectIcon();
    }
    
    private void init() {
        /*
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
        icon = Icons.getElementHandleIcon (handle.getKind(), null);
         */
        
        // Initialie simpleName, packageName, outerName and icon from handle
        //ElementHandle element = handle.getOldElement();
        ElementHandle element = handle;
        icon = Icons.getElementIcon (element.getKind(), null);
        simpleName = element.getName();
        packageName = element.getIn();
        outerName = null;
    }

    @Override
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


    @Override
    public synchronized Icon getIcon() {
        return icon;
    }
        
    @Override
    public int getOffset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
