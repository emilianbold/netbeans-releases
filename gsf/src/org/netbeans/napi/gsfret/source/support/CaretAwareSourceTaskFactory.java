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
package org.netbeans.napi.gsfret.source.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source.Priority;
import org.netbeans.napi.gsfret.source.SourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * A {@link SourceTaskFactorySupport} that registers tasks to all files that are
 * opened in the editor and are visible. This factory also listens on the caret on
 * opened and visible JTextComponents and reschedules the tasks as necessary.
 *
 * The tasks may access current caret position using {@link #getLastPosition} method.
 *
 * @author Jan Lahoda
 */
public abstract class CaretAwareSourceTaskFactory extends SourceTaskFactory {
    
    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("CaretAwareSourceTaskFactory worker");
    
    private int timeout;
    
    /**Construct the CaretAwareSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public CaretAwareSourceTaskFactory(Phase phase, Priority priority) {
        super(phase, priority);
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
    }
    
    /**@inheritDoc*/
    public List<FileObject> getFileObjects() {
        List<FileObject> files = new ArrayList<FileObject>(OpenedEditors.getDefault().getVisibleEditorsFiles());

        return files;
    }

    private Map<JTextComponent, ComponentListener> component2Listener = new HashMap();
    private static Map<FileObject, Integer> file2LastPosition = new WeakHashMap();
    
    /**Returns current caret position in current {@link JTextComponent} for a given file.
     *
     * @param file file from which the position should be found
     * @return caret position in the current {@link JTextComponent} for a given file.
     */
    public synchronized static int getLastPosition(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Cannot pass null file!");
        }
        
        Integer position = file2LastPosition.get(file);
        
        if (position == null) {
            //no position set yet:
            // XXX Shouldn't that be -1?
            return 0;
        }
        
        return position;    
    }
    
    synchronized static void setLastPosition(FileObject file, int position) {
       file2LastPosition.put(file, position);
    }
    
    private class ChangeListenerImpl implements ChangeListener {
        
        public void stateChanged(ChangeEvent e) {
            List<JTextComponent> added = new ArrayList<JTextComponent>(OpenedEditors.getDefault().getVisibleEditors());
            List<JTextComponent> removed = new ArrayList<JTextComponent>(component2Listener.keySet());
            
            added.removeAll(component2Listener.keySet());
            removed.removeAll(OpenedEditors.getDefault().getVisibleEditors());
            
            for (JTextComponent c : removed) {
                c.removeCaretListener(component2Listener.remove(c));
            }
            
            for (JTextComponent c : added) {
                ComponentListener l = new ComponentListener(c);
                
                c.addCaretListener(l);
                component2Listener.put(c, l);
                
                //TODO: are we in AWT Thread?:
                setLastPosition(OpenedEditors.getFileObject(c), c.getCaretPosition());
            }
            
            fileObjectsChanged();
        }
        
    }
    
    private class ComponentListener implements CaretListener {
        
        private JTextComponent component;
        private final RequestProcessor.Task rescheduleTask;
        
        public ComponentListener(JTextComponent component) {
            this.component = component;
            rescheduleTask = WORKER.create(new Runnable() {
                public void run() {
                    FileObject file = OpenedEditors.getFileObject(ComponentListener.this.component);
                    
                    if (file != null) {
                        setLastPosition(file, ComponentListener.this.component.getCaretPosition());
                        reschedule(file);
                    }
                }
            });
        }
        
        public void caretUpdate(CaretEvent e) {
            FileObject file = OpenedEditors.getFileObject(component);
            
            if (file != null) {
                setLastPosition(file, component.getCaretPosition());
                rescheduleTask.schedule(timeout);
            }
        }
        
    }
}
