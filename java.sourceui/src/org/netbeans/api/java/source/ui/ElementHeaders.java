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

package org.netbeans.api.java.source.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.ui.ElementHeaderFormater;

/**
 *
 * @author phrebejk
 */
public final class ElementHeaders {

    private ElementHeaders() {
    }
    
    public static final String ANNOTATIONS = "%annotations%"; //NOI18N
    public static final String NAME = "%name%"; //NOI18N
    public static final String TYPE = "%type%"; //NOI18N
    public static final String THROWS = "%throws%"; //NOI18N
    public static final String IMPLEMENTS = "%implements%"; //NOI18N
    public static final String EXTENDS = "%extends%"; //NOI18N
    public static final String TYPEPARAMETERS = "%typeparameters%"; //NOI18N
    public static final String FLAGS = "%flags%"; //NOI18N
    public static final String PARAMETERS = "%parameters%"; //NOI18N
    
    
    /** Formats header of a tree. The tree must represent an element e.g. type
     * method, field, ...
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param treePath TreePath to the tree header is required for
     * @param info CompilationInfo
     * @param formatString Formating string
     * @return Formated header of the tree
     */
    public static String getHeader(TreePath treePath, CompilationInfo info, String formatString) {
        assert info != null;
        assert treePath != null;
        Element element = info.getTrees().getElement(treePath);
        if (element!=null)
            return getHeader(element, info, formatString);
        return null;
    }

    /** Formats header of an element.
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param element Element to be formated
     * @param info Compilation info
     * @param formatString Formating string
     * @return Formated header of the element
     */
    public static String getHeader(Element element, CompilationInfo info, String formatString) {
        assert element != null;
        assert info != null;
        assert formatString != null;
        Tree tree = info.getTrees().getTree(element);
        if (tree != null) {
            if (tree.getKind() == Tree.Kind.METHOD) {
                return ElementHeaderFormater.getMethodHeader((MethodTree) tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.CLASS) {
                return ElementHeaderFormater.getClassHeader((ClassTree)tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                return ElementHeaderFormater.getVariableHeader((VariableTree)tree, info, formatString);
            }
        }
        return formatString.replaceAll(NAME, element.getSimpleName().toString()).replaceAll("%[a-z]*%", ""); //NOI18N
    }
    
     /** Computes distance between strings
      * @param s First string
      * @param t Second string
      * @return Distance between the strings. (Number of changes which have to 
      *         be done to get from <CODE>s</CODE> to <CODE>t</CODE>.
      */
    public static int getDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length ();
        m = t.length ();
        if (n == 0) {
          return m;
        }
        if (m == 0) {
          return n;
        }
        d = new int[n+1][m+1];

        // Step 2

        for (i = 0; i <= n; i++) {
          d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
          d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

          s_i = s.charAt (i - 1);

          // Step 4

          for (j = 1; j <= m; j++) {

            t_j = t.charAt (j - 1);

            // Step 5

            if (s_i == t_j) {
              cost = 0;
            }
            else {
              cost = 1;
            }

            // Step 6
            d[i][j] = min (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

          }

        }

        // Step 7

        return d[n][m];        
    }
  

    // Private methods ---------------------------------------------------------
    
    
    private static int min (int a, int b, int c) {
        int mi;
               
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;

   }
    
    

}
