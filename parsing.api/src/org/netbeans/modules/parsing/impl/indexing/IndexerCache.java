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

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public abstract class IndexerCache <T> {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public static synchronized IndexerCache<CustomIndexerFactory> getCifCache() {
        if (instanceCIF == null) {
            instanceCIF = new IndexerCache<CustomIndexerFactory>(CustomIndexerFactory.class) {
                protected @Override String getIndexerName(CustomIndexerFactory indexerFactory) {
                    return indexerFactory.getIndexerName();
                }
                protected @Override int getIndexerVersion(CustomIndexerFactory indexerFactory) {
                    return indexerFactory.getIndexVersion();
                }
            };
        }
        return instanceCIF;
    }

    public static synchronized IndexerCache<EmbeddingIndexerFactory> getEifCache() {
        if (instanceEIF == null) {
            instanceEIF = new IndexerCache<EmbeddingIndexerFactory>(EmbeddingIndexerFactory.class) {
                protected @Override String getIndexerName(EmbeddingIndexerFactory indexerFactory) {
                    return indexerFactory.getIndexerName();
                }

                protected @Override int getIndexerVersion(EmbeddingIndexerFactory indexerFactory) {
                    return indexerFactory.getIndexVersion();
                }
            };
        }
        return instanceEIF;
    }

    public Collection<? extends IndexerInfo<T>> getIndexers(Set<IndexerInfo<T>> changedIndexers) {
        final Object [] data = getData(changedIndexers);
        @SuppressWarnings("unchecked")
        List<IndexerInfo<T>> infos = (List<IndexerInfo<T>>) data[2];
        return infos;
    }

    public Map<String, Set<IndexerInfo<T>>> getIndexersMap(Set<IndexerInfo<T>> changedIndexers) {
        final Object [] data = getData(changedIndexers);
        @SuppressWarnings("unchecked")
        Map<String, Set<IndexerInfo<T>>> infosMap = (Map<String, Set<IndexerInfo<T>>>) data[1];
        return infosMap;
    }

    public Collection<? extends IndexerInfo<T>> getIndexersFor(String mimeType) {
        final Object [] data = getData(null);
        @SuppressWarnings("unchecked")
        Map<String, Set<IndexerInfo<T>>> infosMap = (Map<String, Set<IndexerInfo<T>>>) data[1];
        Set<IndexerInfo<T>> infos = infosMap.get(mimeType);
        return infos == null ? Collections.<IndexerInfo<T>>emptySet() : infos;
    }

    public Collection<? extends IndexerInfo<T>> getIndexersByName(String indexerName) {
        final Object [] data = getData(null);
        @SuppressWarnings("unchecked")
        Map<String, Set<IndexerInfo<T>>> infosMap = (Map<String, Set<IndexerInfo<T>>>) data[0];
        Set<IndexerInfo<T>> info = infosMap.get(indexerName);
        return info;
    }


//    public List<? extends String> getAvailableMimeTypes() {
//        return null;
//    }
//

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * This class can either be initialized with an indexer factory and mime types
     * or with an indexer identification (name & version) and mime types. Only instances
     * that have the indexer factory are ever passed outside of IndexerCache.
     *
     * @param <T>
     */
    public static final class IndexerInfo<T> {

        public T getIndexerFactory() {
            return indexerFactory;
        }

        public Collection<? extends String> getMimeTypes() {
            return mimeTypes;
        }

        public boolean isAllMimeTypesIndexer() {
            return mimeTypes.contains(ALL_MIME_TYPES);
        }

        public String getIndexerName() {
            return indexerName;
        }

        public int getIndexerVersion() {
            return indexerVersion;
        }

        // --------------------------------------------------------------------
        // Private implementation
        // --------------------------------------------------------------------

        private final String indexerName;
        private final int indexerVersion;
        private final T indexerFactory;
        private final Set<String> mimeTypes;

        private IndexerInfo(T indexerFactory, String indexerName, int indexerVersion, Set<String> mimeTypes) {
            this.indexerFactory = indexerFactory;
            this.indexerName = indexerName;
            this.indexerVersion = indexerVersion;
            this.mimeTypes = Collections.unmodifiableSet(mimeTypes);
        }

    } // End of IndexerInfo

    // -----------------------------------------------------------------------
    // Protected implementation
    // -----------------------------------------------------------------------

    protected abstract String getIndexerName(T indexerFactory);

    protected abstract int getIndexerVersion(T indexerFactory);
    
    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(IndexerCache.class.getName());
    
    private static final String ALL_MIME_TYPES = ""; //NOI18N
    private static final Collection<? extends String> SLOW_MIME_TYPES = Arrays.asList(new String [] {
        "text/sh", //NOI18N
        "text/x-persistence1.0", //NOI18N
        "text/x-orm1.0", //NOI18N
        "application/xhtml+xml", //NOI18N
        "text/x-maven-pom+xml", //NOI18N
        "text/x-maven-profile+xml", //NOI18N
        "text/x-maven-settings+xml", //NOI18N
        "text/x-ant+xml", //NOI18N
        "text/x-nbeditor-fontcolorsettings", //NOI18N
        "text/x-nbeditor-keybindingsettings", //NOI18N
        "text/x-nbeditor-preferences", //NOI18N
        "text/x-dd-servlet2.2", //NOI18N
        "text/x-dd-servlet2.3", //NOI18N
        "text/x-dd-servlet2.4", //NOI18N
        "text/x-dd-servlet2.5", //NOI18N
        "text/x-dd-servlet3.0", //NOI18N
        "text/x-dd-servlet-fragment3.0", //NOI18N
        "text/x-dd-ejbjar2.0", //NOI18N
        "text/x-dd-ejbjar2.1", //NOI18N
        "text/x-dd-ejbjar3.0", //NOI18N
        "text/x-dd-client1.3", //NOI18N
        "text/x-dd-client1.4", //NOI18N
        "text/x-dd-client5.0", //NOI18N
        "text/x-dd-application1.4", //NOI18N
        "text/x-dd-application5.0", //NOI18N
        "text/x-dd-sun-web+xml", //NOI18N
        "text/x-dd-sun-ejb-jar+xml", //NOI18N
        "text/x-dd-sun-application+xml", //NOI18N
        "text/x-dd-sun-app-client+xml", //NOI18N
        "text/tomcat5+xml", //NOI18N
        "text/x-tld", //NOI18N
        "text/x-jsf+xml", //NOI18N
        "text/x-struts+xml", //NOI18N
        "application/x-schema+xml", //NOI18N
        "text/x-wsdl+xml", //NOI18N
        "text/x-springconfig+xml", //NOI18N
        "text/x-tmap+xml", //NOI18N
        "text/x-bpel+xml", //NOI18N
        "application/xslt+xml", //NOI18N
        "text/x-jelly+xml", //NOI18N
        "text/x-h", //NOI18N
        "application/x-java-archive", //NOI18N
        "application/x-exe", //NOI18N
        "application/x-executable+elf", //NOI18N
        "application/x-object+elf", //NOI18N
        "application/x-core+elf", //NOI18N
        "application/x-shobj+elf", //NOI18N
        "application/x-elf", //NOI18N
        "text/x-nbeditor-codetemplatesettings", //NOI18N
        "text/x-nbeditor-macrosettings", //NOI18N
        "text/x-hibernate-cfg+xml", //NOI18N
        "text/x-hibernate-mapping+xml", //NOI18N
        "text/x-hibernate-reveng+xml", //NOI18N
        "text/x-ruby", //NOI18N
        "text/x-php5", //NOI18N
    });

    private static IndexerCache<CustomIndexerFactory> instanceCIF = null;
    private static IndexerCache<EmbeddingIndexerFactory> instanceEIF = null;

    private final Class<T> type;
    private final String infoFileName;
    private final Tracker tracker = new Tracker();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Map<String, Set<IndexerInfo<T>>> infosByName = null;
    private Map<String, Set<IndexerInfo<T>>> infosByMimeType = null;
    private List<IndexerInfo<T>> orderedInfos = null;

    private IndexerCache(Class<T> type) {
        this.type = type;
        this.infoFileName = "last-known-" + type.getSimpleName() + ".properties"; //NOI18N
        EditorSettings.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(tracker, EditorSettings.getDefault()));
    }

    /**
     * This method should not be called when holding any lock since it calls
     * Lookup.Result.allInstances which can block.
     * @param factories - non null map for collecting results
     */
    private void collectIndexerFactoriesRegisteredForAllLanguages(Map<T, Set<String>> factories) {
        Lookup.Result<T> r = tracker.getLookupData(ALL_MIME_TYPES);
        for (T factory : r.allInstances()) {
            Set<String> mimeTypes = factories.get(factory);
            if (mimeTypes == null) {
                mimeTypes = new HashSet<String>();
                mimeTypes.add(ALL_MIME_TYPES);
                factories.put(factory, mimeTypes);
            } // else the factory is already in the map (this should not happen unless ill configured in the layer)
        }
    }

    /**
     * This method should not be called when holding any lock since it calls
     * Lookup.Result.allInstances which can block.
     * @param factories - non null map for collecting results
     */
    private void collectIndexerFactoriesRegisteredForEachParticularLanguage(Map<T, Set<String>> factories) {
        Set<String> mimeTypes = Util.getAllMimeTypes();
        for (String mimeType : mimeTypes) {
            Lookup.Result<T> r = tracker.getLookupData(mimeType);
            for (T factory : r.allInstances()) {
                Set<String> factoryMimeTypes = factories.get(factory);
                if (factoryMimeTypes == null) {
                    factoryMimeTypes = new HashSet<String>();
                    factoryMimeTypes.add(mimeType);
                    factories.put(factory, factoryMimeTypes);
                } else if (!factoryMimeTypes.contains(ALL_MIME_TYPES)) {
                    factoryMimeTypes.add(mimeType);
                }
            }
        }
    }

    private Object[] getData(Set<IndexerInfo<T>> changedIndexers) {
        boolean fire = false;

        synchronized (this) {
            if (infosByName == null) {
                Map<T, Set<String>> factories = new LinkedHashMap<T, Set<String>>();
                collectIndexerFactoriesRegisteredForAllLanguages(factories);
                collectIndexerFactoriesRegisteredForEachParticularLanguage(factories);

                Map<String, Set<IndexerInfo<T>>> _infosByName = new HashMap<String, Set<IndexerInfo<T>>>();
                Map<String, Set<IndexerInfo<T>>> _infosByMimeType = new HashMap<String, Set<IndexerInfo<T>>>();
                List<IndexerInfo<T>> _orderedInfos = new ArrayList<IndexerInfo<T>>();
                for (T factory : factories.keySet()) {
                    Set<String> mimeTypes = factories.get(factory);
                    String factoryName = getIndexerName(factory);
                    IndexerInfo<T> info = new IndexerInfo<T>(factory, factoryName, getIndexerVersion(factory), mimeTypes);

                    // infos by name
                    {
                        Set<IndexerInfo<T>> infos = _infosByName.get(factoryName);
                        if (infos == null) {
                            infos = new HashSet<IndexerInfo<T>>();
                            _infosByName.put(factoryName, infos);
                        }
                        infos.add(info);
                    }

                    // infos by mimetype
                    for (String mimeType : mimeTypes) {
                        Set<IndexerInfo<T>> infos = _infosByMimeType.get(mimeType);
                        if (infos == null) {
                            infos = new HashSet<IndexerInfo<T>>();
                            _infosByMimeType.put(mimeType, infos);
                        }
                        infos.add(info);
                    }

                    _orderedInfos.add(info);
                }

                // the comparator instance must not be cached, because it uses data
                // from the default lookup
                Collections.sort(_orderedInfos, new C());
                infosByName = Collections.unmodifiableMap(_infosByName);
                infosByMimeType = Collections.unmodifiableMap(_infosByMimeType);
                orderedInfos = Collections.unmodifiableList(_orderedInfos);

                Map<String, IndexerInfo<T>> lastKnownInfos = readLastKnownIndexers();
                writeLastKnownIndexers(infosByName);
                
                Map<String, Set<IndexerInfo<T>>> addedOrChangedInfosMap = new HashMap<String, Set<IndexerInfo<T>>>();
                diff(lastKnownInfos, infosByName, addedOrChangedInfosMap);
                
                if (changedIndexers == null) {
                    fire = true;
                    changedIndexers = new HashSet<IndexerInfo<T>>();
                }
                for(Set<IndexerInfo<T>> addedOrChangedInfos : addedOrChangedInfosMap.values()) {
                    changedIndexers.addAll(addedOrChangedInfos);
                }

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Ordered indexers of {0}: ", type.getName()); //NOI18N
                    for (IndexerInfo<T> ii : orderedInfos) {
                        LOG.log(Level.FINE, "  {0} {1}: {2}", new Object[] { //NOI18N
                            ii.getIndexerFactory(),
                            changedIndexers != null && changedIndexers.contains(ii) ? "(modified)" : "", //NOI18N
                            ii.getMimeTypes()});
                    }
                }
            }

            if (fire) {
                pcs.firePropertyChange(type.getName(), null, changedIndexers);
            }

            return new Object [] { infosByName, infosByMimeType, orderedInfos };
        }
    }

    private void diff(Map<String, IndexerInfo<T>> lastKnownInfos, Map<String, Set<IndexerInfo<T>>> currentInfosMap, Map<String, Set<IndexerInfo<T>>> addedOrChangedInfosMap) {
        for(String indexerName : currentInfosMap.keySet()) {
            if (!lastKnownInfos.containsKey(indexerName)) {
                addedOrChangedInfosMap.put(indexerName, currentInfosMap.get(indexerName));
            } else {
                IndexerInfo<T> lastKnownInfo = lastKnownInfos.get(indexerName);
                Set<IndexerInfo<T>> currentInfos = currentInfosMap.get(indexerName);

                // check versions
                for(IndexerInfo<T> currentInfo : currentInfos) {
                    if (lastKnownInfo.getIndexerVersion() != currentInfo.getIndexerVersion()) {
                        Set<IndexerInfo<T>> addedOrChangedInfos = addedOrChangedInfosMap.get(indexerName);
                        if (addedOrChangedInfos == null) {
                            addedOrChangedInfos = new HashSet<IndexerInfo<T>>();
                            addedOrChangedInfosMap.put(indexerName, addedOrChangedInfos);
                        }
                        addedOrChangedInfos.add(currentInfo);
                    }
                }

                // check mimetypes
                for(IndexerInfo<T> currentInfo : currentInfos) {
                    if (!lastKnownInfo.getMimeTypes().containsAll(currentInfo.getMimeTypes())) {
                        Set<IndexerInfo<T>> addedOrChangedInfos = addedOrChangedInfosMap.get(indexerName);
                        if (addedOrChangedInfos == null) {
                            addedOrChangedInfos = new HashSet<IndexerInfo<T>>();
                            addedOrChangedInfosMap.put(indexerName, addedOrChangedInfos);
                        }
                        addedOrChangedInfos.add(currentInfo);
                    }
                }
            }
        }
    }

    private Map<String, IndexerInfo<T>> readLastKnownIndexers() {
        Map<String, IndexerInfo<T>> lki = new HashMap<String, IndexerInfo<T>>();

        FileObject cacheFolder = CacheFolder.getCacheFolder();
        FileObject infoFile = cacheFolder.getFileObject(infoFileName);
        if (infoFile != null) {
            Properties props = new Properties();
            try {
                InputStream is = infoFile.getInputStream();
                try {
                    props.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException ioe) {
                LOG.log(Level.FINE, "Can't read " + infoFile.getPath() + " file", ioe); //NOI18N
                props = null;
            }

            if (props != null) {
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String indexerName = ((String) entry.getKey()).trim();
                    int indexerVersion = 0;
                    Set<String> indexerMimeTypes = new HashSet<String>();
                    String[] indexerData = ((String) entry.getValue()).trim().split(","); //NOI18N
                    if (indexerData.length > 0) {
                        indexerVersion = Integer.parseInt(indexerData[0]);
                        if (indexerData.length > 1) {
                            for (int i = 1; i < indexerData.length; i++) {
                                String mimeType = indexerData[i];
                                if (mimeType.equals("<all>")) { //NOI18N
                                    indexerMimeTypes.add(ALL_MIME_TYPES);
                                    break;
                                } else {
                                    indexerMimeTypes.add(mimeType);
                                }
                            }
                        }
                    }

                    if (indexerName.length() > 0 && indexerVersion != -1 && indexerMimeTypes.size() > 0) {
                        if (!lki.containsKey(indexerName)) {
                            IndexerInfo<T> iinfo = new IndexerInfo<T>(null, indexerName, indexerVersion, indexerMimeTypes);
                            lki.put(indexerName, iinfo);
                        } else {
                            LOG.log(Level.FINE, "Ignoring duplicate indexers data: name={0}, version={1}, mimeTypes={2}", new Object[]{indexerName, indexerVersion, indexerMimeTypes}); //NOI18N
                        }
                    } else {
                        LOG.log(Level.FINE, "Ignoring incomplete indexer data: name={0}, version={1}, mimeTypes={2}", new Object[]{indexerName, indexerVersion, indexerMimeTypes}); //NOI18N
                    }
                }
            }
        }

        return lki;
    }

    private void writeLastKnownIndexers(Map<String, Set<IndexerInfo<T>>> lki) {
        Properties props = new Properties();
        for(String indexerName : lki.keySet()) {
            Set<IndexerInfo<T>> iinfos = lki.get(indexerName);
            int indexerVersion = -1;
            Set<String> mimeTypes = new HashSet<String>();
            for(IndexerInfo<T> iinfo : iinfos) {
                if (indexerVersion == -1) {
                    indexerVersion = iinfo.getIndexerVersion();
                } else if (indexerVersion != iinfo.getIndexerVersion()) {
                    LOG.warning(iinfo.getIndexerFactory() + " has different version then other instances of the same factory: version=" + iinfo.getIndexerVersion() + ", others=" + indexerVersion);
                    continue;
                }
                mimeTypes.addAll(iinfo.getMimeTypes());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(indexerVersion);
            if (mimeTypes.size() > 0) {
                sb.append(",");//NOI18N
                for(Iterator<? extends String> i = mimeTypes.iterator(); i.hasNext(); ) {
                    String mimeType = i.next();
                    if (mimeType.length() == 0) {
                        sb.append("<all>"); //NOI18N
                        break;
                    } else {
                        sb.append(mimeType);
                        if (i.hasNext()) {
                            sb.append(","); //NOI18N
                        }
                    }
                }
            }

            props.put(indexerName, sb.toString());
        }

        FileObject cacheFolder = CacheFolder.getCacheFolder();
        try {
            FileObject infoFile = FileUtil.createData(cacheFolder, infoFileName);
            OutputStream os = infoFile.getOutputStream();
            try {
                props.store(os, "Last known indexer " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date())); //NOI18N
            } finally {
                os.close();
            }
        } catch (IOException ioe) {
            LOG.log(Level.FINE, "Can't write " + infoFileName + " file in " + cacheFolder.getPath(), ioe); //NOI18N
        }
    }
    
    private final class Tracker implements LookupListener, PropertyChangeListener, Runnable {

        // --------------------------------------------------------------------
        // Public implementation
        // --------------------------------------------------------------------

        public Lookup.Result<T> getLookupData(String mimeType) {
            Lookup.Result<T> r = results.get(mimeType);
            if (r == null) {
                r = MimeLookup.getLookup(mimeType).lookupResult(type);
                r.addLookupListener(this);
                results.put(mimeType, r);
                LOG.log(Level.FINE, "{0}: listening on MimeLookup for {1}", new Object [] { type.getName(), mimeType }); //NOI18N
            }
            return r;
        }

        // --------------------------------------------------------------------
        // LookupListener implementation
        // --------------------------------------------------------------------

        public void resultChanged(LookupEvent ev) {
            task.schedule(0);
        }

        // --------------------------------------------------------------------
        // PropertyChangeListener implementation
        // --------------------------------------------------------------------

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || EditorSettings.PROP_MIME_TYPES.equals(evt.getPropertyName())) {
                task.schedule(123);
            }
        }

        // --------------------------------------------------------------------
        // Runnable implementation
        // --------------------------------------------------------------------

        public void run() {
            synchronized (IndexerCache.this) {
                IndexerCache.this.infosByName = null;
                IndexerCache.this.infosByMimeType = null;
                IndexerCache.this.orderedInfos = null;
                LOG.log(Level.FINE, "{0}: resetting indexer cache", type.getName());
            }
        }

        // --------------------------------------------------------------------
        // Private implementation
        // --------------------------------------------------------------------

        private final Map<String, Lookup.Result<T>> results = new HashMap<String, Lookup.Result<T>>();
        private final RequestProcessor.Task task = RequestProcessor.getDefault().create(this);

    } // End of Tracker class

    private final class C implements Comparator<IndexerInfo<T>> {

        public C() {
            Map<String, Integer> order = null;
            Method getMIMETypesMethod = null;
            try {
                getMIMETypesMethod = MIMEResolver.class.getDeclaredMethod("getMIMETypes"); //NOI18N
            } catch (Exception ex) {
                // ignore
            }

            if (getMIMETypesMethod != null) {
                Collection<? extends MIMEResolver> resolvers = Lookup.getDefault().lookupAll(MIMEResolver.class);
                order = new HashMap<String, Integer>();
                int idx = 0;
                for(MIMEResolver r : resolvers) {
                    String [] mimeTypes = null;
                    try {
                        mimeTypes = (String []) getMIMETypesMethod.invoke(r);
                    } catch (Exception e) {
                        // ignore;
                    }

                    if (mimeTypes != null) {
                        for(String mimeType : mimeTypes) {
                            order.put(mimeType, idx);
                        }
                    }

                    idx++;
                }
            }

            orderByResolvers = order != null && order.size() > 0 ? order : null;
        }

        public int compare(IndexerInfo<T> o1, IndexerInfo<T> o2) {
            if (orderByResolvers != null) {
                return compareByResolvers(o1, o2);
            } else {
                return compareBySlowMimeTypes(o1, o2);
            }
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final Map<String, Integer> orderByResolvers;

        private int compareByResolvers(IndexerInfo<T> o1, IndexerInfo<T> o2) {
            Collection<? extends String> mimeTypes1 = o1.getMimeTypes();
            Collection<? extends String> mimeTypes2 = o2.getMimeTypes();

            // check the all mime types category
            boolean all1 = mimeTypes1.contains(ALL_MIME_TYPES);
            boolean all2 = mimeTypes2.contains(ALL_MIME_TYPES);
            if (all1 && all2) {
                return 0;
            } else if (all1) {
                return 1;
            } else if (all2) {
                return -1;
            }

            // check the mime types order
            Integer order1 = highestOrder(mimeTypes1);
            Integer order2 = highestOrder(mimeTypes2);
            if (order1 == null && order2 == null) {
                return 0;
            } else if (order1 == null) {
                return 1;
            } else if (order2 == null) {
                return -1;
            } else {
                return order1.intValue() - order2.intValue();
            }
        }

        private int compareBySlowMimeTypes(IndexerInfo<T> o1, IndexerInfo<T> o2) {
            Collection<? extends String> mimeTypes1 = o1.getMimeTypes();
            Collection<? extends String> mimeTypes2 = o2.getMimeTypes();

            // check the all mime types category
            boolean all1 = mimeTypes1.contains(ALL_MIME_TYPES);
            boolean all2 = mimeTypes2.contains(ALL_MIME_TYPES);
            if (all1 && all2) {
                return 0;
            } else if (all1) {
                return 1;
            } else if (all2) {
                return -1;
            }

            // check the slow mimetypes category
            boolean slow1 = Util.containsAny(mimeTypes1, SLOW_MIME_TYPES);
            boolean slow2 = Util.containsAny(mimeTypes2, SLOW_MIME_TYPES);
            if (slow1 && slow2) {
                return 0;
            } else if (slow1) {
                return 1;
            } else if (slow2) {
                return -1;
            }

            // both indexers belong to the fast mimetypes category
            return 0;
        }

        private Integer highestOrder(Collection<? extends String> mimeTypes) {
            Integer highest = null;

            for(String mimeType : mimeTypes) {
                Integer order = orderByResolvers.get(mimeType);
                if (order == null) {
                    highest = null;
                    break;
                } else if (highest == null || highest.intValue() < order.intValue()) {
                    highest = order;
                }
            }

            return highest;
        }
    } // End of C class
}
