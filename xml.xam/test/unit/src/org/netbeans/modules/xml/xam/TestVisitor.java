/*
 * TestVisitor.java
 *
 * Created on January 6, 2006, 4:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xam;

import java.util.List;

/**
 *
 * @author Nam Nguyen
 */
public class TestVisitor {
    
    public void visit(TestComponent2 c) {
        visitChildren(c);
    }
    
    protected void visitChildren(TestComponent2 c) {
        List<TestComponent2> children = c.getChildren();
        for (int i=0; i<children.size(); i++) {
            children.get(i).accept(this);
        }
    }
}
