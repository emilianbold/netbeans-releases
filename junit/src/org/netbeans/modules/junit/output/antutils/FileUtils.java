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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output.antutils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.openide.util.Utilities;

/**
 *
 * @author  Marian Petras
 */
public class FileUtils {

    /**
     */
    FileUtils() {
    }

    /**
     */
    static File resolveFile(File file, String filename) {
        if (!new File(filename).isAbsolute()) {
            filename = filename.replace('/', File.separatorChar)
                               .replace('\\', File.separatorChar);
            filename = new File(file, filename).getAbsolutePath();
        }
        return normalizePath(filename);
    }

    /**
     * Returns a normalized form of a given path.
     *
     * @param  path  path to normalize - must be absolute
     * @return  the path in a normalized form
     */
    static File normalizePath(String path) {
        path = path.replace('/', File.separatorChar)
                   .replace('\\', File.separatorChar);
        
        assert new File(path).isAbsolute();
        
        String root;
        int relPathIndex;
        
        /* Detect type of path, normalize the root and find the beginning
           of the path after the root: */
        if ((path.length() > 1) && (path.charAt(1) == ':')
                && Utilities.isWindows()) {
            
            root = path.substring(0, 2).toUpperCase() + File.separatorChar;
            relPathIndex = path.charAt(2) == '/' ? 3 : 2;
            
        } else {
            root = File.separator;
            relPathIndex = 1;
        }
        
        /* Resolve "." and ".." directories: */
        List<String> stack = new ArrayList<String>(20); //non-synchronized stack
        int stackSize = 0;
        String relPath = removeRedundantSlashes(path.substring(relPathIndex));
        StringTokenizer tokenizer = new StringTokenizer(relPath,
                                                        File.separator);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (".".equals(token)) {                                    //NOI18N
                continue;
            } else if ("..".equals(token)) {                            //NOI18N
                assert stackSize != 0;
                stack.remove(--stackSize);
            } else {
                stack.add(stackSize++, token);
            }
        }
        
        /* Finally build the path:  path = root + path elements */
        StringBuilder sb = new StringBuilder(path.length() + 5);
        sb.append(root);
        if (!stack.isEmpty()) {
            sb.append(stack.get(0));   //there is a separator at the end of root
            for (int i = 1; i < stack.size(); i++) {
                sb.append(File.separatorChar).append(stack.get(i));
            }
        }
        
        return new File(sb.toString());
    }
    
    /**
     */
    static final String removeRedundantSlashes(String path) {
        StringBuilder buf = new StringBuilder(path.length());
        
        boolean wasSeparator = true;
        for (char c : path.toCharArray()) {
            if ((c != File.separatorChar) || !wasSeparator) {
                buf.append(c);
            }
            wasSeparator = (c == File.separatorChar);
        }
        return buf.toString();
    }

}

