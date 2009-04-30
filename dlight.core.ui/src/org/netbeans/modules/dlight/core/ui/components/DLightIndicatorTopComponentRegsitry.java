/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.ui.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.openide.windows.TopComponent;

/**
 *
 * @author mt154047
 */
final class DLightIndicatorTopComponentRegsitry implements PropertyChangeListener {

    static String PROP_DLIGHT_TC_ACTIVATED = "dlightIndTcActivated"; //NOI18N
    static String PROP_DLIGHT_TC_OPENED = "dlightIndTcOpened";//NOI18N
    static String PROP_DLIGHT_TC_CLOSED = "dlightIndTcClosed";//NOI18N
    private static DLightIndicatorTopComponentRegsitry instance = null;

    //private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    private PropertyChangeSupport pcs;
    private Reference<DLightIndicatorsTopComponent> active = new WeakReference<DLightIndicatorsTopComponent>(null);
    private final Set<DLightIndicatorsTopComponent> opened;

    private DLightIndicatorTopComponentRegsitry() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
        opened = new HashSet<DLightIndicatorsTopComponent>();
    }

    static DLightIndicatorTopComponentRegsitry getRegistry() {
        if (instance == null) {
            instance = new DLightIndicatorTopComponentRegsitry();
        }
        return instance;
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(l);
    }

    void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    private void firePropertyChangeEvent(PropertyChangeEvent e) {
        if (pcs != null) {
            pcs.firePropertyChange(e);
        }
    }

    Set<DLightIndicatorsTopComponent> getOpened() {
        return new HashSet<DLightIndicatorsTopComponent>(opened);
    }

    DLightIndicatorsTopComponent getActivated() {
        return getActive();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //We should hanlde here all opening/closing
        //we should add/remove to the proper lists
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            TopComponent tc = TopComponent.getRegistry().getActivated();
            //we will set lastActivatedRef only to Workspace
            if (tc != null && tc instanceof DLightIndicatorsTopComponent) {
                //we should invoke setActive here
                setActive((DLightIndicatorsTopComponent) tc);
                firePropertyChangeEvent(new PropertyChangeEvent(evt.getSource(), PROP_DLIGHT_TC_ACTIVATED, evt.getOldValue(), evt.getNewValue()));
            }
        } else if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
            if (evt.getNewValue() instanceof DLightIndicatorsTopComponent) {
                DLightIndicatorsTopComponent tc = (DLightIndicatorsTopComponent) evt.getNewValue();
                opened.remove(tc);
                if (getActive() == tc) {
                    setActive(null);
                }
                //check if closed component was active
                firePropertyChangeEvent(new PropertyChangeEvent(evt.getSource(), PROP_DLIGHT_TC_CLOSED, evt.getOldValue(), evt.getNewValue()));
            }
        } else if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) evt.getNewValue();

            if (tc instanceof DLightIndicatorsTopComponent) {
                opened.add((DLightIndicatorsTopComponent) tc);
                setActive((DLightIndicatorsTopComponent) tc);
                firePropertyChangeEvent(new PropertyChangeEvent(evt.getSource(), PROP_DLIGHT_TC_ACTIVATED, evt.getOldValue(), evt.getNewValue()));
            //firePropertyChangeEvent(new PropertyChangeEvent(evt.getSource(), PROP_DLIGHT_TC_OPENED, evt.getOldValue(), evt.getNewValue()));
            }
        } else if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt.getPropertyName())) {
        }
    //throw new UnsupportedOperationException("Not supported yet.");
    }

    private synchronized void setActive(DLightIndicatorsTopComponent tc) {
        active = new WeakReference<DLightIndicatorsTopComponent>(tc);
//    Node[] _nodes = (tc == null ? new Node[0] : tc.getActivatedNodes());
//    if (_nodes != null){
//      nodes = _nodes;
//      if (pcs != null){
//        pcs.firePropertyChange(PROP_DLIGHT_ACTIVATED_NODES, null, null);
//      }
//    }
//    if (pcs != null){
////      pcs.firePropertyChange(PROP_DLIGHT_TC_ACTIVATED, null, null);
//      pcs.firePropertyChange(PROP_DLIGHT_CURRENT_NODES, null, null);
//    }
    }

    private DLightIndicatorsTopComponent getActive() {
        return active.get();
    }
}
