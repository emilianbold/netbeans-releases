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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.ReferenceableXslComponent;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.XslReference;


/**
 * This is custom resolver for AttributeSet reference list.
 * 
 * @author ads
 *
 */
class AttributeSetReferenceListResolver implements
        ReferenceListResolveFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.ReferenceListResolveFactory#isApplicable(java.lang.Class)
     */
    public boolean isApplicable( Class referenceType ) {
        return AttributeSet.class.isAssignableFrom( referenceType );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.ReferenceListResolveFactory#resolve(org.netbeans.modules.xslt.model.impl.XslComponentImpl, java.lang.Class, java.lang.String)
     */
    public <T extends ReferenceableXslComponent> List<XslReference<T>> resolve(
            AttributeAccess access, Class<T> clazz, String value )
    {
        StringTokenizer tokenizer = new StringTokenizer(value, " ");
        List<Reference<T>> references = new LinkedList<Reference<T>>();
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            Collection<Reference<T>> collection = find( clazz , next , access );
            references.addAll( collection );
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends ReferenceableXslComponent> Collection<Reference<T>> 
        find( Class<T> clazz, String next , AttributeAccess access) 
    {
        assert AttributeSet.class.isAssignableFrom( clazz );
        LinkedHashSet<XslModel> list = Utilities.getAvailibleModels( 
                access.getComponent().getModel());
        Collection<Reference<T>> collection = new LinkedList<Reference<T>>();
        QName qName = getQName( next , access );
        for (XslModel model : list) {
           if ( Model.State.VALID.equals( model.getState() )) {
               Stylesheet stylesheet = model.getStylesheet();
               if ( stylesheet == null ) {
                   continue;
               }
               List<AttributeSet> children = 
                   stylesheet.getChildren( AttributeSet.class );
               Collection<AttributeSet> result = find( children , qName );
               for (AttributeSet set : result) {
                   XslReference<AttributeSet> ref = 
                       new GlobalReferenceImpl<AttributeSet>( set , 
                           AttributeSet.class , access.getComponent() );
                   if ( result != null ) {
                       collection.add( (Reference<T>) ref );
                   } 
               }
           }
        }
        return null;
    }
    
    private QName getQName( String value, AttributeAccess access ) {
        assert value!=null;
        String[] parts = value.split(":"); //NOI18N
        String prefix = null;
        String localPart;
        if (parts.length == 2) {
            prefix = parts[0];
            localPart = parts[1];
        } else {
            localPart = parts[0];
        }
        String ns = access.getComponent().lookupNamespaceURI(prefix);
        return new QName( ns , localPart , prefix );
    }

    private Collection<AttributeSet> find( 
            List<AttributeSet> children , QName qName) 
    {
        Collection<AttributeSet> collection = new LinkedList<AttributeSet>();
        assert qName != null;
        for (AttributeSet set : children) {
               QName name = set.getName();
               if ( name == null ) {
                   continue;
               }
               String localPart = name.getLocalPart();
               String ns = name.getNamespaceURI();
               if ( qName.getLocalPart().equals( localPart)  
                   && Utilities.equals( qName.getNamespaceURI() , ns ) )
               {
                   collection.add( set );
               }
       }
       return collection;
    }

}
