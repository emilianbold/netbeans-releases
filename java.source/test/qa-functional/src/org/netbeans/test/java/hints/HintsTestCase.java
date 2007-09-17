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
package org.netbeans.test.java.hints;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.swing.ListModel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.test.java.JavaTestCase;

/**
 *
 * @author Jiri Prox
 */
public class HintsTestCase extends JavaTestCase {
    
    public HintsTestCase(String name) {
        super(name);
    }
    
    private void compareArrays(String[] ethalon, String[] current) {
        for (int i = 0; i < current.length; i++) {
            String curItem = current[i];
            int j = 0;
            for (j = 0; j < ethalon.length; j++) {
                String ethalItem = ethalon[j];
                if(curItem.startsWith(ethalItem)) break;
            }
            assertFalse("Item "+curItem+" is missing in ethalon list",j==ethalon.length);
        }
        
        for (int i = 0; i < ethalon.length; i++) {
            String ethalItem = ethalon[i];
            int j = 0;
            for (j = 0; j < current.length; j++) {
                String curItem = current[j];
                if(curItem.startsWith(ethalItem)) break;
            }
            assertFalse("Item "+ethalItem+" is missing in current list",j==current.length);
        }
    }
    
    protected EditorOperator editor;
    
    protected EditorOperator target;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openDefaultProject();
        target = null;
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(editor!=null) editor.close(false);
        super.tearDown();
    }
      
    public void useHint(String hint,String[] hints,String pattern) {
        new EventTool().waitNoEvent(500);        
        editor.pushKey(KeyEvent.VK_ENTER,KeyEvent.ALT_DOWN_MASK);
        JListOperator jlo = null;
        try {
          jlo = new JListOperator(MainWindowOperator.getDefault());        
        } catch(AssertionFailedErrorException afee) {          
          editor.pushKey(KeyEvent.VK_ENTER,KeyEvent.ALT_DOWN_MASK);
          jlo = new JListOperator(MainWindowOperator.getDefault());        
        }
        ListModel model = jlo.getModel();
        int index = -1;
        String[] list = new String[model.getSize()];
        for (int i = 0; i < model.getSize(); i++) {
            String desc = ((Fix)model.getElementAt(i)).getText();
            list[i] = desc;
            if(desc.startsWith(hint)) index = i;
        }
        if(hints!=null) compareArrays(hints,list);
        assertFalse("Required hint "+hint+" not found",index==-1);
        jlo.setSelectedIndex(index);
        jlo.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(750);
        String result;
        if(target==null) {
            result = editor.getText();
        } else {
            target.setVisible(true);
            target.save();
            result = target.getText();
        }
        Pattern p = Pattern.compile(pattern,Pattern.DOTALL);
        if(!p.matcher(result).matches() ){
            log("Pattern: "+pattern);
            System.out.println(pattern);
            log("-------------------");
            log(result);
            System.out.println(result);
            fail("Expected pattern not found");
        }
    }
    
}
