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

package org.netbeans.modules.cnd.classview;

import java.io.File;
import java.io.PrintStream;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * base class for class view golden tests
 *
 * @author Alexander Simon
 */
public class BaseTestCase extends TraceModelTestBase implements CsmModelListener {
    private boolean isReparsed;
    
    public BaseTestCase(String testName, boolean isReparsed) {
        super(testName);
        this.isReparsed = isReparsed;
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.classview.no-loading-node","true");
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    protected void doTest(File testFile, PrintStream streamOut, PrintStream streamErr, Object ... params) throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            performModelTest(testFile, streamOut, streamErr);
            performTest("");
        } finally {
            // restore err and out
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }
    
    
    @Override
    protected void performTest(String source) throws Exception {
        CsmProject project = getCsmProject();
        assertNotNull("Project not found",project);
        childrenUpdater = new ChildrenUpdater();
        CsmNamespace globalNamespace = project.getGlobalNamespace();
        NamespaceKeyArray global = new NamespaceKeyArray(childrenUpdater, globalNamespace);
        dump(global,"", !isReparsed);
        getModel().addModelListener(this);
        for(CsmFile file : project.getHeaderFiles()){
            reparseFile(file);
        }
        dump(global,"", isReparsed);
    }
    
    private void dump(HostKeyArray children, String ident, boolean trace){
        Node[] nodes = children.getNodes();
        for(Node node : nodes){
            String res = ident+node.getDisplayName()+" / "+getNodeIcon(node);
            if (trace) {
                System.out.println(res);
            }
            Children child = node.getChildren();
            if (child instanceof HostKeyArray){
                dump((HostKeyArray)child, ident+"\t", trace);
            }
        }
    }
    private String getNodeIcon(Node node){
        CsmObject obj = ((AbstractCsmNode)node).getCsmObject();
        String path = CsmImageLoader.getImagePath(obj);
        return new File(path).getName();
    }

    private ChildrenUpdater childrenUpdater;

    public void projectOpened(CsmProject project) {
    }

    public void projectClosed(CsmProject project) {
    }

    public void modelChanged(CsmChangeEvent e) {
        childrenUpdater.update(new SmartChangeEvent(e));
    }
}
