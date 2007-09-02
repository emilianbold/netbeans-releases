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

package org.netbeans.spi.gsf;

//import java.io.File;
import org.netbeans.api.gsf.ParserFile;
import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class DefaultParserFile implements ParserFile {
    private FileObject fileObject;
    private String relative;
    private boolean platform;
    
    public DefaultParserFile(FileObject fo, String relative, boolean platform) {
        this.fileObject = fo;
        this.relative = relative;
        this.platform = platform;
    }
    
//    public DefaultParserFile(String path, String relative) {
//        this.path = path;
//        this.relative = relative;
//    }

    public FileObject getFileObject() {
//        if (fileObject == null) {
//            assert path != null;
//            fileObject = FileUtil.toFileObject(new File(path));
//        }
        return fileObject;
    }

    public String getRelativePath() {
        return relative;
    }
    
    public String getNameExt() {
        return getFileObject().getNameExt();
    }
    
    public String getExtension() {
        return getFileObject().getExt();
    }
    
    @Override
    public String toString() {
        return getNameExt();
    }

    public boolean isPlatform() {
        return platform;
    }
}
