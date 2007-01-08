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
