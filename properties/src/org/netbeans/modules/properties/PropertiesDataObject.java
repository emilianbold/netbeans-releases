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
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.openide.*;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;


/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/
public final class PropertiesDataObject extends MultiDataObject {
    
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 4795737295255253334L;

    /** Icon base for the <code>PropertiesDataNode</code> node. */
    static final String PROPERTIES_ICON_BASE = "org/netbeans/modules/properties/propertiesObject"; // NOI18N
    
    /** Icon bas2 for the <code>PropertiesDataNode</code> node. */
    static final String PROPERTIES_ICON_BASE2 = "org/netbeans/modules/properties/propertiesLocale"; // NOI18N
    
    /** MIME type for properties. */
    public static final String MIME_PROPERTIES = "text/x-properties"; // NOI18N

    /** Structural view of the dataobject */
    protected transient BundleStructure bundleStructure;


    /** Constructor. */
    public PropertiesDataObject (final FileObject obj, final MultiFileLoader loader)
    throws DataObjectExistsException {
        super(obj, loader);
        // use editor support
        initialize();
    }

    
    /** Initializes the object. Used by construction and deserialized. */
    private void initialize() {
        bundleStructure = null;
        getCookieSet().add(new PropertiesOpen(this));
        getCookieSet().add(((PropertiesFileEntry)getPrimaryEntry()).getPropertiesEditor());
    }

    /** Returns the support object for JTable-editing. Should be used by all subentries as well */
    public PropertiesOpen getOpenSupport () {
        return (PropertiesOpen)getCookie(OpenCookie.class);
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
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx (PropertiesDataObject.class);
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
            pcl = new PropertyChangeListener () {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PROP_FILES)) {
                        mySetKeys();
                    }
                }
            }; 

            PropertiesDataObject.this.addPropertyChangeListener (new WeakListener.PropertyChange(pcl));
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