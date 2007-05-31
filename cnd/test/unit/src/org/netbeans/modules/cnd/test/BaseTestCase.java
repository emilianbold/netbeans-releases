/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.test;

import java.io.File;
import javax.swing.JEditorPane;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * NbTestCase class => NB JUnit module is absent in target platform
 *
 * To solve this problem NB JUnit must be installed
 * For instance from Netbeans Update Center Beta:
 * - start target(!) platform as IDE from command line (/opt/NBDEV/bin/netbeans)
 * - in opened IDE go into Tools->Update Center
 * - select "Netbeans Update Center Beta"
 * -- if absent => configure it using the following url as example
 *    http://www.netbeans.org/updates/beta/55_{$netbeans.autoupdate.version}_{$netbeans.autoupdate.regnum}.xml?{$netbeans.hash.code}
 * - press Next
 * - in Libraries subfoler found NB JUnit module
 * - Add it and install
 * - close target IDE and reload development IDE to update the information of
 *         available modules in target's platform
 */

/**
 * base class to isolate using of NbJUnit library
 * ${xtest.data} vallue is usually ${module}/test/unit/data folder
 * @author Vladimir Voskresensky
 */
public abstract class BaseTestCase extends NbTestCase {
    
    /** Creates a new instance of BaseTestCase */
    public BaseTestCase(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        super.setUp();
        // this is the only way to init extension-based recognizer
        FileUtil.setMIMEType("cc", "text/x-c++");
        FileUtil.setMIMEType("h", "text/x-c++");
        FileUtil.setMIMEType("c", "text/x-c");
        
        JEditorPane.registerEditorKitForContentType("text/x-c++", "org.netbeans.modules.cnd.editor.cplusplus.CCKit");
        
        JEditorPane.registerEditorKitForContentType("text/x-c", "org.netbeans.modules.cnd.editor.cplusplus.CKit");
    }

    /**
     * Get the test method specific data file; 
     * usually it is ${xtest.data}/${classname}/filename
     * @see getTestCaseDataClass
     * @see getTestCaseDataDir
     */
    protected File getDataFile(String filename) {
        return new File(getTestCaseDataDir(), filename);
    }

    /** Get the test method specific golden file as ${xtest.data}/goldenfiles/${classname}/filename
     * @param filename filename to get from golden files directory
     * @return golden file
     * @see getTestCaseGoldenDataClass
     */
    public File getGoldenFile(String filename) {
        String fullClassName = getTestCaseGoldenDataClass().getName();
        String goldenFileName = fullClassName.replace('.', File.separatorChar) + File.separator + filename;
        File goldenFile = new File(getDataDir() + "/goldenfiles/" + goldenFileName);
        return goldenFile;
    }

    /**
     * this method is responsible for construction of part
     * ${classname}
     * in path ${xtest.data}/goldenfiles/${classname}/filename
     * @see getGoldenFile
     */
    protected Class getTestCaseGoldenDataClass() {
        return getTestCaseDataClass();
    }

    /**
     * Get the test method specific data dir
     * usually it is ${xtest.data}/${classname}
     * @see getTestCaseDataClass
     */
    protected File getTestCaseDataDir() {
        File dataDir = super.getDataDir();
        String fullClassName = getTestCaseDataClass().getName();
        String filePath = fullClassName.replace('.', File.separatorChar);
        return Manager.normalizeFile(new File(dataDir, filePath));
    }

    /**
     * this method is responsible for construction of part
     * ${classname}
     * in path ${xtest.data}/${classname}
     * @see getGoldenFile
     */    
    protected Class getTestCaseDataClass() {
        return this.getClass();
    }
}
