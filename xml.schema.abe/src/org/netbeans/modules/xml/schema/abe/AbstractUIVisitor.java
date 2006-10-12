/*
 * AbstractUIVisitor.java
 *
 * Created on August 30, 2006, 8:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

/**
 *
 * @author girix
 */
public abstract class AbstractUIVisitor implements UIVisitor{
    
    /** Creates a new instance of AbstractUIVisitor */
    public AbstractUIVisitor() {
    }

    public void visit(StartTagPanel panel) {
    }

    public void visit(GlobalElementsContainerPanel panel) {
    }

    public void visit(GlobalComplextypeContainerPanel panel) {
    }

    public void visit(AttributePanel panel) {
    }

    public void visit(NamespacePanel panel) {
    }

    public void visit(CompositorPanel panel) {
    }

    public void visit(ElementPanel elementPanel) {
    }
    
}
