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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.java.source.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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

    private Map<JTextComponent, ComponentListener> component2Listener = new WeakHashMap<JTextComponent, SelectionAwareJavaSourceTaskFactory.ComponentListener>();
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
        
        private Reference<JTextComponent> componentRef;
        private final RequestProcessor.Task rescheduleTask;
        
        public ComponentListener(final JTextComponent component) {
            this.componentRef = new WeakReference<JTextComponent>(component);
            rescheduleTask = WORKER.create(new Runnable() {
                public void run() {
                    JTextComponent component = componentRef == null ? null : componentRef.get();
                    if (component == null) {
                        return;
                    }
                    FileObject file = OpenedEditors.getFileObject(component);
                    
                    if (file != null) {
                        reschedule(file);
                    }
                }
            });
        }
        
        public void caretUpdate(CaretEvent e) {
            JTextComponent component = componentRef == null ? null : componentRef.get();
            if (component == null) {
                return;
            }
            FileObject file = OpenedEditors.getFileObject(component);
            
            if (file != null) {
                setLastSelection(OpenedEditors.getFileObject(component), component.getSelectionStart(), component.getSelectionEnd());
                rescheduleTask.schedule(timeout);
            }
        }
        
    }
}
