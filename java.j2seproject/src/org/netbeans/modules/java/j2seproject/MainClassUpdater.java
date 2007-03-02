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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
public class MainClassUpdater extends FileChangeAdapter implements PropertyChangeListener {
    
    private static RequestProcessor performer = new RequestProcessor();
    
    private final Project project;
    private final PropertyEvaluator eval;
    private final UpdateHelper helper;
    private final ClassPath sourcePath;
    private final String mainClassPropName;
    private FileObject current;
    
    /** Creates a new instance of MainClassUpdater */
    public MainClassUpdater(final Project project, final PropertyEvaluator eval,
        final UpdateHelper helper, final ClassPath sourcePath, final String mainClassPropName) {
        assert project != null;
        assert eval != null;
        assert helper != null;
        assert sourcePath != null;
        assert mainClassPropName != null;
        this.project = project;
        this.eval = eval;
        this.helper = helper;
        this.sourcePath = sourcePath;
        this.mainClassPropName = mainClassPropName;
        this.eval.addPropertyChangeListener(this);
        this.addFileChangeListener ();
    }
    
    public synchronized void unregister () {
        if (current != null) {
            current.removeFileChangeListener(this);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.mainClassPropName.equals(evt.getPropertyName())) {
            this.addFileChangeListener ();
        }
    }
    
    @Override
    public void fileRenamed (final FileRenameEvent evt) {
        final FileObject _current;
        synchronized (this) {
            _current = this.current;
        }
        if (evt.getFile() == _current) {
            Runnable r = new Runnable () {
                public void run () {  
                    try {
                        final String oldMainClass = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<String>() {
                            public String run() throws Exception {
                                return eval.getProperty(mainClassPropName);
                            }
                        });

                        Collection<ElementHandle<TypeElement>> main = SourceUtils.getMainClasses(_current);
                        String newMainClass = null;
                        if (!main.isEmpty()) {
                            ElementHandle<TypeElement> mainHandle = main.iterator().next();
                            newMainClass = mainHandle.getQualifiedName();
                        }                    
                        if (newMainClass != null && !newMainClass.equals(oldMainClass) && helper.requestSave() &&
                                // XXX ##84806: ideally should update nbproject/configs/*.properties in this case:
                            eval.getProperty(J2SEConfigurationProvider.PROP_CONFIG) == null) {
                            final String newMainClassFinal = newMainClass;
                            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                                public Void run() throws Exception {                                                                                    
                                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                    props.put (mainClassPropName, newMainClassFinal);
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                    ProjectManager.getDefault().saveProject (project);
                                    return null;
                                }
                            });
                        }
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                    catch (MutexException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            }
            else {
                SwingUtilities.invokeLater(r);
            }
        }
    }
    
    private void addFileChangeListener () {
        performer.post( new Runnable () {
            public void run() {
                try {
                    SourceUtils.waitScanFinished();
                    synchronized (MainClassUpdater.this) {
                        if (current != null) {
                            current.removeFileChangeListener(MainClassUpdater.this);
                            current = null;
                        }            
                    }
                    final String mainClassName = org.netbeans.modules.java.j2seproject.MainClassUpdater.this.eval.getProperty(mainClassPropName);
                    final FileObject[] _current = new FileObject[1];
                    if (mainClassName != null) {
                        FileObject[] roots = sourcePath.getRoots();
                        if (roots.length>0) {
                            ClassPath bootCp = ClassPath.getClassPath(roots[0], ClassPath.BOOT);
                            ClassPath compileCp = ClassPath.getClassPath(roots[0], ClassPath.COMPILE);
                            final ClasspathInfo cpInfo = ClasspathInfo.create(bootCp, compileCp, sourcePath);
                            JavaSource js = JavaSource.create(cpInfo);
                            js.runUserActionTask(new CancellableTask<CompilationController>() {
                                public void cancel() {                    
                                }
                                public void run(CompilationController c) throws Exception {
                                    TypeElement te = c.getElements().getTypeElement(mainClassName);
                                    if (te != null) {
                                        _current[0] = SourceUtils.getFile(te, cpInfo);                                        
                                    }
                                }                
                            }, true);
                        }
                    }
                    synchronized (MainClassUpdater.this) {
                        current = _current[0];
                        if (current != null && sourcePath.contains(current)) {
                            current.addFileChangeListener(MainClassUpdater.this);
                        }
                    }
                } catch (InterruptedException e) {
                    Exceptions.printStackTrace(e);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }});
    }

}
