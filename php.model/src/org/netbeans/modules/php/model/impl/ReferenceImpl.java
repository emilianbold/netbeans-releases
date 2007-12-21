/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.php.model.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.refs.ReferenceResolver;
import org.openide.util.Lookup;


/**
 * Reference implementation for classes and interfaces identifiers.
 * 
 * @author ads
 *
 */
class ReferenceImpl<T extends SourceElement> implements Reference<T> {

    /**
     * @param source element from which reference should be resolved  
     * ( <code>source</code> element has this reference ) 
     */
    ReferenceImpl( SourceElementImpl source, String identifier , 
            Class<T> clazz )
    {
        mySource = source;
        myIdentifier = identifier;
        myClass = clazz;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Reference#get()
     */
    public T get() {
        List<ReferenceResolver> resolvers = getResolvers();
        assert resolvers != null;
        List<T> result = null;
        for (ReferenceResolver resolver : resolvers) {
            List<T> found = resolver.resolve( getSource(), getIdentifier(), 
                    getType(), true );
            if ( found != null && found.size() >0 ){
                result = add( result , found );
            }
        }
            
        if ( result != null && result.size() >0 ) {
            return result.get( result.size() -1 );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Reference#getIdentifier()
     */
    public String getIdentifier() {
        return myIdentifier;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Reference#getSource()
     */
    public SourceElement getSource() {
        return mySource;
    }
    
    protected <S extends SourceElement> List<ReferenceResolver> getResolvers( 
            Class<S> clazz) 
    {
        List<ReferenceResolver> result = myResovers.get( clazz);
        if ( result != null ) {
            return result;
        }
        Collection<? extends ReferenceResolver> collection= 
            Lookup.getDefault().lookupAll( ReferenceResolver.class );
        for ( ReferenceResolver resolver : collection) {
            if ( resolver.isApplicable( clazz )) {
                result = add( result , resolver );
            }
        }
        myResovers.put( clazz, result );
        return result;
    }
    
    private List<T> add( List<T> list , List<T> elements ){
        if ( list == null ){
            list = new LinkedList<T>();
        }
        list.addAll( elements );
        return list;
    }
    
    private List<ReferenceResolver> add( List<ReferenceResolver> list, 
            ReferenceResolver resolver )
    {
        if ( list == null ){
            list = new LinkedList<ReferenceResolver>();
        }
        list.add( resolver );
        return list;
    }

    
    private Class<T> getType(){
        return myClass;
    }
    
    private List<ReferenceResolver> getResolvers() {

        return getResolvers( getType() );
    }
    
    private String myIdentifier;
    
    private Class<T> myClass;
    
    private SourceElementImpl mySource;
    
    private Map<Class<? extends SourceElement>, List<ReferenceResolver>> myResovers = 
        new HashMap<Class<? extends SourceElement>, List<ReferenceResolver>>();

}
