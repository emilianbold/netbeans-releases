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
package org.netbeans.modules.cnd.model.tasks;

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
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

public abstract class CaretAwareCsmFileTaskFactory extends CsmFileTaskFactory {

    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("CaretAwareCsmFileTaskFactory worker"); //NOI18N
    private int timeout;

    public CaretAwareCsmFileTaskFactory() {
        super();
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> files = OpenedEditors.filterSupportedFiles(OpenedEditors.getDefault().getVisibleEditorsFiles());

        return files;
    }
    private Map<JTextComponent, ComponentListener> component2Listener = new HashMap<JTextComponent, ComponentListener>();
    private static Map<FileObject, Integer> file2LastPosition = new WeakHashMap<FileObject, Integer>();

    public synchronized static int getLastPosition(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Cannot pass null file!"); //NOI18N
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
