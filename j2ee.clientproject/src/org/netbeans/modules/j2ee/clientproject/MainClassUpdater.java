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
package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.RequestProcessor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

//TODO: RETOUCHE almost complete rewrite needed
class MainClassUpdater implements PropertyChangeListener {

    private static RequestProcessor performer = new RequestProcessor();

    private final Project project;
    private final PropertyEvaluator eval;
    private final UpdateHelper helper;
    private final ClassPath sourcePath;
    private final String mainClassPropName;
//    private JavaClass mainClass;

    public MainClassUpdater (Project project, PropertyEvaluator eval, UpdateHelper helper, ClassPath sourcePath, String mainClassPropName) {
        this.project = project;
        this.eval = eval;
        this.helper = helper;
        this.sourcePath = sourcePath;
        this.mainClassPropName = mainClassPropName;
        this.eval.addPropertyChangeListener (this);
//        this.addClassListener ();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (mainClassPropName.equals(evt.getPropertyName())) {
//            this.addClassListener ();
        }
    }

//    public void change(MDRChangeEvent event) {
//        if (event.isOfType (AttributeEvent.EVENTMASK_ATTRIBUTE)) {
//            AttributeEvent atEvent = (AttributeEvent) event;
//            String attributeName = atEvent.getAttributeName();
//            if ("name".equals(attributeName)) { //NOI18N
//                final String newMainClassName = (String) atEvent.getNewElement();
//                if (newMainClassName != null) {                    
//                    Runnable r = new Runnable () {
//                        public void run () {
//                            try {
//                                //#63048:Deadlock while renaming main class of older j2se project
//                                //Don't show a modal dialog under mutex
//                                final String oldMainClass = (String) ProjectManager.mutex().readAccess(
//                                        new Mutex.ExceptionAction () {
//                                            public Object run () throws Exception {
//                                                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                                                return props.getProperty(mainClassPropName);
//                                            }
//                                });                        
//                                if (!newMainClassName.equals(oldMainClass) && helper.requestSave()) {
//                                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction () {
//                                        public Object run() throws Exception {
//                                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                                            props.put(mainClassPropName, newMainClassName);
//                                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
//                                            ProjectManager.getDefault().saveProject (project);
//                                            return null;
//                                        }
//                                    });
//                                }
//                            } catch (MutexException e) {
//                                ErrorManager.getDefault().notify (e);
//                            }
//                            catch (IOException ioe) {
//                                ErrorManager.getDefault().notify (ioe);
//                            }
//                        }
//                    };
//                    //#63048:Deadlock while renaming main class of older j2se project
//                    //If we are not in the AWT thread reschedule it,
//                    //the UpdateHelper may need to display a dialog
//                    if (SwingUtilities.isEventDispatchThread()) {
//                        r.run();
//                    }
//                    else {
//                        SwingUtilities.invokeLater(r);
//                    }
//                }
//            }
//        }
//    }

    public synchronized void unregister () {
//        if (mainClass != null) {
//            ((MDRChangeSource)mainClass).removeListener (this);
//            mainClass = null;
//        }
    }

//    private void addClassListener () {
//        performer.post( new Runnable () {
//            public void run() {
//                //XXX: Implementation dependency, no way how to do it
//                JMManager manager = (JMManager) JavaMetamodel.getManager();
//                manager.waitScanFinished();
//                JavaModel.getJavaRepository().beginTrans(false);
//                try {
//                    JavaModel.setClassPath (sourcePath);
//                    String mainClassName = MainClassUpdater.this.eval.getProperty (mainClassPropName);
//                    Type type = manager.getDefaultExtent().getType().resolve(mainClassName);
//                    if ((type instanceof JavaClass) && ! (type instanceof UnresolvedClass)) {
//                        synchronized (MainClassUpdater.this) {
//                            if (MainClassUpdater.this.mainClass != null) {
//                                ((MDRChangeSource)mainClass).removeListener (MainClassUpdater.this);
//                            }
//                            MainClassUpdater.this.mainClass = (JavaClass) type;
//                            ((MDRChangeSource)MainClassUpdater.this.mainClass).addListener (MainClassUpdater.this);
//                        }
//                    }
//                } finally {
//                    JavaModel.getJavaRepository().endTrans();
//                }
//            }
//        });
//    }

}
