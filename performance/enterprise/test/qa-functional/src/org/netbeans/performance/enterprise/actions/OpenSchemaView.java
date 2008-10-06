/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.performance.enterprise.actions;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class OpenSchemaView extends PerformanceTestCase {
    
    private static String projectName, schemaName;
    private boolean testGC = false;
    private List<WeakReference> refObj = new ArrayList<WeakReference>();
    private List<WeakReference> refDoc = new ArrayList<WeakReference>();
    private String filename = CommonUtilities.getProjectsDir() 
            + "SOATestProject" + File.separator + "src";
    
    /** Creates a new instance of OpenSchemaView */
    public OpenSchemaView(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
    }
    
    public OpenSchemaView(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
    }
    
    public void testOpenSchemaView(){
        projectName = "TravelReservationService";
        schemaName = "OTA_TravelItinerary";
        filename += File.separator + schemaName + ".xsd";
        doMeasurement();
    }
    
    public void testOpenComplexSchemaView(){
        projectName = "SOATestProject";
        schemaName = "fields";
        filename += File.separator + schemaName + ".xsd";
        doMeasurement();
    }

    public void testGCwithOpenComplexSchemaView(){
        testGC = true;
        testOpenComplexSchemaView();
        for (WeakReference ref : refObj) {
            log("Trying to assertGC for " + ref);
            NbTestCase.assertGC("Schema data object can disappear", ref);            
        }
        log("All Schema data objects are GCed");
        for (WeakReference ref : refDoc) {
            log("Trying to assertGC for " + ref);
            NbTestCase.assertGC("Schema document can disappear", ref);            
        }
        log("All Schema documents are GCed");
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        Node schemaNode = new Node(new EPUtilities().getProcessFilesNode(projectName),schemaName+".xsd");
        schemaNode.callPopup().pushMenuNoBlock("Open");
        //new OpenAction().performPopup(schemaNode);
        return XMLSchemaComponentOperator.findXMLSchemaComponentOperator(schemaName+".xsd");
    }

    @Override
    protected void initialize() {
        super.initialize();
        System.gc();
        new EventTool().waitNoEvent(3000);
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void close(){
        if (testGC) {
            try {
                log("filename = " + filename);
                FileObject fo = FileUtil.toFileObject(new File(filename));
                if (fo == null) {
                    log("fo == null");
                }
                DataObject obj = DataObject.find(fo);
                if (obj == null) {
                    log("obj == null");
                }
                log("obj = " + obj);
                EditorCookie ed = obj.getLookup().lookup(EditorCookie.class);
                if (ed == null) {
                }
                log("ed = " + ed);
    //            JEditorPane[] arr = ed.getOpenedPanes();
//                StyledDocument document = ed.getDocument();

                refObj.add(new WeakReference(obj));
//                refDoc.add(new WeakReference(document));
            } catch (Exception e) {
                throw new RuntimeException("Failed to initiate WeakReferences", e);
            }
        }
        System.gc();
        new EventTool().waitNoEvent(3000);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(OpenSchemaView.class)
            .addTest("testOpenSchemaView")
            .addTest("testOpenComplexSchemaView")
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );    
    }
    
}
