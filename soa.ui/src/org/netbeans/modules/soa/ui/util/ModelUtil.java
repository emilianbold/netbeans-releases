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
package org.netbeans.modules.soa.ui.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.StringTokenizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ModelUtil {
    public static final String FORWARD_SLASH = "/"; // NOI18N
    public static final String UP_REL_FOLDER = "../"; // NOI18N
    public static final String CUR_REL_FOLDER = "./"; // NOI18N
    public static final String WSDL = "wsdl"; // NOI18N
    // TODO m
    public static final String SRC = "src"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(ModelUtil.class.getName());

    private ModelUtil() {
    }
    
    public static String rel2absolut(FileObject startPoint
            , String relLocation) {
        if (startPoint == null || relLocation == null) {
            return null;
        }
        
        if (!startPoint.isFolder()) {
            startPoint = startPoint.getParent();
        }
        
        if (relLocation.startsWith(UP_REL_FOLDER)) {
            int upRelLength = UP_REL_FOLDER.length();
            while (relLocation.startsWith(UP_REL_FOLDER)) {
                startPoint = startPoint.getParent();
                relLocation = relLocation.substring(upRelLength);
            }
            
        } else if (relLocation.startsWith(CUR_REL_FOLDER)) {
            relLocation = relLocation.substring(CUR_REL_FOLDER.length());
        }
        return FileUtil.toFile(startPoint).getPath()+"/"+relLocation;
    }
    
    /**
     * TODO m - looks like general utility method for all modules
     */
    public static FileObject getRelativeFO(FileObject startPoint
            , String relLocation) {
        if (startPoint == null || relLocation == null) {
            return null;
        }
        
        if (!startPoint.isFolder()) {
            startPoint = startPoint.getParent();
        }
        
        if (relLocation.startsWith(UP_REL_FOLDER)) {
            int upRelLength = UP_REL_FOLDER.length();
            while (relLocation.startsWith(UP_REL_FOLDER)) {
                startPoint = startPoint.getParent();
                relLocation = relLocation.substring(upRelLength);
            }
            
        } else if (relLocation.startsWith(CUR_REL_FOLDER)) {
            relLocation = relLocation.substring(CUR_REL_FOLDER.length());
        }
        return startPoint.getFileObject(relLocation);
    }
    
    /**
     * TODO m - looks like general utility method for all modules
     */
    public static String getRelativePath(FileObject fromFo, FileObject toFo) {
        String relativePath = FileUtil.getRelativePath(fromFo, toFo);
        if (relativePath != null) {
            return relativePath;
        }
        
        if (!fromFo.isFolder()) {
            fromFo = fromFo.getParent();
        }
        
        StringTokenizer fromPath = new StringTokenizer(FileUtil.toFile(fromFo).getPath()
                , FORWARD_SLASH);
        StringTokenizer toPath = new StringTokenizer(FileUtil.toFile(toFo).getPath()
                , FORWARD_SLASH);
        String tmpFromFolder = null;
        String tmpToFolder = null;
        while (fromPath.hasMoreTokens()) {
            tmpFromFolder = fromPath.nextToken();
            tmpToFolder = toPath.hasMoreTokens() ? toPath.nextToken() : null;
            if (!(tmpFromFolder.equals(tmpToFolder))) {
                break;
            }
        }
        if (tmpToFolder == null) {
            return null;
        }
        
        StringBuffer fromRelativePathPart = new StringBuffer(UP_REL_FOLDER);
        while (fromPath.hasMoreTokens()) {
            fromPath.nextToken();
            fromRelativePathPart.append(UP_REL_FOLDER);
        }
        
        StringBuffer toRelativePathPart = new StringBuffer(tmpToFolder);
        while (toPath.hasMoreTokens()) {
            toRelativePathPart.append(FORWARD_SLASH).append(toPath.nextToken());
        }
        
        return fromRelativePathPart.append(toRelativePathPart).toString();
    }
}
