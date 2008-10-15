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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.navigation;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.tree.TreeModel;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.JavaTestCase;

/**
 *
 * @author Jiri Prox
 */
public class MembersViewTest extends JavaTestCase{

    public MembersViewTest(String name) {
        super(name);
    }
    
    private String indent(int cols) {
        StringBuffer sb = new StringBuffer();
        while(cols>0) {
            sb.append("    ");
            cols--;
        }
        return sb.toString();
    }
    
    public void recurse(Object node,JTreeOperator  jto,int level) {
        if(node!=null) {
            String l = indent(level)+node.toString();
            ref(l);
            System.out.println(l);
        }        
        for(int i=0;i<jto.getChildCount(node);i++) {
            Object child = jto.getChild(node, i);
            recurse(child, jto, level+1);            
        }
    }
    
    public void testBasic() {        
        openSourceFile("org.netbeans.test.java.navigation.MembersViewTest", "SuperClass");
        EditorOperator editor = new EditorOperator("SuperClass");
        editor.setCaretPosition(24,13);
        editor.pressKey(KeyEvent.VK_F12, KeyEvent.CTRL_DOWN_MASK);
        new EventTool().waitNoEvent(1000);
        MembersOperator m = new MembersOperator();
        JTreeOperator jto = m.treeJTree();
        TreeModel model = jto.getModel();
        Object root = model.getRoot();
        recurse(root, jto, 0);
        m.close();                        
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openDefaultProject();
    }

    @Override
    protected void tearDown() throws Exception {
        
        File golden = getGoldenFile();
        File diff = new File(getWorkDir(),getName()+".diff");
        assertFile(getRefFile(),golden, diff);
        super.tearDown();
    }
    
    protected File getRefFile() throws IOException {
        File f = new File(getWorkDir(),getName()+".ref");
        return f;
    }
    
    
     public static void main(String[] args) {
        new TestRunner().run(MembersViewTest.class);
    }
     
     public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MembersViewTest.class).enableModules(".*").clusters(".*"));
    }

}
