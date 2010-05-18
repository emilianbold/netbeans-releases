/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * WebServiceClassPathProvider.java
 *
 * Created on September 14, 2005, 4:03 PM
 *
 */
package org.netbeans.modules.mobility.end2end.util;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author suchys
 */
public class WebServiceClassPathProvider implements ClassPathProvider {
    
    protected Project webProject;
    private ClassPath cp;
    
    public ClassPath findClassPath(final FileObject file, final String type) {
        if(ClassPath.SOURCE.equals(type)){
            assert file != null;
            webProject = FileOwnerQuery.getOwner(file);
            assert webProject != null;
            //if (cp == null){
            cp = ClassPathFactory.createClassPath( new WSProjectClassPathImpl() );
            //}
            return cp;
        }
        return null;
    }
    
    private class WSProjectClassPathImpl implements ClassPathImplementation {
        final protected PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        WSProjectClassPathImpl(){
            //to avoid creation of accessor class
        }
        
        public List<PathResourceImplementation> getResources() {
            return Arrays.asList(new PathResourceImplementation[]{ new PathResourceImplementation() {
                
                public ClassPathImplementation getContent() {
                    return null;
                }
                
                public URL[] getRoots() {
                    final List<URL> urls = new ArrayList<URL>();
                    FileObject fo = webProject.getProjectDirectory().getFileObject("build/generated/wsclient/"); //NOI18N
                    if (fo != null){
                        try {
                            urls.add(fo.getURL());
                        } catch (FileStateInvalidException ex) {
                        }
//                        JavaModel.getJavaRepository().beginTrans(false);
//                        try {
//                            for (final Enumeration<? extends FileObject> enu = fo.getChildren(true); enu.hasMoreElements();) {
//                                final FileObject elem = enu.nextElement();
//                                JavaModel.getResource(elem);
//                            }
//                        } catch (Exception e){
//                        } finally {
//                            JavaModel.getJavaRepository().endTrans();
//                        }
                    }
                    fo = webProject.getProjectDirectory().getFileObject("build/generated/wsbinary/"); //NOI18N
                    if (fo != null){
                        try {
                            urls.add(fo.getURL());
                        } catch (FileStateInvalidException ex) {
                        }
                    }
                    //support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
                    return urls.toArray(new URL[urls.size()]);
                }
                
                
                /**
                 * Registers PropertyChangeListener to receive events.
                 * @param listener The listener to register.
                 */
                public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
                    support.addPropertyChangeListener(listener);
                }
                
                /**
                 * Removes PropertyChangeListener from the list of listeners.
                 * @param listener The listener to remove.
                 */
                public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
                    support.removePropertyChangeListener(listener);
                }
                
            }});
        }
        
        /**
         * Utility field holding list of PropertyChangeListeners.
         */
        private transient java.util.ArrayList<java.beans.PropertyChangeListener> propertyChangeListenerList;
        
        /**
         * Registers PropertyChangeListener to receive events.
         * @param listener The listener to register.
         */
        public synchronized void addPropertyChangeListener(final java.beans.PropertyChangeListener listener) {
            if (propertyChangeListenerList == null ) {
                propertyChangeListenerList = new java.util.ArrayList<java.beans.PropertyChangeListener>();
            }
            propertyChangeListenerList.add(listener);
        }
        
        /**
         * Removes PropertyChangeListener from the list of listeners.
         * @param listener The listener to remove.
         */
        public synchronized void removePropertyChangeListener(final java.beans.PropertyChangeListener listener) {
            if (propertyChangeListenerList != null ) {
                propertyChangeListenerList.remove(listener);
            }
        }
        
        /**
         * Notifies all registered listeners about the event.
         *
         * @param event The event to be fired
         */
        @SuppressWarnings("unused")
		private void firePropertyChangeListenerPropertyChange(final java.beans.PropertyChangeEvent event) {
            java.util.ArrayList<java.beans.PropertyChangeListener> list;
            synchronized (this) {
                if (propertyChangeListenerList == null) return;
                list = new ArrayList<java.beans.PropertyChangeListener>(propertyChangeListenerList);
            }
            for ( java.beans.PropertyChangeListener cl : list ) {
                cl.propertyChange(event);
            }
        }
    }
    
}
