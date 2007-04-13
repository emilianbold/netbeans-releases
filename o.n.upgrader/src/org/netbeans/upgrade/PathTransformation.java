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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.upgrade;

/**
 *
 * @author Radek Matous
 */
public class PathTransformation {
    private String version;
    private PathTransformation(String version) {
        this.version = version;
    }
    public static PathTransformation getInstance(String version) {
        return new PathTransformation(version);
    }
    
    public String transformPath(String path) {
        boolean isCreator = version.startsWith("2_");//NOI18N
        if (isCreator) {
            return transformCreatorPath(path);
        }
        return path;
    }
    
    //CREATOR
    public String transformCreatorPath(String path) {
        String keyToReplace = null;
        String[] keysToReplace = new String[] {"context.xml"};//NOI18N
        for (String key : keysToReplace) {
            if (path.startsWith(key)) {
                keyToReplace = key;
                break;
            }
        }
        if (keyToReplace != null) {
            return path.replace(keyToReplace, "config/"+version+"/"+keyToReplace);//NOI18N
        }
        return path;
    }    
}
