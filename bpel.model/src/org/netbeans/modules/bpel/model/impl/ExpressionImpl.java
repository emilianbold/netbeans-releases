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
package org.netbeans.modules.bpel.model.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public abstract class ExpressionImpl extends BpelEntityImpl implements Expression {


    ExpressionImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }


    ExpressionImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#getContent()
     */
    public String getContent() {
        return getCorrectedText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#setContent(java.lang.String)
     */
    public void setContent( String content ) throws VetoException {
        setText( content );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#getExpressionLanguage()
     */
    public String getExpressionLanguage() {
        return getAttribute( BpelAttributes.EXPRESSION_LANGUAGE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#setExpressionLanguage(java.lang.String)
     */
    public void setExpressionLanguage( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.EXPRESSION_LANGUAGE , value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExpressionLanguageSpec#removeExpressionLanguage()
     */
    public void removeExpressionLanguage(){
        removeAttribute( BpelAttributes.EXPRESSION_LANGUAGE );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret= new Attribute[]{ BpelAttributes.CONTENT , 
                    BpelAttributes.EXPRESSION_LANGUAGE };
            myAttributes.compareAndSet( null, ret );
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
    
    @Override
    public String toString() {
        return getContent().trim();
    }
    
}
