/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.folding;

import java.io.PrintStream;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 * Utility class. Contains mostly debug messages suport.
 *
 * @author Marek Fukala
 */

public class JspFoldUtils {

    
    public static void printFolds(FoldOperation fo) {
        printFolds(fo.getHierarchy(), System.out);
    }
    
    /** Prints folds recursivelly into standard output*/
    public static void printFolds(FoldHierarchy fh, PrintStream out) {
        fh.lock();
        try {
            Fold rootFold = fh.getRootFold();
            printChildren(rootFold, 0, out);
        } finally {
            fh.unlock();
        }
    }
    
    private static void printChildren(Fold f, int level, PrintStream out) {
        int foldCount = f.getFoldCount();
        //indent
        for( int i = 0; i < level; i ++) System.out.print(" ");
        //print this fold info
        out.println(f.getDescription() + "[" + f.getType().toString() + "; " + f.getStartOffset() + " - " + f.getEndOffset() + "]");
        System.out.println(f.getDescription() + "[" + f.getType().toString() + "; " + f.getStartOffset() + " - " + f.getEndOffset() + "]");
        //print children
        for (int i = 0; i < foldCount; i++) {
            Fold childFold = f.getFold(i);
            printChildren(childFold, level + 4, out);
        }
    }
  
    public static String getContextName(int typeId) {
        switch(typeId) {
            case JspSyntaxSupport.COMMENT_COMPLETION_CONTEXT:
                return "comment";
            case JspSyntaxSupport.CONTENTL_COMPLETION_CONTEXT:
                return "content language";
            case JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT:
                return "directive";
            case JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT:
                return "end tag";
            case JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT:
                return "scripting";
            case JspSyntaxSupport.TAG_COMPLETION_CONTEXT:
                return "tag";
            case JspSyntaxSupport.TEXT_COMPLETION_CONTEXT:
                return "text";
            default:
                return "?";
        }
    } 
    
}
