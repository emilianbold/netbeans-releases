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

package org.netbeans.modules.websvc.api;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomJAXWSClientViewProviderTest extends NbTestCase {
    
    static {
        CustomJAXWSClientViewProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomJAXWSClientViewProviderTest */
    public CustomJAXWSClientViewProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(JAXWSClientViewProvider.class));
        assertEquals("there should be 1 instance - from websvc/clientapi", 1, res.allInstances().size());
    }
    
    public void testGetWebServicesClientView() {
        JAXWSClientView view = JAXWSClientView.getJAXWSClientView();
        assertNotNull("found view support", view);
    }
    
}
