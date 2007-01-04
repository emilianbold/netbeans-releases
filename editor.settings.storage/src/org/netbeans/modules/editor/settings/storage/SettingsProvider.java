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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
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

    private static final Logger LOG = Logger.getLogger(SettingsProvider.class.getName());
    
    private final Map<MimePath, WeakReference<Lookup>> cache = new WeakHashMap<MimePath, WeakReference<Lookup>>();
    
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
                    
                    // Get the special test profile name and the real mime path
                    String profile = path.substring(0, idx);
                    MimePath realMimePath = MimePath.parse(path.substring(idx + 1));
                    
                    lookup = new ProxyLookup(new Lookup [] {
                        new MyLookup(realMimePath, profile),
                        Lookups.exclude(
                            MimeLookup.getLookup(realMimePath),
                            new Class [] {
                                FontColorSettings.class,
                                KeyBindingSettings.class
                            })
                    });
                } else {
                    lookup = new MyLookup(mimePath, null);
                }
                
                cache.put(mimePath, new WeakReference<Lookup>(lookup));
            }
            
            return lookup;
        }
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener {
        
        private final MimePath mimePath;
        private final MimePath [] allMimePaths;
        private final boolean specialFcsProfile;
        private String fcsProfile;
        
        private final InstanceContent ic;
        private Object fontColorSettings = null;
        private Object keyBindingSettings = null;
        
        private KeyBindingSettingsImpl kbsi;
        
        public MyLookup(MimePath mimePath, String profile) {
            this(mimePath, profile, new InstanceContent());
        }
        
        private MyLookup(MimePath mimePath, String profile, InstanceContent ic) {
            super(ic);

            this.mimePath = mimePath;
            this.allMimePaths = computeInheritedMimePaths(mimePath);
            
            if (profile == null) {
                // Use the selected current profile
                String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                this.fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(currentProfile);
                this.specialFcsProfile = false;
            } else {
                // This is the special test profile derived from the mime path.
                // It will never change.
                this.fcsProfile = profile;
                this.specialFcsProfile = true;
            }
            
            this.ic = ic;
            
            // Start listening
            EditorSettings es = EditorSettings.getDefault();
            es.addPropertyChangeListener(WeakListeners.propertyChange(this, es));
            
            this.kbsi = KeyBindingSettingsImpl.get(mimePath);
            this.kbsi.addPropertyChangeListener(WeakListeners.propertyChange(this, this.kbsi));
        }

        protected void initialize() {
            synchronized (this) {
                fontColorSettings = new CompositeFCS(allMimePaths, fcsProfile);
                keyBindingSettings = this.kbsi.createInstanceForLookup();

                ic.set(Arrays.asList(new Object [] {
                    fontColorSettings,
                    keyBindingSettings
                }), null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                boolean fcsChanged = false;
                boolean kbsChanged = false;

//                if (mimePath.getPath().contains("xml")) {
//                    System.out.println("@@@ propertyChange: mimePath = " + mimePath.getPath() + " profile = " + fcsProfile + " property = " + evt.getPropertyName() + " oldValue = " + (evt.getOldValue() instanceof MimePath ? ((MimePath) evt.getOldValue()).getPath() : evt.getOldValue()) + " newValue = " + evt.getNewValue());
//                }
                
                // Determine what has changed
                if (this.kbsi == evt.getSource()) {
                    kbsChanged = true;
                    
                } else if (evt.getPropertyName() == null) {
                    // reset all
                    if (!specialFcsProfile) {
                        String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                        fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(currentProfile);
                    }
                    fcsChanged = true;
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_HIGHLIGHT_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        fcsChanged = true;
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_TOKEN_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        MimePath changedMimePath = (MimePath) evt.getOldValue();
                        if (isDerivedFromMimePath(changedMimePath)) {
                            fcsChanged = true;
                        }
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_CURRENT_FONT_COLOR_PROFILE)) {
                    if (!specialFcsProfile) {
                        String newProfile = (String) evt.getNewValue();
                        fcsProfile = EditorSettingsImpl.getInstance().getInternalFontColorProfile(newProfile);
                        fcsChanged = true;
                    }
                }
                
                // Update lookup contents
                boolean updateContents = false;
                
                if (fcsChanged && fontColorSettings != null) {
                    fontColorSettings = new CompositeFCS(allMimePaths, fcsProfile);
                    updateContents = true;
                }
                
                if (kbsChanged  && keyBindingSettings != null) {
                    keyBindingSettings = this.kbsi.createInstanceForLookup();
                    updateContents = true;
                }
                
                if (updateContents) {
                    ic.set(Arrays.asList(new Object [] {
                        fontColorSettings,
                        keyBindingSettings
                    }), null);
                }
            }
        }

        private boolean isDerivedFromMimePath(MimePath mimePath) {
            for(MimePath mp : allMimePaths) {
                if (mp == mimePath) {
                    return true;
                }
            }
            return false;
        }
        
        private static MimePath [] computeInheritedMimePaths(MimePath mimePath) {
            List<String> paths = callSwitchLookupComputePaths(mimePath);

            if (paths != null) {
                ArrayList<MimePath> mimePaths = new ArrayList<MimePath>(paths.size());

                for (String path : paths) {
                    mimePaths.add(MimePath.parse(path));
                }

                return mimePaths.toArray(new MimePath[mimePaths.size()]);
            } else {
                return new MimePath [] { mimePath, MimePath.EMPTY };
            }
        }

        @SuppressWarnings("unchecked")
        private static List<String> callSwitchLookupComputePaths(MimePath mimePath) {
            try {
                ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
                Class clazz = classLoader.loadClass("org.netbeans.modules.editor.mimelookup.impl.SwitchLookup"); //NOI18N
                Method method = clazz.getDeclaredMethod("computePaths", MimePath.class, String.class, String.class); //NOI18N
                method.setAccessible(true);
                List<String> paths = (List<String>) method.invoke(null, mimePath, null, null);
                return paths;
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't call org.netbeans.modules.editor.mimelookup.impl.SwitchLookup.computePath(MimeLookup, String, String).", e); //NOI18N
                return null;
            }
        }
    } // End of MyLookup class
}
