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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.test.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;



/**
 * DOCUMENT ME!
 *
 * @author Bing Lu
 * @author rselvaraj
 */
public class TestChildren extends Children.Keys implements PropertyChangeListener {
    private final FileObject mTestDir;
    private final JbiProject mProject;    
    
    private static final String[] TEST_PROPERTY_FILES = new String[] {
        "Invoke.properties", "Concurrent.properties", "Correlation.properties", }; // NOI18N
   
    
    /**
     * Creates a new TestChildren object.
     *
     *
     *
     * @param project DOCUMENT ME!
     */
    public TestChildren(JbiProject project, FileObject testDir) {
        mProject = project;
        mTestDir = testDir;
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
    
    
    private static boolean isTest(FileObject fo) {
        if (!fo.isFolder()) {
            return false;
        }
        
        for (int i = 0; i < TEST_PROPERTY_FILES.length; i++) {
            FileObject prop = fo.getFileObject(TEST_PROPERTY_FILES[i]);
            if (prop != null) {
                return true;
            }
        }
        
        return false;
    }
    
    private void updateKeys() {
        List keys = Collections.EMPTY_LIST;
        FileObject[] tests = mTestDir.getChildren();
        keys = new ArrayList();
        for (int i = 0; i < tests.length; i++) {
            if (isTest(tests[i])) {
                keys.add(tests[i]);
            }
        }
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((FileObject)o1).getName().compareTo(((FileObject)o2).getName());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
        setKeys(keys);
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
        return new Node[] {new TestcaseNode(mProject, fo)};
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
