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
package org.netbeans.tax;

import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNotationDecl extends TreeNodeDecl implements DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    /** */
    public static final String PROP_NAME      = "name"; // NOI18N
    /** */
    public static final String PROP_PUBLIC_ID = "publicId"; // NOI18N
    /** */
    public static final String PROP_SYSTEM_ID = "systemId"; // NOI18N
    
    
    /** */
    private String name;
    
    /** -- can be null. */
    private String systemId;
    
    /** -- can be null. */
    private String publicId;
    
    
    //
    // init
    //
    
    /** Creates new TreeNotationDecl.
     * @throws InvalidArgumentException
     */
    public TreeNotationDecl (String name, String publicId, String systemId) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        this.name     = name;
        
        checkPublicId (publicId);
        checkSystemId (systemId);
        checkExternalId (publicId, systemId);
        this.systemId = systemId;
        this.publicId = publicId;
    }
    
    
    /** Creates new TreeNotationDecl -- copy constructor. */
    protected TreeNotationDecl (TreeNotationDecl notationDecl) {
        super (notationDecl);
        
        this.name     = notationDecl.name;
        this.publicId = notationDecl.publicId;
        this.systemId = notationDecl.systemId;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeNotationDecl (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeNotationDecl peer = (TreeNotationDecl) object;
        if (!!! Util.equals (this.getName (), peer.getName ())) {
            return false;
        }
        if (!!! Util.equals (this.getSystemId (), peer.getSystemId ())) {
            return false;
        }
        if (!!! Util.equals (this.getPublicId (), peer.getPublicId ())) {
            return false;
        }
        
        return true;
    }
    
    /*
     * Merges properties: name, system ID and public ID.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeNotationDecl peer = (TreeNotationDecl) treeObject;
        
        setNameImpl (peer.getName ());
        setSystemIdImpl (peer.getSystemId ());
        setPublicIdImpl (peer.getPublicId ());
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public String getName () {
        return name;
    }
    
    /**
     */
    private final void setNameImpl (String newName) {
        String oldName = this.name;
        
        this.name = newName;
        
        firePropertyChange (PROP_NAME, oldName, newName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setName (String newName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.name, newName) )
            return;
        checkReadOnly ();
        checkName (newName);
        
        //
        // set new value
        //
        setNameImpl (newName);
    }
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkNotationDeclName (name);
    }
    
    /**
     */
    public String getPublicId () {
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
        checkExternalId (newPublicId, this.systemId);
        
        //
        // set new value
        //
        setPublicIdImpl (newPublicId);
    }
    
    /**
     */
    protected final void checkPublicId (String publicId) throws InvalidArgumentException {
        TreeUtilities.checkNotationDeclPublicId (publicId);
    }
    
    /**
     */
    public String getSystemId () {
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
        checkExternalId (this.publicId, newSystemId);
        
        //
        // set new value
        //
        setSystemIdImpl (newSystemId);
    }
    
    /**
     */
    protected final void checkSystemId (String systemId) throws InvalidArgumentException {
        TreeUtilities.checkNotationDeclSystemId (systemId);
    }
    
    
    /**
     */
    protected final void checkExternalId (String publicId, String systemId) throws InvalidArgumentException {
        if ( (publicId == null) && (systemId == null) ) {
            throw new InvalidArgumentException (Util.THIS.getString ("EXC_invalid_nulls"),
            new NullPointerException ());
        }
    }
    
}
