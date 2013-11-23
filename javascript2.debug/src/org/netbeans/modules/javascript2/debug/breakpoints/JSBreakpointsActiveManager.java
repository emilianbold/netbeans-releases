/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.debug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Tracking of active breakpoints flag.
 * 
 * @author Martin
 */
public final class JSBreakpointsActiveManager {
    
    private static JSBreakpointsActiveManager INSTANCE;
    
    private Collection<? extends JSBreakpointsActiveService> activeServices;
    private final Object activeServicesLock = new Object();
    private final PropertyChangeListener servicePCL = new ServicePropertyChangeListener();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Boolean lastActive = null;
    
    private JSBreakpointsActiveManager() {
        final Lookup.Result<JSBreakpointsActiveService> lookupResult =
                Lookup.getDefault().lookupResult(JSBreakpointsActiveService.class);
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                 initServices(lookupResult.allInstances());
            }
        });
        initServices(lookupResult.allInstances());
    }
    
    public static synchronized JSBreakpointsActiveManager getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new JSBreakpointsActiveManager();
        }
        return INSTANCE;
    }
    
    private void initServices(Collection<? extends JSBreakpointsActiveService> activeServices) {
        for (JSBreakpointsActiveService as : activeServices) {
            as.addPropertyChangeListener(servicePCL);
        }
        synchronized (activeServicesLock) {
            this.activeServices = activeServices;
            lastActive = null;
        }
        fireChange();
    }
    
    public boolean areBreakpointsActive() {
        boolean are = true;
        synchronized (activeServicesLock) {
            if (lastActive != null) {
                are = lastActive.booleanValue();
            } else {
                for (JSBreakpointsActiveService as : activeServices) {
                    if (!as.areBreakpointsActive()) {
                        are = false;
                        break;
                    }
                }
                lastActive = are;
            }
        }
        return are;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void fireChange() {
        pcs.firePropertyChange(JSBreakpointsActiveService.PROP_BREAKPOINTS_ACTIVE, null, null);
    }
    
    private final class ServicePropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JSBreakpointsActiveService.PROP_BREAKPOINTS_ACTIVE.equals(evt.getPropertyName())) {
                synchronized (activeServicesLock) {
                    lastActive = null;
                }
                fireChange();
            }
        }
        
    }
}
