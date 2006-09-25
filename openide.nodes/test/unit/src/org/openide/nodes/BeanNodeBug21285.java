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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node.Property;

/**
 * Regression test for bug #21285<br>
 * For more info please see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=21285">
 * descrition in issuezilla</a>
 *
 * @author  Petr Hrebejk
 */
public class BeanNodeBug21285 extends NbTestCase {


    /** Creates new TextTest */
    public BeanNodeBug21285(String s) {
        super(s);
    }
    
    public static void main(String[] args)throws Exception {

         
        BeanInfo bi = Introspector.getBeanInfo( BadBeanHidden.class );
        PropertyDescriptor[] ps = bi.getPropertyDescriptors();
        
        for ( int i = 0; i < ps.length; i++ ) {
            System.out.println( i + " : " + ps[i]);
            System.out.println("  Read : " + ps[i].getReadMethod() );
            System.out.println("  Write : " + ps[i].getWriteMethod() );
            System.out.println(" TYPE " + ps[i].getPropertyType() );
            if ( ps[i] instanceof IndexedPropertyDescriptor ) {
                System.out.println("  I Read : " + ((IndexedPropertyDescriptor)ps[i]).getIndexedReadMethod() );
                System.out.println("  I Write : " +((IndexedPropertyDescriptor)ps[i]).getIndexedWriteMethod() );
                System.out.println(" TYPE " + ((IndexedPropertyDescriptor)ps[i]).getIndexedPropertyType() );
            }
            
            
        }
    }


    /** Regression test to reproduce bug #21858. */
    public void testBadBean() throws Exception {

        BeanNode bn = new BeanNode( new BadBeanHidden() );
        Node.PropertySet ps[] = bn.getPropertySets();
        
        try {
            for (int i = 0; i < ps.length; i++) {
                 Set<Property> props = new HashSet<Property>( 
                    Arrays.asList(ps[i].getProperties()));
            }
        }
        catch ( NullPointerException e ) {
            assertTrue( "The NullPointerException thrown", false );
        }
        
        assertTrue( true );
    }
    
}
