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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for New REST from Patterns wizard
 *
 * @author lukas
 */
public class PatternsTest extends RestTestBase {

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

        /**
         * Method for getting correct index of the package combo box in the new RESTful
         * web service from patterns wizard for given type of the resource
         *
         * @return index of the package combo box
         */
        public int getResourcePackageJComboIndex() {
            switch (this) {
                case Singleton:
                    return 2;
                case ContainerItem:
                case CcContainerItem:
                    return 0;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the resource class name txt field
         * in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the resource class name txt field
         */
        public int getResourceClassNameTxtIndex() {
            switch (this) {
                case Singleton:
                    return 0;
                case ContainerItem:
                case CcContainerItem:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the resource Path txt field
         * in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the resource Path txt field
         */
        public int getResourcePathTxtIndex() {
            switch (this) {
                case Singleton:
                    return 2;
                case ContainerItem:
                case CcContainerItem:
                    return 5;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the container resource class name txt field
         * in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the container resource class name txt field
         */
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

        /**
         * Method for getting correct index of the container resource Path txt field
         * in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the container resource Path txt field
         */
        public int getContainerResourcePathTxtIndex() {
            switch (this) {
                case Singleton:
                    return -1;
                case ContainerItem:
                case CcContainerItem:
                    return 8;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the Mime-Type combo box
         * in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the Mime-Type combo box
         */
        public int getResourceMimeTypeJComboIndex() {
            switch (this) {
                case Singleton:
                    return 0;
                case ContainerItem:
                case CcContainerItem:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the resource representation class
         * name txt field in the new RESTful web service from patterns wizard
         * for given type of the resource
         *
         * @return index of the resource representation class name txt field
         */
        public int getRepresentationClassTxtIndex() {
            switch (this) {
                case Singleton:
                    return 1;
                case ContainerItem:
                case CcContainerItem:
                    return 6;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the container resource class select
         * button in the new RESTful web service from patterns wizard for given type
         * of the resource
         *
         * @return index of the container resource class select button
         */
        public int getRepresentationClassSelectIndex() {
            switch (this) {
                case Singleton:
                    return 1;
                case ContainerItem:
                case CcContainerItem:
                    return 3;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Method for getting correct index of the container resource
         * representation class name txt field in the new RESTful web service
         * from patterns wizard for given type of the resource
         *
         * @return index of the container resource representation class name txt field
         */
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

        /**
         * Method for getting correct index of the container resource representation
         * class select button in the new RESTful web service from patterns wizard
         * for given type of the resource
         *
         * @return index of the container resource representation class select button
         */
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

    /**
     * Def constructor.
     *
     * @param testName name of particular test case
     */
    public PatternsTest(String name) {
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
        String name = "Item1"; //NOI18N
        Set<File> files = createWsFromPatterns(null, Pattern.CcContainerItem, null);
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
        jcbo.clickMouse();
        jcbo.clearText();
        jcbo.typeText(getRestPackage());
        if (name != null) {
            //we're not using Defs when name != null !!!
            //set resource class name
            JTextFieldOperator jtfo = new JTextFieldOperator(wo, pattern.getResourceClassNameTxtIndex());
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
                //set resource Path
                jtfo = new JTextFieldOperator(wo, pattern.getResourcePathTxtIndex());
                jtfo.clearText();
                jtfo.typeText(name + "URI"); //NOI18N
            } else {
                //set resource Path
                jtfo = new JTextFieldOperator(wo, pattern.getResourcePathTxtIndex());
                jtfo.clearText();
                jtfo.typeText("{" + name + "URI}"); //NOI18N
                //set container resource class name
                jtfo = new JTextFieldOperator(wo, pattern.getContainerResourceClassNameTxtIndex());
                jtfo.clearText();
                jtfo.typeText(name + "CClass"); //NOI18N
                //set container resource Path
                jtfo = new JTextFieldOperator(wo, pattern.getContainerResourcePathTxtIndex());
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
                    new JTextFieldOperator(nbo, 0).typeText("Preferences"); //NOI18N
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
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                    createdFiles.add(getFileFromProject(name + "CClass")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("ItemResource")); //NOI18N
                    createdFiles.add(getFileFromProject("ItemsResource")); //NOI18N
                }
                break;
            case CcContainerItem:
                if (name != null) {
                    createdFiles.add(getFileFromProject(name + "Cl")); //NOI18N
                    createdFiles.add(getFileFromProject(name + "CClass")); //NOI18N
                } else {
                    createdFiles.add(getFileFromProject("ItemResource_1")); //NOI18N
                    createdFiles.add(getFileFromProject("ItemsResource_1")); //NOI18N
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
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(NbModuleSuite.createConfiguration(PatternsTest.class),
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testNodes", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy" //NOI18N
                ).enableModules(".*").clusters(".*")); //NOI18N
    }
}
