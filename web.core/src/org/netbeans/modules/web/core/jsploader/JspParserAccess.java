/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.web.core.jsploader;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            if (ec != null && (ec instanceof CloneableEditorSupport)) {
                try {
                    
                    result = ((CloneableEditorSupport)ec).getInputStream();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
