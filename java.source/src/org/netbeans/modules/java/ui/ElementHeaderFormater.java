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
package org.netbeans.modules.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.util.Context;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.pretty.VeryPretty;

/** Temporary Should be removed soon
 *
 * @author phrebejk
 */
public class ElementHeaderFormater {

    private ElementHeaderFormater() {
    }
    
    public static String getMethodHeader(MethodTree tree, CompilationInfo info, String s) {
        Context context = JavaSourceAccessor.INSTANCE.getJavacTask(info).getContext();
        VeryPretty veryPretty = new VeryPretty(context);
        return veryPretty.getMethodHeader(tree, s);
    }

    public static String getClassHeader(ClassTree tree, CompilationInfo info, String s) {
        Context context = JavaSourceAccessor.INSTANCE.getJavacTask(info).getContext();
        VeryPretty veryPretty = new VeryPretty(context);
        return veryPretty.getClassHeader(tree, s);
    }
    
    public static String getVariableHeader(VariableTree tree, CompilationInfo info, String s) {
        Context context = JavaSourceAccessor.INSTANCE.getJavacTask(info).getContext();
        VeryPretty veryPretty = new VeryPretty(context);
        return veryPretty.getVariableHeader(tree, s);
    }
    
}
