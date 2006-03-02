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
    
    public void visit(TestComponent c) {
        visitChildren(c);
    }
    
    protected void visitChildren(TestComponent c) {
        List<TestComponent> children = c.getChildren();
        for (int i=0; i<children.size(); i++) {
            visit(children.get(i));
        }
    }
}
