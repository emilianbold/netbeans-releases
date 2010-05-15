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
package org.netbeans.modules.bpel.model.xam;

import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.impl.BpelEntityImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;

/**
 *
 * @author ads
 */
public class BpelEntityTest extends TestCase {
    
    public BpelEntityTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BpelEntityTest.class);
        
        return suite;
    }


    public void testGetUID() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        ExtendableActivity activity = model.getProcess().getActivity();
        
        assertNotNull( activity.getUID() );
    }
    
    public void testCut() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        ExtendableActivity activity = model.getProcess().getActivity();
        UniqueId id = activity.getUID();
        
        BpelEntity entity = activity.cut();
        assertTrue( id == entity.getUID() );
        
        assertTrue( entity instanceof ExtendableActivity );
        
        assertTrue( model.getEntity( id ) == entity );
    }
    
    /**
     * This is actually test for some service that move namespaces from 
     * current element to root after placing this element into some container. 
     */
    public void testNamespaceMovingUp() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        PartnerLink link = model.getProcess().getPartnerLinkContainer().
            getPartnerLink( 1 );
        link = (PartnerLink) link.copy( model.getProcess().getPartnerLinkContainer() );
        model.getProcess().getPartnerLinkContainer().addPartnerLink( link );
        final PartnerLink link1 = model.getProcess().getPartnerLinkContainer().getPartnerLink(2);
        model.invoke( new Callable() {
            public Object call( ) {
                //((BpelEntityImpl)link1).setAttribute( "" , new PrefixAttribute("xmlns:otherns"), null);
                ((BpelEntityImpl)link1).removePrefix("otherns");
                /*String str =  ((BpelEntityImpl)link1).getAttribute( BpelAttributes.NAME);
                Util.debug( str);*/
                //return str;
                return link1;
            }
        } , null );
        
        
        
        //StringBuilder builder = new StringBuilder();
        //Util.flush( model , builder );
        //Util.debug( builder.toString() );
    }
    
    public void testGetAttribute() throws Exception  {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        PartnerLink link = model.getProcess().getPartnerLinkContainer().
        getPartnerLink( 0 );
        String str = ((BpelEntityImpl )link).getAttribute( 
                BpelAttributes.NAME );
        Util.debug( str );
    }
    
}
