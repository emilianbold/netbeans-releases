/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.DTD;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentType extends AbstractTreeDTD implements TreeDTDRoot, Document.Child {
    /** */
    public static final String PROP_ELEMENT_NAME = "elementName"; // NOI18N
    /** */
    public static final String PROP_PUBLIC_ID    = "publicId"; // NOI18N
    /** */
    public static final String PROP_SYSTEM_ID    = "systemId"; // NOI18N
    
    
    /** */
    private String elementName;
    
    /** -- can be null. */
    private String publicId;
    
    /** -- can be null. */
    private String systemId;
    
    private TreeObjectList externalDTDList;
    
    private String internalDTDText;  //!!! it is accesed by introspection, it a hack
    
    //
    // init
    //
    
    /**
     * Creates new TreeDocumentType.
     * @throws InvalidArgumentException
     */
    public TreeDocumentType (String elementName, String publicId, String systemId) throws InvalidArgumentException {
        super ();
        
        checkElementName (elementName);
        checkPublicId (publicId);
        checkSystemId (systemId);
        
        this.elementName = elementName;
        this.publicId    = publicId;
        this.systemId    = systemId;
        
        externalDTDList = new TreeObjectList (createExternalDTDListContentManager ());
    }
    
    
    /** Creates new TreeDocumentType.
     * @throws InvalidArgumentException
     */
    public TreeDocumentType (String elementName) throws InvalidArgumentException {
        this (elementName, null, null);
    }
    
    /** Creates new TreeDocumentType -- copy constructor. */
    protected TreeDocumentType (TreeDocumentType documentType, boolean deep) {
        super (documentType, deep);
        
        this.elementName = documentType.elementName;
        this.publicId    = documentType.publicId;
        this.systemId    = documentType.systemId;
        this.internalDTDText = documentType.internalDTDText;
        
        this.externalDTDList = new TreeObjectList (createExternalDTDListContentManager ());
        if (deep) {
            this.externalDTDList.addAll ((TreeObjectList)documentType.externalDTDList.clone ());
        }
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeDocumentType (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeDocumentType peer = (TreeDocumentType) object;
        if (!!! Util.equals (this.getElementName (), peer.getElementName ()))
            return false;
        if (!!! Util.equals (this.getPublicId (), peer.getPublicId ()))
            return false;
        if (!!! Util.equals (this.getSystemId (), peer.getSystemId ()))
            return false;
        if (!!! Util.equals (this.externalDTDList, peer.externalDTDList))
            return false;
        
        return true;
    }
    
    /*
     * Merges documet root name, publicId and system ID properties.
     * External DTD list merging is delegated.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeDocumentType peer = (TreeDocumentType) treeObject;
        
        setElementNameImpl (peer.getElementName ());
        setPublicIdImpl (peer.getPublicId ());
        setSystemIdImpl (peer.getSystemId ());
        internalDTDText = peer.internalDTDText;
        externalDTDList.merge (peer.externalDTDList);
    }
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
        
        externalDTDList.setReadOnly (newReadOnly);
    }
    
    
    //
    // parent
    //
    
    
    /**
     */
    public boolean hasChildNodes (Class childClass, boolean recursive) {
        Iterator[] its = new Iterator[] {
            getChildNodes ().iterator (),
            externalDTDList.iterator ()
        };
        
        for (int i = 0; i<its.length; i++) {
            Iterator it = its[i];
            while (it.hasNext ()) {
                TreeChild child = (TreeChild)it.next ();
                
                // add matching leaf node
                
                if (childClass == null || childClass.isAssignableFrom (child.getClass ())) {
                    return true;
                }
                
                // do recursive descent into kids
                
                if ( recursive && (child instanceof TreeParentNode) ) {
                    if ( ((TreeParentNode)child).hasChildNodes (childClass, true) == true ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @return copy collection containing references from internal and external part of DTD
     */
    public Collection getChildNodes (Class childClass, boolean recursive) {
        Collection allChildNodes = new LinkedList ();
        
        Iterator[] its = new Iterator[] {
            getChildNodes ().iterator (),
            externalDTDList.iterator ()
        };
        
        for (int i = 0; i<its.length; i++) {
            Iterator it = its[i];
            while (it.hasNext ()) {
                TreeChild child = (TreeChild)it.next ();
                if (childClass == null || childClass.isAssignableFrom (child.getClass ())) {
                    allChildNodes.add (child);
                }
                
                if ( recursive && (child instanceof TreeParentNode) ) {
                    allChildNodes.addAll (((TreeParentNode)child).getChildNodes (childClass, true));
                }
            }
        }
        
        return allChildNodes;
    }
    
    
    //
    // itself
    //
    
    /**
     * Return child list representing external DTD content.
     */
    public final TreeObjectList getExternalDTD () {
        return externalDTDList;
    }
    
    /**
     */
    public final String getElementName () {
        return elementName;
    }
    
    /**
     */
    private final void setElementNameImpl (String newElementName) {
        String oldElementName = this.elementName;
        
        this.elementName = newElementName;
        
        firePropertyChange (PROP_ELEMENT_NAME, oldElementName, newElementName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setElementName (String newElementName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.elementName, newElementName) )
            return;
        checkReadOnly ();
        checkElementName (newElementName);
        
        //
        // set new value
        //
        setElementNameImpl (newElementName);
    }
    
    /**
     */
    protected final void checkElementName (String elementName) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypeElementName (elementName);
    }
    
    /**
     */
    public final String getPublicId () {
        return publicId;
    }
    
    /**
     */
    private final void setPublicIdImpl (String newPublicId) {
        String oldPublicId = this.publicId;
        
        this.publicId = newPublicId;
        
        firePropertyChange (PROP_PUBLIC_ID, oldPublicId, newPublicId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setPublicId (String newPublicId) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.publicId, newPublicId) )
            return;
        checkReadOnly ();
        checkPublicId (newPublicId);
        
        //
        // set new value
        //
        setPublicIdImpl (newPublicId);
    }
    
    /**
     */
    protected final void checkPublicId (String publicId) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypePublicId (publicId);
    }
    
    
    /**
     */
    public final String getSystemId () {
        return systemId;
    }
    
    /**
     */
    private final void setSystemIdImpl (String newSystemId) {
        String oldSystemId = this.systemId;
        
        this.systemId = newSystemId;
        
        firePropertyChange (PROP_SYSTEM_ID, oldSystemId, newSystemId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setSystemId (String newSystemId) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.systemId, newSystemId) )
            return;
        checkReadOnly ();
        checkSystemId (newSystemId);
        
        //
        // set new value
        //
        setSystemIdImpl (newSystemId);
    }
    
    /**
     */
    protected final void checkSystemId (String systemId) throws InvalidArgumentException {
        TreeUtilities.checkDocumentTypeSystemId (systemId);
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    /**
     */
    protected TreeObjectList.ContentManager createExternalDTDListContentManager () {
        return new ExternalDTDContentManager ();
    }
    
    
    /**
     * Internal DTD content manager.
     * All kids use as parent node wrapping TreeDocumentType.
     * All kids must be DocumentType.Child instances.
     */
    protected class ChildListContentManager extends AbstractTreeDTD.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDocumentType.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DocumentType.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
    
    /**
     * External DTD content manager (assigned to externalDTDList).
     * All kids use as parent node wrapping TreeDocumentType.
     * All kids must be DTD.Child instances.
     */
    protected class ExternalDTDContentManager extends TreeParentNode.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDocumentType.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DTD.Child.class, obj);
        }
        
    }
    
}
