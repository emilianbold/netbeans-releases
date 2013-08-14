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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.parsing;


import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.ClassDataLoader;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/** Creates various kinds of file objects 
 *
 * XXX - Rename to JavaFileObjects
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 */
public class FileObjects {
    
    public static final Comparator<String> SIMPLE_NAME_STRING_COMPARATOR = new Comparator<String>(){
        @Override
        public int compare( String o1, String o2 ) {
            return getSimpleName( o1 ).compareTo( getSimpleName( o2 ) );
        }
    };
    
    public static final Comparator<JavaFileObject> SIMPLE_NAME_FILEOBJECT_COMPARATOR = new Comparator<JavaFileObject>(){
        @Override
        public int compare( JavaFileObject o1, JavaFileObject o2 ) {
            String n1 = getSimpleName( o1 );
            String n2 = getSimpleName( o2 );
            return n1.compareTo( n2 );
        }
    };
    
    
    public static final String JAVA  = JavaDataLoader.JAVA_EXTENSION;
    public static final String CLASS = ClassDataLoader.CLASS_EXTENSION;
    public static final String JAR   = "jar";  //NOI18N
    public static final String FILE  = "file"; //NOI18N
    public static final String ZIP   = "zip";  //NOI18N
    public static final String HTML  = "html"; //NOI18N
    public static final String SIG   = "sig";  //NOI18N
    public static final String RS    = "rs";   //NOI18N
    public static final String RX    = "rx";   //NOI18N
    public static final String RAPT  = "rapt"; //NOI18N
    public static final String RES   = "res";  //NOI18N
    public static final char NBFS_SEPARATOR_CHAR = '/';  //NOI18N

    public static final String RESOURCES = "resouces." + FileObjects.RES;  //NOI18N

    private static final String encodingName = new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();    
    //todo: If more clients than btrace will need this, create a SPI.
    private static final Set<String> javaFlavorExt = new HashSet<String>();
    static {
        javaFlavorExt.add("btrace");    //NOI18N
    }

    /** Creates a new instance of FileObjects */
    private FileObjects() {
    }
    
    // Public methods ----------------------------------------------------------
    
    
    
    /**
     * Creates {@link JavaFileObject} for a file inside an archive file. The archive file
     * is opened every time an input stream of this {@link JavaFileObject} is needed, it may
     * slow down the javac attribution.
     * @param zip an archive file
     * @param folder in the archive
     * @param name the base (simple name)
     * @return {@link JavaFileObject}, never returns null
     */
    public static InferableJavaFileObject zipFileObject( File zipFile, String folder, String baseName, long mtime) {
        assert zipFile != null;                
        return new ZipFileObject( zipFile, folder, baseName, mtime);
    }
    
    /**
     * Creates {@link JavaFileObject} for a file inside an archive file. The returned {@link JavaFileObject}
     * tries to use {@link RandomAccessFile} to read the archive entry, in the case when it's not able to
     * find the entry (unsupported zip file format) it delegates into the {@link ZipFile}.
     * @param zip an archive file
     * @param folder in the archive
     * @param name the base (simple name)
     * @param offset the start of zip entry in the zip file
     * @return {@link JavaFileObject}, never returns null
     */
    public static InferableJavaFileObject zipFileObject (File zipFile, String folder, String baseName, long mtime, long offset) {
        assert zipFile != null;
        return new FastZipFileObject (zipFile, folder, baseName, mtime, offset);
    }
    
    /**
     * Creates {@link JavaFileObject} for a file inside an {@link ZipFile}. The returned {@link JavaFileObject}
     * uses an opened ZipFile. It's a fastes way to read the archive file content, but the opened {@link ZipFile}s
     * cannot be modified. So, this {@link JavaFileObject}s can be used only for platform classpath.
     * @param zip an archive file
     * @param folder in the archive
     * @param name the base (simple name)
     * @param offset the start of zip entry in the zip file
     * @return {@link JavaFileObject}, never returns null
     */
    public static InferableJavaFileObject zipFileObject(
            @NonNull final ZipFile zipFile,
            @NonNull final String folder,
            @NonNull final String baseName,
            final long mtime) {
        return zipFileObject(zipFile, null, folder, baseName, mtime);
    }


    /**
     * Creates {@link JavaFileObject} for a file inside an {@link ZipFile}. The returned {@link JavaFileObject}
     * uses an opened ZipFile. It's a fastes way to read the archive file content, but the opened {@link ZipFile}s
     * cannot be modified. So, this {@link JavaFileObject}s can be used only for platform classpath.
     * @param zip an archive file
     * @param pathToRootInArchive  relative path to root in the archive
     * @param folder in the archive
     * @param name the base (simple name)
     * @param offset the start of zip entry in the zip file
     * @return {@link JavaFileObject}, never returns null
     */
    public static InferableJavaFileObject zipFileObject(
            @NonNull final ZipFile zipFile,
            @NullAllowed final String pathToRootInArchive,
            @NonNull final String folder,
            @NonNull final String baseName,
            final long mtime) {
        assert zipFile != null;
        return new CachedZipFileObject (zipFile, pathToRootInArchive, folder, baseName, mtime);
    }
            
    /**
     * Creates {@link JavaFileObject} for a regular {@link File}
     * @param file for which the {@link JavaFileObject} should be created
     * @param root - the classpath root owning the file
     * @param encoding - the file's encoding
     * @return {@link JavaFileObject}, never returns null
     */
    public static @NonNull PrefetchableJavaFileObject fileFileObject( final @NonNull File file, final @NonNull File root,
            final @NullAllowed JavaFileFilterImplementation filter, final @NullAllowed Charset encoding) {
        assert file != null;
        assert root != null;
        final String[] pkgNamePair = getFolderAndBaseName(getRelativePath(root,file),File.separatorChar);
        return new FileBase( file, convertFolder2Package(pkgNamePair[0], File.separatorChar), pkgNamePair[1], filter, encoding);
    }

    /**
     * Creates {@link File} based {@link JavaFileObject} for {@link Indexable}.
     * @param indexable for which the {@link JavaFileObject} should be created
     * @param root - the classpath root owning the file
     * @param encoding - the file's encoding
     * @return {@link JavaFileObject}, never returns null
     */
    @NonNull
    public static PrefetchableJavaFileObject fileFileObject(
        @NonNull final Indexable indexable,
        @NonNull final File root,
        @NullAllowed final JavaFileFilterImplementation filter,
        @NullAllowed final Charset encoding) throws IOException {
        assert indexable != null;
        assert root != null;
        final String[] pkgNamePair = getFolderAndBaseName(
                indexable.getRelativePath(),
                NBFS_SEPARATOR_CHAR);
        try {
            final File file = Utilities.toFile(indexable.getURL().toURI());
            return new FileBase(
                file,
                convertFolder2Package(pkgNamePair[0]),
                pkgNamePair[1],
                filter,
                encoding);
        } catch (URISyntaxException use) {
            throw new IOException(use);
        }
    }

    /**
     * Creates a FileObject for newly created file
     * @param root owning the file
     * @param path the path (separated by '/') to target folder relative to root
     * @param name of the file
     * @return {@link JavaFileObject}
     */
    public static @NonNull JavaFileObject templateFileObject (final @NonNull FileObject root, final @NonNull String path, final @NonNull String name) {
        assert root != null;
        assert path != null;        
        JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(root);
        Charset encoding = FileEncodingQuery.getEncoding(root);
        File rootFile = FileUtil.toFile(root);
        if (rootFile == null) {
            throw new IllegalArgumentException ();
        }
        File file = FileUtil.normalizeFile(new File (new File (rootFile, path.replace(NBFS_SEPARATOR_CHAR, File.separatorChar)), name));
        return new NewFromTemplateFileObject (file, convertFolder2Package(path), name, filter, encoding);
    }
    
    /**
     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject}
     * Any client which needs to create {@link JavaFileObject} for java
     * source file should use this factory method.
     * @param {@link FileObject} for which the {@link JavaFileObject} should be created
     * @param {@link FileObject} root owning the file
     * @return {@link JavaFileObject}, never returns null
     * @exception {@link IOException} may be thrown
     */
    public static SourceFileObject nbFileObject (final FileObject file, final FileObject root) throws IOException {
        return nbFileObject (file, root, null, false);
    }

        /**
     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject} which may not exist (the FileObject is resolved on demand)
     * Any client which needs to create {@link JavaFileObject} for java
     * source file should use this factory method.
     * @param path of the {@link JavaFileObject} should be created
     * @param {@link FileObject} root owning the file
     * @return {@link JavaFileObject}, never returns null
     * @exception {@link IOException} may be thrown
     */
    public static SourceFileObject nbFileObject (final URL path, final FileObject root) throws IOException {
        final SourceFileObject.Handle handle = new SourceFileObject.Handle(root){

            @Override
            protected FileObject resolveFileObject(boolean write) {
                FileObject res = super.resolveFileObject(write);
                if (res == null) {
                    try {
                        if (write) {
                            //Create the file
                            file = FileUtil.createData(root,getRelativePath());
                        } else {
                            //Resolve file
                            file = URLMapper.findFileObject(path);
                        }
                        res = file;
                    } catch (IOException e) {
                        //pass, return null
                    }
                }
                return res;
            }

            @Override
            protected URL getURL() throws IOException {
                return path;
            }

            @Override
            protected String getExt() {
                String ext = super.getExt();
                if (ext == null) {
                    ext = FileObjects.getExtension(path.getPath());
                }
                return ext;
            }

            @Override
            protected String getName(boolean includeExtension) {
                String name = super.getName(includeExtension);
                if (name == null) {
                    name = FileObjects.getBaseName(path.getPath(),NBFS_SEPARATOR_CHAR);
                    if (!includeExtension) {
                        name = FileObjects.stripExtension(name);
                    }
                }
                return name;
            }

            @Override
            protected String getRelativePath() {
                String relativePath = super.getRelativePath();
                if (relativePath == null) {
                    try {
                        relativePath = FileObjects.getRelativePath(root.getURL(), path);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return relativePath;
            }

            @Override
            public boolean equals(Object obj) {
                if (file != null) {
                    return super.equals(obj);
                } else if (obj instanceof SourceFileObject.Handle) {
                    try {
                        return getURL().equals(((SourceFileObject.Handle) obj).getURL());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return false;
            }

            @Override
            public int hashCode() {
                return path.hashCode();
            }

        };
        return new SourceFileObject(handle, null);
    }

    /**
     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject}
     * Any client which needs to create {@link JavaFileObject} for java
     * source file should use this factory method.
     * @param {@link FileObject} for which the {@link JavaFileObject} should be created
     * @param {@link FileObject} root owning the file
     * @param renderNow if true the snap shot of the file is taken immediately
     * @return {@link JavaFileObject}, never returns null
     * @exception {@link IOException} may be thrown
     */
    public static SourceFileObject nbFileObject (final FileObject file, final FileObject root, JavaFileFilterImplementation filter, boolean renderNow) throws IOException {
        assert file != null;
        if (!file.isValid() || file.isVirtual()) {
            throw new InvalidFileException (file);
        }
        return new SourceFileObject (file, root, filter, renderNow);
    }
    
    /**
     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject}
     * Any client which needs to create {@link JavaFileObject} for java
     * source file should use this factory method.
     * @param {@link FileObject} for which the {@link JavaFileObject} should be created
     * @param {@link FileObject} root owning the file
     * @param renderNow if true the snap shot of the file is taken immediately
     * @return {@link JavaFileObject}, never returns null
     * @exception {@link IOException} may be thrown
     */
    public static SourceFileObject nbFileObject (final FileObject file, final FileObject root,
            final JavaFileFilterImplementation filter, final CharSequence content) throws IOException {
        assert file != null;
        if (!file.isValid() || file.isVirtual()) {
            throw new InvalidFileException (file);
        }
        return new SourceFileObject (file, root, filter, content);
    }
    
    /**
     * Creates virtual {@link JavaFileObject} with given name and content.
     * This method should be used only by tests, regular client should never
     * use this method.
     * @param pkg packageName     
     * @param name the name of the {@link JavaFileObject}
     * @param content the content of the {@link JavaFileObject}
     * @return {@link JavaFileObject}, never returns null
     */
    public static PrefetchableJavaFileObject memoryFileObject(final CharSequence pkg, final CharSequence name, CharSequence content) {
        return memoryFileObject(pkg, name, null, System.currentTimeMillis(), content);
    }
    /**
     * Creates virtual {@link JavaFileObject} with given name and content.
     * This method should be used only by tests, regular client should never
     * use this method.
     * @param pkg packageName     
     * @param name the name of the {@link JavaFileObject}
     * @param URI uri of the {@link JavaFileObject}, if null the relative URI
     * in the form binaryName.extension is generated.
     * @param lastModified mtime of the virtual file
     * @param content the content of the {@link JavaFileObject}
     * @return {@link JavaFileObject}, never returns null
     */
    public static PrefetchableJavaFileObject memoryFileObject(final CharSequence pkg, final CharSequence name,
        final URI uri, final long lastModified, final CharSequence content) {
        Parameters.notNull("pkg", pkg);
        Parameters.notNull("name", name);
        Parameters.notNull("content", content);
        final String pkgStr  = (pkg instanceof String) ? (String) pkg : pkg.toString();
        final String nameStr = (name instanceof String) ? (String) name : name.toString();        
        int length = content.length();        
        if ( length != 0 && Character.isWhitespace( content.charAt( length - 1 ) ) ) {
            return new MemoryFileObject(pkgStr, nameStr, uri, lastModified, CharBuffer.wrap( content ) );
        }
        else {
            return new MemoryFileObject(pkgStr, nameStr, uri, lastModified, (CharBuffer)CharBuffer.allocate( length + 1 ).append( content ).append( ' ' ).flip() );
        }        
    }

    /**
     * Wraps an existing JFO so that writes + creations on the file are suppressed. Does not wrap
     * if the JFO is already a wrapper(performance).
     *
     * @return JFO wrapper which silently ignores modifications
     */
    public static InferableJavaFileObject nullWriteFileObject(@NonNull final InferableJavaFileObject delegate) {
        return delegate instanceof NullWriteFileObject ? delegate : new NullWriteFileObject(delegate);
    }

    /**
     * Removes extension from fileName
     * @param fileName to remove extension from
     * @return the fileName without extension
     */
    public static String stripExtension( String fileName ) {        
        int dot = fileName.lastIndexOf(".");
        return (dot == -1 ? fileName : fileName.substring(0, dot));
    }    

    /**
     * Returns file extension
     * @param fileName to get extension from
     * @return the file extension or empty string when fileName has no extension
     */
    public static String getExtension (final String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot == -1 || dot == fileName.length() -1 ) ? "" : fileName.substring(dot+1);    //NOI18N
    }
    
    
    /**
     * Returns the name of JavaFileObject, similar to
     * {@link java.io.File#getName}
     */
    public static String getName (final JavaFileObject fo, final boolean noExt) {
        assert fo != null;
        if (fo instanceof Base) {
            Base baseFileObject = (Base) fo;
            return noExt ? baseFileObject.getNameWithoutExtension() : baseFileObject.getName();
        }
        try {
            final URL url = fo.toUri().toURL();
            String path = url.getPath();
            int index1 = path.lastIndexOf(NBFS_SEPARATOR_CHAR);
            int len;
            if (noExt) {
               final int index2 = path.lastIndexOf('.');
               if (index2>index1) {
                   len = index2;
               }
               else {
                   len = path.length();
               }
            }
            else {
                len = path.length();
            }
            path = path.substring(index1+1,len);
            return path;
        } catch (MalformedURLException e) {
            return null;
        }        
    }
        
    
    /**
     * Returns the basename name without folder path
     *  @param file name, eg. obtained from {@link FileObjects#getPath} or {java.io.File.getPath}
     *  @return the base name
     *  @see #getBaseName(String,char)
     */
    public static String getBaseName( String fileName ) {
        return getBaseName(fileName, File.separatorChar);
    }
    
    /**
     * Returns the basename name without folder path. You can specify
     * the path separator since eg zip files uses '/' regardless of platform.
     *  @param file name, eg. obtained from {@link FileObjects#getPath} or {java.io.File.getPath}
     *  @param separator path separator
     *  @return the base name
     */
    public static String getBaseName( String fileName, char separator ) {
        return getFolderAndBaseName(fileName, separator)[1];
    }
    
    
    /**
     *Returns the folder (package name separated by original separators)
     *and base name.
     * @param path
     * @return array of 2 strings, 1st the folder 2nd the base name
     */
    public static String[] getFolderAndBaseName (final String fileName, final char separator) {
        final int i = fileName.lastIndexOf( separator );
        if ( i == -1 ) {
            return new String[] {"",fileName};  //NOI18N
        }
        else {
            return new String[] {
                fileName.substring(0,i),
                fileName.substring( i + 1 )
            };
        }
    }

    /**
     * Returns binary name of given file within a root
     * @param file to get binary name for
     * @param root the root owning the file
     * @return the binary name
     */
    public static @NonNull String getBinaryName (final @NonNull File file, final @NonNull File root) {
        assert file != null && root != null;
        String fileName = FileObjects.getRelativePath (root, file);
        int index = fileName.lastIndexOf('.');  //NOI18N
        if (index > 0) {
            fileName = fileName.substring(0,index);
        }        
        return fileName.replace(File.separatorChar,'.');   //NOI18N        
    }

    /**
     * Converts a package name into folder using '/' as separator character
     * @param packageName to be converted
     * @return the folder name
     */
    public static @NonNull String convertPackage2Folder(final @NonNull String packageName ) {
        return convertPackage2Folder(packageName, NBFS_SEPARATOR_CHAR );
    }

    /**
     * Converts a package name into folder using given separator character
     * @param packageName to be converted
     * @param separatorChar separator
     * @return the folder name
     */
    public static @NonNull String convertPackage2Folder(final @NonNull String packageName, final char separatorChar) {
        return packageName.replace( '.',separatorChar); //NOI18N
    }

    /**
     * Converts a folder into package name
     * @param folderName to be converted, the folder name components has to be separated by '/'
     * @return the package name
     */
    public static @NonNull String convertFolder2Package (@NonNull String folderName) {
        return convertFolder2Package (folderName, NBFS_SEPARATOR_CHAR);
    }

    /**
     * Converts a folder into package name
     * @param folderName to be converted
     * @param separatorChar separator used in folderName
     * @return the package name
     */
    public static @NonNull String convertFolder2Package( @NonNull String folderName, char folderSeparator ) {
        return folderName.replace( folderSeparator, '.' );
    }

    /**
     * Checks if the given folder is a parent of the given file
     * @param folder to test
     * @param file to test
     * @return true if folder is a parent of given file
     */
    public static boolean isParentOf (final @NonNull URL folder, final @NonNull URL file) {
        assert folder != null && file != null;
        return file.toExternalForm().startsWith(folder.toExternalForm());
    }

    /**
     * Resolves a relative path within a given package
     * @param packageName in which the relative path should be resolved
     * @param relativeName to resolve
     * @return a relative path resolved in package as path separated by '/' character
     */
    public static @NonNull String getRelativePath (final @NonNull String packageName, final @NonNull String relativeName) {
        if (packageName.isEmpty()) return relativeName;
        StringBuilder relativePath = new StringBuilder ();
        relativePath.append(packageName.replace('.',NBFS_SEPARATOR_CHAR));  //NOI18N
        relativePath.append(NBFS_SEPARATOR_CHAR);
        relativePath.append(relativeName);
        return relativePath.toString();
    }

    /**
     * Returns a tuple {parentPath,simpleName} for given fully qualified name
     * @param fqn to get the parent name tuple for
     * @return a tuple {parentPath, simpleName}
     */
    public static @NonNull String[] getParentRelativePathAndName (@NonNull final String fqn) {
        final String[] result = getPackageAndName(fqn);
        if (result != null) {
            result[0] = result[0].replace('.',NBFS_SEPARATOR_CHAR);      //NOI18N
        }
        return result;
    }     

    /**
     * Returns a tuple {package,simpleName} for given fully qualified name
     * @param fqn to get the package simpleName tuple for
     * @return a tuple {package,simpleName}
     */
    public static @NonNull String[] getPackageAndName (final @NonNull String fqn) {
        if (fqn.charAt(fqn.length()-1) == '.') {
            return null;
        }
        final int index = fqn.lastIndexOf('.');
        if (index<0) {
            return new String[] {
                "",     //NOI18N
                fqn
            };
        }
        else {
            return new String[] {
                fqn.substring(0,index),
                fqn.substring(index+1)
            };
        }
    }
    
    
    /**
     * Determines {@link JavaFileObject.Kind} for given extension
     * @param extension
     * @return the found kind
     */ 
    public static @NonNull JavaFileObject.Kind getKind (final @NullAllowed String extension) {
        if (extension == null) {
            return JavaFileObject.Kind.OTHER;
        }
        final String lcextension = extension.toLowerCase();
        if (FileObjects.JAVA.equals(lcextension)) {
                return JavaFileObject.Kind.SOURCE;
        }
        if (FileObjects.CLASS.equals(lcextension) || FileObjects.SIG.equals(lcextension)) {
                return JavaFileObject.Kind.CLASS;
        }
        if (FileObjects.HTML.equals(lcextension)) {
                return JavaFileObject.Kind.HTML;
        }
        if (javaFlavorExt.contains(lcextension))  {
            return JavaFileObject.Kind.SOURCE;
        }
        return JavaFileObject.Kind.OTHER;
    }

    /**
     * Recursively deletes the folder
     * @param folder to be deleted
     */
    public static void deleteRecursively (final @NonNull File folder) {
        assert folder != null;        
        if (folder.isDirectory()) {
            File[] children = folder.listFiles();
            if (children != null) {
                for (File file : children) {
                    deleteRecursively(file);
                }
            }
        }
        folder.delete();
    }

    /**
     * Returns a relative path of given file in given root
     * @param root owning the file
     * @param fo a file to get the relative path for
     * @return the relative path
     */
    public static @NonNull String getRelativePath (final @NonNull File root, final @NonNull File fo) {
        final String rootPath = root.getAbsolutePath();
        final String foPath = fo.getAbsolutePath();
        assert foPath.startsWith(rootPath) : String.format("getRelativePath(%s, %s)", rootPath, foPath);    //NOI18N
        int index = rootPath.length();
        if (rootPath.charAt(index - 1) != File.separatorChar) {
            index++;
        }
        int foIndex = foPath.length();
        if (foIndex <= index) {
            return ""; //NOI18N
        }
        return foPath.substring(index);
    }

    /**
     * Returns a relative path of given file in given root
     * @param root owning the file
     * @param fo a file to get the relative path for
     * @return the relative path
     */
    public static @NonNull String getRelativePath (@NonNull final URL root, @NonNull final URL fo) throws URISyntaxException {
        final String path = getRelativePath(Utilities.toFile(root.toURI()), Utilities.toFile(fo.toURI()));
        return path.replace(File.separatorChar, NBFS_SEPARATOR_CHAR);
    }
    
    @NonNull
    public static byte[] asBytes(@NonNull final File file) throws IOException {
        byte[] data = new byte[(int)file.length()];
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            int read, len = 0;
            while ((read=in.read(data, len, data.length-len))>0) {
                len+=read;
            }
            if (len != data.length) {
                data = Arrays.copyOf(data, len);
            }
        } finally {
            in.close();
        }
        return data;
    }

    // <editor-fold defaultstate="collapsed" desc="Private helper methods">
    private static CharSequence getCharContent(InputStream ins, Charset encoding, JavaFileFilterImplementation filter, long expectedLength, boolean ignoreEncodingErrors) throws IOException {
        char[] result;
        Reader in;

        if (encoding != null) {
            in = new InputStreamReader (ins, encoding);
        } else {
            in = new InputStreamReader (ins);
        }
        if (filter != null) {
            in = filter.filterReader(in);
        }
        int red = 0;
        try {
            int len = (int) expectedLength;
            if (len == 0) len++; //len - red would be 0 while reading from the stream
            result = new char [len+1];
            int rv;
            while ((rv=in.read(result,red,len-red))>=0) {
                red += rv;
                //In case the filter enlarged the file
                if (red == len) {
                    char[] _tmp = new char[2*len];
                    System.arraycopy(result, 0, _tmp, 0, len);
                    result = _tmp;
                    len = result.length;
                }
            }
        } finally {
            in.close();
        }
        result[red++]='\n'; //NOI18N
        CharSequence buffer = CharBuffer.wrap (result, 0, red);
        return buffer;
    }

    private static String getSimpleName(JavaFileObject fo ) {
        String name = getName(fo,true);
        int i = name.lastIndexOf( '$' );
        if ( i == -1 ) {
            return name;
        }
        else {
            return name.substring( i + 1 );
        }
    }

    private static String getSimpleName( String fileName ) {

        String name = getBaseName( fileName );

        int i = name.lastIndexOf( '$' );
        if ( i == -1 ) {
            return name;
        }
        else {
            return name.substring( i + 1 );
        }

    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="JavaFileObject implementation">    
    public static abstract class Base implements InferableJavaFileObject {

        protected final JavaFileObject.Kind kind;
        protected final String pkgName;
        protected final String nameWithoutExt;
        protected final String ext;        
        
        protected Base (final String pkgName, final String name) {
            assert pkgName != null;
            assert name != null;
            this.pkgName = pkgName;
            String[] res = getNameExtPair(name);
            this.nameWithoutExt = res[0];
            this.ext = res[1];
            this.kind = FileObjects.getKind (this.ext);
        }
                
        @Override
        public JavaFileObject.Kind getKind() {
            return this.kind;
        }
        
        @Override
        public boolean isNameCompatible (String simplename, JavaFileObject.Kind k) {
            return this.kind == k && nameWithoutExt.equals(simplename);
	}        
        
        @Override
        public NestingKind getNestingKind() {
            return null;
        }
        
        @Override
        public Modifier getAccessLevel() {
            return null;
        }
    
        @Override
        public String toString() {
            return this.toUri().toString();
        }
        
        public String getPackage () {
            return this.pkgName;
        }
        
        public String getNameWithoutExtension () {
            return this.nameWithoutExt;
        }
        
        @Override
        public String getName () {
            return this.nameWithoutExt + '.' + ext;
        }
        
        public String getExt () {
            return this.ext;
        }
        
        public boolean isVirtual () {
            return false;
        }
        
        @Override
        public final String inferBinaryName () {
            final StringBuilder sb = new StringBuilder ();
            sb.append (this.pkgName);
            if (sb.length()>0) {
                sb.append('.'); //NOI18N
            }
            sb.append(this.nameWithoutExt);
            return sb.toString();   
        }
        
        private static String[] getNameExtPair (String name) {
            int index = name.lastIndexOf ('.');            
            String namenx;
            String ext;
            if (index <= 0) {
                namenx =name;
                ext = "";   //NOI18N
            }
            else {
                namenx = name.substring(0,index);
                if (index == name.length()-1) {
                    ext = "";
                }
                else {
                    ext = name.substring(index+1);
                }
            }
            return new String[] {
              namenx,
              ext
            };
        }
    }

    @Trusted
    public static class FileBase extends Base implements PrefetchableJavaFileObject {

        private static final boolean isWindows = Utilities.isWindows();
        protected final File f;
        protected final JavaFileFilterImplementation filter;
        protected final Charset encoding;
        URI uriCache;
        private volatile CharSequence data;

        protected FileBase (final File file,
                final String pkgName,
                final String name,
                final JavaFileFilterImplementation filter,
                final Charset encoding) {
            super (pkgName, name);
            assert file != null;
            this.f = file;
            this.filter = filter;
            this.encoding = encoding;
        }

        public File getFile () {
            return this.f;
        }

        @Override
        public InputStream openInputStream() throws IOException {
	    return new BufferedInputStream (new FileInputStream(f));
	}

        @Override
	public Reader openReader (boolean b) throws IOException {
	    return encoding == null ?
                new InputStreamReader(openInputStream()):
                new InputStreamReader(openInputStream(), encoding);
	}

        @Override
	public OutputStream openOutputStream() throws IOException {
            final File parent = f.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
	    return new FileOutputStream(f);
	}

        @Override
	public Writer openWriter() throws IOException {
            if (encoding != null) {
                return new OutputStreamWriter(openOutputStream(), encoding);
            } else {
                return new OutputStreamWriter(openOutputStream());
            }
	}

        @Override
	public boolean isNameCompatible(String simplename, JavaFileObject.Kind kind) {
            return isWindows ?
                this.kind == kind && nameWithoutExt.equalsIgnoreCase(simplename) :
                super.isNameCompatible(simplename, kind);
	}

        @Override
        public URI toUri () {
            if (this.uriCache == null) {
                this.uriCache = Utilities.toURI(f);
            }
            return this.uriCache;
        }

        @Override
        public long getLastModified() {
	    return f.lastModified();
	}

        @Override
	public boolean delete() {
	    return f.delete();
	}

        @Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            CharSequence res = data;
            if (res == null) {
                res = getCharContentImpl(ignoreEncodingErrors);
            }
            return res;
        }        

	@Override
	public boolean equals(final Object other) {
	    if (!(other instanceof FileBase))
		return false;
	    final FileBase o = (FileBase) other;
	    return f.equals(o.f);
	}

	@Override
	public int hashCode() {
	    return f.hashCode();
	}
        
        @Override
        public int prefetch() throws IOException {
            final CharSequence chc = getCharContentImpl(true);
            data = chc;
            return chc.length();
        }
        
        @Override
        public int dispose() {
            final CharSequence _data = data;
            if (_data != null) {
                data = null;
                return _data.length();
            } else {
                return 0;
            }
        }
        
        private CharSequence getCharContentImpl(boolean ignoreEncodingErrors) throws IOException {
            return FileObjects.getCharContent(openInputStream(), encoding, filter, f.length(), ignoreEncodingErrors);
        }
    }
        
    @Trusted
    private static class NewFromTemplateFileObject extends FileBase {

        public NewFromTemplateFileObject (File f, String packageName, String baseName, JavaFileFilterImplementation filter, Charset encoding) {
            super (f,packageName,baseName, filter, encoding);
        }
        
        @Override
        public InputStream openInputStream () throws IOException {
            if (f.exists()) {
                return super.openInputStream();
            }
            return new ByteArrayInputStream (new byte[0]);
        }
        
        @Override
        public Reader openReader (boolean b) throws IOException {
            if (f.exists()) {
                return super.openReader(b);
            }
            return new StringReader ("");   //NOI18N
        }
        
        @Override
        public OutputStream openOutputStream () throws IOException {
            if (!f.exists()) {
                create ();
            }
            return super.openOutputStream();
        }
        
        @Override 
        public Writer openWriter () throws IOException {
            if (!f.exists()) {
                create ();
            }
            return super.openWriter();
        }
        
        @Override
        public CharSequence getCharContent (boolean ignoreEncodingErrors) throws IOException {
            if (f.exists()) {
                return super.getCharContent(ignoreEncodingErrors);
            }
            return "";                      //NOI18N
        }
        
        private void create() throws IOException {
            File parent = f.getParentFile();
            FileObject parentFo = FileUtil.createFolder(parent);
            assert parentFo != null;
            DataFolder target = DataFolder.findFolder(parentFo);
            FileObject template = FileUtil.getConfigFile("Templates/Classes/Empty.java");     //NOI18N
            if(template == null) {
                FileUtil.createData(parentFo, f.getName());
                return;
            }
            DataObject templateDobj = DataObject.find(template);
            String simpleName = FileObjects.stripExtension(f.getName());
            DataObject newDobj = templateDobj.createFromTemplate(target, simpleName);
            assert newDobj != null;
        }
    }

    abstract static class ZipFileBase extends Base {
        
        protected final long mtime;
        protected final String resName;
        
        public ZipFileBase (
                @NullAllowed final String pathToRootInArchive,
                @NonNull final String folderName,
                @NonNull final String baseName,
                final long mtime) {
            super (convertFolder2Package(folderName),baseName);
            assert pathToRootInArchive == null || pathToRootInArchive.charAt(pathToRootInArchive.length()-1) == NBFS_SEPARATOR_CHAR;
            this.mtime = mtime;
            if (folderName.length() == 0) {
                if (pathToRootInArchive != null) {
                    final StringBuilder rn = new StringBuilder(
                            pathToRootInArchive.length() + baseName.length());
                    rn.append(pathToRootInArchive);
                    rn.append(baseName);
                    resName = rn.toString();
                } else {
                    this.resName = baseName;
                }
            } else {
                final int pathToRootLen = pathToRootInArchive == null ?
                        0 :
                        pathToRootInArchive.length();
                final StringBuilder rn = new StringBuilder (
                        pathToRootLen + folderName.length() + 1 + baseName.length());
                if (pathToRootInArchive != null) {
                    rn.append(pathToRootInArchive);
                }
                rn.append(folderName);
                rn.append(NBFS_SEPARATOR_CHAR);
                rn.append(baseName);
                this.resName = rn.toString();
            }
        }
        
        @Override
        public OutputStream openOutputStream() throws IOException {
	    throw new UnsupportedOperationException();
	}

        @Override
	public Reader openReader(boolean b) throws IOException {
            if (this.getKind() == JavaFileObject.Kind.CLASS) {
                throw new UnsupportedOperationException();
            }
            else {
                return new InputStreamReader (openInputStream(),FileObjects.encodingName);
            }
	}

        @Override
        public Writer openWriter() throws IOException {
	    throw new UnsupportedOperationException();
	}
        
        @Override
        public long getLastModified() {
	    return mtime;
	}

        @Override
	public boolean delete() {
	    throw new UnsupportedOperationException();
	}

        @Override
        @SuppressWarnings("empty-statement")
	public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
	    Reader r = openReader(ignoreEncodingErrors);
            try {
                int red = 0, rv;

                int len = (int)this.getSize();
                char[] result = new char [len+1];
                while ((rv=r.read(result,red,len-red))>0 && (red=red+rv)<len);

                int j=0;
                for (int i=0; i<red;i++) {
                    if (result[i] =='\r') {                                          //NOI18N
                        if (i+1>=red || result[i+1]!='\n') {                         //NOI18N
                            result[j++] = '\n';                                      //NOI18N
                        }
                    }
                    else {
                        result[j++] = result[i];
                    }
                }
                result[j]='\n';                                                      //NOI18N
                return CharBuffer.wrap (result,0,j);
            } finally {
                r.close();
            }
	}
        
        @Override
        public final URI toUri () {
            URI  zdirURI = this.getArchiveURI();
            try {
                //Optimistic try and see
                return new URI ("jar:"+zdirURI.toString()+"!/"+resName);  //NOI18N
            } catch (URISyntaxException e) {
                //Need to encode the resName part (slower)
                final StringBuilder sb = new StringBuilder ();
                final String[] elements = resName.split("/");                 //NOI18N
                try {
                    for (int i = 0; i< elements.length; i++) {
                        String element = elements[i];
                        element = URLEncoder.encode(element, "UTF-8");       //NOI18N
                        element = element.replace("+", "%20");               //NOI18N
                        sb.append(element);
                        if (i< elements.length - 1) {
                            sb.append(NBFS_SEPARATOR_CHAR);
                        }
                    }
                    return new URI("jar:"+zdirURI.toString()+"!/"+sb.toString());    //NOI18N
                } catch (final UnsupportedEncodingException e2) {
                    throw new IllegalStateException(e2);
                }
                catch (final URISyntaxException e2) {
                    throw new IllegalStateException(e2);
                }
            }
        }
        
        @Override
	public int hashCode() {
	    return this.resName.hashCode();
	}                
        
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof ZipFileBase))
		return false;
	    ZipFileBase o = (ZipFileBase) other;
	    return getArchiveURI().equals(o.getArchiveURI()) && resName.equals(o.resName);
	}
        
        protected abstract URI getArchiveURI ();
        
        protected abstract long getSize() throws IOException;
        
    }

    @Trusted
    private static class ZipFileObject extends ZipFileBase {
	

	/** The zipfile containing the entry.
	 */
	protected final File archiveFile;
        

        ZipFileObject(final File archiveFile, final String folderName, final String baseName, long mtime) {
            super (null, folderName,baseName,mtime);
            assert archiveFile != null : "archiveFile == null";   //NOI18N
	    this.archiveFile = archiveFile;
            
	}

        @Override
        public InputStream openInputStream() throws IOException {            
            class ZipInputStream extends InputStream {

                private ZipFile zipfile;
                private InputStream delegate;

                /**
                 * Creates new ZipInputStream.
                 * When ZipInputStream is created it owns the given ZipFile
                 * and closes it when this InputStream is closed or IOException
                 * is thrown by the constructer.
                 */
                public ZipInputStream (ZipFile zf) throws IOException {
                    assert zf != null;
                    this.zipfile = zf;
                    try {
                        this.delegate = zf.getInputStream(new ZipEntry(resName));
                        if (this.delegate == null) {                        
                            throw new IOException();
                        }
                    } catch (IOException e) {
                        try {
                            this.zipfile.close();
                        } catch (IOException e2) {/*Outher exception is more important*/}
                        throw e;
                    }
                }

                @Override
                public int read() throws IOException {
                    throw new java.lang.UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int read(byte b[], int off, int len) throws IOException {
                    return delegate.read(b, off, len);
                }

                @Override
                public int available() throws IOException {
                    return this.delegate.available();
                }

                @Override
                public void close() throws IOException {
                    try {
                        this.delegate.close();
                    } finally {
                        this.zipfile.close();
                    }
                }


            }
            // long time = System.currentTimeMillis();
            ZipFile zf = new ZipFile (archiveFile);
            // System.out.println("ZF OPEN " + archiveFile.getPath() + " took: " + (System.currentTimeMillis() - time )+ "ms." );
            return new BufferedInputStream (new ZipInputStream (zf));
	}
        
        @Override
        public URI getArchiveURI () {
            return Utilities.toURI(this.archiveFile);
        }
        
        @Override
        protected long getSize () throws IOException {
            ZipFile zf = new ZipFile (archiveFile);
            try {
                ZipEntry ze = zf.getEntry(this.resName);
                return ze == null ? 0L : ze.getSize();
            } finally {
                zf.close();
            }
        }
    }

    @Trusted
    private static class FastZipFileObject extends ZipFileObject {
        
        private long offset;
        
        FastZipFileObject (final File archiveFile, final String folderName, final String baseName, long mtime, long offset) {            
            super (archiveFile, folderName, baseName, mtime);
            this.offset = offset;
        }
        
        @Override
        public InputStream openInputStream () throws IOException {
            try {
                return new BufferedInputStream (FastJar.getInputStream(archiveFile, offset));
            } catch (FileNotFoundException fnf) {
                //No need to delegate to super (file does not exist)
                throw fnf;
            } catch (IOException e) {
                //Broken or unsupported zip file, try to delegate to ZipFile
                return super.openInputStream();
            } catch (IndexOutOfBoundsException e) {
                return super.openInputStream();
            }
        }
        
        @Override 
        public long getSize () throws IOException {
            try {
                ZipEntry e = FastJar.getZipEntry (archiveFile, offset);
                if (e != null) {
                    long size = e.getSize();
                    //When there is no size, csize and CRC in the LOC table
                    //delegate to super, needs to take it from the CEN table
                    //but we don't have the offset in CEN table here.
                    if (size != -1) {
                        return size;
                    }
                }
            } catch (IOException e) {
                //Handled below
            }
            return super.getSize();
        }
    }

    @Trusted
    private static class CachedZipFileObject extends ZipFileBase {
        
        private final ZipFile zipFile;
        
        CachedZipFileObject(
                @NonNull final ZipFile zipFile,
                @NullAllowed final String pathToRootInArchive,
                @NonNull final String folderName,
                @NonNull final String baseName,
                final long mtime) {
            super (pathToRootInArchive, folderName, baseName, mtime);
            assert zipFile != null : "archiveFile == null";   //NOI18N
	    this.zipFile = zipFile;
	}
        
        @Override
        public InputStream openInputStream() throws IOException {
            return new BufferedInputStream (this.zipFile.getInputStream(new ZipEntry (this.resName)));
	}
        
        @Override
        public URI getArchiveURI () {
            return Utilities.toURI(new File (this.zipFile.getName()));
        }
        
        @Override
        protected long getSize() throws IOException {
            ZipEntry ze = this.zipFile.getEntry(this.resName);
            return ze == null ? 0L : ze.getSize();
        }
    }
    
    
    /** Temporary FileObject for parsing input stream.
     */
    @Trusted
    private static class MemoryFileObject extends Base implements PrefetchableJavaFileObject {
        
        final long lastModified;
        final CharBuffer cb;
        final URI uri;
        final boolean isVirtual;
        
        public MemoryFileObject(final String packageName, final String fileName,
                final URI uri, final long lastModified, final CharBuffer cb ) {            
            super (packageName, fileName);    //NOI18N
            this.cb = cb;
            this.lastModified = lastModified;
            this.uri = uri;
            this.isVirtual = uri != null;
        }
        

        /**
         * Get the character content of the file, if available.
         * @param ignoreEncodingErrors if true, encoding errors will be replaced by the
         * default translation character; otherwise they should be reported as diagnostics.
         * @throws UnsupportedOperationException if character access is not supported
         */
        @Override
        public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors) throws java.io.IOException {
            return cb.duplicate();
        }

        @Override
        public boolean delete() {
            // Do nothing
            return false;
        }        

        @Override
        public URI toUri () {
            if (this.uri != null) {
                return this.uri;
            }
            else {
                return URI.create (convertPackage2Folder(this.pkgName) + NBFS_SEPARATOR_CHAR + this.nameWithoutExt);
            }
        }
        
        @Override
        public boolean isVirtual () {
            return isVirtual;
        }

        @Override
        public long getLastModified() {
            return this.lastModified;
        }

        /**
         * Get an InputStream for this object.
         * 
         * @return an InputStream for this  object.
         * @throws UnsupportedOperationException if the byte access is not supported
         */
        @Override
        public InputStream openInputStream() throws java.io.IOException {
            return new ByteArrayInputStream(cb.toString().getBytes("UTF-8"));
        }

        /**
         * Get an OutputStream for this object.
         * 
         * @return an OutputStream for this  object.
         * @throws UnsupportedOperationException if byte access is not supported
         */
        @Override
        public java.io.OutputStream openOutputStream() throws java.io.IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Get a reader for this object.
         * 
         * @return a Reader for this file object.
         * @throws UnsupportedOperationException if character access is not supported
         * @throws IOException if an error occurs while opening the reader
         */
        @Override
        public java.io.Reader openReader (boolean b) throws java.io.IOException {
            return new StringReader(this.cb.toString());
        }

        /**
         * Get a writer for this object.
         * @throws UnsupportedOperationException if character access is not supported
         * @throws IOException if an error occurs while opening the writer
         */
        @Override
        public java.io.Writer openWriter() throws java.io.IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Nothing to prefetch it's already in RAM.
         * Is the {@link PrefetchableJavaFileObject} just to
         * prevent down casts.
         * @return zero
         * @throws IOException 
         */
        @Override
        public int prefetch() throws IOException {
            return 0;
        }

        /**
         * Nothing to prefetch it's already in RAM.
         * Is the {@link PrefetchableJavaFileObject} just to
         * prevent down casts.
         * @return zero
         */
        @Override
        public int dispose() {
            return 0;
        }
        
        
        
    }

    private static class NullWriteFileObject extends ForwardingInferableJavaFileObject {
        private NullWriteFileObject (@NonNull final InferableJavaFileObject delegate) {
            super (delegate);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new NullOutputStream();
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }

        private static class NullOutputStream extends OutputStream {
            @Override
            public void write(int b) throws IOException {
                //pass
            }
        }
    }

    //</editor-fold>

    public static class InvalidFileException extends IOException {

        public InvalidFileException () {
            super ();
        }

        public InvalidFileException (final FileObject fo) {
            super (NbBundle.getMessage(FileObjects.class,"FMT_InvalidFile",FileUtil.getFileDisplayName(fo)));
        }
    }
}
