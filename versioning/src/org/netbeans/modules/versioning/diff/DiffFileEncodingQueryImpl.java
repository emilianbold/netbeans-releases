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

package org.netbeans.modules.versioning.diff;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * A FileEncodingQueryImplementation which allows to register a reference files encoding 
 * for another files, so that a getEncoding call for a file actualy returns the reference 
 * files encoding.
 * 
 * @author Tomas Stupka
 */
public class DiffFileEncodingQueryImpl extends FileEncodingQueryImplementation {                        
    
    private Map<File, Charset> charsetToFile;
        
    public Charset getEncoding(FileObject fo) {   
        try {
            if(charsetToFile == null || charsetToFile.isEmpty() || fo == null || fo.isFolder()) {
                return null;
            }       
            File file = FileUtil.toFile(fo);            
            if(file == null) {
                return null;
            }
            synchronized(charsetToFile) {
                return charsetToFile.get(file);
            }
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            return  null;
        }        
    }      

    /**
     * Retrieves the Charset for the referenceFile and associates it with
     * all files from the given list. A following getEncoding() call for 
     * any file from the list will then return the referenceFile-s Charset.
     * 
     * @param referrenceFile the file which charset has to be used when encoding the files from the given list
     * @param files files to be encoded with the refernceFile-s charset 
     * 
     */ 
    void associateEncoding(File referenceFile, List<File> files) {
        FileObject fo = FileUtil.toFileObject(referenceFile);
        if(fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(fo);        
        if(c == null) {
            return;
        }
        if(charsetToFile == null) {
            charsetToFile = new WeakHashMap<File, Charset>();
        }        
        synchronized(charsetToFile) {
            for(File file : files) {
                charsetToFile.put(file, c);    
            }            
        }
    }   

    /**
     * Resets the asociation to a charset for every given file
     * 
     * @param files the files which have to be deregistered
     */ 
    void resetEncodingForFiles(List<File> files) {        
        if(charsetToFile == null || files == null || files.size() == 0) {
            return;
        }       
        synchronized(charsetToFile) {
            for(File file : files) {
                charsetToFile.remove(file);
            }
        }
    }
    
}
