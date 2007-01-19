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

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Vita Stejskal
 */
public final class HighlightingManager {

    private static final Logger LOG = Logger.getLogger(HighlightingManager.class.getName());
    
    public static synchronized HighlightingManager getInstance() {
        if (instance == null) {
            instance = new HighlightingManager();
        }
        return instance;
    }
    
    public HighlightsContainer getHighlights(JTextComponent pane, HighlightsLayerFilter filter) {
        return getHighlighting(pane).getContainer(filter);
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private static HighlightingManager instance;
    
    /* package */ final WeakHashMap<JTextComponent, WeakReference<Highlighting>> CACHE = 
        new WeakHashMap<JTextComponent, WeakReference<Highlighting>>();
    
    /** Creates a new instance of HighlightingManager */
    private HighlightingManager() {
    }
    
    private Highlighting getHighlighting(JTextComponent pane) {
        synchronized (CACHE) {
            WeakReference<Highlighting> ref = CACHE.get(pane);
            Highlighting h = ref == null ? null : ref.get();

            if (h == null) {
                h = new Highlighting(pane);
                CACHE.put(pane, new WeakReference<Highlighting>(h));
            }
            
            return h;
        }
    }
    
    private static final class Highlighting implements PropertyChangeListener {
    
        private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
        private static final String PROP_DOCUMENT = "document"; //NOI18N
        private static final String PROP_HL_INCLUDES = "HighlightsLayerIncludes"; //NOI18N
        private static final String PROP_HL_EXCLUDES = "HighlightsLayerExcludes"; //NOI18N

        // The factories changes tracking
        private Lookup.Result<HighlightsLayerFactory> factories = null;
        private LookupListener factoriesTracker = new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                rebuildAllContainers();
            }
        };

        // The FontColorSettings changes tracking
        private Lookup.Result<FontColorSettings> settings = null;
        private LookupListener settingsTracker = new LookupListener() {
            public void resultChanged(LookupEvent ev) {
//                System.out.println("Settings tracker for '" + (lastKnownMimePaths == null ? "null" : lastKnownMimePaths[0].getPath()) + "'");
                resetAllContainers();
            }
        };

        private final JTextComponent pane;
        private HighlightsLayerFilter paneFilter;
        private Document lastKnownDocument = null;
        private MimePath [] lastKnownMimePaths = null;
        
        private final WeakHashMap<HighlightsLayerFilter, WeakReference<CompoundHighlightsContainer>> containers = 
            new WeakHashMap<HighlightsLayerFilter, WeakReference<CompoundHighlightsContainer>>();
        
        public Highlighting(JTextComponent pane) {
            this.pane = pane;
            this.paneFilter = new RegExpFilter(pane.getClientProperty(PROP_HL_INCLUDES), pane.getClientProperty(PROP_HL_EXCLUDES));
            this.pane.addPropertyChangeListener(WeakListeners.propertyChange(this, pane));
            
            rebuildAll();
        }

        public synchronized HighlightsContainer getContainer(HighlightsLayerFilter filter) {
            WeakReference<CompoundHighlightsContainer> ref = containers.get(filter);
            CompoundHighlightsContainer container = ref == null ? null : ref.get();

            if (container == null) {
                container = new CompoundHighlightsContainer();
                rebuildContainer(filter, container);
                
                containers.put(filter, new WeakReference<CompoundHighlightsContainer>(container));
            }

            return container;
        }
        
        // ----------------------------------------------------------------------
        //  PropertyChangeListener implementation
        // ----------------------------------------------------------------------

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || PROP_DOCUMENT.equals(evt.getPropertyName())) {
                rebuildAll();
            }

            if (PROP_HL_INCLUDES.equals(evt.getPropertyName()) || PROP_HL_EXCLUDES.equals(evt.getPropertyName())) {
                synchronized (this) {
                    paneFilter = new RegExpFilter(pane.getClientProperty(PROP_HL_INCLUDES), pane.getClientProperty(PROP_HL_EXCLUDES));
                    rebuildAllContainers();
                }
            }
        }
        
        // ----------------------------------------------------------------------
        //  Private implementation
        // ----------------------------------------------------------------------

        private MimePath [] getAllDocumentMimePath() {
            Document doc = pane.getDocument();
            String mainMimeType;

            Object propMimeType = doc.getProperty(PROP_MIME_TYPE);
            if (propMimeType != null) {
                mainMimeType = propMimeType.toString();
            } else {
                mainMimeType = pane.getUI().getEditorKit(pane).getContentType();
            }

            return new MimePath [] { MimePath.parse(mainMimeType) };
        }
        
        private synchronized void rebuildAll() {
            // Get the new set of mime path
            MimePath [] mimePaths = getAllDocumentMimePath();

            // Recalculate factories and all containers if needed
            if (!Utilities.compareObjects(lastKnownDocument, pane.getDocument()) ||
                !Arrays.equals(lastKnownMimePaths, mimePaths)
            ) {
                // Unregister listeners
                if (factories != null) {
                    factories.removeLookupListener(factoriesTracker);
                }
                if (settings != null) {
                    settings.removeLookupListener(settingsTracker);
                }

                if (mimePaths != null) {
                    ArrayList<Lookup> lookups = new ArrayList<Lookup>();
                    for(MimePath mimePath : mimePaths) {
                        lookups.add(MimeLookup.getLookup(mimePath));
                    }

                    ProxyLookup lookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
                    factories = lookup.lookup(new Lookup.Template<HighlightsLayerFactory>(HighlightsLayerFactory.class));
                    settings = lookup.lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
                } else {
                    factories = null;
                    settings = null;
                }
                
                // Start listening again
                if (factories != null) {
                    factories.addLookupListener(factoriesTracker);
                    factories.allItems(); // otherwise we won't get any events at all
                }
                if (settings != null) {
                    settings.addLookupListener(settingsTracker);
                    settings.allItems(); // otherwise we won't get any events at all
                }

                lastKnownDocument = pane.getDocument();
                lastKnownMimePaths = mimePaths;
                
                rebuildAllContainers();
            }
        }
        
        private synchronized void resetAllContainers() {
            for(HighlightsLayerFilter filter : containers.keySet()) {
                WeakReference<CompoundHighlightsContainer> ref = containers.get(filter);
                CompoundHighlightsContainer container = ref == null ? null : ref.get();
                
                if (container != null) {
                    container.resetCache();
                }
            }
        }
        
        private synchronized void rebuildAllContainers() {
            for(HighlightsLayerFilter filter : containers.keySet()) {
                WeakReference<CompoundHighlightsContainer> ref = containers.get(filter);
                CompoundHighlightsContainer container = ref == null ? null : ref.get();
                
                if (container != null) {
                    rebuildContainer(filter, container);
                }
            }
        }

        private synchronized void rebuildContainer(HighlightsLayerFilter filter, CompoundHighlightsContainer container) {
            if (factories != null) {
                Document doc = pane.getDocument();
                Collection<? extends HighlightsLayerFactory> all = factories.allInstances();
                HashMap<String, HighlightsLayer> layers = new HashMap<String, HighlightsLayer>();

                HighlightsLayerFactory.Context context = HighlightingSpiPackageAccessor.get().createFactoryContext(doc, pane);

                for(HighlightsLayerFactory factory : all) {
                    HighlightsLayer [] factoryLayers = factory.createLayers(context);
                    if (factoryLayers == null) {
                        continue;
                    }
                    
                    for(HighlightsLayer layer : factoryLayers) {
                        HighlightsLayerAccessor layerAccessor = 
                            HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                        
                        String layerTypeId = layerAccessor.getLayerTypeId();
                        if (!layers.containsKey(layerTypeId)) {
                            layers.put(layerTypeId, layer);
                        }
                    }
                }

                // Sort the layers by their z-order
                List<? extends HighlightsLayer> sortedLayers;
                try {
                    sortedLayers = HighlightingSpiPackageAccessor.get().sort(layers.values());
                } catch (TopologicalSortException tse) {
                    ErrorManager.getDefault().notify(tse);
                    @SuppressWarnings("unchecked") //NOI18N
                    List<? extends HighlightsLayer> sl
                            = (List<? extends HighlightsLayer>)tse.partialSort();
                    sortedLayers = sl;
                }
                
                // filter the layers
                sortedLayers = paneFilter.filterLayers(Collections.unmodifiableList(sortedLayers));
                sortedLayers = filter.filterLayers(Collections.unmodifiableList(sortedLayers));

                // Get the containers
                ArrayList<HighlightsContainer> hcs = new ArrayList<HighlightsContainer>();
                for(HighlightsLayer layer : sortedLayers) {
                    HighlightsLayerAccessor layerAccessor = 
                        HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                        
                    hcs.add(layerAccessor.getContainer());
                }
                
                if (LOG.isLoggable(Level.FINE)) {
                    logLayers(doc, lastKnownMimePaths, sortedLayers);
                }
                
                container.setLayers(doc, hcs.toArray(new HighlightsContainer[hcs.size()]));
            } else {
                container.setLayers(null, null);
            }
        }

        private static void logLayers(Document doc, MimePath [] mimePaths, List<? extends HighlightsLayer> layers) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("HighlighsLayers {\n"); //NOI18N
            sb.append(" * document : "); //NOI18N
            sb.append(doc.toString());
            sb.append("\n"); //NOI18N

            sb.append(" * mime paths : \n"); //NOI18N
            for(MimePath mimePath : mimePaths) {
                sb.append("    "); //NOI18N
                sb.append(mimePath.getPath());
                sb.append("\n"); //NOI18N
            }
            
            sb.append(" * layers : \n"); //NOI18N
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                sb.append("    "); //NOI18N
                sb.append(layerAccessor.getLayerTypeId());
                sb.append("\n"); //NOI18N
            }
            
            sb.append("}\n"); //NOI18N
            
            LOG.fine(sb.toString());
        }
        
    } // End of Highlighting class
    
    private static final class RegExpFilter implements HighlightsLayerFilter {
        
        private final List<Pattern> includes;
        private final List<Pattern> excludes;
        
        public RegExpFilter(Object includes, Object excludes) {
            this.includes = buildPatterns(includes);
            this.excludes = buildPatterns(excludes);
        }

        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            List<? extends HighlightsLayer> includedLayers;
            
            if (includes.isEmpty()) {
                includedLayers = layers;
            } else {
                includedLayers = filter(layers, includes, true);
            }
            
            List<? extends HighlightsLayer> filteredLayers;
            if (excludes.isEmpty()) {
                filteredLayers = includedLayers;
            } else {
                filteredLayers = filter(includedLayers, excludes, false);
            }
            
            return filteredLayers;
        }

        private static List<? extends HighlightsLayer> filter(
            List<? extends HighlightsLayer> layers,
            List<Pattern> patterns,
            boolean includeMatches // true means include matching layers, false means include non-matching layers
        ) {
            List<HighlightsLayer> filtered = new ArrayList<HighlightsLayer>();
            
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                for(Pattern pattern : patterns) {
                    boolean matches = pattern.matcher(layerAccessor.getLayerTypeId()).matches();
                    
                    if (matches && includeMatches) {
                        filtered.add(layer);
                    }
                    
                    if (!matches && !includeMatches) {
                        filtered.add(layer);
                    }
                }
            }
            
            return filtered;
        }
        
        private static List<Pattern> buildPatterns(Object expressions) {
            List<Pattern> patterns = new ArrayList<Pattern>();
            
            if (expressions instanceof String) {
                try {
                    patterns.add(Pattern.compile((String) expressions));
                } catch (PatternSyntaxException e) {
                    LOG.log(Level.WARNING, "Ignoring invalid regexp for the HighlightsLayer filtering.", e); //NOI18N
                }
            } else if (expressions instanceof String[]) {
                for(String expression : (String []) expressions) {
                    try {
                        patterns.add(Pattern.compile(expression));
                    } catch (PatternSyntaxException e) {
                        LOG.log(Level.WARNING, "Ignoring invalid regexp for the HighlightsLayer filtering.", e); //NOI18N
                    }
                }
            }
            
            return patterns;
        }
    } // End of RegExpFilter class
}
