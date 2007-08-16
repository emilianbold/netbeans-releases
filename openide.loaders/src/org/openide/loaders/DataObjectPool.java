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

package org.openide.loaders;


import java.lang.ref.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Registration of all data objects in the system.
* Maps data objects to its handlers.
*
* @author Jaroslav Tulach
*/
final class DataObjectPool extends Object
implements ChangeListener {
    /** set to null if the constructor is called from somewhere else than DataObject.find
     * Otherwise contains items that have just been created in this thread and
     * shall be notified.
     */
    private static final ThreadLocal<Collection<Item>> FIND = new ThreadLocal<Collection<Item>>();
    
    /** validator */
    private static final Validator VALIDATOR = new Validator ();

    private static final Collection<Item> TOKEN = Collections.unmodifiableList(new ArrayList<Item>());

    /** assignes file objects a unique instance of Item, if it has been created
     */
    private Map<FileObject,Item> map = new DoubleHashMap();
    /** just for testing purposes
     */
    static final void fastCache(boolean fast) {
        if (fast) {
            POOL.children = null;
        } else {
            POOL.children = new HashMap<FileObject, List<Item>>();
        }
    }
    /** map that assigns to each folder list of Items created for its children */
    private Map<FileObject,List<Item>> children = new HashMap<FileObject, List<Item>>();
    
    /** covers all FileSystems we're listening on */
    private Set<FileSystem> knownFileSystems = new WeakSet<FileSystem>();
    
    /** error manager to log what is happening here */
    private static final Logger err = Logger.getLogger("org.openide.loaders.DataObject.find"); // NOI18N
    
    /** the pool for all objects. Use getPOOL method instead of direct referencing
     * this field.
     */
    private static DataObjectPool POOL;

    /** Lock for creating POOL instance */
    private static Object lockPOOL = new Object();

    /** check to know if someone is waiting in waitNotified, changed from
     * inside synchronized block, but read without synchronization, that is
     * why it is made volatile
     */
    private volatile long inWaitNotified = -1;
    
    /** Get the instance of DataObjectPool - value of static field 'POOL'.
     * Initialize the field if necessary.
     *
     * @return The DataObjectPool.
     */
    static DataObjectPool getPOOL() {
        synchronized (lockPOOL) {
            if (POOL != null)
                return POOL;
            POOL = new DataObjectPool ();
        }
        
        lp.addChangeListener(POOL);

        return POOL;
    }
    
    /** Allows DataObject constructors to be called.
     * @return a key to pass to exitAllowConstructor
     */
    private static Collection<Item> enterAllowContructor() {
        Collection<Item> prev = FIND.get();
        FIND.set (TOKEN);
        return prev;
    }
    
    /** Disallows DataObject constructors to be called and notifies 
     * all created DataObjects.
     */
    private static void exitAllowConstructor(Collection<Item> previous) {
        Collection<Item> l = FIND.get ();
        FIND.set (previous);
        if (l != TOKEN) getPOOL ().notifyCreationAll(l);
    }
	
    /** Method to check whether the constructor is allowed.
     */
    final static boolean isConstructorAllowed() {
        return FIND.get() != null;
    }

    /** Calls into one loader. Setups security condition to allow DataObject ocnstructor
     * to succeed.
     */
    public static DataObject handleFindDataObject (DataLoader loader, FileObject fo, DataLoader.RecognizedFiles rec) 
    throws java.io.IOException {
        DataObject ret;
        
        Collection<Item> prev = enterAllowContructor();
        try {
            // make sure this thread is allowed to recognize
            getPOOL ().enterRecognition(fo);
            
            ret = loader.handleFindDataObject (fo, rec);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
    }

    /** Creates and finishes registration of MultiDataObject.
     */
    public static MultiDataObject createMultiObject (MultiFileLoader loader, FileObject fo)
    throws java.io.IOException {
        MultiDataObject ret;
        
        Collection<Item> prev = enterAllowContructor();
        try {
            ret = loader.createMultiObject (fo);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
     }
    
    /** Calls into FolderLoader. Setups security condition to allow DataObject constructor
     * to succeed.
     */
    public static MultiDataObject createMultiObject(DataLoaderPool.FolderLoader loader, FileObject fo, DataFolder original) throws java.io.IOException {
        MultiDataObject ret;
        
        Collection<Item> prev = enterAllowContructor();
        try {
            ret = loader.createMultiObject (fo, original);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
     }
    
        
    
    /** Executes atomic action with privilege to create DataObjects.
     */
    public void runAtomicActionSimple (FileObject fo, FileSystem.AtomicAction action) 
    throws java.io.IOException {
        Collection<Item> prev = enterAllowContructor();
        try {
            fo.getFileSystem ().runAtomicAction(action);
        } finally {
            exitAllowConstructor (prev);
        }
    }
    
    //
    // Support for running really atomic actions
    //
    private Thread atomic;
    private RequestProcessor privileged;
    /** the folder that is being modified */
    private FileObject blocked;
    public void runAtomicAction (final FileObject target, final FileSystem.AtomicAction action) 
    throws java.io.IOException {
        
        class WrapAtomicAction implements FileSystem.AtomicAction {
            public void run () throws java.io.IOException {
                Thread prev;
                FileObject prevBlocked;
                synchronized (DataObjectPool.this) {
                    // make sure that we are the ones that own 
                    // the recognition process
                    enterRecognition (null);
                    prev = atomic;
                    prevBlocked = blocked;
                    atomic = Thread.currentThread ();
                    blocked = target;
                }

                Collection<Item> findPrev = enterAllowContructor();
                try {
                    action.run ();
                } finally {
                    synchronized (DataObjectPool.this) {
                        atomic = prev;
                        blocked = prevBlocked;
                        DataObjectPool.this.notifyAll ();
                    }
                    exitAllowConstructor (findPrev);
                }
            }
        } // end of WrapAtomicAction
        
        target.getFileSystem ().runAtomicAction(new WrapAtomicAction ());
    }
    
    /** The thread that runs in atomic action wants to delegate its privilege
     * to somebody else. Used in DataFolder.getChildren that blocks on 
     * Folder Recognizer thread.
     *
     * @param delegate the privileged processor
     */
    public synchronized void enterPrivilegedProcessor(RequestProcessor delegate) {
        if (atomic == Thread.currentThread()) {
            if (privileged != null) throw new IllegalStateException ("Previous privileged is not null: " + privileged + " now: " + delegate); // NOI18N
            privileged = delegate;
        }
        // wakeup everyone in enterRecognition, as this changes the conditions there
        notifyAll ();
    }
    
    /** Exits the privileged processor.
     */
    public synchronized void exitPrivilegedProcessor(RequestProcessor delegate) {
        if (atomic == Thread.currentThread ()) {
            if (privileged != delegate) throw new IllegalStateException ("Trying to unregister wrong privileged. Prev: " + privileged + " now: " + delegate); // NOI18N
            privileged = null;
        }
        // wakeup everyone in enterRecognition, as this changes the conditions there
        notifyAll ();
    }
    
    /** Ensures it is safe to enter the recognition. 
     * @param fo file object we want to recognize or null if we do not know it
     */
    private synchronized void enterRecognition (FileObject fo) {
        // wait till nobody else stops the recognition
        for (;;) {
            if (atomic == null) {
                // ok, I am the one who can enter
                break;
            }
            if (atomic == Thread.currentThread()) {
                // ok, reentering again
                break;
            }
            
            if (privileged != null && privileged.isRequestProcessorThread()) {
                // ok, we have privileged request processor thread
                break;
            }
            
            if (fo != null && blocked != null && !blocked.equals (fo.getParent ())) {
                // access to a file in different folder than it is blocked
                // => go on
                break;
            }
            
            if (err.isLoggable(Level.FINE)) {
                err.fine("Enter recognition block: " + Thread.currentThread()); // NOI18N
                err.fine("            waiting for: " + fo); // NOI18N
                err.fine("        blocking thread: " + atomic); // NOI18N
                err.fine("             blocked on: " + blocked); // NOI18N
            }
            try {
                if (FolderList.isFolderRecognizerThread()) {
                    inWaitNotified = System.currentTimeMillis();
                }
                wait ();
            } catch (InterruptedException ex) {
                // means nothing, go on
            } finally {
                if (FolderList.isFolderRecognizerThread()) {
                    inWaitNotified = -1;
                }
            }
        } 
    }
    
    /** Collection of all objects that has been created but their
    * creation has not been yet notified to OperationListener.postCreate
    * method.
    */
    private Set<Item> toNotify = new HashSet<Item>();
    
    /** Constructor.
     */
    private DataObjectPool () {
    }

    /** Checks whether there is a data object with primary file
    * passed thru the parameter.
    *
    * @param fo the file to check
    * @return data object with fo as primary file or null
    */
    public DataObject find (FileObject fo) {
        synchronized (this) {
            Item doh = map.get(fo);
            if (doh == null) {
                return null;
            }
            
            // do not return DOs before their creation were notified to OperationListeners
            if (toNotify.contains (doh)) {
                // special test for data objects calling this method from 
                // their own constructor, those are ok to be returned if
                // they exist
                Collection<Item> l = FIND.get();
                if (l == null || !l.contains (doh)) {
                    return null;
                }
            }

            return doh.getDataObjectOrNull ();
        }
    }
    
    /** mapping of files to registration count */
    private final Map<FileObject,Integer> registrationCounts = new WeakHashMap<FileObject,Integer>();
    void countRegistration(FileObject fo) {
        Integer i = registrationCounts.get(fo);
        Integer i2;
        if (i == null) {
            i2 = 1;
        } else {
            i2 = i + 1;
        }
        registrationCounts.put(fo, i2);
    }
    /** For use from FolderChildren. @see "#20699" */
    int registrationCount(FileObject fo) {
        Integer i = registrationCounts.get(fo);
        if (i == null) {
            return 0;
        } else {
            return i;
        }
    }
    
    /** Refresh of all folders.
    */
    private void refreshAllFolders () {
        Set<FileObject> files;
        synchronized (this) {
            files = new HashSet<FileObject>(map.keySet());
        }

        for (FileObject fo : files) {
            if (fo.isFolder ()) {
                DataObject obj = find (fo);
                if (obj instanceof DataFolder) {
                    DataFolder df = (DataFolder)obj;
                    FileObject file = df.getPrimaryFile ();
                    synchronized (this) {
                        if (toNotify.isEmpty() || !toNotify.contains(map.get(file))) {
                            FolderList.changedDataSystem (file);
                        }
                    }
                }
            }
        }
    }

    /** Rescans all fileobjects in given set.
    * @param s mutable set of FileObjects
    * @return set of DataObjects that refused to be revalidated
    */
    public Set<DataObject> revalidate (Set<FileObject> s) {
        return VALIDATOR.revalidate (s);
    }

    /** Rescan all primary files of currently existing data
    * objects.
    *
    * @return set of DataObjects that refused to be revalidated
    */
    public Set<DataObject> revalidate () {
        Set<FileObject> files;
        synchronized (this) {
            files = createSetOfAllFiles (map.values ());
        }

        return revalidate (files);
    }

    /** Notifies that an object has been created.
     * @param obj the object that was created
    */
    public void notifyCreation (DataObject obj) {
        notifyCreation (obj.item);
    }

    private static final DataLoaderPool lp = DataLoaderPool.getDefault();
    
    /** Notifies the creation of an item*/
    private void notifyCreation (Item item) {
        synchronized (this) {
            if (err.isLoggable(Level.FINE)) {
                err.fine("Notify created: " + item + " by " + Thread.currentThread()); // NOI18N
            }
            
            if (toNotify.isEmpty()) {
                if (err.isLoggable(Level.FINE)) {
                    err.fine("  but toNotify is empty"); // NOI18N
                }
                return;
            }
            
            if (!toNotify.remove (item)) {
                if (err.isLoggable(Level.FINE)) {
                    err.fine("  the item is not there: " + toNotify); // NOI18N
                }
                return;
            }
            
            // if somebody is caught in waitNotified then wake him up
            notifyAll ();
        }
        
        DataObject obj = item.getDataObjectOrNull ();
        if (obj != null) {
            lp.fireOperationEvent (
                new OperationEvent (obj), OperationEvent.CREATE
            );
        }
    }
    
    /** Notifies all objects in the list */
    private void notifyCreationAll(Collection<Item> l) {
        if (l.isEmpty()) return;
        for (Item i : l) {
            notifyCreation (i);
        }
    }
	
    /** Wait till the data object will be notified. But wait limited amount
     * of time so we will not deadlock
     *
     * @param obj data object to check
     */
    public void waitNotified (DataObject obj) {
        for (;;) {
            synchronized (this) {
                try {
                    enterRecognition (obj.getPrimaryFile().getParent());

                    if (toNotify.isEmpty()) {
                        return;
                    }

                    Collection<Item> l = FIND.get ();
                    if (l != null && l.contains (obj.item)) {
                        return;
                    }

                    if (!toNotify.contains (obj.item)) {
                        return;
                    }

                    if (err.isLoggable(Level.FINE)) {
                        err.fine("waitTillNotified: " + Thread.currentThread()); // NOI18N
                        err.fine("      waitingFor: " + obj.getPrimaryFile ().getPath ()); // NOI18N
                    }

                    if (FolderList.isFolderRecognizerThread()) {
                        inWaitNotified = System.currentTimeMillis();
                    }
                    wait ();
                } catch (InterruptedException ex) {
                    // never mind
                } finally {
                    if (FolderList.isFolderRecognizerThread()) {
                        inWaitNotified = -1;
                    }
                }
            }
        }
    }

    /** Allows to check whether folder recognizer is in waitNotified method in order
     * to detect more precisly the condition needed for deadlock #65543 really
     * happened.
     * @return the time for how long the folder recognizer is waiting or -1 if it is not
     */
    final long timeInWaitNotified() {
        long l = inWaitNotified;
        if (l == -1) {
            return -1;
        } else {
            l = System.currentTimeMillis() - l;
            if (l < 0) {
                l = 0;
            }
            return l;
        }
    }
    
    
    /** Add to list of created objects.
     */
    private void notifyAdd (Item item) {
        toNotify.add (item);
        Collection<Item> l = FIND.get ();
        if (l == TOKEN) FIND.set(l = new ArrayList<Item>());
        l.add (item);
    }
    
    private static final Logger LISTENER = Logger.getLogger("org.openide.loaders.DataObjectPool.Listener"); // NOI18N

    
    /** Listener used to distribute the File events to their DOs.
     * [pnejedly] A little bit about its internals/motivation:
     * Originally, every created DO have hooked its onw listener to the primary
     * FO's parent folder for listening on primary FO changes. The listener
     * was enhanced in MDO to also cover secondaries.
     * Now there is one FSListener per FileSystem which have to distribute
     * the events to the DOs using limited DOPool's knowledge about FO->DO
     * mapping. Because the mapping knowledge is limited to primary FOs only,
     * it have to resort to notifying all known DOs for given folder
     * if the changed file is not known. Although it is not as good as direct
     * notification used for known primaries, it is still no worse than
     * all DOs listening on their folder themselves as it spares at least
     * the zillions of WeakListener instances.
     */
    private final class FSListener extends FileChangeAdapter {
        FSListener() {}
        private Collection<Item> getTargets(FileEvent fe) {
            FileObject fo = fe.getFile();
            List<Item> toNotify = new LinkedList<Item>();
            // The FileSystem notifying us about the changes should
            // not hold any lock so we're safe here
            synchronized (DataObjectPool.this) {
                Item itm = map.get(fo);
                if (itm != null) { // the file was someones' primary
                    return Collections.singleton(itm); // so notify only owner
                } else { // unknown file or someone secondary
                    List<Item> arr = children.get(fo.getParent());
                    if (arr != null) {
                        return new ArrayList<Item>(arr);
                    } else {
                        return Collections.emptyList();
                    }
                }
            }
        }

        public void fileChanged(FileEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileChanged: " + fe); // NOI18N
            }
            for (Item item : getTargets(fe)) {
                DataObject dobj = item.getDataObjectOrNull();
                if (LISTENER.isLoggable(Level.FINE)) {
                    LISTENER.fine("  to: " + dobj); // NOI18N
                }
                if (dobj != null) dobj.notifyFileChanged(fe);
            }
        }

        public void fileRenamed (FileRenameEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileRenamed: " + fe); // NOI18N
            }
            for (Item item : getTargets(fe)) {
                DataObject dobj = item.getDataObjectOrNull();
                if (LISTENER.isLoggable(Level.FINE)) {
                    LISTENER.fine("  to: " + dobj); // NOI18N
                }
                if (dobj != null) dobj.notifyFileRenamed(fe);
            }
        }

        public void fileDeleted (FileEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileDeleted: " + fe); // NOI18N
            }
            for (Item item : getTargets(fe)) {
                DataObject dobj = item.getDataObjectOrNull();
                if (LISTENER.isLoggable(Level.FINE)) {
                    LISTENER.fine("  to: " + dobj); // NOI18N
                }
                if (dobj != null) dobj.notifyFileDeleted(fe);
            }
        }

        public void fileDataCreated (FileEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileDataCreated: " + fe); // NOI18N
            }
            for (Item item : getTargets(fe)) {
                DataObject dobj = item.getDataObjectOrNull();
                if (LISTENER.isLoggable(Level.FINE)) {
                    LISTENER.fine("  to: " + dobj); // NOI18N
                }
                if (dobj != null) dobj.notifyFileDataCreated(fe);
            }
            ShadowChangeAdapter.checkBrokenDataShadows(fe);
        }
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileAttributeChanged: " + fe); // NOI18N
            }
            for (Item item : getTargets(fe)) {
                DataObject dobj = item.getDataObjectOrNull();
                if (LISTENER.isLoggable(Level.FINE)) {
                    LISTENER.fine("  to: " + dobj); // NOI18N
                }
                if (dobj != null) dobj.notifyAttributeChanged(fe);
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            if (LISTENER.isLoggable(Level.FINE)) {
                LISTENER.fine("fileFolderCreated: " + fe); // NOI18N
            }
            ShadowChangeAdapter.checkBrokenDataShadows(fe);
        }
    }
    
    /** Registers new DataObject instance.
    * @param fo primary file for obj
    * @param loader the loader of the object to be created
    *
    * @return object with common information for this <CODE>DataObject</CODE>
    * @exception DataObjectExistsException if the file object is already registered
    */
    public Item register (FileObject fo, DataLoader loader) throws DataObjectExistsException {
        if (FIND.get () == null) throw new IllegalStateException ("DataObject constructor can be called only thru DataObject.find - use that method"); // NOI18N
        
        // here we're registering a listener on fo's FileSystem so we can deliver
        // fo changes to DO without lots of tiny listeners on folders
        // The new DS bound to a repository can simply place a single listener
        // on its repository instead of registering listeners on FileSystems. 
        try { // to register a listener of fo's FileSystem
            FileSystem fs = fo.getFileSystem();
            synchronized (knownFileSystems) {
                if (! knownFileSystems.contains(fs)) {
                    fs.addFileChangeListener (new FSListener());
                    knownFileSystems.add(fs);
                }
            }
        } catch (FileStateInvalidException e ) {
            // no need to listen then
        }
        
        Item doh;
        DataObject obj;
        synchronized (this) {
            doh = map.get(fo);
            // if Item for this file has not been created yet
            if (doh == null) {
                doh = new Item (fo);
                map.put (fo, doh);
                countRegistration(fo);
                notifyAdd (doh);

                VALIDATOR.notifyRegistered (fo);

                return doh;
            }
            
            obj = doh.getDataObjectOrNull ();

            if (obj == null) {
                // the item is to be finalize => create new
                doh = new Item (fo);
                map.put (fo, doh);
                countRegistration(fo);
                notifyAdd (doh);

                return doh;
            }
            
            if (!VALIDATOR.reregister (obj, loader)) {
                throw new DataObjectExistsException (obj);
            }
        }
        
        try {
            obj.setValid (false);
            synchronized (this) {
                // check if there isn't any new data object registered 
                // when this thread left synchronization block.
                Item doh2 = map.get(fo);
                if (doh2 == null) {
                    doh = new Item (fo);
                    map.put (fo, doh);
                    countRegistration(fo);
                    notifyAdd (doh);

                    return doh;
                }
            }
        } catch (java.beans.PropertyVetoException ex) {
            VALIDATOR.refusingObjects.add (obj);
        }
        throw new DataObjectExistsException (obj);
    }

    /** Notifies all newly created objects to

    /** Deregister.
    * @param item the item with common information to deregister
    * @param refresh true if the parent folder should be refreshed
    */
    private synchronized void deregister (Item item, boolean refresh) {
        FileObject fo = item.primaryFile;

        Item previous = map.remove(fo);

        if (previous != null && previous != item) {
            // ops, mistake,
            // return back the original
            map.put (fo, previous);
            countRegistration(fo);
            // Furthermore, item is probably in toNotify by mistake.
            // Observed in DataFolderTest.testMove: after vetoing the move
            // of a data folder, the bogus item for the temporary new folder
            // (e.g. BB/AAA/A1) is left in the toNotify pool forever. This
            // point is reached; remove it now. -jglick
            if (toNotify.remove(item)) {
                notifyAll();
            }
            return;
        }

        // refresh of parent folder
        if (refresh) {
            fo = fo.getParent ();
            if (fo != null) {
                Item item2 = map.get (fo);
                if (item2 != null) {
                    DataFolder df = (DataFolder) item2.getDataObjectOrNull();
                    if (df != null) {
                        VALIDATOR.refreshFolderOf (df);
                    }
                }
            }
        }
    }

    /** Changes the primary file to new one.
    * @param item the item to change
    * @param newFile new primary file to set
    */
    private synchronized void changePrimaryFile (
        Item item, FileObject newFile
    ) {
        if (item.primaryFile == newFile) {
            return;
        }
        map.remove (item.primaryFile);
        item.primaryFile = newFile;
        map.put (newFile, item);
        countRegistration(newFile);
    }

    /** When the loader pool is changed, then all objects are rescanned.
    */
    public void stateChanged (javax.swing.event.ChangeEvent ev) {
        Set<Item> set;
        synchronized (this) {
            // copy the values synchronously
            set = new HashSet<Item>(map.values());
        }
        revalidate(createSetOfAllFiles(set));
    }
    
    /** Create list of all files for given collection of data objects.
    * @param c collection of DataObjectPool.Item
    * @return set of files
    */
    private static Set<FileObject> createSetOfAllFiles(Collection<Item> c) {
        Set<FileObject> set = new HashSet<FileObject>(c.size() * 7);
        
        for (Item item : c) {
            DataObject obj = item.getDataObjectOrNull ();
            if (obj != null) {
                getPOOL ().waitNotified (obj);
                set.addAll (obj.files ());
            }
        }
        return set;
    }
    
    /** Returns all currently existing data
    * objects.
    */    
    Iterator<Item> getActiveDataObjects () {
        synchronized (this) {
            return new ArrayList<Item>(map.values()).iterator();
        }
    }

    /** One item in object pool.
    */
    static final class Item extends Object {
        /** initial value of obj field. */
        private static final Reference<DataObject> REFERENCE_NOT_SET = new WeakReference<DataObject>(null);

        /** weak reference data object with this primary file */
        private Reference<DataObject> obj = REFERENCE_NOT_SET;
        
        /** primary file */
        FileObject primaryFile;
        
        // [PENDING] hack to check the stack when the DataObject has been created
        //    private Exception stack;

        /** @param fo primary file
        * @param pool object pool
        */
        public Item (FileObject fo) {
            this.primaryFile = fo;

            // [PENDING] // stores stack
            /*      java.io.StringWriter sw = new java.io.StringWriter ();
                  stack = new Exception ();
                }

                // [PENDING] toString returns original stack
                public String toString () {
                  return stack.toString ();*/
        }

        /** Setter for the data object. Called immediatelly as possible.
        * @param obj the data object for this item
        */
        public void setDataObject (DataObject obj) {
            this.obj = new ItemReference (obj, this);
            
            if (obj != null && !obj.getPrimaryFile ().isValid ()) {
                // if the primary file is already invalid =>
                // mark the object as invalid
                deregister (false);
            }
            
            synchronized (DataObjectPool.getPOOL()) {
                DataObjectPool.getPOOL().notifyAll();
            }
        }

        /** Getter for the data object.
        * @return the data object or null
        */
        DataObject getDataObjectOrNull () {
            synchronized (DataObjectPool.getPOOL()) {
                while (this.obj == REFERENCE_NOT_SET) {
                    try {
                        DataObjectPool.getPOOL().wait ();
                    }
                    catch (InterruptedException exc) {
                    }
                }
            }
            
            return this.obj == null ? null : this.obj.get();
        }
        
        /** Getter for the data object.
        * @return the data object
        * @exception IllegalStateException if the data object has been lost
        *   due to weak references (should not happen)
        */
        public DataObject getDataObject () {
            DataObject obj = getDataObjectOrNull ();
            if (obj == null) {
                throw new IllegalStateException ();
            }
            return obj;
        }

        /** Deregister one reference.
        * @param refresh true if the parent folder should be refreshed
        */
        public void deregister (boolean refresh) {
            getPOOL().deregister (this, refresh);
        }

        /** Changes the primary file to new one.
        * @param newFile new primary file to set
        */
        public void changePrimaryFile (FileObject newFile) {
            getPOOL().changePrimaryFile (this, newFile);
        }

        /** Is the item valid?
        */
        public boolean isValid () {
            if (getPOOL().map.get (primaryFile) == this) {
                return primaryFile.isValid();
            } else {
                return false;
            }
            
        }
        
        public String toString () {
            DataObject obj = this.obj.get ();
            if (obj == null) {
                return "nothing[" + primaryFile + "]"; // NOI18N
            }
            return obj.toString ();
        }
    }

    /** WeakReference - references a DataObject, strongly references an Item */
    static final class ItemReference extends WeakReference<DataObject>
    implements Runnable {
        /** Reference to an Item */
        private Item item;
        
        ItemReference(DataObject dobject, Item item) {
            super(dobject, org.openide.util.Utilities.activeReferenceQueue());
            this.item = item;
        }

        /** Does the cleanup of the reference */
        public void run () {
            item.deregister(false);
            item = null;
        }
        
    }
    
    /** Validator to allow rescan of files.
    */
    private static final class Validator extends Object
    implements DataLoader.RecognizedFiles {
        /** error manager to log what is happening here */
        private static final Logger err = Logger.getLogger("org.openide.loaders.DataObject.Validator"); // NOI18N
        
        /** set of all files that should be revalidated */
        private Set<FileObject> files;
        /** current thread that is in the validator */
        private Thread current;
        /** number of threads waiting to enter the validation */
        private int waiters;
        /** Number of calls to enter by current thread minus 1 */
        private int reenterCount;
        /** set of files that has been marked recognized */
        private Set<FileObject> recognizedFiles;
        /** set with all objects that refused to be discarded */
        private Set<DataObject> refusingObjects;
        /** set of files that has been registered during revalidation */
        private Set<FileObject> createdFiles;

	Validator() {}

        /** Enters the section.
        * @param set mutable set of files that should be processed
        * @return the set of files concatenated with any previous sets
        */
        private synchronized Set<FileObject> enter(Set<FileObject> set) {
            boolean log = err.isLoggable (Level.FINE);
            if (log) {
                err.fine("enter: " + set + " on thread: " + Thread.currentThread ()); // NOI18N
            }
            if (current == Thread.currentThread ()) {
                reenterCount++;
                if (log) {
                    err.fine("current thread, rentered: " + reenterCount); // NOI18N
                }
            } else {
                waiters++;
                if (log) {
                    err.fine("Waiting as waiter: " + waiters); // NOI18N
                }
                while (current != null) {
                    try {
                        wait ();
                    } catch (InterruptedException ex) {
                    }
                }
                current = Thread.currentThread ();
                waiters--;
                if (log) {
                    err.fine("Wait finished, waiters: " + waiters + " new current: " + current); // NOI18N
                }
            }
            
            if (files == null) {
                if (log) {
                    err.fine("New files: " + set); // NOI18N
                }
                files = set;
            } else {
                files.addAll (set);
                if (log) {
                    err.fine("Added files: " + set); // NOI18N
                    err.fine("So they are: " + files); // NOI18N
                }
            }

            return files;
        }

        /** Leaves the critical section.
        */
        private synchronized void exit () {
            boolean log = err.isLoggable (Level.FINE);
            if (reenterCount == 0) {
                current = null;
                if (waiters == 0) {
                    files = null;
                }
                notify ();
                if (log) {
                    err.fine("Exit and notify from " + Thread.currentThread ()); // NOI18N
                }
            } else {
                reenterCount--;
                if (log) {
                    err.fine("Exit reentrant: " + reenterCount); // NOI18N
                }
            }
        }

        /** If there is another waiting thread, then I can
        * cancel my computation.
        */
        private synchronized boolean goOn () {
            return waiters == 0;
        }

        /** Called to either refresh folder, or register the folder to be
        * refreshed later is validation is in progress.
        */
        public void refreshFolderOf (DataFolder df) {
            if (createdFiles == null) {
                // no validator in progress
                FolderList.changedDataSystem (df.getPrimaryFile ());
            }
        }

        /** Mark this file as being recognized. It will be excluded
        * from further processing.
        *
        * @param fo file object to exclude
        */
        public void markRecognized (FileObject fo) {
            recognizedFiles.add (fo);
        }

        public void notifyRegistered (FileObject fo) {
            if (createdFiles != null) {
                createdFiles.add (fo);
            }
        }

        /** Reregister new object for already existing file object.
        * @param obj old object existing
        * @param loader loader of new object to create
        * @return true if the old object has been discarded and new one can
        *    be created
        */
        public boolean reregister (DataObject obj, DataLoader loader) {
            if (recognizedFiles == null) {
                // revalidation not in progress
                return false;
            }

            if (obj.getLoader () == loader) {
                // no change in loader =>
                return false;
            }

            if (createdFiles.contains (obj.getPrimaryFile ())) {
                // if the file already has been created
                return false;
            }

            if (refusingObjects.contains (obj)) {
                // the object has been refused before
                return false;
            }

            return true;
        }

        /** Rescans all fileobjects in given set.
        * @param s mutable set of FileObjects
        * @return set of objects that refused to be revalidated
        */
        public Set<DataObject> revalidate (Set<FileObject> s) {
            
            // ----------------- fix of #30559 START
            if ((s.size() == 1) && (current == Thread.currentThread ())) {
                if (files != null && files.contains(s.iterator().next())) {
                    return new HashSet<DataObject>();
                }
            }
            // ----------------- fix of #30559 END
            
            // holds all created object, so they are not garbage
            // collected till this method ends
            List<DataObject> createObjects = new LinkedList<DataObject>();
            boolean log = err.isLoggable (Level.FINE);
            try {
                
                s = enter (s);
                
                recognizedFiles = new HashSet<FileObject>();
                refusingObjects = new HashSet<DataObject>();
                createdFiles = new HashSet<FileObject>();

                DataLoaderPool pool = lp;
                Iterator<FileObject> it = s.iterator();
                while (it.hasNext () && goOn ()) {
                    try {
                        FileObject fo = it.next();
                        if (log) {
                            err.fine("Iterate: " + fo); // NOI18N
                        }
                        
                        if (!recognizedFiles.contains (fo)) {
                            // first of all test if the file is on a valid filesystem
                            boolean invalidate = false;

                            // the previous data object should be canceled
                            DataObject orig = getPOOL().find (fo);
                            if (log) {
                                err.fine("Original: " + orig); // NOI18N
                            }
                            if (orig == null) {
                                // go on
                                continue;
                            }

                            // findDataObject
                            // is not using method DataObjectPool.find to locate data object
                            // directly for primary file, that is good
                            DataObject obj = pool.findDataObject (fo, this);
                            createObjects.add (obj);

                            invalidate = obj != orig;

                            if (invalidate) {
                                if (log) {
                                    err.fine("Invalidate: " + obj); // NOI18N
                                }
                                it.remove();                                
                                try {
                                    orig.setValid (false);
                                } catch (java.beans.PropertyVetoException ex) {
                                    refusingObjects.add (orig);
                                    if (log) {
                                        err.fine("  Refusing: " + orig); // NOI18N
                                    }
                                }
                            }
                        }
                    } catch (DataObjectExistsException ex) {
                        // this should be no problem here
                    } catch (java.io.IOException ioe) {
                        Logger.getLogger(DataObjectPool.class.getName()).log(Level.WARNING, null, ioe);
                    } catch (ConcurrentModificationException cme) {
                        // not very nice but the only way I could come up to handle this:
                        // java.util.ConcurrentModificationException
                        //   at java.util.HashMap$HashIterator.remove(HashMap.java:755)
                        //   at org.openide.loaders.DataObjectPool$Validator.revalidate(DataObjectPool.java:916)
                        //   at org.openide.loaders.DataObjectPool.revalidate(DataObjectPool.java:203)
                        //   at org.openide.loaders.DataObjectPool.stateChanged(DataObjectPool.java:527)
                        //   at org.openide.loaders.DataLoaderPool$1.run(DataLoaderPool.java:128)
                        //   at org.openide.util.Task.run(Task.java:136)
                        //[catch] at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:635)
                        // is to ignore the exception and continue
                        it = s.iterator();
                        if (log) {
                            err.log(Level.FINE, null, cme);
                            err.fine("New iterator over: " + s); // NOI18N
                        }
                    }
                }
                return refusingObjects;
            } finally {
                recognizedFiles = null;
                refusingObjects = null;
                createdFiles = null;

                exit ();

                if (log) {
                    err.fine("will do refreshAllFolders: "+ s.size ()); // NOI18N
                }
                
                getPOOL().refreshAllFolders ();
                
                if (log) {
                    err.fine("refreshAllFolders done"); // NOI18N
                }
            }
        }
        
    } // end of Validator
    private final class DoubleHashMap extends HashMap<FileObject,Item> {
        public DoubleHashMap() {
            super(512);
        }
        
        public Item put(FileObject obj, Item item) {
            Item prev = super.put(obj, item);
            if (children == null) {
                return prev;
            }
            
            FileObject parent = obj.getParent();
            if (parent == null) {
                return prev;
            }
            List<Item> arr = children.get(parent);
            if (arr == null) {
                arr = new ArrayList<Item>();
            }
            arr.add(item);
            return prev;
        }
        public Item remove(Object obj) {
            Item prev = super.remove(obj);
            if (! (obj instanceof FileObject)) {
                return prev;
            }
            if (children == null) {
                return prev;
            }
            
            FileObject parent = ((FileObject)obj).getParent();
            if (parent == null) {
                return prev;
            }
            List<Item> arr = children.get(parent);
            if (arr != null) {
                arr.remove(obj);
                if (arr.isEmpty()) {
                    children.remove(parent);
                }
            }
            return prev;
        }
    } // end of DoubleHashMap

}
