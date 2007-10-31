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


package org.netbeans.modules.web.core.jsploader;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.WeakListeners;

public class JspParserAccess {
    
    public static synchronized JspParserAPI.WebModule getJspParserWM(WebModule webModule) {
        return new WM(webModule);
    }
    
    // PENDING - will also need this:
    //public static JspParserAPI.WebModule getParserWMOutsideWM (FileObject packageRoot) { }
    
    private static final class WM extends JspParserAPI.WebModule implements PropertyChangeListener {
        
        WebModule webModule;
        PropertyChangeSupport pcs;
        
        /** Creates an instance of a new web module for the parser.
         * @param docBase the document base of the web module. May be null if
         *  we are parsing a tag file that it outside of a web module.
         */
        private WM(WebModule webModule) {
            this.webModule = webModule;
            pcs = new PropertyChangeSupport(this);
            //Listen on the changes for libraries
            if(webModule != null) {
                ClassPath cp = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.EXECUTE);
                cp.addPropertyChangeListener(WeakListeners.propertyChange(this, cp));
            }
        }
        
        public File[] getExtraClasspathEntries(){
            File[] entries = null;
            if (webModule != null){
                Project p = FileOwnerQuery.getOwner(webModule.getDocumentBase());
                J2eeModuleProvider jprovider = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
                if (jprovider != null){
                    String serverID = jprovider.getServerInstanceID();
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverID);
                    // platform can be null, if the target server is not resolved. 
                    if (platform != null)
                        entries = platform.getClasspathEntries();
                }
                
                
            }
            return entries;
        }
        
        public FileObject getDocumentBase() {
            if (webModule != null)
                return webModule.getDocumentBase();
            return null;
        }
        
        /** Returns InputStream for the file open in editor or null
         * if the file is not open.
         */
        public java.io.InputStream getEditorInputStream(FileObject fo) {
            InputStream result = null;
            EditorCookie ec = null;
            try {
                
                ec = (EditorCookie)(DataObject.find(fo)).getCookie(EditorCookie.class);
            } catch (org.openide.loaders.DataObjectNotFoundException e){
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            if (ec != null && (ec instanceof CloneableEditorSupport)) {
                try {
                    
                    result = ((CloneableEditorSupport)ec).getInputStream();
                } catch (IOException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
            return result;
        }
        
        
        private void fireLibraries() {
            firePropertyChange(JspParserAPI.WebModule.PROP_LIBRARIES);
        }
        
        private void firePackageRoots() {
            firePropertyChange(JspParserAPI.WebModule.PROP_PACKAGE_ROOTS);
        }
        
        private void firePropertyChange(String propertyName) {
            pcs.firePropertyChange(propertyName, null, null);
        }
        
        @Override
        public FileObject getWebInf() {
            return webModule.getWebInf();
        }
/*        public FileObject[] getPackageRoots() {
            FileObject[] roots = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.EXECUTE).getRoots();
            ArrayList folders = new ArrayList();
            for (int i = 0; i < roots.length; i++){
                if (!roots[i].isData ()) { //NOI18N
                    folders.add(roots[i]);
                }
            }
            return (FileObject[])folders.toArray(new FileObject[folders.size()]);
        }
 */
        
/*        public FileObject[] getLibraries() {
            // PENDING - better implementation when we will be able distinguish the libraries.
            FileObject[] roots = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.EXECUTE).getRoots();
            ArrayList lib = new ArrayList();
            try{
                for (int i = 0; i < roots.length; i++){
                    if (roots[i].getURL().getProtocol().equals("jar")) { //NOI18N
                        lib.add(roots[i]);
                    }
                }
            }
            catch(org.openide.filesystems.FileStateInvalidException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return (FileObject[])lib.toArray(new FileObject[lib.size()]);
 
        }*/
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())){
                fireLibraries();
            }
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())){
                firePackageRoots();
            }
        }
        
    }
}
