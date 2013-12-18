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
package org.netbeans.modules.cnd.repository.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.api.FilePath;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.Layer;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerFactory;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayerListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataInputStream;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataOutputStream;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author akrasny
 */
/* package */ final class Storage {

    // A list of all layers that belong to this Storage.
    private final List<Layer> layers;
    private final List<LayerDescriptor> layerDescriptors;
    // A list of all client FileSystems: clientFileSystem <-> clientFileSystemID
    private final FileSystemsDictionary clientFileSystemsDictionary = new FileSystemsDictionary();
    // A list of all client UnitDescriptors: clientUnitDescriptor <-> clientShortUnitID
    private final UnitDescriptorsDictionary clientUnitDescriptorsDictionary = new UnitDescriptorsDictionary();
    // For each clientShortUnitID: A list of all client FilePaths: filePathID <-> clientFilePaths
    // (the same file has the same filePathID in any layer)
    private final Map<Integer, FilePathsDictionary> filePathDictionaries = new HashMap<Integer, FilePathsDictionary>();
    // For LayerDescriptor -> map of translation clientFileSystemID => fileSystemIndexInLayer
    private final Map<LayerDescriptor, Map<Integer, Integer>> fileSystemsTranslationMap = new HashMap<LayerDescriptor, Map<Integer, Integer>>();
    // For LayerDescriptor -> map of translation clientShortUnitID => unitIDInLayer
    private final ConcurrentHashMap<LayerDescriptor, Map<Integer, Integer>> unitsTranslationMap = new ConcurrentHashMap<LayerDescriptor, Map<Integer, Integer>>();
    // Encodes/decodes storage ID into unitID: clientShortUnitID <-> clientLongUnitID
    private final UnitsConverter storageMask;
    private final ReentrantLock storageLock = new ReentrantLock();
    private final int storageID;
    private final LayeringSupportImpl layeringSupport;
    private static final java.util.logging.Logger log = org.netbeans.modules.cnd.repository.Logger.getInstance();

    Storage(final int persistMechanismVersion, final int storageID, final List<LayerDescriptor> layerDescriptors, final UnitsConverter unitIDConverter) {
        assert layerDescriptors != null; //&& layerDescriptors.size() > 0;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "New storage with storageID == {0} created",
                    new Object[]{storageID, unitIDConverter});
        }

        this.storageID = storageID;
        this.storageMask = unitIDConverter;
        final Collection<? extends LayerListener> lst =
                Lookups.forPath(LayerListener.PATH).lookupAll(LayerListener.class);
        this.layers = Collections.unmodifiableList(createLayers(layerDescriptors, lst, persistMechanismVersion));
        assert layers != null;// && layers.size() > 0;
        // Initialize layerDescriptors list with descriptors of created layers
        // only.
        List<LayerDescriptor> descriptors = new ArrayList<LayerDescriptor>();
        for (Layer layer : layers) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Layer added: {0}", new Object[]{layer.getLayerDescriptor()});
            }
            descriptors.add(layer.getLayerDescriptor());
        }
        this.layerDescriptors = Collections.unmodifiableList(descriptors);                
        this.layeringSupport = new LayeringSupportImpl();
    }

    /**
     * Returns client unitID (100001) based on clientUnitDescriptor.
     *
     * @param clientUnitDescriptor remote-host-fs:/builds/latest/projects/Test
     * @return - 100001
     */
    // TODO: it would be good to pre-create entries for dependent Units here as
    // well. In this case nothing will be done in layerToClient code...
    public int getUnitID(final UnitDescriptor clientUnitDescriptor) {
        if (clientUnitDescriptorsDictionary.contains(clientUnitDescriptor)) {
            return storageMask.layerToClient(clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor));
        }

        // Register a new ClientShortUnitID and init unitsTranslationMap for it ...
        // clientUnitDescriptor <-> clientShortUnitID
        final int clientShortUnitID = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);

        final FileSystem clientFileSystem = clientUnitDescriptor.getFileSystem();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);

        // 1. find matched filesystem in layers and fill fileSystemsTranslationMap
        // for all layers for this clientFileSystemID
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            Map<Integer, Integer> map = fileSystemsTranslationMap.get(ld);
            // map: clientFileSystemID => fileSystemIndexInLayer
            if (map == null) {
                map = new HashMap<Integer, Integer>();
                fileSystemsTranslationMap.put(ld, map);
            }
            if (!map.containsKey(clientFileSystemID)) {
                int matchedFileSystemIndexInLayer = layer.findMatchedFileSystemIndexInLayer(clientFileSystem);
     //           assert matchedFileSystemIndexInLayer != -1 : "Matched file system not found";
                map.put(clientFileSystemID, matchedFileSystemIndexInLayer);
            }
        }

        updateUnitsTranslationMap(clientUnitDescriptor);
        return storageMask.layerToClient(clientShortUnitID);
    }

    private void updateUnitsTranslationMap(UnitDescriptor clientUnitDescriptor) {
        final int clientShortUnitID = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);
        // 2. find matched Unit in layers and fill unitsTranslationMap for all
        // layers for the clientUnitDescriptor
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            Map<Integer, Integer> map = unitsTranslationMap.get(ld);
            // map: clientShortUnitID => unitIDInLayer
            if (map == null) {
                map = new ConcurrentHashMap<Integer, Integer>();
                Map<Integer, Integer> old = unitsTranslationMap.putIfAbsent(ld, map);
                if (old != null) {
                    map = old;
                }
            }

            if (!map.containsKey(clientShortUnitID)) {
                int matchedUnitIDInLayer = findMatchedUnitIDInLayer(layer, clientUnitDescriptor);
//                if (matchedUnitIDInLayer == -1 && layer.getWriteCapability() != null) {
//                    UnitDescriptor layerUnitDescriptor = createLayerUnitDescriptor(layer, clientUnitDescriptor);
//
//
//
//                    matchedUnitIDInLayer = layer.getUnitsTable().indexOf(layerUnitDescriptor);
//                    layer.getWriteCapability().registerNewUnit(matchedUnitIDInLayer, layerUnitDescriptor);
//                    matchedUnitIDInLayer = 
//                }
                map.put(clientShortUnitID, matchedUnitIDInLayer);
            }
        }
    }

    private List<Layer> createLayers(List<LayerDescriptor> layerDescriptors, Collection<? extends LayerListener> lst, int persistMechanismVersion) {
        Collection<? extends LayerFactory> factories = Lookup.getDefault().lookupAll(LayerFactory.class);
        List<Layer> result = new ArrayList<Layer>();

        for (LayerDescriptor layerDescriptor : layerDescriptors) {
            Layer layer = null;
            for (LayerFactory factory : factories) {
                if (factory.canHandle(layerDescriptor)) {
                    layer = factory.createLayer(layerDescriptor);
                    break;
                }
            }
            if (layer != null) {
                //TODO: exceptions listener
                // layer.setExceptionsListener(exceptionsListener);
                //check if layer can be opened by other layering clients
                boolean isOK = true;
                for (LayerListener layerListener : lst) {
                    isOK &= layerListener.layerOpened(layerDescriptor);
                }                
                boolean success = layer.startup(persistMechanismVersion, !isOK);
                //ant check if layers is correct from the other layering clients point of view
                if (success) {
                    result.add(layer);
                }
            }
        }

        return result;
    }
    
    
    
    boolean maintain(long timeout){
        if (layers.isEmpty()) {
            return false;
        }

        Layer[] unitList = layers.toArray(new Layer[layers.size()]);
        Arrays.sort(unitList, new MaintenanceComparator());
        boolean needMoreTime = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < unitList.length; i++) {
            final WriteLayerCapability writeCapability = unitList[i].getWriteCapability();
            if (writeCapability == null) {
                //no need to maintain read onle layers
                continue;
            }
            if (timeout <= 0) {
                needMoreTime = true;
                break;
            }

            try {
                if (writeCapability.maintenance(timeout)) {
                    needMoreTime = true;
                }
            } catch (IOException ex) {
            }
            timeout -= (System.currentTimeMillis() - start);
        }
        return needMoreTime;        
    }
    

    public RepositoryDataInputStream getInputStream(Key key) {
            
        for (Layer layer : layers) {
            final LayerDescriptor ld = layer.getLayerDescriptor();
            LayerKey layerKey = getReadLayerKey(key, layer);
            if (layerKey == null) {
                // Not in this layer.
                continue;
            }
            if (layer.removedTableKeySet().contains(layerKey)){
                return null;
            }
            log.log(Level.FINE, "will get ByteBuffer from the read capability for the key "
                    + "with unit id:{0} and behaviour: {1}", new Object[]{key.getUnitId(), key.getBehavior()});//NOI18N
            ByteBuffer rawData = layer.getReadCapability().read(layerKey);
            if (rawData != null) {
                return new RepositoryDataInputStream(
                        new ByteArrayInputStream(rawData.array()),
                        new UnitIDReadConverterImpl(layer),
                        new FSReadConverterImpl(ld));
            }
        }
        return null;
    }

    // TODO: For now only single writeable layer is supported. Use multiplexer..
    public RepositoryDataOutputStream getOutputStream(Key key) {
        for (Layer layer : layers) {
            UnitIDWriteConverterImpl unitIDConverter = new UnitIDWriteConverterImpl(layer);
            FSConverter fsConverter = new FSWriteConverterImpl(layer);
            WriteLayerCapability wc = layer.getWriteCapability();
            if (wc != null) {
                LayerKey layerKey = getWriteLayerKey(key, layer);
                if (layerKey != null) {
                    return new RepositoryDataOutputStream(
                            layerKey,
                            wc,
                            unitIDConverter,
                            fsConverter);
                }
            }
        }
        RepositoryExceptions.throwException(this, key, new RepositoryException(true));
        return null;
    }

    public List<LayerDescriptor> getLayerDescriptors() {
        return layerDescriptors;
    }

    void shutdown() {
        storageLock.lock();
        try {
            Collection<Integer> clientShortUnitIDs = clientUnitDescriptorsDictionary.getUnitIDs();
            for (Integer clientShortUnitID : clientShortUnitIDs) {
                closeUnit(clientShortUnitID);
            }
            for (Layer layer : layers) {
                layer.shutdown();
            }
        } finally {
            storageLock.unlock();
        }
    }

    private LayerKey getReadLayerKey(Key key, Layer layer) {
        UnitIDReadConverterImpl unitIDConverter = new UnitIDReadConverterImpl(layer);
        // 100001
        int unitId = key.getUnitId();
        // 5
        Integer layerUnitID = unitIDConverter.clientToLayer(unitId);
        if (layerUnitID < 0) {
            // Not in this layer...
            return null;
        }
        return LayerKey.create(key, layerUnitID);
    }

    private LayerKey getWriteLayerKey(Key key, Layer layer) {
        UnitsConverter unitIDConverter = new UnitIDWriteConverterImpl(layer);
        // 100007
        int clientUnitID = key.getUnitId();
        // 5
        Integer layerUnitID = unitIDConverter.clientToLayer(clientUnitID);
        if (layerUnitID < 0) {
            throw new InternalError();
        }
        return LayerKey.create(key, layerUnitID);
    }

    void openUnit(int clientUnitID) {
        // unitID == 100001
        // 1
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        UnitDescriptor clientUnitDescriptor = clientUnitDescriptorsDictionary.getUnitDescriptor(clientShortUnitID);
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientUnitDescriptor.getFileSystem());
        Layer layer_to_read_files_from = null;
        int unit_id_layer_to_read_files_from = -1;
        
        for (Layer layer : layers) {
            UnitIDReadConverterImpl unitIDConverter = new UnitIDReadConverterImpl(layer);
            // 5
            Integer unitIDInLayer = unitIDConverter.clientToLayer(clientUnitID);
            if (unitIDInLayer < 0) {
                // There is no this Unit in this layer
                continue;
            }
            
            layer_to_read_files_from = layer;
            unit_id_layer_to_read_files_from = unitIDInLayer;
            layer.openUnit(unitIDInLayer);

            Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
            // map: clientFileSystemID => fileSystemIndexInLayer
            Integer requiredFileSystem = map.get(clientFileSystemID);
            if (requiredFileSystem == null) {
                throw new InternalError();
            }
            if (requiredFileSystem.intValue() < 0) {
                continue;
            }
        }

        // FileName table is the same in all the layers. (Layer0 is 
        // an exception). So we need to read it only once and
        // put converted paths to the fsDictionary (for this unit 
        // (clientUnitID)).
        synchronized (filePathDictionaries) {
            FilePathsDictionary fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict == null) {
                List<CharSequence> convertedTable;
                if (layer_to_read_files_from != null) {
                    List<CharSequence> fileNameTable = layer_to_read_files_from.getFileNameTable(unit_id_layer_to_read_files_from);
                    convertedTable = new ArrayList<CharSequence>(fileNameTable.size());
                    for (CharSequence fname : fileNameTable) {
                        FilePath sourceFSPath = new FilePath(layer_to_read_files_from.getUnitsTable().get(unit_id_layer_to_read_files_from).getFileSystem(), fname.toString());
                        CharSequence pathInLayer = RepositoryMapper.map(clientUnitDescriptor.getFileSystem(), sourceFSPath);
                        convertedTable.add(pathInLayer);
                    }
                } else {
                    convertedTable = new ArrayList<CharSequence>();
                }
                filePathDictionaries.put(clientShortUnitID, new FilePathsDictionary(convertedTable));
            }
        }
    }

    private void closeUnit(int shortClientUnitID) {
        Integer clientUnitID = storageMask.layerToClient(shortClientUnitID);
        closeUnit(clientUnitID, false, Collections.<Integer>emptySet());
    }

    void closeUnit(int clientUnitID, boolean cleanRepository, Set<Integer> requiredUnits) {
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        FilePathsDictionary files;
        synchronized (filePathDictionaries) {
            files = filePathDictionaries.get(clientShortUnitID);
        }
        List<CharSequence> flist = files == null
                ? Collections.<CharSequence>emptyList() : files.toList();
        for (Layer layer : layers) {
            Map<Integer, Integer> map = unitsTranslationMap.get(layer.getLayerDescriptor());
            final Integer unitIDInLayer = map.get(clientShortUnitID);
            // map: clientShortUnitID => unitIDInLayer
            layer.closeUnit(unitIDInLayer, cleanRepository, requiredUnits);
            WriteLayerCapability writeCapability = layer.getWriteCapability();
            if (writeCapability != null) {
                if (!cleanRepository) {
                    writeCapability.storeFilesTable(unitIDInLayer, flist);
                }
            }
        }
    }

    /**
     *
     * @param unitID - 10001
     * @param fileIdx
     * @return
     */
    CharSequence getFileName(int unitID, int fileIdx) {
        // 1
        Integer unmaskedID = storageMask.clientToLayer(unitID);
        FilePathsDictionary fsDict;
        synchronized (filePathDictionaries) {
            fsDict = filePathDictionaries.get(unmaskedID);
            if (fsDict == null) {
                openUnit(unmaskedID);
                fsDict = filePathDictionaries.get(unmaskedID);
            }            
        }
        return fsDict.getFilePath(fileIdx);
    }

    int getFileID(int clientUnitID, CharSequence fileName) {
        Integer clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        FilePathsDictionary fsDict;

        synchronized (filePathDictionaries) {
            fsDict = filePathDictionaries.get(clientShortUnitID);
            if (fsDict == null) {
                openUnit(clientUnitID);
                fsDict = filePathDictionaries.get(clientShortUnitID);
            }
        }

        return fsDict.getFileID(fileName);
    }

    CharSequence getUnitName(int unitID) {
        Integer unmaskedID = storageMask.clientToLayer(unitID);
        return clientUnitDescriptorsDictionary.getUnitDescriptor(unmaskedID).getName();
    }

    // TODO: to be removed when implementing TextIndex Layering
//    URI getStorageLocation() {
//        return layerDescriptors.get(0).getURI();
//    }

    private int findMatchedUnitIDInLayer(Layer layer, UnitDescriptor clientUnitDescriptor) {
        List<UnitDescriptor> layerUnitsTable = layer.getUnitsTable();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientUnitDescriptor.getFileSystem());
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        Integer requiredFileSystem = map.get(clientFileSystemID);
        if (requiredFileSystem == null) {
            throw new InternalError();
        }
        if (requiredFileSystem.intValue() < 0) {
            return -1;
        }

        for (int unitIDInLayer = 0; unitIDInLayer < layerUnitsTable.size(); unitIDInLayer++) {
            UnitDescriptor layerUnitDescriptor = layerUnitsTable.get(unitIDInLayer);
            FileSystem unitFileSystemInLayer = layerUnitDescriptor.getFileSystem();
            int fileSystemIndexInLayer = layer.getFileSystemsTable().indexOf(unitFileSystemInLayer);
//            assert fileSystemIndexInLayer != -1;
            if (requiredFileSystem.equals(fileSystemIndexInLayer)) {
                if (RepositoryMapper.matches(layerUnitDescriptor, clientUnitDescriptor)) {
                    // units: clientShortUnitID => unitIDInLayer
                    return unitIDInLayer;
                }
            }
        }
        return -1;
    }

    private UnitDescriptor createClientUnitDescriptor(Layer layer, UnitDescriptor layerUnitDescriptor) {
        FileSystem unitFileSystemInLayer = layerUnitDescriptor.getFileSystem();
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        int fileSystemIndexInLayer = layer.getFileSystemsTable().indexOf(unitFileSystemInLayer);
        if (fileSystemIndexInLayer == -1) {
            throw new InternalError();
        }
        int clientFileSystemID = -1;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(fileSystemIndexInLayer)) {
                clientFileSystemID = entry.getKey();
                break;
            }
        }
        if (clientFileSystemID == -1) {
            throw new InternalError();
        }
        FileSystem clientFileSystem = clientFileSystemsDictionary.getFileSystem(clientFileSystemID);
        return RepositoryMapper.map(clientFileSystem, layerUnitDescriptor);
    }

    private UnitDescriptor createLayerUnitDescriptor(Layer layer, UnitDescriptor clientUnitDescriptor) {
        Map<Integer, Integer> map = fileSystemsTranslationMap.get(layer.getLayerDescriptor());
        // map: clientFileSystemID => fileSystemIndexInLayer
        FileSystem clientFileSystem = clientUnitDescriptor.getFileSystem();
        int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);
        Integer fileSystemIndexInLayer = map.get(clientFileSystemID);
        if (fileSystemIndexInLayer == null || fileSystemIndexInLayer.intValue() == -1) {
            fileSystemIndexInLayer = layer.getWriteCapability().registerClientFileSystem(clientFileSystem);
            map.put(clientFileSystemID, fileSystemIndexInLayer);
        }
        return RepositoryMapper.map(layer.getFileSystemsTable().get(fileSystemIndexInLayer), clientUnitDescriptor);
    }

    void removeUnit(int clientLongUnitID) {
        try {
            for (Layer layer : layers) {
                WriteLayerCapability wc = layer.getWriteCapability();
                UnitIDReadConverterImpl unitIDConverter = new UnitIDReadConverterImpl(layer);
                Integer unitIDInLayer = unitIDConverter.clientToLayer(clientLongUnitID);
                if (wc != null) {
                    wc.removeUnit(unitIDInLayer);
                }
            }
        } finally {
            clientUnitDescriptorsDictionary.remove(storageMask.clientToLayer(clientLongUnitID));
        }
    }

    int clientToLayer(final LayerDescriptor layerDescriptor, int clientUnitID) {
        int clientShortUnitID = storageMask.clientToLayer(clientUnitID);
        Map<Integer, Integer> unitsMap = unitsTranslationMap.get(layerDescriptor);
        if (unitsMap == null) {
            return -1;
        }
        return unitsMap.get(clientShortUnitID);
    }

    UnitsConverter getStorageMask() {
        return storageMask;
    }

    UnitsConverter getReadUnitsConverter(LayerDescriptor layerDescriptor) {
        int layerID = layerDescriptors.indexOf(layerDescriptor);
        assert layerID >= 0;
        Layer layer = layers.get(layerID);
        return new UnitIDReadConverterImpl(layer);
    }

    UnitsConverter getWriteUnitsConverter(LayerDescriptor layerDescriptor) {
        int layerID = layerDescriptors.indexOf(layerDescriptor);
        assert layerID >= 0;
        Layer layer = layers.get(layerID);
        return new UnitIDWriteConverterImpl(layer);
    }

    LayeringSupport getLayeringSupport() {
        return layeringSupport;
    }
    
    final void remove(Key key) {
        //if the only layer and it is writable - just physically remove
        //need to support removed_table on the level of read layer
        //we will keep the table of removed keys (if any) on this level        
        boolean keepInRemovedTable = false;
        for (Layer layer : layers) {
            final ReadLayerCapability readCapability = layer.getReadCapability();
            WriteLayerCapability wc = layer.getWriteCapability();
            //we have read-only layer, need to keep removed table in some writable layer
            if (readCapability != null && wc == null) {
                LayerKey layerKey = getReadLayerKey(key, layer);
                keepInRemovedTable = keepInRemovedTable || readCapability.knowsKey(layerKey);
            }
            
            if (wc != null) {
                LayerKey layerKey = getWriteLayerKey(key, layer);
                if (layerKey != null) {
                    //if we have read only layer we should fill removed table and persist it
                    //TODO: but we should do it only of the key exists in read-only layer,
                    //otherwise we can remove it completely 
                    wc.remove(layerKey, keepInRemovedTable);
                }
            }
        }        
        
    }

    private class LayeringSupportImpl implements LayeringSupport {
        
        @Override
        public List<LayerDescriptor> getLayerDescriptors() {
            return Storage.this.getLayerDescriptors();
        }

        @Override
        public int getStorageID() {
            return storageID;
        }

        @Override
        public UnitsConverter getReadUnitsConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getReadUnitsConverter(layerDescriptor);
        }

        @Override
        public UnitsConverter getWriteUnitsConverter(LayerDescriptor layerDescriptor) {
            return Storage.this.getWriteUnitsConverter(layerDescriptor);
        }
        
    }

    private class UnitIDReadConverterImpl implements UnitsConverter {

        private final Map<Integer, Integer> map;
        // map: clientShortUnitID => unitIDInLayer
        private final Layer layer;

        private UnitIDReadConverterImpl(Layer layer) {
            this.layer = layer;
            map = unitsTranslationMap.get(layer.getLayerDescriptor());
            if (map == null) {
                throw new InternalError("unitsTranslationMap must contain entry for " + layer + " at this point."); // NOI18N
            }
        }

        /**
         * Gets as a parameter 100001
         *
         * @param unitID - 100001
         * @return 5
         */
        @Override
        public int clientToLayer(int clientLongUnitID) {
            int clientShortUnitID = storageMask.clientToLayer(clientLongUnitID);
            final Integer result = map.get(clientShortUnitID);
            return result == null ? -1 : result.intValue();
        }

        // Gets  as a parameter 5
        // Returns 1000001
        @Override
        public int layerToClient(int unitIDInLayer) {
            Integer result = null;
            // map: clientShortUnitID => unitIDInLayer
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(unitIDInLayer)) {
                    result = entry.getKey();
                }
            }

            if (result == null) {
                // This means that there is a Unit in the Layer (layer's units 
                // are initialized), but it was not registered in a map yet.
                //
                // The situation occurs when reading unit from a stream that
                // has a refference to a different unit.
                // ('Quote' (ID==0) unit was registered, and its corespondent
                // InputStream was passed for instantiation. But there was a 
                // refference (ID==1) in the stream and now instantiator needs
                // a clientUnitID for the ID==1 in the layer..
                // The problem (?) is that we need to find a mapping between 
                // Unit1 in Layer1 and client's Units... 
                // Ideally we need to call getUnitID(clientUnitDescription). But
                // we don't have clientUnitDescription. And to create it we need
                // clientFileSystem and clientUnitName
                //
                // Is it possible to have something like 
                // clientUnitDescriptor = createClientUnitDescriptor(layerUnitDescription)?
                //
                // Perhaps this could be postponed some-how (like perform a fake
                // registration here and when reader asks for getUnitID in its 
                // terms do the actual mapping and return the ID that we get here?
                //
                // Do registration.
                // TODO: HOW TO DEAL WITH THIS ???
                UnitDescriptor layerUnit = layer.getUnitsTable().get(unitIDInLayer);
                // 'Reserve' a new ID for the layer.
                // Put a wrapper around the layer's Unit
                // Once client's unitDescriptor is in the game we will substitute
                // this wrapper with a real clientUnitDescriptor.

                UnitDescriptor clientUnitDescriptor = createClientUnitDescriptor(layer, layerUnit);
                result = clientUnitDescriptorsDictionary.getUnitID(clientUnitDescriptor);
                map.put(result, unitIDInLayer);
                updateUnitsTranslationMap(clientUnitDescriptor);
            }

            return result.equals(-1) ? -1 : storageMask.layerToClient(result);
        }
    }

    private class UnitIDWriteConverterImpl implements UnitsConverter {

        private final Map<Integer, Integer> map;
        private final Layer layer;
        // map: clientShortUnitID => unitIDInLayer

        private UnitIDWriteConverterImpl(Layer layer) {
            this.layer = layer;
            map = unitsTranslationMap.get(layer.getLayerDescriptor());
            if (map == null) {
                throw new InternalError("unitsTranslationMap must contain entry for " + layer + " at this point."); // NOI18N
            }
        }

        @Override
        public int clientToLayer(int clientUnitID) {
            log.log(Level.FINE, "UnitIDWriteConverterImpl.clientToLayer ({0})", clientUnitID);
            int clientShortUnitID = storageMask.clientToLayer(clientUnitID);
            final Integer result;
            synchronized (map) {
                result = map.get(clientShortUnitID);
                if (result == null) {
                    throw new InternalError();
                }
                if (result.intValue() == -1) {
                    UnitDescriptor unitDescriptor = clientUnitDescriptorsDictionary.getUnitDescriptor(clientShortUnitID);
                    UnitDescriptor layerUnitDescriptor = createLayerUnitDescriptor(layer, unitDescriptor);
                    int res = layer.getWriteCapability().registerNewUnit(layerUnitDescriptor);
                    map.put(clientShortUnitID, res);
                    return res;
                }
            }
            return result.intValue();
        }

        @Override
        public int layerToClient(int unitIDInLayer) {
            throw new InternalError("Should not be called"); // NOI18N
        }
    }

    private class FSReadConverterImpl implements FSConverter {

        private final Map<Integer, Integer> map;
        // map: clientFileSystemID => fileSystemIndexInLayer

        /**
         * Convertor created for rmi layer (rmi://akrasny@enum) does the
         * following conversions: layerToClient: 0 ('localhost') -> akrasny@enum
         * clientToLayer: akrasny@enum -> 0 ('localhost');
         *
         * Convertor created for r/o layer (localhost) does the following
         * conversions:
         *
         * layerToClient: 0 ('localhost') -> akrasny@enum clientToLayer:
         * akrasny@enum -> 0 ('localhost');
         *
         */
        private FSReadConverterImpl(LayerDescriptor ld) {
            this.map = fileSystemsTranslationMap.get(ld);
            if (map == null) {
                throw new InternalError("fileSystemsTranslationMap must contain entry for " + ld + " at this point."); // NOI18N
            }
        }

        /**
         *
         * @param fileSystemIndexInLayer - index of 'localhost' in this layer
         * (ld)
         * @return akrasny@enum
         */
        @Override
        public FileSystem layerToClient(int fileSystemIndexInLayer) {
            log.log(Level.FINE, "FSReadConverterImpl.clientToLayer ({0})", fileSystemIndexInLayer);
            int clientFileSystemID = -1;
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(fileSystemIndexInLayer)) {
                    clientFileSystemID = entry.getKey();
                }
            }

            return clientFileSystemID < 0 ? null : clientFileSystemsDictionary.getFileSystem(clientFileSystemID);
        }

        /**
         *
         * @param clientFileSystem - akrasny@enum
         * @return index of localhost
         */
        @Override
        public int clientToLayer(FileSystem clientFileSystem) {
            throw new InternalError("Should not be called"); // NOI18N
        }
    }

    private class FSWriteConverterImpl implements FSConverter {

        private final Map<Integer, Integer> map;
        // map: clientFileSystemID => fileSystemIndexInLayer
        private final Layer layer;

        /**
         * Convertor created for rmi layer (rmi://akrasny@enum) does the
         * following conversions: layerToClient: 0 ('localhost') -> akrasny@enum
         * clientToLayer: akrasny@enum -> 0 ('localhost');
         *
         * Convertor created for r/o layer (localhost) does the following
         * conversions:
         *
         * layerToClient: 0 ('localhost') -> akrasny@enum clientToLayer:
         * akrasny@enum -> 0 ('localhost');
         *
         */
        private FSWriteConverterImpl(final Layer layer) {
            this.layer = layer;
            LayerDescriptor ld = layer.getLayerDescriptor();
            this.map = fileSystemsTranslationMap.get(ld);
            if (map == null) {
                throw new InternalError("fileSystemsTranslationMap must contain entry for " + ld + " at this point."); // NOI18N
            }
        }

        /**
         *
         * @param fileSystemIndexInLayer - index of 'localhost' in this layer
         * (ld)
         * @return akrasny@enum
         */
        @Override
        public FileSystem layerToClient(int fileSystemIndexInLayer) {
            throw new InternalError("Should not be called"); // NOI18N
        }

        /**
         *
         * @param clientFileSystem - akrasny@enum
         * @return index of localhost
         */
        @Override
        public int clientToLayer(final FileSystem clientFileSystem) {
            log.log(Level.FINE, "FSWriteConverterImpl.clientToLayer ({0})", clientFileSystem);
            int clientFileSystemID = clientFileSystemsDictionary.getFileSystemID(clientFileSystem);
            Integer result = map.get(clientFileSystemID);
            if (result == null) {
                throw new InternalError();
            }
            if (result.intValue() == -1) {
                return layer.getWriteCapability().registerClientFileSystem(clientFileSystem);
            }
            return result.intValue();
        }
    }
    private static class MaintenanceComparator implements Comparator<Layer>, Serializable {

        private static final long serialVersionUID = 7249069246763182397L;

        @Override
        public int compare(Layer o1, Layer o2) {
            return getMaintenanceWeight(o2) -  getMaintenanceWeight(o1);
        }
        
        private  int getMaintenanceWeight(Layer layer) {
            try {
                final WriteLayerCapability writeCapability = layer.getWriteCapability();
                if (writeCapability == null){
                    return 0;
                }
                return writeCapability.getMaintenanceWeight();
            } catch (IOException ex) {
            }
        return 0;
        }
    }    
}
