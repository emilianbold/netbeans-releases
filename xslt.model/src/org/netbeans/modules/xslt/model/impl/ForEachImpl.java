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

import java.util.List;

import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ForEachImpl extends SequenceElementConstructorImpl implements ForEach {

    ForEachImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    ForEachImpl( XslModelImpl model ){
        super( model , XslElements.FOR_EACH );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept( XslVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return ForEach.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ForEach#addSort(org.netbeans.modules.xslt.model.Sort, int)
     */
    public void addSort( Sort sort, int position ) {
        insertAtIndex( SORT_PROPERTY , sort, position );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ForEach#appendSort(org.netbeans.modules.xslt.model.Sort)
     */
    public void appendSort( Sort sort ) {
        addBefore( SORT_PROPERTY , sort , SEQUENCE_ELEMENTS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ForEach#getSorts()
     */
    public List<Sort> getSorts() {
        return getChildren( Sort.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ForEach#removeSort(org.netbeans.modules.xslt.model.Sort)
     */
    public void removeSort( Sort sort ) {
        removeChild( SORT_PROPERTY,  sort );
    }
    
}
