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
package org.netbeans.modules.bpel.model.spi;

import java.util.Set;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public interface EntityFactory {

    /**
     * Checks either <code>namespaceUri</code> is applicable for this factory.
     * @param namespaceUri Namespace for check.
     * @return true if this factory is applicable.
     */
    boolean isApplicable( String namespaceUri );

    /**
     * @return Set with qNames of elements that this factory supports. 
     */
    Set<QName> getElementQNames();
    
    /**
     * Creates new entity for specified <code>element</code> inside parent 
     * <code>container</code>.
     * This method is used inside OM when it parses XML.
     * @param container Parent object.
     * @param element DOM low level element. 
     * @return instantiated model entity.
     */
    BpelEntity create( BpelContainer container , Element element);

    /**
     * Creates new entity for specified <code>element</code> inside parent 
     * <code>container</code>.
     * This method is used inside OM when it parses XML.
     * @param container Parent object.
     * @param element DOM low level element. 
     * @return instantiated model entity.
     */
    BpelEntity create( BpelContainer container , Element element, String namespaceURI);
    
    /**
     * Creates unattached OM entity with specified <code>clazz</code>. 
     * This method used by builder for creation element by client.
     * @param <T> Class OM entity.
     * @param clazz Type for entity that will be created.
     * @return instantiated model entity.
     */
    <T extends BpelEntity> T create(BpelBuilderImpl builder, Class<T> clazz );
    
    boolean canExtend(ExtensibleElements extensible, Class<? extends BpelEntity> extensionType);
    
}
