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

import java.io.File;
import java.io.FileInputStream;

import javax.swing.text.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.bpel.model.impl.BpelModelImpl;


/**
 * @author ads
 *
 */
public class SyncTest extends TestCase {

    public SyncTest( String arg0 ) {
        super(arg0);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SyncTest.class);
        
        return suite;
    }
    
    public void testSync() throws Exception {
        Util.createData();
        
        Document doc = Util.loadDocument(getClass().getResourceAsStream(
                "data/test_orig.bpel"));
        BpelModelImpl model = new BpelModelImpl( doc , null );
        model.sync();
        
        doc.remove( 0 , doc.getLength() );
        Document d = Util.loadDocument(getClass().getResourceAsStream(
                "data/test_changed.bpel"));
        
        doc.insertString( 0 , d.getText( 0, d.getLength()), null );
        model.sync();

    }

}
