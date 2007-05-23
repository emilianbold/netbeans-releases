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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;


/**
 *
 * @author Jan Lahoda
 */
public class SuspiciousNamesCombination extends AbstractHint {
    
    /** Creates a new instance of SuspiciousNamesCombination */
    public SuspiciousNamesCombination() {
        super( false, false, AbstractHint.HintSeverity.WARNING );
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION, Kind.ASSIGNMENT, Kind.VARIABLE);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        switch (treePath.getLeaf().getKind()) {
            case METHOD_INVOCATION:
                return handleMethodInvocation(info, treePath);
            case ASSIGNMENT:
                return handleAssignment(info, treePath);
            case VARIABLE:
                return handleVariable(info, treePath);
            default:
                return null;
        }
    }

    public void cancel() {
        // XXX implement me
    }
    
    
    
    private List<ErrorDescription> handleMethodInvocation(CompilationInfo info, TreePath treePath) {
        Element el = info.getTrees().getElement(treePath);
        
        if (el == null || (el.getKind() != ElementKind.CONSTRUCTOR && el.getKind() != ElementKind.METHOD)) {
            return null;
        }
        
        MethodInvocationTree mit = (MethodInvocationTree) treePath.getLeaf();
        ExecutableElement    ee  = (ExecutableElement) el;
        
        if (ee.getParameters().size() != mit.getArguments().size()) {
            //should not happen?
            return null;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        for (int cntr = 0; cntr < ee.getParameters().size(); cntr++) {
            String         declarationName = ee.getParameters().get(cntr).getSimpleName().toString();
            ExpressionTree arg             = mit.getArguments().get(cntr);
            String         actualName      = getName(arg);
            
            if (isConflicting(declarationName, actualName)) {
                long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), arg);
                long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), arg);
                
                if (start != (-1) && end != (-1)) {
                    result.add(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Suspicious names combination", info.getFileObject(), (int) start, (int) end));
                }
            }
        }
        
        return result;
    }
    
    private List<ErrorDescription> handleAssignment(CompilationInfo info, TreePath treePath) {
        AssignmentTree at = (AssignmentTree) treePath.getLeaf();
        
        String declarationName = getName(at.getVariable());
        String actualName      = getName(at.getExpression());
        
        if (isConflicting(declarationName, actualName)) {
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), at.getVariable());
            long end   = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), at.getVariable());
            
            if (start != (-1) && end != (-1)) {
                return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Suspicious names combination", info.getFileObject(), (int) start, (int) end));
            }
        }
        
        return null;
    }
    
    private List<ErrorDescription> handleVariable(CompilationInfo info, TreePath treePath) {
        VariableTree vt = (VariableTree) treePath.getLeaf();
        
        if (vt.getName() == null)
            return null;
        
        String declarationName = vt.getName().toString();
        String actualName      = getName(vt.getInitializer());
        
        if (isConflicting(declarationName, actualName)) {
            try {
                int[] span = Utilities.findIdentifierSpan(treePath, info.getCompilationUnit(), info.getTrees().getSourcePositions(), info.getDocument());
                
                if (span != null && span[0] != (-1) && span[1] != (-1)) {
                    return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Suspicious names combination", info.getFileObject(), span[0], span[1]));
                }
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
        
        return null;
    }
    
    static String getName(ExpressionTree et) {
        if (et == null)
            return null;
        
        switch (et.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) et).getName().toString();
            case METHOD_INVOCATION:
                return getName(((MethodInvocationTree) et).getMethodSelect());
            case MEMBER_SELECT:
                return ((MemberSelectTree) et).getIdentifier().toString();
            default:
                return null;
        }
    }
    
    private boolean isConflicting(String declarationName, String actualName) {
        if (declarationName == null || actualName == null)
            return false;
        
        int declarationCat = findCategory(declarationName);
        int actualCat      = findCategory(actualName);
        
        return declarationCat != actualCat && declarationCat != (-1) && actualCat != (-1);
    }
    
    private int findCategory(String name) {
        Set<String> broken = breakName(name);
        int index = 0;
        
        for (List<String> names : NAME_CATEGORIES) {
            Set<String> copy = new HashSet<String>(names);
            
            copy.retainAll(broken);
            
            if (!copy.isEmpty()) {
                return index;
            }
            
            index++;
        }
        
        return -1;
    }
    
    static Set<String> breakName(String name) {
        Set<String> result = new HashSet<String>();
        int wordStartOffset = 0;
        int index = 0;
        
        while (index < name.length()) {
            if (Character.isUpperCase(name.charAt(index))) {
                //starting new word:
                if (wordStartOffset < index) {
                    result.add(name.substring(wordStartOffset, index).toLowerCase());
                }
                wordStartOffset = index;
            }
            
            if (name.charAt(index) == '-') {
                //starting new word:
                if (wordStartOffset < index) {
                    result.add(name.substring(wordStartOffset, index).toLowerCase());
                }
                wordStartOffset = index + 1;
            }
            
            index++;
        }
        
        if (wordStartOffset < index) {
            result.add(name.substring(wordStartOffset, index).toLowerCase());
        }
        
        return result;
    }
    
    private List<List<String>> NAME_CATEGORIES = Arrays.asList(
            Arrays.asList("x", "width"),
            Arrays.asList("y", "height")
    );
    
    
    public String getId() {
        return SuspiciousNamesCombination.class.getName();
    }

    public String getDisplayName() {
        return "Suspicious Names Combination";
    }

    public String getDescription() {
        return "Suspicious Names Combination by Jan Lahoda";
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    private JPanel panel; // For debugging purposes only 
            
    public synchronized JComponent getCustomizer(Preferences node) {
        
        if ( panel == null ) {
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add( new JLabel( "Some options"), BorderLayout.NORTH );
            panel.add( new JCheckBox( "Ignore somethig"), BorderLayout.CENTER );
        }
        
        return panel;
    }    
    
}
