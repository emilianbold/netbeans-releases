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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import java.util.HashMap;

import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Marker interface. Any element in OM will implement this interface. One should
 * use for keep reference to OM model element use BpelEement instead of Object.
 * 
 * @author ads
 */
public interface BpelEntity extends DocumentComponent<BpelEntity> {

    /**
     * Namespace uri for current BPEL spec.
     */
    String BUSINESS_PROCESS_NS_URI = 
        "http://docs.oasis-open.org/wsbpel/2.0/process/executable";     // NOI18N
    
    String BUSINESS_PROCESS_DRAFT_NS_URI =
        "http://schemas.xmlsoap.org/ws/2004/03/business-process/";      // NOI18N
    
    String BUSINESS_PROCESS_1_1_NS_URI =
        "http://schemas.xmlsoap.org/ws/2003/03/business-process/";      // NOI18N
    
    /** {@inheritDoc} */
    BpelContainer getParent();

    /**
     * Accessor to model. It should be used for getting reference to model
     * instead of acccessing to root process ( process could be got from Model ).
     * 
     * Note that this method returns always not null reference to model,
     * even if component is deleted. It differs from getModel() method
     * which return null in this case. 
     * @return bpel OM model.
     */
    BpelModel getBpelModel();

    /**
     * @return interface in OM that represented by this entity.
     */
    Class<? extends BpelEntity> getElementType();

    /**
     * Creates deep copy of OM element. <code>uniqueMap</code> is used for
     * storing correspondance between this element and return element. Uid of
     * this copied element ( end each deep child ) will be null. After attaching
     * element to OM it will have different uid . Cookies are not copied for
     * copy element ( and any child ).
     * 
     * @param uniqueMap
     *            map that will be used for filling unique ids.
     * @return copy of this entity.
     */
    BpelEntity copy( HashMap<UniqueId, UniqueId> uniqueMap );

    /**
     * Returns element that is copy of this element. This element and all its
     * children will have the same unique ids. Original element ( this ) is
     * removed from tree. Uid of this element ( and each child ) will be null.
     * After pasting element into OM pasted element ( and its children
     * respectively ) will have the same uid as original. After pasting element
     * will be invalid and if you will use it for new pasting then it will be
     * the same as pasting of copy ( see copy method ). All cookies from
     * original element will be saved for cut element ( and any child ).
     * 
     * @return copy of this entity that could be used for inserting in OM.
     */
    BpelEntity cut();

    /**
     * Returns unique id of element in OM. This uid will be null if element was
     * created via factory ( BpelElementBuilder class ). Element could have not
     * null uid even if it not in OM. This will be in case when it was copied or
     * cut.
     * 
     * @return unique id of this entity.
     */
    UniqueId getUID();

    /**
     * This method will apply visitor to each element in model starting from
     * this. <code>visitor</code> should not know nothing about OM tree
     * structure. It means it should not perfrom navigation through children 
     * in "visit" method. Om element "accept" method will apply visitor 
     * to any child itself. 
     * 
     * @param visitor
     *            visitor for OM.
     */
    void accept( SimpleBpelModelVisitor visitor );
    
    /**
     * This method will apply visitor to <code>this</code> OM element.
     * <code>visitor</code> should perform navigation in tree ( if it wants by
     * itself ).
     * @param visitor visitor for OM.
     */
    void accept( BpelModelVisitor visitor );
    
    /**
     * Returns ExNamespaceContext context for this element. This context can be
     * used for retrieving prefixes by uri, uri by prefixes.
     * 
     * @return namespace context object.
     */
    ExNamespaceContext getNamespaceContext();

    /**
     * Returns stored object with given <code>key</code> that was previusly
     * put into this entity via setCookie.
     * 
     * @param key
     *            key for cookie retrieveing
     * @return object that stored as cookie identified by <code>key</code>.
     */
    Object getCookie( Object key );

    /**
     * Set cookie for this entity.
     * 
     * @param key
     *            key for cookie.
     * @param obj
     *            any object that we want to store here.
     */
    void setCookie( Object key, Object obj );

    /**
     * Removes object with given <code>key</code> from cookies.
     * 
     * @param key
     *            key of object that we want to remove from cookies.
     */
    void removeCookie( Object key );
    
}
