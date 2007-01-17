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
package org.netbeans.modules.java.editor.overridden;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.timers.TimesCollector;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationsHolder implements PropertyChangeListener {
    
    private static final Map<FileObject, AnnotationsHolder> file2Annotations = new HashMap<FileObject, AnnotationsHolder>();
    
    public static synchronized AnnotationsHolder get(FileObject file) {
        AnnotationsHolder a = file2Annotations.get(file);
        
        if (a != null) {
            return a;
        }
        
        try {
            DataObject od = DataObject.find(file);
            EditorCookie.Observable ec = od.getCookie(EditorCookie.Observable.class);
            
            if (ec == null) {
                return null;
            }
            
            file2Annotations.put(file, a = new AnnotationsHolder(file, ec));
            
            return a;
        } catch (IOException ex) {
            IsOverriddenAnnotationHandler.LOG.log(Level.INFO, null, ex);
            
            return null;
        }
    }
    
    private final FileObject file;
    private final EditorCookie.Observable ec;
    
    private AnnotationsHolder(FileObject file, EditorCookie.Observable ec) {
        this.file = file;
        this.ec   = ec;
        this.annotations = new ArrayList<IsOverriddenAnnotation>();
        
        ec.addPropertyChangeListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkForReset();
            }
        });
        
        TimesCollector.getDefault().reportReference(file, AnnotationsHolder.class.getName(), "[M] Overridden AnnotationsHolder", this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_OPENED_PANES.endsWith(evt.getPropertyName()) || evt.getPropertyName() == null) {
            checkForReset();
        }
    }
    
    private void checkForReset() {
        assert SwingUtilities.isEventDispatchThread();
        
        if (ec.getOpenedPanes() == null) {
            //reset:
            synchronized (AnnotationsHolder.class) {
                file2Annotations.remove(file);
            }
            
            setNewAnnotations(Collections.<IsOverriddenAnnotation>emptyList());
            ec.removePropertyChangeListener(this);
        }
    }

    private final List<IsOverriddenAnnotation> annotations;
    
    public synchronized void setNewAnnotations(List<IsOverriddenAnnotation> as) {
        final List<IsOverriddenAnnotation> toRemove = new ArrayList<IsOverriddenAnnotation>(annotations);
        final List<IsOverriddenAnnotation> toAdd    = new ArrayList<IsOverriddenAnnotation>(as);
        
        annotations.clear();
        annotations.addAll(as);
        
        Runnable doAttachDetach = new Runnable() {
            public void run() {
                for (IsOverriddenAnnotation a : toRemove) {
                    a.detachImpl();
                }
                
                for (IsOverriddenAnnotation a : toAdd) {
                    a.attach();
                }
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            doAttachDetach.run();
        } else {
            SwingUtilities.invokeLater(doAttachDetach);
        }
    }
    
    public synchronized List<IsOverriddenAnnotation> getAnnotations() {
        return new ArrayList<IsOverriddenAnnotation>(annotations);
    }
}
