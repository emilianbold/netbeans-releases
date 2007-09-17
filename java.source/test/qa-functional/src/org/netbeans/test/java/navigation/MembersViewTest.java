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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.navigation;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.java.JavaTestCase;

/**
 *
 * @author jp159440
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
        Members m = new Members();
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

}
