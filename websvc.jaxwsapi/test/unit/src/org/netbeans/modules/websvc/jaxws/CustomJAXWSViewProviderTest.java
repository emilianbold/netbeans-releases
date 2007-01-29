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

package org.netbeans.modules.websvc.jaxws;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomJAXWSViewProviderTest extends NbTestCase {
    
    static {
        CustomJAXWSViewProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomJAXWSViewProviderTest */
    public CustomJAXWSViewProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result<JAXWSViewProvider> res = Lookup.getDefault().lookup(new Lookup.Template<JAXWSViewProvider>(JAXWSViewProvider.class));
        assertEquals("there should be 1 instance - from websvc/jaxwsapi", 1, res.allInstances().size());
    }
    
    public void testGetJAXWSView() {
        JAXWSView view = JAXWSView.getJAXWSView();
        assertNotNull("found view support", view);
    }
    
}
