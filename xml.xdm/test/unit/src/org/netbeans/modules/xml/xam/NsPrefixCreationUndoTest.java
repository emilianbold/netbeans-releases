/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.xam;

import javax.swing.text.Document;
import junit.framework.*;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.xam.TestComponent.A;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author Nikita Krjukov
 */
public class NsPrefixCreationUndoTest extends NbTestCase {
    AbstractModelTest.PropertyListener plistener;
    AbstractModelTest.TestComponentListener listener;
    TestModel model;
    Document doc;

    public NsPrefixCreationUndoTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        listener = new AbstractModelTest.TestComponentListener();
        plistener = new AbstractModelTest.PropertyListener();
    }
    
    private void defaultSetup() throws Exception {
        doc = Util.getResourceAsDocument("resources/test1.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
    }

    @Override
    protected void tearDown() throws Exception {
        if (model != null) {
            model.removePropertyChangeListener(plistener);
            model.removeComponentListener(listener);
        }
    }

    public static Test suite() {
        return new TestSuite(NsPrefixCreationUndoTest.class);
    }

    /**
     * Tests Issue #166177
     * @throws Exception
     */
    public void testUndoNsPrefixCreation() throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        A a = model.getRootComponent().getChild(A.class);
        //
        String xdmModelTextInitial = Util.getXdmBasedModelText(model);
        String xamModelTextInitial = Util.getXamBasedModelText(model);
        //
        // Add child component to A element
        model.startTransaction();
        try {
            TestComponent.Aa newChildAa = new TestComponent.Aa(model, 1);
            a.appendChild("setup", newChildAa);
        } finally {
            model.endTransaction();
        }
        //
        TestComponent.Aa newAa = a.getChild(TestComponent.Aa.class);
        assertEquals(TestComponent.NS2_URI, newAa.getNamespaceURI());
        //
        String xdmModelTextAfterAdd = Util.getXdmBasedModelText(model);
        String xamModelTextAfterAdd = Util.getXamBasedModelText(model);
        //
        ur.undo();
        //
        // Check the XDM and XAM models' structure return back to initial state after undo.
        String xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        String xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
        ur.redo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextAfterAdd);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextAfterAdd);
        //
        ur.undo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
    }

    /**
     * Tests procesing of an interruption inside of ComponentUpdater with
     * a RuntimeException. According to TestComponentUpdater class,
     * a RuntimeException should be thrown when an Err element is added to the model. 
     * 
     * This test shows that regardless of the exception, the XAM model remains 
     * valid due to AbstractDocumentModel.refresh() method is called. 
     *
     * @throws Exception
     */
    public void testInterruptedComponentUpdater() throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        A a = model.getRootComponent().getChild(A.class);
        //
        String xdmModelTextInitial = Util.getXdmBasedModelText(model);
        String xamModelTextInitial = Util.getXamBasedModelText(model);
        //
        // Add child component to A element
        model.startTransaction();
        try {
            TestComponent.Err newChildErr = new TestComponent.Err(model, 1);
            a.appendChild(TestComponent.Err.LNAME, newChildErr);
        } finally {
            model.endTransaction();
        }
        //
        TestComponent.Err newErr = a.getChild(TestComponent.Err.class);
        assertEquals(TestComponent.NS_ERR_URI, newErr.getNamespaceURI());
        //
        String xdmModelTextAfterAdd = Util.getXdmBasedModelText(model);
        String xamModelTextAfterAdd = Util.getXamBasedModelText(model);
        //
        ur.undo();
        //
        // Check the XDM and XAM models' structure return back to initial state after undo.
        String xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        String xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
        try {
            ur.redo();
        } catch (Exception ex) {
            String exMsg = ex.getMessage();
            assertEquals(exMsg, "Test synch crashed.");
        }
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextAfterAdd);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextAfterAdd);
        //
        ur.undo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
    }

}
