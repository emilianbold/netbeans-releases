/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.navigation.overrides;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * After org.netbeans.modules.java.editor.overridden.AnnotationsHolder by
 * @author Jan Lahoda
 * @author Vladimir Kvashin
 */
public class AnnotationsHolder implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(AnnotationsHolder.class.getName());

    /**
     * Maps file objects to AnnotationsHolder instances.
     * Synchronized by itself
     */
    private static final Map<DataObject, AnnotationsHolder> file2holders = new HashMap<DataObject, AnnotationsHolder>();
    
    public static AnnotationsHolder get(FileObject file) {
        try {
            DataObject od = DataObject.find(file);
            synchronized (file2holders) {
                AnnotationsHolder a = file2holders.get(od);
                if (a != null) {
                    return a;
                }
                EditorCookie.Observable ec = od.getLookup().lookup(EditorCookie.Observable.class);
                if (ec == null) {
                    return null;
                }
                file2holders.put(od, a = new AnnotationsHolder(od, ec));
                return a;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);            
            return null;
        }
    }
    
    private final DataObject file;
    private final EditorCookie.Observable ec;

    /**
     * Contains annotations that has been already attached to the document.
     * Accessed ONLY WITHIN AWT THREAD => needs no synchronization.
     */
    private final Collection<OverriddeAnnotation> attachedAnnotations = new ArrayList<OverriddeAnnotation>();

    /**
     * Annotations that are to be attached to the document.
     * Synchronized by pendingAnnotationsLock.
     */
    private Collection<OverriddeAnnotation> pendingAnnotations = null;

    /** 
     * A lock for accesing pendingAnnotations
     */
    private final Object pendingAnnotationsLock = new Object();
    
    private AnnotationsHolder(DataObject file, EditorCookie.Observable ec) {
        this.file = file;
        this.ec   = ec;        
        ec.addPropertyChangeListener(this);        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkForReset();
            }
        });
        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden AnnotationsHolder", new Object[] {file.getPrimaryFile(), this}); //NOI18N
     }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_OPENED_PANES.endsWith(evt.getPropertyName()) || evt.getPropertyName() == null) {
            checkForReset();
        }
    }
    
    private void checkForReset() {
        assert SwingUtilities.isEventDispatchThread();        
        if (ec.getOpenedPanes() == null) {
            //reset:
            synchronized (file2holders) {
                file2holders.remove(file);
            }            
            setNewAnnotations(Collections.<OverriddeAnnotation>emptyList());
            ec.removePropertyChangeListener(this);
        }
    }
    
    public void setNewAnnotations(Collection<OverriddeAnnotation> annotations2set) {

        synchronized (pendingAnnotationsLock) {
            pendingAnnotations = new ArrayList<OverriddeAnnotation>(annotations2set);
        }

        Runnable doAttachDetach = new Runnable() {
            @Override
            public void run() {
                assertUiThread();
                // First, clear old annotations.
                // This should be done even if annotations are to be updated again.
                for (OverriddeAnnotation a : attachedAnnotations) {
                    a.detachImpl();
                }
                attachedAnnotations.clear();
                // Remember pendingAnnotations and set it to null
                Collection<OverriddeAnnotation> toAdd;
                synchronized (pendingAnnotationsLock) {
                    toAdd = pendingAnnotations;
                    pendingAnnotations = null;
                }
                if (toAdd == null) {
                    return;
                }
                for (OverriddeAnnotation a : toAdd) {
                    a.attach();
                    attachedAnnotations.add(a);
                }
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            doAttachDetach.run();
        } else {
            SwingUtilities.invokeLater(doAttachDetach);
        }
    }

    /**
     * Gets annotations that have been attached to the document.
     * Should be called ONLY FROM AWT THREAD
     */
    public synchronized List<OverriddeAnnotation> getAttachedAnnotations() {
        assertUiThread();
        return new ArrayList<OverriddeAnnotation>(attachedAnnotations);
    }

    private void assertUiThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("AnnotationsHolder.getAnnotations should be called only from AWT thread!"); //NOI18N
        }
    }
}
