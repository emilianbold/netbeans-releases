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

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author ads
 *
 */
public class CopyTest extends TestCase {

    public CopyTest( String arg0 ) {
        super(arg0);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CopyTest.class);
        
        return suite;
    }
    
    public void testGetTo() throws Exception {
        Util.createData();
        BpelModelImpl model = Util.loadModel(); 
        
        Sequence sequence = (Sequence) model.getProcess().getActivity();
        Flow flow = (Flow)sequence.getActivity(0);
        Assign assign = (Assign)flow.getActivity(0);
        Copy copy = (Copy)assign.getAssignChild(0);
        assertNotNull( copy.getTo() );
        assertNotNull( copy.getFrom());
    }

}
