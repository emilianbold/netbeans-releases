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
package org.netbeans.modules.java.debug;

import java.util.ArrayList;
import java.util.Iterator;
import org.openide.nodes.AbstractNode;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Shows all Errors inside navigator
 * @author Max Sauer
 */
public class ErrorNode extends AbstractNode implements OffsetProvider {

    private CompilationInfo info;
    private Diagnostic diag;
    
    /** Creates a new instance of ErrorNode */
    public ErrorNode(CompilationInfo info, Diagnostic diag) {
        super(Children.LEAF); //always leaf
        this.info = info;
        this.diag = diag;
        String ss = diag.getMessage(Locale.ENGLISH);
        setDisplayName(diag.getCode() + " " + diag.getKind() + ": " + ss); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
    }
    
    public static Node getTree(CompilationInfo info) {
        List<Node> result = new ArrayList<Node>();
        new FindChildrenErrorVisitor(info).scan(result);
        Children.Array c = new Children.Array();
        c.add(result.toArray(new Node[0]));
        return new AbstractNode(c);
    }
    
    public int getStart() {
        return (int) diag.getStartPosition();
    }

    public int getEnd() {
        return (int) diag.getEndPosition();
    }

    public int getPreferredPosition() {
        return (int) diag.getPosition();
    }
    
    
    
    private static class FindChildrenErrorVisitor {
        
        private CompilationInfo info;
        
        public FindChildrenErrorVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        private void scan(List<Node> result) {
            Iterator<Diagnostic> it = info.getDiagnostics().iterator();
            while(it.hasNext()) {
                Diagnostic diag = it.next();
                result.add(new ErrorNode(info, diag));
            }
        }
        
    }
    
}
