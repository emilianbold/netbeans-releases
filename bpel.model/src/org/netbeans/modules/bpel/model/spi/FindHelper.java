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

package org.netbeans.modules.bpel.model.spi;

import java.util.Iterator;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;

/**
 * This is helper interface for finding various elements in OM.
 *
 * @author ads
 */
public interface FindHelper {

    /**
     * Returns XPath expression for OM element.
     * 
     * @param entity
     *            BpelEntity object.
     * @return xpath for <code>entity</code>.
     */
    String getXPath( BpelEntity entity );

    /**
     * Returns iterator that iterate over hierarchical scope parents of current
     * element.
     * 
     * @param entity
     *            BpelEntity object.
     * @return iterator for iterate through BaseScope .
     */
    Iterator<BaseScope> scopeIterator( BpelEntity entity );
    
    /**
     * Returns iterator that iterate over hierarchical VariableDeclarationScope
     * parents of current element.
     * 
     * @param entity BpelEntity object.
     * @return iterator for iterate through VariableDeclarationScope .
     */ 
    Iterator<VariableDeclarationScope> varaibleDeclarationScopes( 
            BpelEntity entity );

    /**
     * Returns nearest Activity that enclose this element.
     * 
     * @param entity
     *            BpelEntity object.
     * @return nearest parent activity.
     */
    Activity getParentActivity( BpelEntity entity );

    /**
     * Returns reference to element in model by <code>xpath</code> expression.
     * Runtime exception can be thrown if <code>xpath</code>
     * is bad XPath expression.
     * 
     * @param model
     *            model which will be used for search.
     * @param xpath XPath expression.
     * @return array of found model elements.
     */
    BpelEntity[] findModelElements( BpelModel model, String xpath );
            
}
