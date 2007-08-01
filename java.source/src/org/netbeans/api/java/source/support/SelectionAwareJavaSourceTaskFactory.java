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
 * opened in the editor and are visible. This factory also listens on the selection in
 * opened and visible JTextComponents and reschedules the tasks as necessary.
 *
 * The tasks may access current selection span using {@link #getLastSelection} method.
 *
 * @since 0.15
 * 
 * @author Jan Lahoda
 */
public abstract class SelectionAwareJavaSourceTaskFactory extends JavaSourceTaskFactory {
    
    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("SelectionAwareJavaSourceTaskFactory worker");
    
    private int timeout;
    private String[] supportedMimeTypes;
    
    /**Construct the SelectionAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public SelectionAwareJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String []) null);
    }
    
    /**Construct the SelectionAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run
     * @since 0.22
     */
    public SelectionAwareJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority);
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }
    
    /**@inheritDoc*/
    public List<FileObject> getFileObjects() {
        List<FileObject> files = OpenedEditors.filterSupportedMIMETypes(OpenedEditors.getDefault().getVisibleEditorsFiles(), supportedMimeTypes);

        return files;
    }

    private Map<JTextComponent, ComponentListener> component2Listener = new HashMap<JTextComponent, SelectionAwareJavaSourceTaskFactory.ComponentListener>();
    private static Map<FileObject, Integer> file2SelectionStartPosition = new WeakHashMap<FileObject, Integer>();
    private static Map<FileObject, Integer> file2SelectionEndPosition = new WeakHashMap<FileObject, Integer>();
    
    /**Returns current selection span in current {@link JTextComponent} for a given file.
     *
     * @param file file from which the position should be found
     * @return selection span in the current {@link JTextComponent} for a given file.
     *         <code>null</code> if no selection available so far.
     */
    public synchronized static int[] getLastSelection(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Cannot pass null file!");
        }
        
        Integer startPosition = file2SelectionStartPosition.get(file);
        Integer endPosition = file2SelectionEndPosition.get(file);
        
        if (startPosition == null || endPosition == null) {
            //no position set yet:
            return null;
        }
        
        return new int[] {startPosition, endPosition};
    }
    
    private synchronized static void setLastSelection(FileObject file, int startPosition, int endPosition) {
        file2SelectionStartPosition.put(file, startPosition);
        file2SelectionEndPosition.put(file, endPosition);
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
                setLastSelection(OpenedEditors.getFileObject(c), c.getSelectionStart(), c.getSelectionEnd());
            }
            
            fileObjectsChanged();
        }
        
    }
    
    private class ComponentListener implements CaretListener {
        
        private JTextComponent component;
        private final RequestProcessor.Task rescheduleTask;
        
        public ComponentListener(final JTextComponent component) {
            this.component = component;
            rescheduleTask = WORKER.create(new Runnable() {
                public void run() {
                    FileObject file = OpenedEditors.getFileObject(ComponentListener.this.component);
                    
                    if (file != null) {
                        reschedule(file);
                    }
                }
            });
        }
        
        public void caretUpdate(CaretEvent e) {
            FileObject file = OpenedEditors.getFileObject(component);
            
            if (file != null) {
                setLastSelection(OpenedEditors.getFileObject(component), component.getSelectionStart(), component.getSelectionEnd());
                rescheduleTask.schedule(timeout);
            }
        }
        
    }
}
