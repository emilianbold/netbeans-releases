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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/**
 * Regression test for bug #21285<br>
 * For more info please see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=21285">
 * descrition in issuezilla</a>
 *
 * @author  Petr Hrebejk
 */
public class BeanNodeBug21341 extends NbTestCase {


    /** Creates new TextTest */
    public BeanNodeBug21341(String s) {
        super(s);
    }
    
    public static void main(String[] args)throws Exception {

         
        BeanInfo bi = Introspector.getBeanInfo( Bean21341Hidden.class );
        BeanDescriptor bd = bi.getBeanDescriptor();
        
        System.out.println("  shortDescription : " + bd.getShortDescription() );
        System.out.println("  helpID           : " + bd.getValue( "HelpID" ) );
        //junit.textui.TestRunner.run(new NbTestSuite(BeanNodeBug21285.class));
    }


    /** Regression test to reproduce bug #21858. */
    public void testHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        HelpCtx hCtx = bn.getHelpCtx();
        
        // System.out.println("HCTX " + hCtx.getHelpID() );
               
        assertTrue( "HelpID".equals( hCtx.getHelpID() ) );
    }
    
    public void testPropertiesHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        Node.PropertySet[] ps = bn.getPropertySets();
        Node.PropertySet propertySet = null;
        
        for( int i = 0; i < ps.length; i++ ) {
            if ( Sheet.PROPERTIES.equals( ps[i].getName() ) ) {
                propertySet = ps[i];
                break;
            }
        }
        
        // System.out.println("PsHelpId " + propertySet.getValue( "helpID" ) );
        
        if ( propertySet == null ) {
            fail( "Property set not found" );
        }
        else {
            assertTrue( "PropertiesHelpID".equals( propertySet.getValue( "helpID" ) ) );
        }
    }
    
    public void testExpertHelpID() throws Exception {

        BeanNode bn = new BeanNode( new Bean21341Hidden() );
        Node.PropertySet[] ps = bn.getPropertySets();
        Node.PropertySet propertySet = null;
        
        for( int i = 0; i < ps.length; i++ ) {
            if ( Sheet.EXPERT.equals( ps[i].getName() ) ) {
                propertySet = ps[i];
                break;
            }
        }

        // System.out.println("ExHelpId " + propertySet.getValue( "helpID" ) );
        
        if ( propertySet == null ) {
            fail( "Property set not found" );
        }
        else {
            assertTrue( "ExpertHelpID".equals( propertySet.getValue( "helpID" ) ) );
        }
    }
    
}
