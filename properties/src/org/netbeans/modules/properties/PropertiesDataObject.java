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
    
    /** MIME type for properties. */
    public static final String MIME_PROPERTIES = "text/x-properties"; // NOI18N

    /** Structural view of the dataobject */
    private transient BundleStructure bundleStructure;
    
    /** Open support for this data object. Provides editable table view on bundle. */
    private transient PropertiesOpen openSupport;

    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 4795737295255253334L;


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
    
    // PENDING very ugly, has to be revised.
    /** Hack to removeSecondaryEntry(MultiDataObject.Entry) method. */
    void removeSecondaryEntryHack(MultiDataObject.Entry entry) {
        super.removeSecondaryEntry(entry);
    }

    /** Deletes all secondary entries and then deletes the primary entry. Overrides superclass method. */
    protected void handleDelete() throws IOException {
        Iterator it = secondaryEntries().iterator();
        while(it.hasNext()) {
            ((PropertiesFileEntry)it.next()).delete();
        }

        getPrimaryEntry().delete();
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

    /** Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (PropertiesDataObject.class);
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
    class PropertiesChildren extends Children.Keys {

        /** Listens to changes on the dataobject */
        private PropertyChangeListener pcl = null;

        
        /** Constructor.*/
        PropertiesChildren() {
            super();
        }

        
        /** Sets all keys in the correct order */
        protected void mySetKeys() {
            TreeSet ts = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (o1 == o2)
                        return 0;
                    if (o1 instanceof MultiDataObject.Entry && o2 instanceof MultiDataObject.Entry)
                        return getSecondaryFilesComparator().compare(((MultiDataObject.Entry)o1).getFile().getName(),
                            ((MultiDataObject.Entry)o2).getFile().getName());
                    else
                        return 0;
                }
            });

            ts.add(getPrimaryEntry());
            
            for (Iterator it = secondaryEntries().iterator();it.hasNext();) {
                FileEntry fe = (FileEntry)it.next();
                ts.add(fe);
            }

            setKeys(ts);
        }

        /** Called to notify that the children has been asked for children
         * after and that they should set its keys.
         */
        protected void addNotify () {
            mySetKeys();
            
            // listener
            if(pcl == null) {
                pcl = new PropertyChangeListener () {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(PROP_FILES.equals(evt.getPropertyName())) {
                            mySetKeys();
                        }
                    }
                }; 

                PropertiesDataObject.this.addPropertyChangeListener(WeakListener.propertyChange(pcl, PropertiesDataObject.this));
            }
        }

        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes).
         */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }

        /** Creates nodes. */
        protected Node[] createNodes (Object key) {
            return new Node[] { ((PropertiesFileEntry)key).getNodeDelegate() };
        }

    } // End of inner class PropertiesChildren.

}
