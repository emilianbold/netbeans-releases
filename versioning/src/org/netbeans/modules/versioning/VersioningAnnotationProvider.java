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
package org.netbeans.modules.versioning;

import java.util.Map.Entry;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.awt.Mnemonics;

import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.masterfs.providers.AnnotationProvider.class)
public class VersioningAnnotationProvider extends AnnotationProvider {
    
    static VersioningAnnotationProvider instance;
    private static final Logger LOG = Logger.getLogger(VersioningAnnotationProvider.class.getName());
    private static final int CACHE_SIZE = getMaxCacheSize();
    private static final long CACHE_ITEM_MAX_AGE = getMaxAge();
    private static final boolean ANNOTATOR_ASYNC = !"false".equals(System.getProperty("versioning.asyncAnnotator", "true")); //NOI18N
    private static final Image EMPTY_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public VersioningAnnotationProvider() {
        instance = this;
    }
    
    private VersioningSystem getOwner(File file) {
        return file == null ? null : VersioningManager.getInstance().getOwner(file);
    }

    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        Image annotatedIcon;
        if (ANNOTATOR_ASYNC) {
            // at first annotate the empty icon and cache this merge icon.
            // otherwise the cached value would be the final merged icon and there might be problems when acquiring the cached value
            // in another annotate round - if the caller decides to annotate a different icon than the one earlier
            annotatedIcon = iconCache.getValue(iconCache.new ItemKey<Image, String>(files, "", EMPTY_ICON)); //NOI18N
            // and finally merge the cached value with the original icon
            annotatedIcon = annotatedIcon == null ? icon : ImageUtilities.mergeImages(icon, annotatedIcon, 0, 0);
        } else {
            // fallback to the old implementation
            annotatedIcon = iconCache.getValue(iconCache.new ItemKey<Image, String>(files, "", icon)); //NOI18N
        }
        return annotatedIcon;
    }

    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        String annotatedName = labelCache.getValue(labelCache.new ItemKey<String, String>(files, name, name));
        return annotatedName == null ? htmlEncode(name) : annotatedName;
    }

    public Action[] actions(Set files) {
        if (files.isEmpty()) return new Action[0];

        List<Action> actions = new ArrayList<Action>();
        LocalHistoryActions localHistoryAction = null;

        // group all given files by owner
        Map<VersioningSystem, List<File>> owners = new HashMap<VersioningSystem, java.util.List<File>>(3);
        for (FileObject fo : (Set<FileObject>) files) {
            File file = FileUtil.toFile(fo);
            if (file != null) {

                // check if there is at least ine file managed by local hisotry
                VersioningSystem localHistory = VersioningManager.getInstance().getLocalHistory(file);
                if(localHistoryAction == null && localHistory != null && localHistory.getVCSAnnotator() != null) {
                    localHistoryAction = SystemAction.get(LocalHistoryActions.class);
                    localHistoryAction.setVersioningSystem(localHistory);
                    actions.add(localHistoryAction);
                }

                VersioningSystem owner = getOwner(file);
                if(owner != null) {
                    List<File> fileList = owners.get(owner);
                    if(fileList == null) {
                        fileList = new ArrayList<File>();
                    }
                    fileList.add(file);
                    owners.put(owner, fileList);
                }
            }
        }

        VersioningSystem vs = null;
        if(owners.keySet().size() == 1) {
            vs = owners.keySet().iterator().next();
        } else {
            return actions.toArray(new Action [actions.size()]);
        } 
        
        VCSAnnotator an = null;
        if (vs != null) {
            an = vs.getVCSAnnotator();
        }
        if (an != null) {
            VersioningSystemActions action = SystemAction.get(VersioningSystemActions.class);
            action.setVersioningSystem(vs);
            actions.add(action);
        }

        return actions.toArray(new Action [actions.size()]);
    }
    
    public static class VersioningSystemActions extends AbstractVersioningSystemActions {               
    }

    public static class LocalHistoryActions extends AbstractVersioningSystemActions {
    }
    
    public abstract static class AbstractVersioningSystemActions extends SystemAction implements ContextAwareAction {
        
        private VersioningSystem system;

        public String getName() {
            return Utils.getDisplayName(system);
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(system.getClass());
        }

        public void actionPerformed(ActionEvent ev) {
            // this item does nothing, this is not a real action
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new RealVersioningSystemActions(system, Utils.contextForLookup(actionContext));
        }

        public void setVersioningSystem(VersioningSystem system) {
            this.system = system;
        }
    }
    
    private static class RealVersioningSystemActions extends AbstractAction implements Presenter.Popup {

        private final VersioningSystem system;
        private final VCSContext context;

        public RealVersioningSystemActions(VersioningSystem system, VCSContext context) {
            super(Utils.getDisplayName(system));
            this.system = system;
            this.context = context;
        }

        public void actionPerformed(ActionEvent e) {
            // this item does also nothing, it displays a popup ;)
        }

        public JMenuItem getPopupPresenter() {
            return new VersioningSystemMenuItem();
        }
        
        private class VersioningSystemMenuItem extends JMenu {
        
            private boolean popupContructed;

            public VersioningSystemMenuItem() {
                Mnemonics.setLocalizedText(this, Utils.getDisplayName(system));
            }

            public void setSelected(boolean selected) {
                if (selected && popupContructed == false) {
                    // lazy submenu construction
                    Action [] actions = system.getVCSAnnotator().getActions(context, VCSAnnotator.ActionDestination.PopupMenu);
                    for (int i = 0; i < actions.length; i++) {
                        Action action = actions[i];
                        if (action == null) {
                            addSeparator();
                        } else {
                            JMenuItem item = Utils.toMenuItem(action);
                            add(item);
                        }
                    }
                    popupContructed = true;
                }
                super.setSelected(selected);
            }
        }
    }

    public InterceptionListener getInterceptionListener() {
        return VersioningManager.getInstance().getInterceptionListener();
    }

    public String annotateName(String name, Set files) {
        return name;    // do not support 'plain' annotations
    }

    static void refreshAllAnnotations() {
        if (instance != null) {
            instance.refreshAnnotations(null);
        }
    }
                   
    /**
     * Refreshes annotations for all given files and all parent folders of those files.
     *
     * @param filesToRefresh files to refresh
     */
    void refreshAnnotations(Set<File> files) {
        refreshAnnotations(files, true);
    }

    void refreshAnnotations(Set<File> files, boolean removeFromCache) {
        if (files == null) {            
            refreshAllAnnotationsTask.schedule(2000);
            return;
        }
        
        for (File file : files) {
            // try to limit the number of normalizeFile calls:
            // let's find the closest existent FO, then list it's parents with FileObject.getParent();
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            if (fo == null) {
                fo = getExistingParent(file);
            } else {
                // file exists, plan it to refresh
                addToMap(filesToRefresh, fo, removeFromCache);
                fo = fo.getParent();
            }

            // fo is the closest existing parent
            for (FileObject parent = fo; parent != null; parent = parent.getParent()) {
                // plan parent to refresh
                addToMap(parentsToRefresh, parent, removeFromCache);
            }
        }
        
        fireFileStatusChangedTask.schedule(2000);
    }
    
    /**
     * Stores all files which have to be refreshed 
     */
    private Map<FileSystem, Set<FileObject>> filesToRefresh = new HashMap<FileSystem, Set<FileObject>>();
    
    /**
     * Stores all parents from files which have to be refreshed 
     */
    private Map<FileSystem, Set<FileObject>> parentsToRefresh = new HashMap<FileSystem, Set<FileObject>>();        
    
    private RequestProcessor rp = new RequestProcessor("Versioning fire FileStatusChanged", 1, true);
    
    /**
     * Refreshes all annotations and clears the maps holding all files and their parents which have to be refreshed
     */
    private RequestProcessor.Task refreshAllAnnotationsTask = rp.create(new Runnable() {        
        public void run() {            
            clearMap(filesToRefresh);
            clearMap(parentsToRefresh);
            labelCache.removeAll();
            iconCache.removeAll();
            
            FileSystem fileSystem = Utils.getRootFilesystem();
            fireFileStatusChanged(new FileStatusEvent(fileSystem, true, true));
        }
    });    
    
    /**
     * Refreshes all files stored in filesToRefresh and parentsToRefresh
     */ 
    private RequestProcessor.Task fireFileStatusChangedTask = rp.create(new Runnable() {        
        public void run() {
            
            // create and fire for all files which have to be refreshed
            List<FileStatusEvent> fileEvents = new ArrayList<FileStatusEvent>(); 
            List<FileStatusEvent> folderEvents = new ArrayList<FileStatusEvent>(); 

            synchronized(filesToRefresh) {
                Set<FileSystem> fileSystems = filesToRefresh.keySet();                
                if(fileSystems == null || fileSystems.size() == 0) {
                    return;
                }
                for (FileSystem fs : fileSystems) {
                    Set<FileObject> files = new HashSet<FileObject>();
                    Set<FileObject> folders = new HashSet<FileObject>();
                    Set<FileObject> set = filesToRefresh.get(fs);
                    for(FileObject fo : set) {
                        if(fo.isFolder()) {
                            folders.add(fo);
                        } else {
                            files.add(fo);
                        }
                    }        
                    set.clear();
                    if(files.size() > 0) {
                        fileEvents.add(new FileStatusEvent(fs, files, true, true));
                    }
                    if(folders.size() > 0) {
                        folderEvents.add(new FileStatusEvent(fs, folders, true,  true));
                    }
                }        
            }    

            fireFileStatusEvents(fileEvents);
            fireFileStatusEvents(folderEvents);

            // create and fire events for all parent from each file which has to be refreshed
            List<FileStatusEvent> parentEvents = new ArrayList<FileStatusEvent>(); 
            synchronized(parentsToRefresh) {
                Set<FileSystem> fileSystems = parentsToRefresh.keySet();
                for (FileSystem fs : fileSystems) {            
                    Set<FileObject> set = parentsToRefresh.get(fs);
                    Set<FileObject> files = new HashSet<FileObject>(set);
                    parentEvents.add(new FileStatusEvent(fs, files, true, false));                                        
                    set.clear();                    
                }                                
            }       
            fireFileStatusEvents(parentEvents);            
        }    
        
        private void fireFileStatusEvents(Collection<FileStatusEvent> events) {
            for(FileStatusEvent event : events) {
                fireFileStatusChanged(event);
            }
        }          
    });    
    
    private void clearMap(Map<FileSystem, Set<FileObject>> map)  {
        synchronized(map) {
            if(map.size() > 0) {                
                map.clear();
            }
        }                    
    }
    
    private void addToMap(Map<FileSystem, Set<FileObject>> map, FileObject fo, boolean removeFromCache) {
        if(fo == null) {
            return;
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException e) {
            // ignore files in invalid filesystems
            return;
        }        
        synchronized (map) {                        
            Set<FileObject> set = map.get(fs);
            if(set == null) {
                set = new HashSet<FileObject>();
                map.put(fs, set);
            }
            set.add(fo);
            if (removeFromCache) {
                labelCache.removeAllFor(fo);
                iconCache.removeAllFor(fo);
            }
        }
    }

    /**
     * Finds and return the closest existing ancestor FO for the given file
     * @param file file to get an ancestor for
     * @return an ancestor fileobject or null if no such exist
     */
    private FileObject getExistingParent (File file) {
        FileObject fo = null;
        for (File parent = file; parent != null && fo == null; parent = parent.getParentFile()) {
            // find the fileobject
            parent = FileUtil.normalizeFile(parent);
            fo = FileUtil.toFileObject(parent);
        }
        return fo;
    }

    private final Cache<Image, String> iconCache = new Cache<Image, String>(Cache.ANNOTATION_TYPE_ICON);
    private final Cache<String, String> labelCache = new Cache<String, String>(Cache.ANNOTATION_TYPE_LABEL);

    /**
     * Keeps cached annotations
     */
    private class Cache<T, KEY> {
        private static final String ANNOTATION_TYPE_ICON = "IconCache"; //NOI18N
        private static final String ANNOTATION_TYPE_LABEL = "LabelCache"; //NOI18N

        private final LinkedHashMap<ItemKey<T, KEY>, Item<T>> cachedValues = new LinkedHashMap<ItemKey<T, KEY>, Item<T>>(CACHE_SIZE) {
            @Override
            protected boolean removeEldestEntry(Entry<ItemKey<T, KEY>, Item<T>> eldest) {
                if (size() >= CACHE_SIZE) {
                    removeFromIndex(eldest.getKey());
                    return true;
                }
                return false;
            }
        };
        private final WeakHashMap<FileObject, Set<ItemKey<T, KEY>>> index = new WeakHashMap<FileObject, Set<ItemKey <T, KEY>>>(CACHE_SIZE);
        private final LinkedHashSet<ItemKey<T, KEY>> filesToAnnotate;
        private final RequestProcessor.Task annotationRefreshTask;
        private final String type;
        private final HashSet<FileObject> refreshedFiles = new HashSet<FileObject>();
        private boolean allCleared;

        Cache(String type) {
            this.annotationRefreshTask = new RequestProcessor("VersioningAnnotator.annotationRefresh", 1, false, false).create(new AnnotationRefreshTask()); //NOI18N
            this.filesToAnnotate = new LinkedHashSet<ItemKey<T, KEY>>();
            assert ANNOTATION_TYPE_ICON.equals(type) || ANNOTATION_TYPE_LABEL.equals(type);
            this.type = type;
        }

        /**
         * Immediately returns cached value, which can be null, and starts a background call to the annotator which owns the set of files
         * @param files set of files to annotate
         * @param initialValue initial value to annotate
         * @return cached value for files or null
         */
        T getValue (ItemKey<T, KEY> key) {
            if (!ANNOTATOR_ASYNC) {
                return annotate(key.getInitialValue(), key.getFiles());
            }
            T cachedValue;
            boolean itemCached = false;
            synchronized (cachedValues) {
                Item<T> cachedItem = cachedValues.get(key);
                cachedValue = cachedItem == null ? null : cachedItem.getValue();
                if (cachedValue != null || cachedValues.containsKey(key)) {
                    itemCached = true;
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0}.getValue() cached: {1} for {2}", //NOI18N
                        new Object[] {type, cachedValue, key.getFiles()});
            }
            if (!itemCached) {
                // not cached yet, plan the annotation
                addFilesToAnnotate(key);
            }
            return cachedValue;
        }

        /**
         * Stores the new value in the cache. Also checks if an event shall be fired to refresh files' annotations.
         * @param files files for which the value will be kept
         * @param value cached value
         * @return true if the event should be fired - it means the old cached value differs from the new one
         */
        private boolean setValue (ItemKey<T, KEY> key, T value) {
            synchronized (cachedValues) {
                if (allCleared) {
                    return false;
                }
                Set<? extends FileObject> files = key.getFiles();
                for (FileObject fo : files) {
                    if (refreshedFiles.contains(fo)) {
                        return false;
                    }
                }
                cachedValues.put(key, new Item(value));
                // reference to the key must be added to index for every file in the set and every parent
                // so the key can be quickly found when refresh annotations event comes
                for (FileObject fo : files) {
                    boolean added = false;
                    do {
                        Set<ItemKey<T, KEY>> sets = index.get(fo);
                        if (sets == null) {
                            sets = new HashSet<ItemKey<T, KEY>>();
                            index.put(fo, sets);
                        }
                        added = sets.add(key);
                    } while (added && (fo = fo.getParent()) != null);
                }
                removeOldValues();
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0}.setValue(): {1} for {2}", new Object[] {type, value, key}); //NOI18N
            }
            return true;
        }

        private void removeOldValues () {
            for (Iterator<Map.Entry<ItemKey<T, KEY>, Item<T>>> it = cachedValues.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<ItemKey<T, KEY>, Item<T>> e = it.next();
                if (!e.getValue().isValid()) {
                    removeFromIndex(e.getKey());
                    it.remove();
                } else {
                    // do not search on, next entries are newer
                    break;
                }
            }
        }

        /**
         * Plan annotation scan for these files.
         * @param files
         * @param initialValue
         */
        private void addFilesToAnnotate (ItemKey<T, KEY> key) {
            boolean start;
            synchronized (filesToAnnotate) {
                start = filesToAnnotate.add(key);
            }
            if (start) {
                annotationRefreshTask.schedule(0);
            }
        }

        private ItemKey<T, KEY> getNextFilesToAnnotate () {
            ItemKey<T, KEY> retval = null;
            synchronized (filesToAnnotate) {
                Iterator<ItemKey<T, KEY>> it = filesToAnnotate.iterator();
                if (it.hasNext()) {
                    retval = it.next();
                    it.remove();
                }
            }
            return retval;
        }

        private T annotate(VCSAnnotator annotator, T initialValue, VCSContext context) {
            if (ANNOTATION_TYPE_LABEL.equals(type)) {
                return (T) annotator.annotateName((String) initialValue, context);
            } else if (ANNOTATION_TYPE_ICON.equals(type)) {
                return (T) annotator.annotateIcon((Image) initialValue, context);
            } else {
                LOG.log(Level.WARNING, "{0}.annotate unsupported", type); //NOI18N
                assert false;
                return null;
            }
        }
        
        private T annotate (T initialValue, Set<? extends FileObject> files) {
            long ft = System.currentTimeMillis();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0}.annotate for {1}", new Object[] {type, files}); //NOI18N
            }
            VCSAnnotator an = null;

            try {
                if (files.isEmpty()) {
                    return initialValue;
                }
                FileObject fo = (FileObject) files.iterator().next();
                VersioningSystem vs = getOwner(FileUtil.toFile(fo));

                if (vs == null) {
                    return null;
                }
                an = vs.getVCSAnnotator();
                if (an == null) {
                    return null;
                }

                VCSContext context = Utils.contextForFileObjects(files);
                return annotate(an, initialValue, context);
            } finally {
                if (LOG.isLoggable(Level.FINE)) {
                    long t = System.currentTimeMillis();
                    if (LOG.isLoggable(Level.FINE)) {
                        if (an != null) {
                            LOG.log(Level.FINE, "{0}.annotate in {1} returns in " + (t - ft) + " millis", //NOI18N
                                    new Object[] {type, an.getClass().getName()});
                        } else {
                            LOG.log(Level.FINE, "{0}.annotate returns in " + (t - ft) + " millis", //NOI18N
                                    new Object[] {type});
                        }
                    }
                }
            }
        }

        private void removeAllFor (FileObject fo) {
            synchronized (cachedValues) {
                refreshedFiles.add(fo);
                LOG.log(Level.FINER, "{0}.removeAllFor(): {1}", new Object[] {type, fo.getPath()}); //NOI18N
                Set<ItemKey<T, KEY>> keys = index.get(fo);
                if (keys != null) {
                    for (ItemKey<T, KEY> key : keys) {
                        cachedValues.remove(key);
                    }
                    ItemKey<T, KEY>[] keysArray = keys.toArray(new ItemKey[keys.size()]);
                    for (ItemKey<T, KEY> key : keysArray) {
                        removeFromIndex(key);
                    }
                }
            }
        }

        private void removeFromIndex (ItemKey<T, KEY> key) {
            Set<? extends FileObject> files = key.getFiles();
            Set<FileObject> removedFor = new HashSet<FileObject>();
            // remove all references for every file and it's parents from the index
            for (FileObject fo : files) {
                do {
                    removedFor.add(fo);
                    Set<ItemKey<T, KEY>> sets = index.get(fo);
                    if (sets != null) {
                        sets.remove(key);
                        if (sets.size() == 0) {
                            // remove the whole entry
                            index.remove(fo);
                        }
                    }
                } while ((fo = fo.getParent()) != null && !removedFor.contains(fo));
            }
        }

        private void removeAll() {
            synchronized (cachedValues) {
                allCleared = true;
                cachedValues.clear();
                index.clear();
            }
        }

        private class AnnotationRefreshTask implements Runnable {
            public void run() {
                ItemKey<T, KEY> refreshCandidate;
                while ((refreshCandidate = getNextFilesToAnnotate()) != null) {
                    T initialValue = refreshCandidate.getInitialValue();
                    Set<? extends FileObject> files = refreshCandidate.getFiles();
                    assert files != null;
                    clearEvents();
                    T newValue = annotate(initialValue, files);
                    boolean fireEvent = setValue(refreshCandidate, newValue);
                    if (fireEvent) {
                        Set<File> filesToRefresh = new HashSet<File>(files.size());
                        for (FileObject fo : files) {
                            File file = FileUtil.toFile(fo);
                            if (file != null) {
                                filesToRefresh.add(file);
                            }
                        }
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "{0}.AnnotationRefreshTask.run(): firing refresh event for {1}", //NOI18N
                                    new Object[] {type, filesToRefresh});
                        }
                        refreshAnnotations(filesToRefresh, false);
                    }
                }
            }
        }

        private void clearEvents() {
            synchronized (cachedValues) {
                refreshedFiles.clear();
                allCleared = false;
            }
        }

        private class Item<T> {
            private final T value;
            private final long timeStamp;

            public Item(T value) {
                this.value = value;
                this.timeStamp = System.currentTimeMillis();
            }

            public T getValue () {
                return value;
            }

            public boolean isValid () {
                return CACHE_ITEM_MAX_AGE == -1 || System.currentTimeMillis() - timeStamp < CACHE_ITEM_MAX_AGE;
            }
        }

        private class ItemKey<T, KEY> {
            private final T initialValue;
            private final KEY keyPart;
            private final Set<? extends FileObject> files;
            private Integer hashCode;

            public ItemKey(Set<? extends FileObject> files, KEY keyPart, T initialValue) {
                assert keyPart != null;
                this.initialValue = initialValue;
                this.keyPart = keyPart;
                this.files = files;
            }

            public T getInitialValue () {
                return initialValue;
            }

            public Set<? extends FileObject> getFiles () {
                return files;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof ItemKey) {
                    ItemKey other = (ItemKey) obj;
                    return files.equals(other.files) && (keyPart.equals(other.keyPart));
                }
                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                // hashCode should allways return the same value during the lifetime of the instance
                if (hashCode == null) {
                    int hash = 5;
                    hash = 29 * hash + (this.keyPart != null ? this.keyPart.hashCode() : 0);
                    hash = 29 * hash + (this.files != null ? this.files.hashCode() : 0);
                    if (hashCode == null) {
                        hashCode = hash;
                    }
                    hashCode = hash;
                }
                return hashCode;
            }

            @Override
            public String toString() {
                return files.toString() + ": " + keyPart.toString() + "(" + (initialValue == null ? "null" : initialValue.toString()) + ")"; //NOI18N
            }
        }
    }

    private static int getMaxCacheSize () {
        String cacheSizeProp = System.getProperty("versioning.annotator.cacheSize", "0"); //NOI18N
        int cacheSize = 0;
        try {
            if (cacheSizeProp != null) {
                cacheSize = Integer.parseInt(cacheSizeProp);
            }
        } catch (NumberFormatException ex) {
            LOG.log(Level.INFO, "Max cache size: " + cacheSizeProp, ex); //NOI18N
            cacheSize = 0;
        }
        if (cacheSize < 250) {
            cacheSize = 500;
        }
        LOG.log(Level.FINE, "getMaxCacheSize(): " + cacheSize);         //NOI18N
        return cacheSize;
    }

    private static long getMaxAge () {
        String cacheItemAgeProp = System.getProperty("versioning.annotator.cacheItem.maxAge", "60000"); //NOI18N
        long cacheItemAge = 0;
        try {
            if (cacheItemAgeProp != null) {
                cacheItemAge = Long.parseLong(cacheItemAgeProp);
            }
        } catch (NumberFormatException ex) {
            LOG.log(Level.INFO, "Max cache item age: " + cacheItemAgeProp, ex); //NOI18N
            cacheItemAge = 0;
        }
        if (cacheItemAge != -1 && cacheItemAge < 60000) {
            cacheItemAge = 10 * 60 * 1000;
        }
        LOG.log(Level.FINE, "getMaxAge(): " + cacheItemAge);            //NOI18N
        return cacheItemAge;
    }

    private static final java.util.regex.Pattern lessThan = java.util.regex.Pattern.compile("<"); //NOI18N
    private String htmlEncode (String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;");               //NOI18N
    }
}
