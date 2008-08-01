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
package org.netbeans.modules.xslt.model.impl;


import java.util.LinkedHashSet;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.model.QualifiedNameable;
import org.netbeans.modules.xslt.model.ReferenceableXslComponent;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.XslReference;


/**
 * 
 * @author ads
 *
 */
class GlobalReferenceImpl<T extends QualifiedNameable> extends 
    AbstractReference<T> implements
        XslReference<T>
{
    private static final Logger LOGGER = Logger.getLogger(GlobalReferenceImpl.class.getName());
    
    // used by XslComponentImpl#createReferenceTo method
    GlobalReferenceImpl( T referenced, Class<T> referencedType, 
            XslComponentImpl parent ) 
    {
        super(referenced, referencedType, parent);
    }

    // used by resolve method
    GlobalReferenceImpl( Class<T> referencedType, 
            XslComponentImpl parent, String ref ) 
    {
        super(referencedType, parent, ref);
        initReferenceString( ref );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Reference#get()
     */
    public T get() {
        if (getReferenced() == null) {
            setReferenced( find() );
        }
        return super.getReferenced();
    }
    
    @Override
    public String getRefString() {
        if (refString == null) {
            T referenced = super.getReferenced();
            assert referenced != null;
            myQname = referenced.getName();
            if ( myQname != null ) {
                String result;
                myPrefix = myQname.getPrefix();
                myLocalPart = myQname.getLocalPart();
                if ( myPrefix == null || myPrefix.length() == 0 ) {
                    result = myLocalPart;
                }
                else {
                    result = myPrefix + ":" +myLocalPart; 
                }
                
                if ( getParent().isInDocumentModel() ) {
                    refString = result;
                }
                else {
                    return result;
                }
            }
        }
        return super.getRefString();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslReference#getQName()
     */
    public QName getQName() {
        checkParentNotRemovedFromModel();
        // don't call isBroken here so as it could be looped through find()

        if ( myQname == null) {
            T referenced = super.getReferenced();
            if (referenced != null) {
                myQname = referenced.getName();
            } else {
                LOGGER.log(Level.INFO,"MSG_InfoReferenceIsNotResolved");
            }
         }
         return myQname;
    }
    
    @Override
    public boolean equals( Object obj )
    {
        if  (this == obj) {
            return true;
        }
        else if ( !( obj instanceof GlobalReferenceImpl )) {
            return false;
        }
        else {
            GlobalReferenceImpl<? extends ReferenceableXslComponent> ref = 
                (GlobalReferenceImpl<? extends ReferenceableXslComponent>) obj;
            return getParent().equals( ref.getParent() )&&
                getQName().equals( ref.getQName() );
        }
    }

    @Override
    public int hashCode()
    {
        return getParent().hashCode();
    }
    
    @Override
    public XslComponentImpl getParent() {
        return (XslComponentImpl) super.getParent();
    }
    
    @Override
    public boolean references(T target) {
        if ( target instanceof QualifiedNameable ) {
            QualifiedNameable nameble = (QualifiedNameable) target;
            return nameble.getName()!= null &&  nameble.getName().equals(
                    getQName() ) && !isBroken() && get() == target;
        }
        return super.references(target);
    }
    
    public void refresh() {
        getRefString();
        setReferenced(null);
    }
    
    protected T getReferenced() {
        if (super.getReferenced() == null) {
            checkParentPartOfModel();
        } else {
            if (super.getParent().getModel() == null) {
                throw new IllegalStateException(
                        "Referencing component has been removed from model."); //NOI18N
            }
            if (super.getReferenced().getModel() == null) {
                throw new IllegalStateException(
                        "Referenced component has been removed from model."); //NOI18N
            }
        }
        return super.getReferenced();
    }
    
    /**
     * @exception IllegalStateException if parent is already removed from a model.
     */
    protected void checkParentNotRemovedFromModel() {
        if (getParent().getModel() == null) {
            throw new IllegalStateException(
                    "Referencing component has been removed from model."); // NOI18N
        }
    }
    
    /**
     * @exception IllegalStateException if parent is not part of a model.
     */
    protected void checkParentPartOfModel() {
        if (! getParent().isInDocumentModel()) {
            throw new IllegalStateException(
                    "Referencing component is not part of model."); //NOI18N
        }
    }
    
    /**
     * Calculate the QName based on the local information
     * without loading the referenced object.
     */
     protected QName calculateQNameLocally() {
        String prefix = getPrefix();
        String localPart = getLocalPart();
        String namespace = null;
        namespace = getParent().lookupNamespaceURI(prefix);
        if (namespace == null) {
            // prefix part is namespace name, which could be the namespace uri
            // itself
            String temp = getParent().lookupPrefix(prefix);
            if (temp != null) {
                prefix = temp;
                namespace = prefix;
            }
        }

        if (prefix == null) {
            return new QName(namespace, localPart);
        }
        else {
            return new QName(namespace, localPart, prefix);
        }
     }
     
     private String getLocalPart() {
         if ( myLocalPart == null ) {
             String ref = getRefString();
             if ( ref == null ) {
                 return null;
             }
             String[] parts = ref.split(":"); //NOI18N
             if (parts.length == 2) {
                 return parts[1];
             }
             else {
                 return parts[0];
             }
         }
         else {
             return myLocalPart;
         }
     }
     
     private String getPrefix() {
         if ( myPrefix == null ) {
             String ref = getRefString();
             if ( ref == null ) {
                 return null;
             }
             String[] parts = ref.split(":"); //NOI18N
             if (parts.length == 2) {
                 return parts[0];
             }
             else {
                 return null;
             }
         }
         else {
             return myPrefix;
         }
     }
     
     private void initReferenceString( String ref ) {
         assert ref!=null;
         refString = ref;
         String[] parts = refString.split(":"); //NOI18N
         if (parts.length == 2) {
             myPrefix = parts[0];
             myLocalPart = parts[1];
         } else {
             myPrefix = null;
             myLocalPart = parts[0];
         }
     }
     
     private T find() {
         LinkedHashSet<XslModel> list = Utilities.getAvailibleModels( 
                 getParent().getModel());
         for (XslModel model : list) {
            if ( Model.State.VALID.equals( model.getState() )) {
                Stylesheet stylesheet = model.getStylesheet();
                if ( stylesheet == null ) {
                    continue;
                }
                List<T> children = stylesheet.getChildren( getType() );
                T result = find( children );
                if ( result != null ) {
                    return result;
                }
            }
         }
         return null;
     }
     
     private T find( List<T> children ) {
         for (T t : children) {
                QName name = t.getName();
                if ( name == null ) {
                    continue;
                }
                String localPart = name.getLocalPart();
                String ns = name.getNamespaceURI();
                if ( getLocalPart().equals( localPart)  
                    /*&& Utilities.equals( getQName().getNamespaceURI() , ns )*/ )
                {   
                    QName thisQName = getQName();
                    if (thisQName == null) {// perhaps element is not resolved yet
                        thisQName = calculateQNameLocally();
                    }
                    if (thisQName != null && Utilities.equals( thisQName.getNamespaceURI() , ns )) {
                        return t;
                    }
                }
        }
        return null;
     }

    private QName myQname;
     
    private String myPrefix;
     
    private String myLocalPart;

}
