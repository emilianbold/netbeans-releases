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

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.Sequence;

        
/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncPerfTest extends AbstractSyncTestCase {
                
    public static final String TEST_XSD  = "resources/OTA_TravelItinerary.xsd";
    public static final String GLOBAL_ELEMENT   = "TravelItinerary";
    
    /**
     * SyncElementTest
     */
    public SyncPerfTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(SyncPerfTest.class);
        return suite;
    }

    public void testElementType() {
        renameGlobalElement();
    }
        
    private void renameGlobalElement() {
        assert(globalElement.getName().equals(GLOBAL_ELEMENT));
        try {
            getSchemaModel().startTransaction();
            GlobalElement ge = (GlobalElement)globalElement.getPeer();
            ge.setName(GLOBAL_ELEMENT + "1");
            getSchemaModel().endTransaction();
            long startTime = System.currentTimeMillis();
            getAXIModel().sync();
            long endTime = System.currentTimeMillis();
            print("Time taken to sync AXI model for OTA: " +
                    (endTime - startTime));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        assert(globalElement.getName().equals(GLOBAL_ELEMENT + "1"));
    }
    
}
