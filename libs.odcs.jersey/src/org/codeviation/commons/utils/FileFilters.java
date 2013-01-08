/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.codeviation.commons.utils;

import java.io.File;
import java.io.FileFilter;
import org.codeviation.commons.patterns.Filter;
import org.codeviation.commons.patterns.Filters;

/** 
 *
 * @author Petr Hrebejk
 */
public class FileFilters {
    
    public static final Filter<File> EXISTS = new UniversalFF(UniversalFF.EXISTS);
    public static final Filter<File> CAN_READ = new UniversalFF(UniversalFF.CAN_READ);
    public static final Filter<File> CAN_WRITE = new UniversalFF(UniversalFF.CAN_WRITE);
    // Requires 1.6
    //public static final Filter<File> CAN_EXECUTE = new UniversalFF(UniversalFF.CAN_EXECUTE);
    public static final Filter<File> IS_FILE = new UniversalFF(UniversalFF.IS_FILE);
    public static final Filter<File> IS_DIRECTORY = new UniversalFF(UniversalFF.IS_DIRECTORY);
    public static final Filter<File> IS_HIDDEN = new UniversalFF(UniversalFF.IS_HIDDEN);
    
    private FileFilters() {}

    public static Filter<File> name(String regexp) {
        return new UniversalFF(regexp);
    }

    public static Filter<File> extension(String extension) {
        UniversalFF ff =  new UniversalFF(-1);
        ff.extension = extension;
        return ff;
    }
            
    /** Contverts File&lt;Filter&gt; to java.io.FileFilter.
     */
    public static FileFilter asFileFilter(Filter<File> filter) {
        if ( filter instanceof FileFilter ) {
            return (FileFilter)filter;
        }
        else {
            return new UniversalFF(filter);        
        }
    }
         
    private static class UniversalFF implements Filter<File>, FileFilter {

        public static final int EXISTS = 0;
        public static final int CAN_READ = EXISTS + 1;
        public static final int CAN_WRITE = CAN_READ + 1;
        public static final int CAN_EXECUTE = CAN_WRITE + 1;
        public static final int IS_FILE = CAN_EXECUTE + 1;        
        public static final int IS_DIRECTORY = IS_FILE + 1;
        public static final int IS_HIDDEN = IS_DIRECTORY + 1;

        
        private int kind = -1;
        private Filter<String> rf;
        private Filter<File> df;
        private String extension;
        
        public UniversalFF(int kind) {
            this.kind = kind;
        }
        
        public UniversalFF(String regexp) {
            this.rf = Filters.Regexp(regexp);
        }


        public UniversalFF(Filter<File> df) {
            this.df = df;
        }
                       
        public boolean accept(File file) {
            
            if ( df != null ) {
                return df.accept(file);
            }
            
            if ( rf != null ) {
                return rf.accept(file.getName());                 
            }

            if ( extension != null ) {
                return file.getName().endsWith(extension);
            }
            
            switch(kind) {
                case EXISTS:
                    return file.exists();
                case CAN_READ:
                    return file.canRead();
                case CAN_WRITE:
                    return file.canWrite();
                //case CAN_EXECUTE:
                    //return file.canExecute();
                    //return file.canRead();
                case IS_FILE:
                    return file.isFile();
                case IS_DIRECTORY:
                    return file.isDirectory();
                case IS_HIDDEN:
                    return file.isHidden();
                default:
                    throw new IllegalStateException("Should never happen");
            }
        }
        
    }
    
}
