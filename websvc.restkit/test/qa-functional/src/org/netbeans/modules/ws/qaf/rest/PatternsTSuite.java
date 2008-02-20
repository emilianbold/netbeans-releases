/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for New REST from Patterns wizard
 *
 * @author lukas
 */
public class PatternsTSuite extends RestTestBase {

    private enum Pattern {

        CcContainerItem,
        ContainerItem,
        Singleton;

        @Override
        public String toString() {
            switch (this) {
                case Singleton:
                    //Singleton
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_SingletonResource");
                case ContainerItem:
                    //Container-Item
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerItem");
                case CcContainerItem:
                    //Client-Controlled Container-Item
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientControl");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getResourcePackageJComboIndex() {
            switch (this) {
                case Singleton:
                    return 1;
                case ContainerItem:
                case CcContainerItem:
                    return 0;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getResourceNameTxtIndex() {
            switch (this) {
                case Singleton:
                    return 4;
                case ContainerItem:
                case CcContainerItem:
                    return 1;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getResourceClassNameTxtIndex() {
            switch (this) {
                case Singleton:
                    return 3;
                case ContainerItem:
                case CcContainerItem:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getResourceURITemplateTxtIndex() {
            switch (this) {
                case Singleton:
                    return 2;
                case ContainerItem:
                case CcContainerItem:
                    return 5;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getContainerResourceClassNameTxtIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 4;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getContainerResourceURITemplateTxtIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 8;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getResourceMimeTypeJComboIndex() {
            switch (this) {
                case Singleton:
                    return 2;
                case ContainerItem:
                case CcContainerItem:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getRepresentationClassTxtIndex() {
            switch (this) {
                case Singleton:
                    return 5;
                case ContainerItem:
                case CcContainerItem:
                    return 6;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getRepresentationClassSelectIndex() {
            switch (this) {
                case Singleton:
                    return 2;
                case ContainerItem:
                case CcContainerItem:
                    return 3;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getContainerRepresentationClassTxtIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 7;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public int getContainerRepresentationClassSelectIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 4;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /** Def constructor.
     * @param testName name of particular test case
     */
    public PatternsTSuite(String name) {
        super(name);
    }

    @Override
    public String getProjectName() {
        return "FromPatterns"; //NOI18N
    }

    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.patterns"; //NOI18N
    }

    /**
     * Test default setting for Singleton pattern
     */
    public void testSingletonDef() {
        Set<File> files = createWsFromPatterns(null, Pattern.Singleton, null);
    }

    /**
     * Test application/json mime setting for Singleton pattern
     */
    public void testSingleton1() {
        String name = "Singleton1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Singleton pattern
     */
    public void testSingleton2() {
        String name = "Singleton2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Singleton pattern
     */
    public void testSingleton3() {
        String name = "Singleton3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.Singleton, MimeType.TEXT_HTML);
    }

    /**
     * Test default setting for Container Item pattern
     */
    public void testContainerIDef() {
        Set<File> files = createWsFromPatterns(null, Pattern.ContainerItem, null);
    }

    /**
     * Test application/json mime setting for Container Item pattern
     */
    public void testContainerI1() {
        String name = "CI1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Container Item pattern
     */
    public void testContainerI2() {
        String name = "CI2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Container Item pattern
     */
    public void testContainerI3() {
        String name = "CI3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.ContainerItem, MimeType.TEXT_HTML);
    }

    /**
     * Test default setting for Client Controlled Container Item pattern
     */
    public void testCcContainerIDef() {
        //TODO: have to set name because of issue 112610
        // http://www.netbeans.org/issues/show_bug.cgi?id=122610
        String name = "Item1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, null);
    }

    /**
     * Test application/json mime setting for Client Controlled Container Item pattern
     */
    public void testCcContainerI1() {
        String name = "CcCI1"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.APPLICATION_JSON);
    }

    /**
     * Test text/plain mime setting for Client Controlled Container Item pattern
     */
    public void testCcContainerI2() {
        String name = "CcCI2"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.TEXT_PLAIN);
    }

    /**
     * Test text/html mime setting for Client Controlled Container Item pattern
     */
    public void testCcContainerI3() {
        String name = "CcCI3"; //NOI18N
        Set<File> files = createWsFromPatterns(name, Pattern.CcContainerItem, MimeType.TEXT_HTML);
    }

    /**
     * Make sure all REST services nodes are visible in project log. view
     */
    public void testNodes() {
        Node restNode = getRestNode();
        assertEquals("missing nodes?", 20, restNode.getChildren().length); //NOI18N
        restNode.tree().clickOnPath(restNode.getTreePath(), 2);
        assertTrue("Node not collapsed", restNode.isCollapsed());
    }

    private Set<File> createWsFromPatterns(String name, Pattern pattern, MimeType mimeType) {
        //RESTful Web Services from Patterns
        String patternsTypeName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromPatterns");
        createNewWSFile(getProject(), patternsTypeName);
        WizardOperator wo = new WizardOperator(patternsTypeName);
        new JRadioButtonOperator(wo, pattern.ordinal()).clickMouse();
        wo.next();
        wo = new WizardOperator(patternsTypeName);
        //set resource package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, pattern.getResourcePackageJComboIndex());
        jcbo.clearText();
        jcbo.typeText(getRestPackage());
        if (name != null) {
            //we're not using Defs when name != null !!!
            //set resource name
            JTextFieldOperator jtfo = new JTextFieldOperator(wo, pattern.getResourceNameTxtIndex());
            jtfo.clearText();
            jtfo.typeText(name + "Rs"); //NOI18N
            //set resource class name
            jtfo = new JTextFieldOperator(wo, pattern.getResourceClassNameTxtIndex());
            jtfo.clearText();
            jtfo.typeText(name + "Cl"); //NOI18N
            //set mimeType
            if (mimeType != null) {
                jcbo = new JComboBoxOperator(wo, pattern.getResourceMimeTypeJComboIndex());
                jcbo.selectItem(mimeType.toString());
            }
            //set resource representation class
            if (MimeType.APPLICATION_JSON.equals(mimeType)) {
                jtfo = new JTextFieldOperator(wo, pattern.getRepresentationClassTxtIndex());
                jtfo.clearText();
                jtfo.typeText("org.codehaus.jettison.json.JSONString"); //NOI18N
            } else if (MimeType.TEXT_PLAIN.equals(mimeType)) {
                new JButtonOperator(wo, pattern.getRepresentationClassSelectIndex()).pushNoBlock();
                //"Find Type"
                String fTypeLbl = Bundle.getStringTrimmed("org.netbeans.modules.java.source.ui.Bundle", "DLG_FindType");
                NbDialogOperator nbo = new NbDialogOperator(fTypeLbl);
                new JTextFieldOperator(nbo, 0).typeText("Level"); //NOI18N
                nbo.ok();
            }
            if (Pattern.Singleton.equals(pattern)) {
                //set resource URI template
                jtfo = new JTextFieldOperator(wo, pattern.getResourceURITemplateTxtIndex());
                jtfo.clearText();
                jtfo.typeText(name + "URI"); //NOI18N
            } else {
                //set resource URI template
                jtfo = new JTextFieldOperator(wo, pattern.getResourceURITemplateTxtIndex());
                jtfo.clearText();
                jtfo.typeText("{" + name + "URI}"); //NOI18N
                //set container resource class name
                jtfo = new JTextFieldOperator(wo, pattern.getContainerResourceClassNameTxtIndex());
                jtfo.clearText();
                jtfo.typeText(name + "CClass"); //NOI18N
                //set container resource URI template
                jtfo = new JTextFieldOperator(wo, pattern.getContainerResourceURITemplateTxtIndex());
                jtfo.clearText();
                jtfo.typeText("/" + name + "ContainerURI"); //NOI18N
                //set container resource representation class
                if (MimeType.APPLICATION_JSON.equals(mimeType)) {
                    jtfo = new JTextFieldOperator(wo, pattern.getContainerRepresentationClassTxtIndex());
                    jtfo.clearText();
                    jtfo.typeText("org.codehaus.jettison.json.JSONObject"); //NOI18N
                } else if (MimeType.TEXT_PLAIN.equals(mimeType)) {
                    new JButtonOperator(wo, pattern.getContainerRepresentationClassSelectIndex()).pushNoBlock();
                    //"Find Type"
                    String fTypeLbl = Bundle.getStringTrimmed("org.netbeans.modules.java.source.ui.Bundle", "DLG_FindType");
                    NbDialogOperator nbo = new NbDialogOperator(fTypeLbl);
                    new JTextFieldOperator(nbo, 0).typeText("Properties"); //NOI18N
                    nbo.ok();
                }
            }
        }
        wo.finish();
        String progressDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RestServicesFromPatternsProgress");
        waitDialogClosed(progressDialogTitle);
        Set<File> createdFiles = new HashSet<File>();
        switch (pattern) {
            case Singleton:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("GenericResource")); //NOI18N
                }
                break;
            case ContainerItem:
            case CcContainerItem:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                    createdFiles.add(getFileFromProject(name + "CClass")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("ItemResource")); //NOI18N
                    createdFiles.add(getFileFromProject("ItemsResource")); //NOI18N
                }
                break;
        }
        closeCreatedFiles(createdFiles);
        checkFiles(createdFiles);
        return createdFiles;
    }

    private File getFileFromProject(String fileName) {
        FileObject fo = getProject().getProjectDirectory().getFileObject("src/java"); //NOI18N
        fo = fo.getFileObject(getRestPackage().replace('.', '/') + "/" + fileName + ".java"); //NOI18N
        assertNotNull(fo);
        return FileUtil.toFile(fo);
    }

    private void closeCreatedFiles(Set<File> files) {
        for (File f : files) {
            EditorOperator eo = new EditorOperator(f.getName());
            eo.close();
        }
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PatternsTSuite("testSingletonDef")); //NOI18N
        suite.addTest(new PatternsTSuite("testContainerIDef")); //NOI18N
        suite.addTest(new PatternsTSuite("testCcContainerIDef")); //NOI18N
        suite.addTest(new PatternsTSuite("testSingleton1")); //NOI18N
        suite.addTest(new PatternsTSuite("testCcContainerI1")); //NOI18N
        suite.addTest(new PatternsTSuite("testSingleton2")); //NOI18N
        suite.addTest(new PatternsTSuite("testContainerI1")); //NOI18N
        suite.addTest(new PatternsTSuite("testContainerI2")); //NOI18N
        suite.addTest(new PatternsTSuite("testSingleton3")); //NOI18N
        suite.addTest(new PatternsTSuite("testContainerI3")); //NOI18N
        suite.addTest(new PatternsTSuite("testCcContainerI2")); //NOI18N
        suite.addTest(new PatternsTSuite("testCcContainerI3")); //NOI18N
        suite.addTest(new PatternsTSuite("testNodes")); //NOI18N
        suite.addTest(new PatternsTSuite("testDeploy")); //NOI18N
        suite.addTest(new PatternsTSuite("testUndeploy")); //NOI18N
        return suite;
    }

    /**
     * Method allowing test execution directly from the IDE.
     */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }
}
