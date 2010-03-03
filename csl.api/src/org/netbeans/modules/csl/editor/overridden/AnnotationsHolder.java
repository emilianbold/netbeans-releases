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
package org.netbeans.modules.csl.editor.overridden;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
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
 *
 * @author Jan Lahoda
 */
public class AnnotationsHolder implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(AnnotationsHolder.class.getName());
    private static final Map<DataObject, AnnotationsHolder> file2Annotations = new HashMap<DataObject, AnnotationsHolder>();
    
    public static synchronized AnnotationsHolder get(FileObject file) {
        try {
            DataObject od = DataObject.find(file);
            AnnotationsHolder a = file2Annotations.get(od);

            if (a != null) {
                return a;
            }

            EditorCookie.Observable ec = od.getLookup().lookup(EditorCookie.Observable.class);
            
            if (ec == null) {
                return null;
            }
            
            file2Annotations.put(od, a = new AnnotationsHolder(od, ec));
            
            return a;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            
            return null;
        }
    }
    
    private final DataObject file;
    private final EditorCookie.Observable ec;
    
    private AnnotationsHolder(DataObject file, EditorCookie.Observable ec) {
        this.file = file;
        this.ec   = ec;
        this.annotations = new ArrayList<IsOverriddenAnnotation>();
        
        ec.addPropertyChangeListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                checkForReset();
            }
        });
        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden AnnotationsHolder", new Object[] {file.getPrimaryFile(), this}); //NOI18N
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
