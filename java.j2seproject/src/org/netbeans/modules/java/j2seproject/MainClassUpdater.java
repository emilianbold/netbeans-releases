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
package org.netbeans.modules.java.j2seproject;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.mdr.events.MDRChangeListener;
import org.netbeans.api.mdr.events.MDRChangeEvent;
import org.netbeans.api.mdr.events.MDRChangeSource;
import org.netbeans.api.mdr.events.AttributeEvent;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.UnresolvedClass;

class MainClassUpdater implements PropertyChangeListener, MDRChangeListener {

    private static RequestProcessor performer = new RequestProcessor();

    private final Project project;
    private final PropertyEvaluator eval;
    private final UpdateHelper helper;
    private final ClassPath sourcePath;
    private final String mainClassPropName;
    private JavaClass mainClass;

    public MainClassUpdater (Project project, PropertyEvaluator eval, UpdateHelper helper, ClassPath sourcePath, String mainClassPropName) {
        this.project = project;
        this.eval = eval;
        this.helper = helper;
        this.sourcePath = sourcePath;
        this.mainClassPropName = mainClassPropName;
        this.eval.addPropertyChangeListener (this);
        this.addClassListener ();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (mainClassPropName.equals(evt.getPropertyName())) {
            this.addClassListener ();
        }
    }

    public void change(MDRChangeEvent event) {
        if (event.isOfType (AttributeEvent.EVENTMASK_ATTRIBUTE)) {
            AttributeEvent atEvent = (AttributeEvent) event;
            String attributeName = atEvent.getAttributeName();
            if ("name".equals(attributeName)) { //NOI18N
                final String newMainClassName = (String) atEvent.getNewElement();
                if (newMainClassName != null) {
                    try {
                        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                String oldMainClass = props.getProperty(mainClassPropName);
                                if (!newMainClassName.equals(oldMainClass)) {
                                    props.put(mainClassPropName, newMainClassName);          //NOI18N
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                                    ProjectManager.getDefault().saveProject (project);
                                }
                                return null;
                            }
                        });
                    } catch (MutexException e) {
                        ErrorManager.getDefault().notify (e);
                    }
                }
            }
        }
    }

    public synchronized void unregister () {
        if (mainClass != null) {
            ((MDRChangeSource)mainClass).removeListener (this);
            mainClass = null;
        }
    }

    private void addClassListener () {
        performer.post( new Runnable () {
            public void run() {
                //XXX: Implementation dependency, no way how to do it
                JMManager manager = (JMManager) JavaMetamodel.getManager();
                manager.waitScanFinished();
                JavaModel.getJavaRepository().beginTrans(false);
                try {
                    JavaModel.setClassPath (sourcePath);
                    String mainClassName = MainClassUpdater.this.eval.getProperty (mainClassPropName);
                    Type type = manager.getDefaultExtent().getType().resolve(mainClassName);
                    if ((type instanceof JavaClass) && ! (type instanceof UnresolvedClass)) {
                        synchronized (MainClassUpdater.this) {
                            if (MainClassUpdater.this.mainClass != null) {
                                ((MDRChangeSource)mainClass).removeListener (MainClassUpdater.this);
                            }
                            MainClassUpdater.this.mainClass = (JavaClass) type;
                            ((MDRChangeSource)MainClassUpdater.this.mainClass).addListener (MainClassUpdater.this);
                        }
                    }
                } finally {
                    JavaModel.getJavaRepository().endTrans();
                }
            }
        });
    }

}
