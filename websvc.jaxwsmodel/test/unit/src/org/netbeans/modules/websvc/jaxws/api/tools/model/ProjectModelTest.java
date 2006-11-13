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

package org.netbeans.modules.websvc.jaxws.api.tools.modelxws.api.tools.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
/*
 * ProjectModelTest.java
 * JUnit based test
 *
 * Created on February 13, 2006, 5:43 PM
 */

/**
 *
 * @author mkuchtiak
 */
public class ProjectModelTest extends NbTestCase {
    
    public ProjectModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testModel() throws IOException{
        File fo = getFile("jax-ws.xml");
        File fo1 = getFile("jax-ws1.xml");
        InputStream is = new FileInputStream(fo);
        InputStream is1 = new FileInputStream(fo1);
        JaxWsModel jaxws = JaxWsModelProvider.getDefault().getJaxWsModel(is);
        is.close();
        assertNotNull("JaxWsModel1 isn't created",jaxws);
        JaxWsModel jaxws1 = JaxWsModelProvider.getDefault().getJaxWsModel(is1); 
        is1.close();
        assertNotNull("JaxWsModel2 isn't created",jaxws1);
        System.out.println("services.length = "+jaxws.getServices().length);
        assertEquals(2,jaxws.getServices().length);
        jaxws.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("propertyChanged:"+evt.getPropertyName()+"   Old Value:"+evt.getOldValue()+"   New Value:"+evt.getNewValue());
            }
        });
        String orgWsdl = jaxws.findServiceByName("A").getWsdlUrl();
        jaxws.merge(jaxws1);
        String newWsdl = jaxws.findServiceByName("AA").getWsdlUrl();
        assertEquals(orgWsdl,newWsdl);
    }

    private File getFile(String file) {
        return new File(getDataDir(),file);
    }
    
    private File newFile(String file) {
        return new File(getDataDir(),file);
    }
    
}
