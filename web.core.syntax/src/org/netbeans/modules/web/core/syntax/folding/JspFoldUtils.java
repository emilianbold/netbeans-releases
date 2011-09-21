/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
