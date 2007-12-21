/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design.actions;

import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author Alexey
 */
public class PatternIterator {

    
    public PatternVisitor visitor;
    
    public PatternIterator( PatternVisitor visitor){
        this.visitor = visitor;
        
    }
    
    public void visitAllPatterns(Pattern pattern) {
        visitPattern(pattern);
    }

    private void visitPattern(Pattern pattern) {
        if (pattern == null) {
            return;
        }

        if (pattern.isSelectable()) {
            visitor.visit(pattern);
        }

        if (pattern instanceof CompositePattern) {
            for (Pattern child : ((CompositePattern) pattern).getNestedPatterns()) {
                visitPattern(child);
            }
        }
    }
}
