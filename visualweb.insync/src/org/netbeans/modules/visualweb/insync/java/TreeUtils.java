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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import static java.lang.reflect.Modifier.*;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author jdeva
 */
public class TreeUtils {
    
    /*
     * Returns treepath for a given tree
     */
    static TreePath getTreePath(CompilationInfo cinfo, Tree tree) {
        return cinfo.getTrees().getPath(cinfo.getCompilationUnit(), tree);
    }
   
    /*
     * Not sure if it is the correct way of computing FQN
     */    
    static String getFQN(CompilationInfo cinfo, Tree tree) {
        return cinfo.getTrees().getTypeMirror(getTreePath(cinfo, tree)).toString();
    }
    
    /*
     * Returns element for a given tree
     */
    static Element getElement(CompilationInfo cinfo, Tree tree) {
        return cinfo.getTrees().getElement(getTreePath(cinfo, tree));
    }
    
    /*
     * Returns element for a given tree
     */
    static TypeElement getTypeElement(CompilationInfo cinfo, Tree tree) {
        return (TypeElement)getElement(cinfo, tree);
    }        
    
    /**
     * Returns the preceding immediate comment
     */    
    static String getPrecedingImmediateCommentText(CompilationInfo cinfo, Tree tree) {
        List<Comment> comments = cinfo.getTreeUtilities().getComments(tree, true);
        return comments.size() > 0 ? comments.get(comments.size()-1).getText() : null;
    }
    
    /**
     * Returns modifiers as bit flags
     */
    static long getModifierFlags(ModifiersTree tree) {
        long flags = 0;
        for(Modifier modifier : tree.getFlags()) {
            switch (modifier) {
                case PUBLIC:       flags |= PUBLIC; break;
                case PROTECTED:    flags |= PROTECTED; break;
                case PRIVATE:      flags |= PRIVATE; break;
                case ABSTRACT:     flags |= ABSTRACT; break;
                case STATIC:       flags |= STATIC; break;
                case FINAL:        flags |= FINAL; break;
                case TRANSIENT:    flags |= TRANSIENT; break;
                case VOLATILE:     flags |= VOLATILE; break;
                case SYNCHRONIZED: flags |= SYNCHRONIZED; break;
                case NATIVE:       flags |= NATIVE; break;
                default:
                    break;
            }
        }
        return flags;
    }
}
