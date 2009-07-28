/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.diff;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileEncodingQueryImplementation.class, position=1)
public class DiffFileEncodingQueryImpl extends FileEncodingQueryImplementation {                        
    
    private Map<File, Charset> fileToCharset;
        
    public Charset getEncoding(FileObject fo) {   
        try {
            if(fileToCharset == null || fileToCharset.isEmpty() || fo == null || fo.isFolder()) {
                return null;
            }       
            File file = FileUtil.toFile(fo);            
            if(file == null) {
                return null;
            }
            synchronized(fileToCharset) {
                return fileToCharset.get(file);
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
    void associateEncoding(FileObject referenceFile, Collection<File> files) {
        if (referenceFile.isFolder()) {
            return;
        }
        associateEncoding(FileEncodingQuery.getEncoding(referenceFile), files);
    }   

    /**
     * Associates the given encoding with all files from the given list.
     * A following getEncoding() call for any file from the list will then
     * return the given Charset.
     *
     * @param charset charset that is to be used when encoding the files from the given list
     * @param files files to be encoded with the given charset
     *
     */
    void associateEncoding(Charset charset, Collection<File> files) {
        if (charset == null) {
            return;
        }
        if(fileToCharset == null) {
            fileToCharset = new WeakHashMap<File, Charset>();
        }
        synchronized(fileToCharset) {
            for(File file : files) {
                fileToCharset.put(file, charset);
            }
        }
    }

    /**
     * Resets the asociation to a charset for every given file
     * 
     * @param files the files which have to be deregistered
     */ 
    void resetEncodingForFiles(Collection<File> files) {
        if(fileToCharset == null || files == null || files.isEmpty()) {
            return;
        }
        synchronized(fileToCharset) {
            for(File file : files) {
                fileToCharset.remove(file);
            }
        }
    }
    
}
