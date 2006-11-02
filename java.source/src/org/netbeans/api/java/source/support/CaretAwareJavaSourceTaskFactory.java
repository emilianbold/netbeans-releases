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
package org.netbeans.api.java.source.support;

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
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**A {@link JavaSourceTaskFactorySupport} that registers tasks to all files that are
 * opened in the editor and are visible. This factory also listens on the caret on
 * opened and visible JTextComponents and reschedules the tasks as necessary.
 *
 * The tasks may access current caret position using {@link #getLastPosition} method.
 *
 * @author Jan Lahoda
 */
public abstract class CaretAwareJavaSourceTaskFactory extends JavaSourceTaskFactory {
    
    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("CaretAwareJavaSourceTaskFactory worker");
    
    private int timeout;
    
    /**Construct the CaretAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public CaretAwareJavaSourceTaskFactory(Phase phase, Priority priority) {
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
