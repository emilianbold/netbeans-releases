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
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.WeakListeners;

        
/**
 * This unit test verifies various state change of the AXIModel.
 * It starts with a broken schema. Fixes it to change the state
 * from NOT_WELL_FORMED to VALID. Then it again makes the schema
 * invalid and the state changes from VALID to NOT_WELL_FORMED.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class StateChangeTest extends AbstractSyncTestCase {
                
    public static final String TEST_XSD  = "resources/brokenSchema.xsd";
    private Document document;
    private AXIModel.State expectedState = null;
    private boolean invalidToValid = false;
    private boolean validToInvalid = false;
    
    public StateChangeTest(String testName) {
        super(testName, TEST_XSD, null);
    }
    
    /**
     * Overwrites setUp.
     */
    protected void setUp() throws Exception {
        super.setUp();
        SchemaModel sm = getSchemaModel();
        document = ((AbstractDocumentModel)sm).getBaseDocument();
        AXIModel axiModel = getAXIModel();
        if (axiModel != null) {
            ModelStateChangeListener listener = new ModelStateChangeListener(axiModel);
            axiModel.addPropertyChangeListener(listener);
        }
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(StateChangeTest.class);
        return suite;
    }

    public void testStateChange() throws InterruptedException {
        AXIModel model = getAXIModel();
        //make the schema valid from invalid
        assert(model.getState() == AXIModel.State.NOT_WELL_FORMED);
        String replaceFrom = "<xsd:element name=\"address\" type=\"addr:USAddr";
        String replaceTo = "<xsd:element name=\"address\" type=\"addr:USAddress\"/>";
        replaceInDocument(replaceFrom, replaceTo);
        expectedState = AXIModel.State.VALID;
        Thread.sleep(10000);
        
        //make the schema invalid from valid
        replaceInDocument(replaceTo, replaceFrom);
        expectedState = AXIModel.State.NOT_WELL_FORMED;
        Thread.sleep(10000);
        
        //finally make sure both the tests have been carried out
        assert(invalidToValid);
        assert(validToInvalid);
    }
    
    private void replaceInDocument(String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Model state change listener.
     */
    private class ModelStateChangeListener implements PropertyChangeListener {        
        private AXIModel model;
        ModelStateChangeListener(AXIModel model) {
            this.model = model;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if(!AXIModel.STATE_PROPERTY.equals(property)) {
                return;
            }
            
            State newState = (State)evt.getNewValue();
            State oldState = (State)evt.getOldValue();
            Object source = evt.getSource();
            assert(source == model);
            if(newState == AXIModel.State.VALID) {
                assert(oldState == AXIModel.State.NOT_WELL_FORMED);
                assert(expectedState == newState);
                AXIDocument doc = model.getRoot();
                assert(doc.getChildren().size() == 7);
                assert(doc.getContentModels().size() == 3);
                assert(doc.getChildElements().size() == 3);
                invalidToValid = true;
                return;
            }
            
            if(newState != AXIModel.State.VALID) {
                assert(oldState == AXIModel.State.VALID);
                assert(expectedState == newState);
                validToInvalid = true;
                return;
            }            
        }        
    }    
}
