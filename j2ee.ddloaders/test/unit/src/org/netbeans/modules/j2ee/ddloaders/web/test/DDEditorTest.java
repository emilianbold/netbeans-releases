/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.test;
        
import java.io.File;
import java.io.IOException;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.AssertionFailedErrorException;

import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.j2ee.ddloaders.web.test.util.Helper;
import org.netbeans.modules.j2ee.ddloaders.web.test.util.StepIterator;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Milan Kuchtiak
 */
public class DDEditorTest extends NbTestCase {

    private static final String CAR_VOLVO = "Volvo";
    private static final String CAR_AUDI = "Audi";

    private DDDataObject dObj;
    private static final String CONTEXT_PARAM_CYLINDERS = "\n  <context-param>\n    <param-name>cylinders</param-name>\n    <param-value>6</param-value>\n  </context-param>";

    public DDEditorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        return new NbTestSuite(DDEditorTest.class);
    }

    public void testReplaceParamValueFromDDAPI() throws IOException {
        initDataObject();
        openInXmlView(dObj);
        FileObject fo = FileUtil.toFileObject(getDDFile());
        WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
        webApp.getContextParam()[0].setParamValue(CAR_VOLVO);
        webApp.write(fo);
        compareGoldenFile("ReplaceParamValue.pass");
        openInDesignView(dObj);
        assertEquals("Context Params Table wasn't changed: ", CAR_VOLVO, (String) getDDBeanModel().getValueAt(0, 1));
    }

    public void testAddParamValueInDesignView() throws IOException {
        initDataObject();
        openInDesignView(dObj);
        final DDBeanTableModel model = getDDBeanModel();
        final int n = model.getRowCount() + 1;
        model.addRow(new Object[]{"color","Blue",""});
        dObj.modelUpdatedFromUI();
        new StepIterator() {
            int sizeContextParam;

            public boolean step() throws Exception {
                sizeContextParam = dObj.getWebApp().sizeContextParam();
                return sizeContextParam == n;
            }

            public void finalCheck() {
                assertEquals("Context Param wasn't added to the model", n, sizeContextParam);
            }
        };

        // test the model

        openInXmlView(dObj);

        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) dObj.getCookie(EditorCookie.class);
        final Document document = editor.getDocument();

        new StepIterator() {
            public boolean step() throws Exception {
                return document.getText(0, document.getLength()).indexOf("<param-value>Blue</param-value>") >= 0;
            }

            public void finalCheck() {
                final Exception error = getError();
                if (error != null) {
                    throw new AssertionFailedErrorException("Failed to read the document: ", error);
                }
                assertEquals("Cannot find new context param element in XML view (editor document)", true, isSuccess());
            }
        };


        new StepIterator() {
            private SaveCookie saveCookie;

            public boolean step() throws Exception {
                saveCookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
                return saveCookie != null;
            }

            public void finalCheck() {
                // check if save cookie was created
                assertNotNull("Data Object Not Modified", saveCookie);
            }
        }.saveCookie.save();

        // compare to golden file
        compareGoldenFile("AddInitParam1.pass");

    }

    public void testAddParamValueInXmlView() throws IOException {
        initDataObject();
        openInXmlView(dObj);
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)dObj.getCookie(EditorCookie.class);
        final Document document = editor.getDocument();


        // check context params table in Design View
        final DDBeanTableModel model = getDDBeanModel();
        final int n = model.getRowCount();

        // wait to see the changes in XML view
        new StepIterator() {
            private int index;

            public boolean step() throws Exception {
                //test the editor document
                String text = document.getText(0,document.getLength());
                index = text.lastIndexOf("</context-param>");
                return index >= 0;
            }

            public void finalCheck() {
                assertEquals("Cannot find new context param element in XML view (editor document)", true, index > 0);
                try {
                    document.insertString(index + 16, CONTEXT_PARAM_CYLINDERS, null);
                } catch (BadLocationException ex) {
                    throw new AssertionFailedErrorException("Failed to read the document: ", ex);
                }
            }
        };


        openInDesignView(dObj);

        new StepIterator() {
            private String paramValue;

            public boolean step() throws Exception {
                // get context params table model
                DDBeanTableModel model = getDDBeanModel();
                if (model.getRowCount() > n) {
                    paramValue = (String) model.getValueAt(n, 0);
                    return "cylinders".equals(paramValue);
                } else {
                    return false;
                }
            }

            public void finalCheck() {
                assertEquals("Context Params Table wasn't changed: ", "cylinders", paramValue);
            }
        };

        // check if save cookie was created
        SaveCookie cookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();

        // compare to golden file
        compareGoldenFile("AddInitParam2.pass");
    }

    public void testReplaceParamValueFromDDAPI2() throws IOException {
        initDataObject();
        openInXmlView(dObj);
        final FileObject fo = FileUtil.toFileObject(getDDFile());
        WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
        webApp.getContextParam()[0].setParamValue(CAR_AUDI);
        webApp.write(fo);

        compareGoldenFile("ReplaceParamValue2.pass");
    }

    public void testCheckParamValueInDesignView2() throws IOException {
        initDataObject();
        openInDesignView(dObj);
        new StepIterator() {
            private String paramValue;

            public boolean step() throws Exception {
                paramValue = (String) getDDBeanModel().getValueAt(0, 1);
                return CAR_AUDI.equals(paramValue);
            }

            public void finalCheck() {
                assertEquals("Context Params Table wasn't changed: ", CAR_AUDI, paramValue);
            }
        };
    }

    public void testFinalSave() throws IOException {
        initDataObject();
        SaveCookie cookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            cookie.save();
        }
    }

    public DDBeanTableModel getDDBeanModel() {
        DDBeanTableModel ddBeanModel;
        try {
            ddBeanModel = Helper.getContextParamsTableModel(dObj);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Context Params section", ex);
        }
        assertNotNull("Table Model Not Found", ddBeanModel);
        return ddBeanModel;
    }

    private File getDDFile() {
        return Helper.getDDFile(getDataDir());
    }

    private void compareGoldenFile(String goldenFileName) throws IOException {
        assertFile(getDDFile(), getGoldenFile(goldenFileName), getWorkDir());
    }

    private void initDataObject() throws DataObjectNotFoundException {
        if (dObj == null) {
            File f = getDDFile();
            FileObject fo = FileUtil.toFileObject(f);
            dObj = ((DDDataObject) DataObject.find(fo));
            assertNotNull("DD DataObject not found", dObj);
        }
    }

    private static void openInXmlView(DDDataObject dObj) {
        ((EditCookie) dObj.getCookie(EditCookie.class)).edit();
        Helper.waitForDispatchThread();
    }

    private static void openInDesignView(DDDataObject dObj) {
        try {
            dObj.showElement(dObj.getWebApp().getContextParam()[0]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to switch to Design View",ex);
        }
        Helper.waitForDispatchThread();
    }

    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}
