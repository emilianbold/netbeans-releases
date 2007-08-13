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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {

    public Utilities() {
    }

    public static String guessName(CompilationInfo info, TreePath tp) {
        String name = getName((ExpressionTree) tp.getLeaf());
        
        if (name == null) {
            return "name";
        }
        
        Scope s = info.getTrees().getScope(tp);
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        }
        
        return proposedName;
    }
    
    public static String getName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return "" + Character.toLowerCase(tm.getKind().name().charAt(0));
        }
        
        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                return firstToLower(dt.asElement().getSimpleName().toString());
            case ARRAY:
                return getName(((ArrayType) tm).getComponentType());
            default:
                return null;
        }
    }
    
    public static String getName(ExpressionTree et) {
        return adjustName(getNameRaw(et));
    }
    
    private static String getNameRaw(ExpressionTree et) {
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
    
    static String adjustName(String name) {
        if (name == null)
            return null;
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0)
            return null;
        
        String cand = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        
        if (SourceVersion.isKeyword(cand)) {
            cand = "a" + name;
        }
        
        return cand;
    }
    
    private static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }
    
    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, ModificationResult diff) throws IOException {
        List<? extends Difference> differences = diff.getDifferences(target);
        ChangeInfo result = null;
        
        try {
            if (differences != null) {
                for (Difference d : differences) {
                    if (d.getNewText() != null) { //to filter out possible removes
                        final PositionRef start = d.getStartPosition();
                        Document doc = start.getCloneableEditorSupport().getDocument();

                        if (doc == null) {
                            doc = start.getCloneableEditorSupport().openDocument();
                        }
                        
                        final Position[] pos = new Position[1];
                        final Document fdoc = doc;
                        
                        doc.render(new Runnable() {

                            public void run() {
                                try {
                                    pos[0] = NbDocument.createPosition(fdoc, start.getOffset(), Position.Bias.Backward);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                        
                        if (pos[0] != null) {
                            result = new ChangeInfo(target, pos[0], pos[0]);
                        }
                        
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        diff.commit();
        
        return result;
    }
    
}
