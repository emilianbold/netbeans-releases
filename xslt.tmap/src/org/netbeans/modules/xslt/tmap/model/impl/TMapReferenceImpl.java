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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.MappedReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapReferenceImpl<T extends TMapReferenceable>
    extends AbstractReference<T>  implements TMapReference<T>, MappedReference
{

    private Attribute myAttribute;
    private TMapReferenceBuilder.TMapResolver myResolver;
    private boolean isResolved;

    TMapReferenceImpl( T target , Class<T> type , AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super( type , parent , value );
        setReferenced( target );
        myResolver = resolver;
        if ( target!= null ){
            setResolved();
        }
    }
    
    TMapReferenceImpl( Class<T> type , AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super( type , parent , value );
        myResolver = resolver;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Reference#get()
     */
    public T get() {
        if ( getReferenced() == null ){
            T ret = myResolver.resolve( this );
            setReferenced( ret );
            return ret;
        }
        return getReferenced();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.references.Reference#isResolved()
     */
    public boolean isResolved() {
        return isResolved;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractReference#references(T)
     */
    @Override
    public boolean references( T component )
    {
        if ( !myResolver.haveRefString( this , component ) ) {
            return false;
        }
        return super.references(component);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.api.MappedReference#getAttribute()
     */
    public Attribute getAttribute() {
        return myAttribute;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.api.MappedReference#setAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
     */
    public void setAttribute( Attribute attr ) {
        myAttribute = attr;
    }

    protected void setResolved(){
        isResolved = true;
    }
    
    @Override
    protected void setReferenced(T referenced) {
        super.setReferenced( referenced );
        if ( referenced != null ){
            setResolved();
        }
    }
}
