/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.nodes;

import java.beans.*;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.*;

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
        //junit.textui.TestRunner.run(new NbTestSuite(BeanNodeBug21285.class));
    }


    /** Regression test to reproduce bug #21858. */
    public void testBadBean() throws Exception {

        BeanNode bn = new BeanNode( new BadBeanHidden() );
        Node.PropertySet ps[] = bn.getPropertySets();
        
        try {
            for (int i = 0; i < ps.length; i++) {
                 java.util.Set props = new java.util.HashSet( 
                    java.util.Arrays.asList(ps[i].getProperties()));
            }
        }
        catch ( NullPointerException e ) {
            assertTrue( "The NullPointerException thrown", false );
        }
        
        assertTrue( true );
    }
    
    protected void setUp() {
    }
    
    protected void tearDown() {
    }

}
