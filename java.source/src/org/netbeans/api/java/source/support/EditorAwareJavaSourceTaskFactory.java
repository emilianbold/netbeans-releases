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

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;

/**A {@link JavaSourceTaskFactorySupport} that registers tasks to all files that are
 * opened in the editor and are visible.
 *
 * @author Jan Lahoda
 */
public abstract class EditorAwareJavaSourceTaskFactory extends JavaSourceTaskFactory {
    
    private String[] supportedMimeTypes;
    
    /**Construct the EditorAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    protected EditorAwareJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String[]) null);
    }
    
    /**Construct the EditorAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run
     * @since 0.21
     */
    protected EditorAwareJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority);
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }
    
    /**@inheritDoc*/
    public List<FileObject> getFileObjects() {
        List<FileObject> files = OpenedEditors.filterSupportedMIMETypes(OpenedEditors.getDefault().getVisibleEditorsFiles(), supportedMimeTypes);

        return files;
    }

    private class ChangeListenerImpl implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fileObjectsChanged();
        }
    }

}
