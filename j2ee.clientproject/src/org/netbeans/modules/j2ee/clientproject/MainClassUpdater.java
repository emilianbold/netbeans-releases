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
package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.RequestProcessor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
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
