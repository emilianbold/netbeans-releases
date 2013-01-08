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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.codeviation.commons.patterns.Factory;
import org.codeviation.commons.patterns.Filter;

/**
 *
 * @author Petr Hrebejk
 */
public final class FileUtil {
    
    private FileUtil() {}

    /** Utility factories */
    public static Factory<URL,File> TO_URL = new UrlFactory();
    public static Factory<String,File> TO_STRING = new StringFactory();
    public static Factory<InputStream,File> TO_INPUT_STREAM = new InputStreamFactory();
    public static Factory<OutputStream,File> TO_OUTPUT_STREAM = new OutputStreamFactory();
        
    public static String asString( File f ) throws IOException {
        
        int size = (int)f.length(); // XXX too long files
        byte[] bbuf = new byte[size];
        
        FileInputStream fis = new FileInputStream(f);        
        int read = fis.read(bbuf);
        
        if ( read < size ) {
            fis.close();
            throw new IOException("Could not read whole file" + f + " read " + read + " instead of " + size);
        }
    
        fis.close();
        
        return new String(bbuf, 0, size);
    }

    public static Iterator<String> asLines(File f) throws FileNotFoundException, IOException {
        return StreamUtil.asLines(new FileInputStream(f));
    }

    public static void copy( File src, File dest ) throws IOException {

        InputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);

        StreamUtil.copy( is, os);

        is.close();
        os.flush();
        os.close();

    }
    
    public static void fromString( File f, String... strings) throws FileNotFoundException, IOException {
        
        OutputStream os = new BufferedOutputStream( new FileOutputStream(f) );
    
        for( String s : strings ) {
            os.write(s.getBytes());
        }
        
        os.close();
    }
    
    public static void fromLines( File f, String... strings) throws FileNotFoundException, IOException {
        
        OutputStream os = new BufferedOutputStream( new FileOutputStream(f) );
    
        for( String s : strings ) {
            os.write(s.getBytes());
            os.write('\n');
        }
        
        os.close();
    }
    
    public static boolean delete(File what) {

        boolean result = true;

        if (what.isDirectory() ) {
            File[] files = what.listFiles();
            if ( files != null && files.length != 0 ) {
                for( File f : files ) {
                    result = result ? delete(f) : false;
                }
            }
        }

        result = result ? what.delete() : false;
        
        return result;
    }



    public static Factory<File,String> fileFactory(File root) {
        return new FileFactory(root, null);
    }
    
    /** File factory which will only find files which satisfy given filter */
    public static Factory<File,String> fileFactory(File root, Filter<File> filter) {
        return new FileFactory(root, filter);
    }

    public static File file(File parent, String... pathElements) {
        return new File(parent, path(File.separatorChar, pathElements));
    }

    public static void assureDirectory( File directory ) throws IOException {
        if ( !directory.isDirectory() ) {
            if (!directory.mkdirs() ) {
                throw new IOException("Can't create directory : " + directory.getAbsolutePath());
            }
        }
    }

    public static String path(String... pathElements) {
        return path( File.pathSeparatorChar, pathElements);
    }
        
    public static String path(char separator, String... pathElements) {
        StringBuilder sb = new StringBuilder();
        
        for( int i = 0; i < pathElements.length; i++ ) {
            sb.append(pathElements[i]);
            if ( i != pathElements.length - 1 ) {
                sb.append(separator);
            }
        }
        
        return sb.toString();
        
    }

    /** If f is absoulte returns f else it returns new File(parent, f.getParh())
     *
     * @param parent
     * @param f
     * @return
     */
    public static File resolve( String parent, String path) {
        return resolve( new File(parent), path == null ? null : new File(path));
    }

    /** If f is absoulte returns f else it returns new File(parent, f.getParh())
     *
     * @param parent
     * @param f
     * @return
     */
    public static File resolve( File parent, String path) {
        return resolve( parent, path == null ? null : new File(path));
    }

    
    /** If f is absoulte returns f else it returns new File(parent, f.getParh())
     *
     * @param parent
     * @param f
     * @return
     */
    public static File resolve( File parent, File path) {

        if ( path == null ) {
            return parent;
        }
        else if ( path.isAbsolute() ) {
            return path;
        }
        else {
            return new File( parent, path.getPath());
        }
        
    }


    
    /** Returns filenane with absolutely no extension. I.e. all after
     * first dot will be striped off.
     * @param name
     */
    public static String nameNoExt(String name) {
        
        if (name == null) {
            return null;
        }
        
        int ild = name.indexOf('.');
        if ( ild == -1 ) {
            return name;
        }
        
        return name.substring(0, ild);
        
    }

    /** Returns all extensions of given file name. E.g. all chars after
     * the first dot.
     * @param name
     */
    public static String extensions(String name) {

        if (name == null) {
            return null;
        }

        int ild = name.indexOf('.');
        if ( ild == -1 ) {
            return "";
        }

        return name.substring(ild + 1, name.length());

    }


    /** Returns extension of given file name. E.g. all chars after
     * the last dot.
     * @param name
     */
    public static String extension(String name) {

        if (name == null) {
            return null;
        }

        int ild = name.lastIndexOf('.');
        if ( ild == -1 ) {
            return "";
        }

        return name.substring(ild + 1, name.length());

    }
    
    public static URL toURL(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }
    
    private static class FileFactory implements Factory<File,String> {

        private File root;
        private Filter<File> filter;

        public FileFactory(File root, Filter<File> filter) {
            this.root = root;
            this.filter = filter;
        }
        
        public File create(String param) {
            File r;
            
            if ( root == null ) {
                r = new File(param);
            }
            else {
                r = new File(root, param);
            }
            
            if ( filter == null ) {
                return r;
            }
            else {            
                return filter.accept(r) ? r : null;
            }
        }
    }
    
    private static class UrlFactory implements Factory<URL,File> {

        public URL create(File file) {            
            try {
                return toURL(file);
            }
            catch ( MalformedURLException ex ) {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
    
    private static class StringFactory implements Factory<String,File> {

        public String create(File file) {
            try {
                return asString(file);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
    
    private static class OutputStreamFactory implements Factory<OutputStream,File> {

        public OutputStream create(File file) {            
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
    
    private static class InputStreamFactory implements Factory<InputStream,File> {

        public InputStream create(File file) {            
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }

}
