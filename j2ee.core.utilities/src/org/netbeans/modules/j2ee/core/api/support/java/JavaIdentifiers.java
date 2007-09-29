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
package org.netbeans.modules.j2ee.core.api.support.java;

import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * This class consists of static utility methods for working
 * with Java identifiers.
 */
public final class JavaIdentifiers {

    private JavaIdentifiers(){
    }

    /**
     * Checks whether the given <code>packageName</code> represents a
     * valid name for a package.
     *
     * @param packageName the package name to check.
     * @return true if the given <code>packageName</code> is a valid package
     * name, false otherwise.
     */
    public static boolean isValidPackageName(String packageName) {
        if (packageName.length() > 0 && packageName.charAt(0) == '.') {// NOI18N
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(packageName, "."); // NOI18N
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {// NOI18N
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the fully qualified name for the given <code>fileObject</code>. If it
     * represents a java package, returns the name of the package (with dots as separators).
     *
     * @param fileObject the file object whose FQN is to be get.
     * @return the FQN for the given file object or null.
     */
    public static String getQualifiedName(FileObject fileObject){
        ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        if (classPath != null) {
            return classPath.getResourceName(fileObject, '.', false);
        }
        return null;
    }
    
    /**
     * Unqualifies the given <code>fqn</code>.
     *
     * @param fqn the fully qualified name unqualify. Must not be null or empty 
     * and must represent a valid fully qualified name.
     * @return the unqualified name.
     * @throws IllegalArgumentException if the given <code>fqn</code> was not 
     * a valid fully qualified name.
     */
    public static String unqualify(String fqn){
        isValidFQN(fqn); //NO18N
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot < 0){
            return fqn;
        }
        return fqn.substring(lastDot + 1);
    }

    /**
     * Gets the package name of the given fully qualified class name.
     * 
     * @param fqn the fully qualified class name. Must not be null or empty 
     * and must represent a valid fully qualified name.
     * @return the name of the package, an empty string if there was no package.
     * @throws IllegalArgumentException if the given <code>fqn</code> was not 
     * a valid fully qualified name.
     */
    public static String getPackageName(String fqn) {
        isValidFQN(fqn); //NO18N
        int lastDot = fqn.lastIndexOf("."); // NOI18N
        if (lastDot < 0){
            return "";
        }
        return fqn.substring(0, lastDot);
    }
    
    private static void isValidFQN(String fqn){
        Parameters.notEmpty("fqn", fqn); //NO18N
        if (fqn.lastIndexOf(".") == fqn.length() -1){
            throw new IllegalArgumentException("The given fqn [" + fqn + "] does not represent a fully qualified class name"); //NO18N
        }
    }

}
