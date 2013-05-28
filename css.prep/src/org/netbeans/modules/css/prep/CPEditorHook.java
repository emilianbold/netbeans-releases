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
package org.netbeans.modules.css.prep;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataObject;
import org.openide.modules.OnStart;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

@OnStart
public class CPEditorHook implements PropertyChangeListener, Runnable {

    private static final RequestProcessor RP = new RequestProcessor(CPEditorHook.class);
    private static final Logger LOG = Logger.getLogger(CPEditorHook.class.getSimpleName());
    
    /**
     * Holds the instance so it is not GCed.
     */
    private static CPEditorHook INSTANCE;
    
    /**
     * Listens on DataObject's property changes.
     */
    private final PropertyChangeListener LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //TODO: the property name is not in any API, just harcoded in GsfDataObject
            if ("fileSaved".equals(evt.getPropertyName())) { //NOI18N
                DataObject dobj = (DataObject)evt.getNewValue();
                LOG.log(Level.INFO, "File {0} has been saved.", dobj.getPrimaryFile().getPath()); //NOI18N
            }
        }
    };
    
    private Collection<DataObject> active = new HashSet<>();

    @SuppressWarnings("LeakingThisInConstructor")
    public CPEditorHook() {
        INSTANCE = this; //don't GC me!
    }

    @Override
    public void run() {
        Registry reg = WindowManager.getDefault().getRegistry();
        reg.addPropertyChangeListener(
                WeakListeners.propertyChange(this, reg));
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        switch (propName) {
            case TopComponent.Registry.PROP_ACTIVATED:
                final TopComponent activated = (TopComponent) evt.getNewValue();
                if (activated == null) {
                    return;
                }
                if (WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            DataObject dobj = activated.getLookup().lookup(DataObject.class);
                            if (dobj == null) {
                                return;
                            }
                            if (!active.contains(dobj)) {
                                CPFileType type = CPFileType.find(dobj.getPrimaryFile().getMIMEType());
                                if (type != null) {
                                    //css preprocessor file, lets listen on it
                                    active.add(dobj);
                                    dobj.addPropertyChangeListener(LISTENER);
                                }
                            }

                        }
                    });
                }
                break;
            case TopComponent.Registry.PROP_TC_CLOSED:
                final TopComponent closed = (TopComponent) evt.getNewValue();
                if (closed == null) {
                    return;
                }
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        DataObject dobj = closed.getLookup().lookup(DataObject.class);
                        if (dobj == null) {
                            return;
                        }
                        if (active.contains(dobj)) {
                            dobj.removePropertyChangeListener(LISTENER);
                            active.remove(dobj);
                        }
                    }
                });
                break;

        }
    }
}
