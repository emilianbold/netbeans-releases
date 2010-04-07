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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
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

    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.HighlightingManager.level=300
    private static final Logger LOG = Logger.getLogger(HighlightingManager.class.getName());
    
    public static synchronized HighlightingManager getInstance() {
        if (instance == null) {
            instance = new HighlightingManager();
        }
        return instance;
    }
    
    public synchronized HighlightsContainer getHighlights(JTextComponent pane, HighlightsLayerFilter filter) {
        Highlighting h = (Highlighting) pane.getClientProperty(Highlighting.class);
        if (h == null) {
            h = new Highlighting(pane);
            pane.putClientProperty(Highlighting.class, h);
        }
        return h.getContainer(filter == null ? HighlightsLayerFilter.IDENTITY : filter);
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private static HighlightingManager instance;
    
    /** Creates a new instance of HighlightingManager */
    private HighlightingManager() {
    }
    
    private static final class Highlighting implements PropertyChangeListener {
    
        private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
        private static final String PROP_DOCUMENT = "document"; //NOI18N
        private static final String PROP_HL_INCLUDES = "HighlightsLayerIncludes"; //NOI18N
        private static final String PROP_HL_EXCLUDES = "HighlightsLayerExcludes"; //NOI18N

        // The factories changes tracking
        private Lookup.Result<HighlightsLayerFactory> factories = null;
        private LookupListener factoriesTracker = new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
                rebuildAllLayers();
            }
        };

        // The FontColorSettings changes tracking
        private Lookup.Result<FontColorSettings> settings = null;
        private LookupListener settingsTracker = new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
//                System.out.println("Settings tracker for '" + (lastKnownMimePaths == null ? "null" : lastKnownMimePaths[0].getPath()) + "'");
                rebuildAllLayers();
            }
        };

        private final JTextComponent pane;
        private HighlightsLayerFilter paneFilter;
        private Reference<Document> lastKnownDocumentRef;
        private MimePath [] lastKnownMimePaths = null;
        private boolean inRebuildAllLayers = false;
        
        // all layers (sorted, but without filtering) and their HighlightsContainers
        private List<? extends HighlightsLayer> allLayers = null;
        private List<HighlightsContainer> allLayerContainers = null;
        
        // CompoundHighlightsContainers with containers from filtered layers
        private final WeakHashMap<HighlightsLayerFilter, WeakReference<CompoundHighlightsContainer>> containers = 
            new WeakHashMap<HighlightsLayerFilter, WeakReference<CompoundHighlightsContainer>>();
        
        @SuppressWarnings("LeakingThisInConstructor")
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
                rebuildContainer(pane.getDocument(), filter, container);
                
                containers.put(filter, new WeakReference<CompoundHighlightsContainer>(container));
            }

            return container;
        }
        
        // ----------------------------------------------------------------------
        //  PropertyChangeListener implementation
        // ----------------------------------------------------------------------

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || PROP_DOCUMENT.equals(evt.getPropertyName())) {
                rebuildAll();
            }

            if (PROP_HL_INCLUDES.equals(evt.getPropertyName()) || PROP_HL_EXCLUDES.equals(evt.getPropertyName())) {
                synchronized (this) {
                    paneFilter = new RegExpFilter(pane.getClientProperty(PROP_HL_INCLUDES), pane.getClientProperty(PROP_HL_EXCLUDES));
                    rebuildAllContainers(pane.getDocument());
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

            Document lastKnownDocument = lastKnownDocumentRef == null ? null : lastKnownDocumentRef.get();

            // Recalculate factories and all containers if needed
            if (!Utilities.compareObjects(lastKnownDocument, pane.getDocument()) ||
                !Arrays.equals(lastKnownMimePaths, mimePaths)
            ) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("rebuildAll: lastKnownDocument = " + simpleToString(lastKnownDocument) + //NOI18N
                            ", document = " + simpleToString(pane.getDocument()) + //NOI18N
                            ", lastKnownMimePaths = " + mimePathsToString(lastKnownMimePaths) + //NOI18N
                            ", mimePaths = " + mimePathsToString(mimePaths)); //NOI18N
                }
                
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
                
                rebuildAllLayers();
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

        private synchronized void rebuildAllLayers() {
            if (inRebuildAllLayers) {
                return;
            }
            
            inRebuildAllLayers = true;
            try {
                Document doc = pane.getDocument();
                if (factories != null) {
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

                    // Get the containers
                    ArrayList<HighlightsContainer> layerContainers = new ArrayList<HighlightsContainer>();
                    for(HighlightsLayer layer : sortedLayers) {
                        HighlightsLayerAccessor layerAccessor = 
                            HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);

                        layerContainers.add(layerAccessor.getContainer());
                    }

                    allLayers = sortedLayers;
                    allLayerContainers = layerContainers;
                } else {
                    allLayers = null;
                    allLayerContainers = null;
                }

                rebuildAllContainers(doc);
            } finally {
                inRebuildAllLayers = false;
            }
        }
        
        private synchronized void rebuildAllContainers(Document document) {
            if (LOG.isLoggable(Level.FINE)) {
                Document lastKnownDocument = lastKnownDocumentRef == null ? null : lastKnownDocumentRef.get();
                LOG.fine("rebuildAllContainers: lastKnownDocument = " + simpleToString(lastKnownDocument) + //NOI18N
                        ", lastKnownMimePaths = " + mimePathsToString(lastKnownMimePaths)); //NOI18N
            }

            for(HighlightsLayerFilter filter : containers.keySet()) {
                WeakReference<CompoundHighlightsContainer> ref = containers.get(filter);
                CompoundHighlightsContainer container = ref == null ? null : ref.get();

                if (container != null) {
                    rebuildContainer(document, filter, container);
                }
            }
        }

        private synchronized void rebuildContainer(Document doc, HighlightsLayerFilter filter, CompoundHighlightsContainer container) {
            if (allLayers != null) {
                List<? extends HighlightsLayer> filteredLayers = paneFilter.filterLayers(Collections.unmodifiableList(allLayers));
                filteredLayers = filter.filterLayers(Collections.unmodifiableList(filteredLayers));

                // Get the containers
                ArrayList<HighlightsContainer> hcs = new ArrayList<HighlightsContainer>();
                for(HighlightsLayer layer : filteredLayers) {
                    int idx = allLayers.indexOf(layer);
                    HighlightsContainer c = allLayerContainers.get(idx);
                    hcs.add(c);
                }
                
                if (LOG.isLoggable(Level.FINEST)) {
                    logLayers(pane.getDocument(), lastKnownMimePaths, filteredLayers, Level.FINEST);
                }
                
                container.setLayers(doc, hcs.toArray(new HighlightsContainer[hcs.size()]));
            } else {
                container.setLayers(null, null);
            }
        }

        private static void logLayers(Document doc, MimePath [] mimePaths, List<? extends HighlightsLayer> layers, Level logLevel) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("HighlighsLayers {\n"); //NOI18N
            
            sb.append(" * document : "); //NOI18N
            sb.append(doc.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(doc)));
            Object streamDescriptor = doc.getProperty(Document.StreamDescriptionProperty);
            sb.append(" [").append(streamDescriptor == null ? "no stream descriptor" : streamDescriptor.toString()).append(']');
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
                sb.append('['); //NOI18N
                sb.append(layerAccessor.getZOrder().toString()); //NOI18N
                sb.append(']'); //NOI18N
                sb.append('@'); //NOI18N
                sb.append(Integer.toHexString(System.identityHashCode(layer)));
                sb.append("\n"); //NOI18N
            }
            
            sb.append("}\n"); //NOI18N
            
            LOG.log(logLevel, sb.toString());
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
    
    private static String simpleToString(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
    
    private static String mimePathsToString(MimePath... mimePaths) {
        if (mimePaths == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            
            sb.append('{'); //NOI18N
            for(MimePath mp : mimePaths) {
                sb.append('\'').append(mp.getPath()).append('\''); //NOI18N
                sb.append(","); //NOI81N
            }
            sb.append('}'); //NOI18N
            
            return sb.toString();
        }
    }
}
