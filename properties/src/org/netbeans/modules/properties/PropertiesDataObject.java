/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

import org.openide.cookies.CompilerCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;


/** 
 * Object that provides main functionality for properties data loader.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 * Represents set of .properties files with same basic name (name without locale postfix).
 *
 * @author Ian Formanek
 */
public final class PropertiesDataObject extends MultiDataObject implements CookieSet.Factory {

    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 4795737295255253334L;
    
    /** MIME type for properties. */
    public static final String MIME_PROPERTIES = "text/x-properties"; // NOI18N

    /** Structural view of the dataobject */
    private transient BundleStructure bundleStructure;
    
    /** Open support for this data object. Provides editable table view on bundle. */
    private transient PropertiesOpen openSupport;

    // Hack due having lock on secondaries, can't override handleCopy, handleMove at all.
    /** Suffix used by copying/moving dataObject. */
    private transient String pasteSuffix;


    /** Constructor. */
    public PropertiesDataObject (final FileObject obj, final MultiFileLoader loader) throws DataObjectExistsException {
        super(obj, loader);
        // use editor support
        initialize();
    }

    
    /** Initializes the object. Used by construction and deserialized. */
    private void initialize() {
        bundleStructure = null;
        
        getCookieSet().add(new Class[] {PropertiesOpen.class, PropertiesEditorSupport.class}, this);
    }

    /** Implements <code>CookieSet.Factory</code> interface method. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(PropertiesOpen.class)) {
            return getOpenSupport();
        } else if(clazz.isAssignableFrom(PropertiesEditorSupport.class)) {
            return ((PropertiesFileEntry)getPrimaryEntry()).getPropertiesEditor();
        } else
            return null;
    }
    
    /** Copies primary and secondary files to new folder.
     * Overrides superclass method.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy */
    protected synchronized DataObject handleCopy(DataFolder df) throws IOException {
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
    protected FileObject handleMove(DataFolder df) throws IOException {
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
    
    /** Creates new name for this instance when moving/copying to new folder destination. 
     * @param folder new folder destination. */
    private String createPasteSuffix(DataFolder folder) {
        String basicName = getPrimaryFile().getName();

        DataObject[] children = folder.getChildren();
        
        
        // Repeat until there is not such file name.
        for(int i = 0; ; i++) {
            String newName;
            
            if(i == 0)
                newName = basicName;
            else
                newName = basicName + i;
            
            boolean exist = false;
            
            for(int j = 0; j < children.length; j++) {
                if(children[j] instanceof PropertiesDataObject && newName.equals(children[j].getName())) {
                    exist = true;
                    break;
                }
            }
                
            if(!exist) {
                if(i == 0)
                    return ""; // NOI18N
                else
                    return "" + i; // NOI18N
            }
        }
    }

    /** Returns open support. It's used by all subentries as open support too. */
    public PropertiesOpen getOpenSupport() {
        if(openSupport == null) {
            synchronized(this) {
                if(openSupport == null)
                    openSupport = new PropertiesOpen(this);
            }
        }
        
        return openSupport;
    }

    /** Updates modification status of this dataobject from its entries. */
    void updateModificationStatus() {
        boolean modif = false;
        if (((PresentableFileEntry)getPrimaryEntry()).isModified())
            modif = true;
        else {
            for (Iterator it = secondaryEntries().iterator(); it.hasNext(); )
                if (((PresentableFileEntry)it.next()).isModified()) {
                    modif = true;
                    break;
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

    /** Gets help context for this object. Overrides superclass method. 
     * @return help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(Util.HELP_ID_PROPERTIES);
    }

    /** Comparator used for ordering secondary files, works over file names */
    public static Comparator getSecondaryFilesComparator() {
        return new KeyComparator();
    }

    /** Deserialization. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initialize();
    }

    
    /** <code>Children</code> for <code>PropertiesDataNode</code>. */
    private class PropertiesChildren extends Children.Keys {

        /** Listens to changes on the dataobject */
        private PropertyChangeListener propertyListener = null;

        
        /** Constructor.*/
        PropertiesChildren() {
            super();
        }

        
        /** Sets all keys in the correct order */
        protected void mySetKeys() {
            TreeSet newKeys = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    
                    if(o1 == null)
                        return -1;
                    
                    if(o2 == null)
                        return 1;
                    
                    if(o1 instanceof String) {
                        return ((String)o1).compareTo((String)o2);
                    }
                    
                    return -1;
                }
            });

            newKeys.add(getPrimaryEntry().getFile().getName());
            
            for(Iterator it = secondaryEntries().iterator();it.hasNext();) {
                FileEntry fe = (FileEntry)it.next();
                newKeys.add(fe.getFile().getName());
            }

            setKeys(newKeys);
        }

        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
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
                    WeakListener.propertyChange(propertyListener, PropertiesDataObject.this));
            }
        }

        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }

        /** Creates nodes for specified key. Implements superclass abstract method. */
        protected Node[] createNodes(Object key) {
            if(key == null)
                return null;
            
            if(!(key instanceof String))
                return null;
            
            String entryName = (String)key;
            
            PropertiesFileEntry entry = (PropertiesFileEntry)getPrimaryEntry();
            
            if(entryName.equals(entry.getFile().getName()))
                return new Node[] {entry.getNodeDelegate()};
            
            for(Iterator it = secondaryEntries().iterator();it.hasNext();) {
                entry = (PropertiesFileEntry)it.next();
                
                if(entryName.equals(entry.getFile().getName()))
                    return new Node[] {entry.getNodeDelegate()};
            }
                
            return null;
        }

    } // End of class PropertiesChildren.

}
