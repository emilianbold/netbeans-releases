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

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.modules.ModuleInstall;
import org.openide.util.WeakListeners;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class EditorBookmarksModule extends ModuleInstall {

    private static final String DOCUMENT_TRACKER_PROP = "EditorBookmarksModule.DOCUMENT_TRACKER_PROP"; //NOI18N
    
    private PropertyChangeListener editorsTracker;
    private PropertyChangeListener documentTracker;
    private PropertyChangeListener openProjectsListener;
    private static List listeners = new ArrayList(); // List<ChangeListener>
    private PropertyChangeListener annotationTypesListener;
    private List lastOpenProjects;

    public void restored () {
        editorsTracker = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // event for the editors tracker
                if (evt.getSource() == EditorRegistry.class) {
                    if (evt.getPropertyName() == null || EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                        JTextComponent jtc = (JTextComponent) evt.getNewValue();
                        BookmarkList.get(jtc.getDocument()); // Initialize the bookmark list

                        PropertyChangeListener l = (PropertyChangeListener) jtc.getClientProperty(DOCUMENT_TRACKER_PROP);
                        if (l == null) {
                            jtc.putClientProperty(DOCUMENT_TRACKER_PROP, documentTracker);
                            jtc.addPropertyChangeListener(documentTracker);
                        }
                    }
                    return;
                }
                
                // event for the document tracker
                if (evt.getSource() instanceof JTextComponent) {
                    if (evt.getPropertyName() == null || "document".equals(evt.getPropertyName())) { //NOI18N
                        Document newDoc = (Document)evt.getNewValue();
                        if (newDoc != null) {
                            BookmarkList.get(newDoc); // ask for the list to initialize it
                        }
                    }
                    return;
                }
            }
        };
        documentTracker = WeakListeners.propertyChange(editorsTracker, null);
        EditorRegistry.addPropertyChangeListener(editorsTracker);
        
        // Start listening on project closing
        openProjectsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                List openProjects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
                // lastOpenProjects will contain the just closed projects
                lastOpenProjects.removeAll(openProjects);
                for (Iterator it = lastOpenProjects.iterator(); it.hasNext();) {
                    Project prj = (Project)it.next();
                    PersistentBookmarks.saveProjectBookmarks(prj);
                }
                lastOpenProjects = new ArrayList(openProjects);
            }
        };
        OpenProjects op = OpenProjects.getDefault();
        lastOpenProjects = new ArrayList(Arrays.asList(op.getOpenProjects()));
        op.addPropertyChangeListener(openProjectsListener);
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                final Iterator it = EditorRegistry.componentList().iterator();
                if (!it.hasNext()){
                    return ;
                }
                
                AnnotationType type = AnnotationTypes.getTypes().getType(NbBookmarkImplementation.BOOKMARK_ANNOTATION_TYPE);
                if (type == null){
                    // bookmark type was not added into AnnotationTypes yet, wait for event
                    AnnotationTypes.getTypes().addPropertyChangeListener(annotationTypesListener = new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            AnnotationType type = AnnotationTypes.getTypes().getType(NbBookmarkImplementation.BOOKMARK_ANNOTATION_TYPE);
                            if (type != null){
                                AnnotationTypes.getTypes().removePropertyChangeListener(annotationTypesListener);
                                while(it.hasNext()) {
                                    JTextComponent jtc = (JTextComponent)it.next();
                                    BookmarkList.get(jtc.getDocument()); // Initialize the bookmark list
                                }
                            }
                        }
                    });
                } else {
                    while(it.hasNext()){
                        JTextComponent jtc = (JTextComponent)it.next();
                        BookmarkList.get(jtc.getDocument()); // Initialize the bookmark list
                    }
                }
            }
        });
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public boolean closing() {
        // this used to be called from close(), but didn't save properly on JDK6,
        // no idea why, see #120880
        finish();
        return super.closing();
    }
    
    /**
     * Called when module is uninstalled.
     */
    public void uninstalled() {
        finish();
    }
    
    private void finish() {
        // Stop listening on projects closing
        OpenProjects.getDefault().removePropertyChangeListener(openProjectsListener);
        
        EditorRegistry.removePropertyChangeListener(editorsTracker);
        
        // Save bookmarks for all opened projects with touched bookmarks
        PersistentBookmarks.saveAllProjectBookmarks();
        
        // XXX: notify NbBookmarkManager that the module is uninstalled and BookmarkList can be removed
        // from doc.clientProperty
        fireChange();
    }

    public static synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public static synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private static void fireChange() {
        ChangeEvent ev = new ChangeEvent(EditorBookmarksModule.class);
        ChangeListener[] ls;
        synchronized (EditorBookmarksModule.class) {
            ls = (ChangeListener[]) listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (int i = 0; i < ls.length; i++) {
            ls[i].stateChanged(ev);
        }
    }
}
