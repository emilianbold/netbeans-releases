/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.api.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class Type extends RepeatableSchemaConstruct {
        
    public static final short FLAVOR_SEQUENCE               = 1;
    public static final short FLAVOR_PRIMITIVE_ENUMERATION  = 2;
    public static final short FLAVOR_PRIMITIVE              = 3;
    
    private short flavor;
    private String javaTypeName;
    
    private List<SchemaConstruct> subconstructs;
    
    /** Creates a new instance of Type */
    public Type() {
        super( SchemaConstruct.ConstructType.TYPE );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        subconstructs = new ArrayList();
    }
    
    public Type( QName name ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        
        subconstructs = new ArrayList();
    }
    
    public Type( QName name, short flavor ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        
        subconstructs = new ArrayList();
        
        this.flavor = flavor;
    }
    
    public Type( QName name, short flavor, int minOccurs, int maxOccurs ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        subconstructs = new ArrayList();
        
        this.flavor = flavor;
        setMinOccurs( minOccurs );
        setMaxOccurs( maxOccurs );
    }
    
    public void setFlavor( short flavor ) {
        this.flavor = flavor;
    }
    
    public short getFlavor() {
        return flavor;
    }
    
    public void setJavaTypeName( String javaTypeName ) {
        this.javaTypeName = javaTypeName;
    }
    
    public String getJavaTypeName() {
        return javaTypeName;
    }
    
    public void addSubconstruct( SchemaConstruct subconstruct ) {
        subconstructs.add( subconstruct );
    }
    
    public List<SchemaConstruct> getSubconstructs() {
        return Collections.unmodifiableList( subconstructs );
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "name='" + getName() + "' ");
        sb.append( "type='" );
        if( FLAVOR_PRIMITIVE == flavor ) {
            sb.append( "primitive" );
        } else if( FLAVOR_SEQUENCE == flavor ) {
            sb.append( "sequence" );
        }
        sb.append( "' name='" + getName());
        sb.append( "' minOccurs='" + getMinOccurs() + "'" );
        if( getMaxOccurs() ==  RepeatableSchemaConstruct.UNBOUNDED ) {
            sb.append( " maxOccurs='unbounded'" );
        } else {
            sb.append( " maxOccurs='" + getMaxOccurs() + "'" );
        }
        
        return sb.toString();
    }
    
    
}
