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


package org.netbeans.modules.compapp.test.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;



/**
 * DOCUMENT ME!
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class TestcaseChildren extends Children.Keys implements PropertyChangeListener {
    private JbiProject mProject;
    private FileObject mTestcaseDir;
        
    /**
     * Creates a new TestcaseChildren object.
     *
     * @param testcaseDir DOCUMENT ME!
     */
    public TestcaseChildren(JbiProject project, FileObject testcaseDir) {
        mProject = project;
        mTestcaseDir = testcaseDir;
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        
        // and listen to changes in the model too:
        //model.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        List keys = new ArrayList();
        
        // Input and Output
//        FileObject[] fileObjects = mTestcaseDir.getChildren();
//        for (int i = 0; i < fileObjects.length; i++) {
//            String name = fileObjects[i].getNameExt();
//            if (name.equals("Input.xml") || name.equals("Output.xml")) {
//                keys.add(fileObjects[i]);
//            }
//        }
//
//        Collections.sort(keys, new Comparator() {
//            public int compare(Object obj1, Object obj2) {
//                if ((obj1 instanceof FileObject) && (obj2 instanceof FileObject)) {
//                    FileObject fo1 = (FileObject) obj1;
//                    FileObject fo2 = (FileObject) obj2;
//
//                    return fo1.getName().compareTo(fo2.getName());
//                } else {
//                    return 0;
//                }
//            }
//        });
        FileObject inputFO = mTestcaseDir.getFileObject("Input.xml");   // NOI18N
        if (inputFO != null) {
            keys.add(inputFO);
        }
        
        FileObject outputFO = mTestcaseDir.getFileObject("Output.xml"); // NOI18N
        if (outputFO != null) {
            keys.add(outputFO);
        }
        
        
        // Actual results
        FileObject realTestCaseResultsDir = getRealTestCaseResultsFolder();
//        if (realTestCaseResultsDir != null) {
//            if (realTestCaseResultsDir.getChildren().length > 0) {
//                keys.add(realTestCaseResultsDir);
//            }
//        }
//
//        DataFolder dataFolder = DataFolder.findFolder(mTestCaseResultsDir);
        if (realTestCaseResultsDir != null) {
            FileObject[] resultFileObjects = realTestCaseResultsDir.getChildren();
            List resultKeys = new ArrayList();
            for (int i = 0; i < resultFileObjects.length; i++) {
                FileObject fo = resultFileObjects[i];
                if (isValidTestCaseResult(fo)) {
                    resultKeys.add(fo);
                }
            }
            
            Collections.sort(resultKeys, new Comparator() {
                public int compare(Object obj1, Object obj2) {
                    if ((obj1 instanceof FileObject) && (obj2 instanceof FileObject)) {
                        FileObject fo1 = (FileObject) obj1;
                        FileObject fo2 = (FileObject) obj2;
                        
                        return fo2.getName().compareTo(fo1.getName());
                    } else {
                        return 0;
                    }
                }
            });
            
            keys.addAll(resultKeys);
        }
        
        setKeys(keys);
    }
    
    private static boolean isValidTestCaseResult(FileObject fo) {
        
        if (fo.isFolder()) {
            return false;
        }
        String name = fo.getNameExt();
        
        // e.x., Actual_20060803211027.xml, Actual_20060803211027_F.xml, Actual_20060803211027_S.xml
//        // Could use regular expression, but that might be too slow.
        
        return name.matches(TestcaseNode.ACTUAL_OUTPUT_REGEX);
//        return name.startsWith("Actual_") && name.endsWith(".xml") && (name.length() == 25);
    }
    
    private FileObject getRealTestCaseResultsFolder() {
        FileObject dir = null;
        
        String testcaseName = mTestcaseDir.getName();
        FileObject testDir = mProject.getTestDirectory();
        if (testDir != null) {
            FileObject testResultsDir = testDir.getFileObject("results"); // TMP  // NOI18N
            if (testResultsDir != null) {
                dir = testResultsDir.getFileObject(testcaseName);
            }
        }
        
        return dir;
    }
    
    
    /**
     * DOCUMENT ME!
     */
    protected void removeNotify() {
        //epp.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Node[] createNodes(Object key) {
        // interpret your key here...usually one node generated, but could be zero or more
        FileObject fo = (FileObject) key;       
        
        try {
            DataObject dataObject = DataFolder.find(fo);
            
            String name = fo.getName();
            
            if (name.equals("Input")) { // NOI18N
                return new Node[] {new TestCaseInputNode(mProject, dataObject)};
            } else if (name.equals("Output")) { // NOI18N
                return new Node[] {new TestCaseOutputNode(mProject, dataObject)};
            } else {
//                return new Node[] {new TestCaseResultsNode(mProject, fo)};                
                return new Node[] {new TestCaseResultNode(mProject, dataObject)};
            }
        } catch(DataObjectNotFoundException e) {
            // Ignore on purpose. This could happen on test case deletion.
        }
        
        return new Node[0];
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ev DOCUMENT ME!
     */
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param pce DOCUMENT ME!
     */
    public void propertyChange(PropertyChangeEvent pce) {
        updateKeys();
    }
}