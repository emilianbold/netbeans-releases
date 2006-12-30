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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 *  @author Jan Jancura
 */
public final class SettingsProvider implements MimeDataProvider {

    private Map<MimePath, WeakReference<Lookup>> cache = new WeakHashMap<MimePath, WeakReference<Lookup>>();
    
    public SettingsProvider () {
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup getLookup(MimePath mimePath) {
        synchronized (cache) {
            WeakReference<Lookup> ref = cache.get(mimePath);
            Lookup lookup = ref == null ? null : ref.get();
            
            if (lookup == null) {
                String path = mimePath.getPath();
                if (path.startsWith("test")) { //NOI18N
                    int idx = path.indexOf('_'); //NOI18N
                    if (idx == -1) {
                        throw new IllegalStateException("Invalid mimePath: " + path); //NOI18N
                    }
                    
                    MimePath realMimePath = MimePath.parse(path.substring(idx + 1));
                    lookup = new ProxyLookup(new Lookup [] {
                        new MyLookup(mimePath),
                        Lookups.exclude(
                            MimeLookup.getLookup(realMimePath),
                            new Class [] {
                                FontColorSettings.class,
                                KeyBindingSettings.class
                            })
                    });
                } else {
                    lookup = new MyLookup(mimePath);
                }
                
                cache.put(mimePath, new WeakReference<Lookup>(lookup));
            }
            
            return lookup;
        }
    }
    
    interface Factory {
        void addPropertyChangeListener (PropertyChangeListener l);
        Object createInstance();
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener {
        
        private MimePath mimePath;
        
        private Factory[] factories;
        private Object[] instances;
        private InstanceContent ic;
        private List removedInstances;
        
        public MyLookup(MimePath mimePath) {
            this(mimePath, new InstanceContent());
        }
        
        private MyLookup(MimePath mimePath, InstanceContent ic) {
            super(ic);
            this.mimePath = mimePath;
            this.ic = ic;
        }
        
        protected void initialize() {
            factories = new Factory [] {
                (Factory) FontColorSettingsImpl.get(mimePath),
                (Factory) KeyBindingSettingsImpl.get(mimePath),
            };
            instances = new Object [factories.length];
            
            for (int i = 0; i < factories.length; i++) {
                instances[i] = factories[i].createInstance();
            }
            
            ic.set(Arrays.asList(instances), null);
            
            for (int i = 0; i < factories.length; i++) {
                factories[i].addPropertyChangeListener(
                    WeakListeners.propertyChange(this, factories[i]));
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            Factory f = (Factory)evt.getSource();
            for (int i = 0; i < factories.length; i++) {
                if (factories[i] == f) {
                    instances[i] = f.createInstance();
                    assert instances[i] != null;
            
                    ic.set(Arrays.asList(instances), null);
                    break;
                }
            }
        }
    } // End of MyLookup class
}
