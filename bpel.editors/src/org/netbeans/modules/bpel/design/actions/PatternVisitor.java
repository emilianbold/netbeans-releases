/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author Alexey
 */
public interface PatternVisitor {
    public void visit(Pattern pattern);
}
