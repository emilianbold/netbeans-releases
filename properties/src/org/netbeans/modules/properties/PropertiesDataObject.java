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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.openide.cookies.SaveCookie;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import static java.util.logging.Level.FINER;


/**
 * Object that provides main functionality for properties data loader.
 * Represents set of .properties files with same basic name (name without locale postfix).
 *
 * @author Ian Formanek
 */
public final class PropertiesDataObject extends MultiDataObject implements CookieSet.Factory {

    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 4795737295255253334L;
    
    static final Logger LOG = Logger.getLogger(PropertiesDataObject.class.getName());

    /** Structural view of the dataobject */
    private transient BundleStructure bundleStructure;
    
    /** Open support for this data object. Provides editable table view on bundle. */
    private transient PropertiesOpen openSupport;

    /** Lock used for synchronization of <code>openSupport</code> instance creation */
    private final transient Object OPEN_SUPPORT_LOCK = new Object();

    // Hack due having lock on secondaries, can't override handleCopy, handleMove at all.
    /** Suffix used by copying/moving dataObject. */
    private transient String pasteSuffix;
    
    /** */
    private Lookup lookup;


    /**
     * Constructs a <code>PropertiesDataObject</code> for a specified
     * primary file.
     *
     * @param  primaryFile  primary file to creata a data object for
     * @param  loader  data loader which recognized the primary file
     * @exception   org.openide.loaders.DataObjectExistsException 
     *              if another <code>DataObject</code> already exists
     *              for the specified file
     */
    public PropertiesDataObject(final FileObject primaryFile,
                                final PropertiesDataLoader loader)
            throws DataObjectExistsException {
        super(primaryFile, loader);
        // use editor support
        initialize();
    }

    /**
     */
    PropertiesEncoding getEncoding() {
        return ((PropertiesDataLoader) getLoader()).getEncoding();
    }
    
    private Lookup getSuperLookup() {
        return super.getLookup();
    }
    
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new ProxyLookup(
                    Lookups.singleton(getEncoding()),
                    Lookups.proxy(
                            new Lookup.Provider() {
                                    public Lookup getLookup() {
                                        return getSuperLookup();
                                    }
                    }));
        }
        return lookup;
    }
    
    /** Initializes the object. Used by construction and deserialized. */
    private void initialize() {
        bundleStructure = null;
        Class<? extends Node.Cookie>[] arr = (Class<Node.Cookie>[]) new Class[2];
        arr[0] = PropertiesOpen.class;
        arr[1] = PropertiesEditorSupport.class;
        getCookieSet().add(arr, this);
    }

    /** Implements <code>CookieSet.Factory</code> interface method. */
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T createCookie(Class<T> clazz) {
        if(clazz.isAssignableFrom(PropertiesOpen.class)) {
            return (T) getOpenSupport();
        } else if(clazz.isAssignableFrom(PropertiesEditorSupport.class)) {
            return (T) ((PropertiesFileEntry)getPrimaryEntry()).getPropertiesEditor();
        } else {
            return null;
        }
    }

    // Accessibility from PropertiesOpen:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    /** Copies primary and secondary files to new folder.
     * Overrides superclass method.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy */
    @Override
    protected synchronized DataObject handleCopy(DataFolder df) throws IOException {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("handleCopy("                                     //NOI18N
                    + FileUtil.getFileDisplayName(df.getPrimaryFile()) + ')');
        }
        try {
            pasteSuffix = createPasteSuffix(df);

            return super.handleCopy(df);
        } finally {
            pasteSuffix = null;
        }
    }
    
    /** Moves primary and secondary files to a new folder.
     * Overrides superclass method.
     * @param df the new folder
     * @return the moved primary file object
     * @throws IOException if there was a problem moving
     * @throws UserCancelException if the user cancelled the move */
    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("handleMove("                                     //NOI18N
                    + FileUtil.getFileDisplayName(df.getPrimaryFile()) + ')');
        }

        // a simple fix of issue #92195 (impossible to save a moved prop. file):
        SaveCookie saveCookie = getCookie(SaveCookie.class);
        if (saveCookie != null) {
            saveCookie.save();
        }

        try {
            pasteSuffix = createPasteSuffix(df);
        
            return super.handleMove(df);
        } finally {
            pasteSuffix = null;
        }
    }
    
    /** Gets suffix used by entries by copying/moving. */
    String getPasteSuffix() {
        return pasteSuffix;
    }

    /** Only accessible method, it is necessary to call MultiDataObject's method
     * from this package.
     */
    void removeSecondaryEntry2(Entry fe) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("removeSecondaryEntry2(Entry "                    //NOI18N
                      + FileUtil.getFileDisplayName(fe.getFile()) + ')');
        }
        removeSecondaryEntry (fe);
    }

    /** Creates new name for this instance when moving/copying to new folder destination. 
     * @param folder new folder destination. */
    private String createPasteSuffix(DataFolder folder) {
        String basicName = getPrimaryFile().getName();

        DataObject[] children = folder.getChildren();
        
        
        // Repeat until there is not such file name.
        for(int i = 0; ; i++) {
            String newName;
            
            if (i == 0) {
                newName = basicName;
            } else {
                newName = basicName + i;
            }
            boolean exist = false;
            
            for(int j = 0; j < children.length; j++) {
                if(children[j] instanceof PropertiesDataObject && newName.equals(children[j].getName())) {
                    exist = true;
                    break;
                }
            }
                
            if(!exist) {
                if (i == 0) {
                    return ""; // NOI18N
                } else {
                    return "" + i; // NOI18N
                }
            }
        }
    }

    /** Returns open support. It's used by all subentries as open support too. */
    public PropertiesOpen getOpenSupport() {
        synchronized(OPEN_SUPPORT_LOCK) {
            if(openSupport == null) {
                openSupport = new PropertiesOpen(this);
            }

            return openSupport;
        }
    }

    /** Updates modification status of this dataobject from its entries. */
    void updateModificationStatus() {
        LOG.finer("updateModificationStatus()");                        //NOI18N
        boolean modif = false;
        if (((PresentableFileEntry)getPrimaryEntry()).isModified())
            modif = true;
        else {
            for (Iterator it = secondaryEntries().iterator(); it.hasNext(); ) {
                if (((PresentableFileEntry)it.next()).isModified()) {
                    modif = true;
                    break;
                }
            }
        }

        super.setModified(modif);
    }

    /** Provides node that should represent this data object. When a node for representation
     * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
     * with only parent changed. This implementation creates instance
     * <CODE>DataNode</CODE>.
     * <P>
     * This method is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */                                                           
    @Override
    protected Node createNodeDelegate () {
        PropertiesChildren pc = new PropertiesChildren();

        // properties node - creates new types
        DataNode dn = new PropertiesDataNode(this, pc);
        return dn;
    }

    /** Returns a structural view of this data object */
    public BundleStructure getBundleStructure() {
        if (bundleStructure == null)
            bundleStructure = new BundleStructure(this);
        return bundleStructure;
    }

    /** Comparator used for ordering secondary files, works over file names */
    public static Comparator<String> getSecondaryFilesComparator() {
        return new KeyComparator();
    }

    /**
     */
    void fireNameChange() {
        LOG.finer("fireNameChange()");                                  //NOI18N
        firePropertyChange(PROP_NAME, null, null);
    }
    
    /** Deserialization. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initialize();
    }

    
    /** Children of this <code>PropertiesDataObject</code>. */
    private class PropertiesChildren extends Children.Keys<String> {

        /** Listens to changes on the dataobject */
        private PropertyChangeListener propertyListener = null;

        
        /** Constructor.*/
        PropertiesChildren() {
            super();
        }

        
        /** Sets all keys in the correct order */
        protected void mySetKeys() {
            TreeSet<String> newKeys = new TreeSet<String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            });

            newKeys.add(getPrimaryEntry().getFile().getName());
            
            for (Entry entry : secondaryEntries()) {
                newKeys.add(entry.getFile().getName());
            }

            setKeys(newKeys);
        }

        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        @Override
        protected void addNotify () {
            mySetKeys();
            
            // listener
            if(propertyListener == null) {
                propertyListener = new PropertyChangeListener () {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(PROP_FILES.equals(evt.getPropertyName())) {
                            mySetKeys();
                        }
                    }
                }; 

                PropertiesDataObject.this.addPropertyChangeListener(
                    WeakListeners.propertyChange(propertyListener, PropertiesDataObject.this));
            }
        }

        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        @Override
        protected void removeNotify () {
            setKeys(new ArrayList<String>());
        }

        /** Creates nodes for specified key. Implements superclass abstract method. */
        protected Node[] createNodes(String entryName) {
            if (entryName == null) {
                return null;
            }
            
            PropertiesFileEntry entry = (PropertiesFileEntry)getPrimaryEntry();
            
            if(entryName.equals(entry.getFile().getName())) {
                return new Node[] {entry.getNodeDelegate()};
            }
            for(Iterator<Entry> it = secondaryEntries().iterator();it.hasNext();) {
                entry = (PropertiesFileEntry)it.next();
                
                if (entryName.equals(entry.getFile().getName())) {
                    return new Node[] {entry.getNodeDelegate()};
                }
            }
                
            return null;
        }

    } // End of class PropertiesChildren.

}
