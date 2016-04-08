/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.spi.debugger.ui;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.Watch.Pin;
import org.netbeans.modules.debugger.ui.annotations.WatchAnnotationProvider;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public final class PinWatchUISupport {
    
    /** Annotation type constant. */
    public static final String WATCH_ANNOTATION_TYPE = "PinnedWatch";
    
    private static final PinWatchUISupport INSTANCE = new PinWatchUISupport();
    private final Object valueProvidersLock = new Object();
    private Map<String, DelegatingValueProvider> valueProviders;
    
    private PinWatchUISupport() {
        WatchAnnotationProvider.PIN_SUPPORT_ACCESS = new WatchAnnotationProvider.PinSupportedAccessor() {
            @Override
            public ValueProvider getValueProvider(EditorPin pin) {
                String id = pin.getVpId();
                if (id == null) {
                    return null;
                }
                synchronized (valueProvidersLock) {
                    return getValueProviders().get(id);
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                new DebuggerManagerAdapter() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        refreshValueProviders();
                    }
                }
        );
    }
    
    public static PinWatchUISupport getDefault() {
        return INSTANCE;
    }
    
    public void pin(Watch watch, String valueProviderId) throws IllegalArgumentException, DataObjectNotFoundException {
        Pin wpin = watch.getPin();
        if (!(wpin instanceof EditorPin)) {
            throw new IllegalArgumentException("Unsupported pin: "+wpin);
        }
        synchronized (valueProvidersLock) {
            if (!getValueProviders().containsKey(valueProviderId)) {
                valueProviders.put(valueProviderId, new DelegatingValueProvider(valueProviderId));
            }
        }
        EditorPin pin = (EditorPin) wpin;
        pin.setVpId(valueProviderId);
        WatchAnnotationProvider.PIN_SUPPORT_ACCESS.pin(watch);
    }
    
    private Map<String, DelegatingValueProvider> getValueProviders() {
        synchronized (valueProvidersLock) {
            if (valueProviders == null) {
                valueProviders = new HashMap<>();
                refreshValueProviders();
            }
            return valueProviders;
        }
    }
    
    private void refreshValueProviders() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        List<? extends ValueProvider> providers;
        if (e == null) {
            providers = dm.lookup (null, ValueProvider.class);
        } else {
            providers = DebuggerManager.join(e, dm).lookup (null, ValueProvider.class);
        }
        if (!providers.isEmpty()) {
            synchronized (valueProvidersLock) {
                if (valueProviders == null) {
                    valueProviders = new HashMap<>();
                }
                Set<String> existingProviderIds = new HashSet<>();
                for (ValueProvider provider : providers) {
                    String id = provider.getId();
                    existingProviderIds.add(id);
                    DelegatingValueProvider dvp = valueProviders.get(id);
                    if (dvp == null) {
                        dvp = new DelegatingValueProvider(id);
                        valueProviders.put(id, dvp);
                    }
                    dvp.setDelegate(provider);
                }
                Set<String> staleProviderIds = new HashSet(valueProviders.keySet());
                staleProviderIds.removeAll(existingProviderIds);
                for (String staleId : staleProviderIds) {
                    valueProviders.get(staleId).setDelegate(null);
                }
            }
        }
    }
    
    private static final class DelegatingValueProvider implements ValueProvider {
        
        private final String id;
        private volatile ValueProvider delegate;
        private final Map<Watch, ValueChangeListener> listeners = new HashMap<>();
        
        DelegatingValueProvider(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getValue(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getValue(watch);
            } else {
                return null;
            }
        }

        @Override
        public synchronized void setChangeListener(Watch watch, ValueChangeListener chl) {
            ValueProvider vp = delegate;
            if (vp != null) {
                vp.setChangeListener(watch, chl);
            }
            listeners.put(watch, chl);
        }

        @Override
        public synchronized void unsetChangeListener(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                vp.unsetChangeListener(watch);
            }
            listeners.remove(watch);
        }
        
        synchronized void setDelegate(ValueProvider delegate) {
            this.delegate = delegate;
            if (delegate == null) {
                for (Map.Entry<Watch, ValueChangeListener> wvl : listeners.entrySet()) {
                    wvl.getValue().valueChanged(wvl.getKey());
                }
            } else {
                for (Map.Entry<Watch, ValueChangeListener> wvl : listeners.entrySet()) {
                    delegate.setChangeListener(wvl.getKey(), wvl.getValue());
                }
            }
        }
        
    }
    
    public static interface ValueProvider {
        
        @NbBundle.Messages("WATCH_EVALUATING=Evaluating...")
        public static String VALUE_EVALUATING = Bundle.WATCH_EVALUATING();
        
        String getId();
        
        String getValue(Watch watch);
        
        void setChangeListener(Watch watch, ValueChangeListener chl);
        
        void unsetChangeListener(Watch watch);
        
        public static interface ValueChangeListener {
            
            void valueChanged(Watch watch);
        }
    }
}
