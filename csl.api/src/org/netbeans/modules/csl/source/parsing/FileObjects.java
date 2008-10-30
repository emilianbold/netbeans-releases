/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.csl.source.parsing;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import org.netbeans.modules.csl.api.ParserFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Creates various kinds of file objects 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
* I've ripped out a bunch of stuff here
 *
 * XXX - Rename to JavaFileObjects
 *
 * @author Petr Hrebejk
 */
public class FileObjects {
    
    public static final Comparator<String> SIMPLE_NAME_STRING_COMPARATOR = new SimpleNameStringComparator();
    public static final Comparator<FileObject> SIMPLE_NAME_FILEOBJECT_COMPARATOR = new SimpleNameFileObjectComparator();
    
    
//    public static final String JAVA  = JavaDataLoader.JAVA_EXTENSION;
//    public static final String CLASS = ClassDataLoader.CLASS_EXTENSION;
    public static final String JAR   = "jar";  //NOI18N
    public static final String FILE  = "file"; //NOI18N
    public static final String ZIP   = "zip";  //NOI18N
    public static final String HTML  = "html"; //NOI18N
    public static final String SIG   = "sig";  //NOI18N
    public static final String RS    = "rs";   //NOI18N
    
    
    /** Creates a new instance of FileObjects */
    private FileObjects() {
    }
    
    // Public methods ----------------------------------------------------------
    
    
    
//    /**
//     * Creates {@link JavaFileObject} for a ZIP entry of given name
//     * @param zip a zip file
//     * @param name the name of entry, the '/' char is a separator
//     * @return {@link JavaFileObject}, never returns null
//     */
//    public static JavaFileObject zipFileObject( File zipFile, String folder, String baseName, long mtime) {
//        assert zipFile != null;                
//        return new ZipFileObject( zipFile, folder, baseName, mtime);
//    }
//    
//    public static JavaFileObject zipFileObject(ZipFile zipFile, String folder, String baseName, long mtime) {
//        assert zipFile != null;
//        return new CachedZipFileObject (zipFile, folder, baseName, mtime);
//    }
//    
//    /**
//     * Creates {@link JavaFileObject} for a regular {@link File}
//     * @param file for which the {@link JavaFileObject} should be created
//     * @pram root - the classpath root owning the file
//     * @return {@link JavaFileObject}, never returns null
//     */
//    public static JavaFileObject fileFileObject( final File file, final File root, final JavaFileFilterImplementation filter) {
//        assert file != null;
//        assert root != null;
//        String[] pkgNamePair = getFolderAndBaseName(getRelativePath(root,file),File.separatorChar);
//        return new RegularFileObject( file, convertFolder2Package(pkgNamePair[0], File.separatorChar), pkgNamePair[1], filter);
//    }
//    public static /*Java*/FileObject fileFileObject( final File file, final File root, Object/* final JavaFileFilterImplementation*/ filter) {
//        assert file != null;
//        assert root != null;
//        // Ugh this is a performance KILLER
//        return FileUtil.toFileObject(file); // XXX consider root?
//    }

    public static ParserFile fileFileObject( final File file, final File root, boolean platform, Object/* final JavaFileFilterImplementation*/ filter) {
        assert file != null;
        assert root != null;
        return new FileParserFile(file, root, platform);
    }
    
    private static class FileParserFile implements ParserFile {
        private File file;
        private File root;
        private FileObject fileObject;
        private String relative;
        private boolean platform;
        
        private FileParserFile(File file, File root, boolean platform) {
            this.file = file;
            this.root = root;
            this.platform = platform;
        }
        
        public FileObject getFileObject() {
            if (fileObject == null) {
                fileObject = FileUtil.toFileObject(file);
            }
            return fileObject;
        }

        public String getRelativePath() {
            if (relative == null) {
                relative = FileObjects.getRelativePath(root, file);
            }
            
            return relative;
        }
    
        public String getNameExt() {
            return file.getName();
        }

        public String getExtension() {
            String name = file.getName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                return name.substring(index+1);
            } else {
                return "";
            }
        }
        
        public String toString() {
            return "FileParserFile(" + getNameExt() + ")";
        }
    
        public boolean isPlatform() {
            return platform;
        }

        public File getFile() {
            return file;
        }
    }
    
    //    
//    /**
//     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject}
//     * Any client which needs to create {@link JavaFileObject} for java
//     * source file should use this factory method.
//     * @param {@link FileObject} for which the {@link JavaFileObject} should be created
//     * @return {@link JavaFileObject}, never returns null
//     * @exception {@link IOException} may be thrown
//     */
//    public static /*Java*/FileObject nbFileObject (final FileObject file) throws IOException {
//        return nbFileObject (file, null, false);
//    }
//    
//    /**
//     * Creates {@link JavaFileObject} for a NetBeans {@link FileObject}
//     * Any client which needs to create {@link JavaFileObject} for java
//     * source file should use this factory method.
//     * @param {@link FileObject} for which the {@link JavaFileObject} should be created
//     * @param renderNow if true the snap shot of the file is taken immediately
//     * @return {@link JavaFileObject}, never returns null
//     * @exception {@link IOException} may be thrown
//     */
//    public static /*Java*/FileObject nbFileObject (final FileObject file, JavaFileFilterImplementation filter, boolean renderNow) throws IOException {
//        assert file != null;
//        if (!file.isValid() || file.isVirtual()) {
//            throw new InvalidFileException (file);
//        }
//        return new SourceFileObject (file, filter, renderNow);
//    }
//    
//    /**
//     * Creates virtual {@link JavaFileObject} with given name and content.
//     * This method should be used only by tests, regular client should never
//     * use this method.
//     * @param content the content of the {@link JavaFileObject}
//     * @param name the name of the {@link JavaFileObject}
//     * @return {@link JavaFileObject}, never returns null
//     */
//    public static JavaFileObject memoryFileObject( CharSequence content, CharSequence name ) {
//        final String nameStr = name.toString();
//        if (!nameStr.equals(getBaseName(nameStr))) {
//            throw new IllegalArgumentException ("Memory is flat");      //NOI18N
//        }
//        int length = content.length();        
//        if ( length != 0 && Character.isWhitespace( content.charAt( length - 1 ) ) ) {
//            return new MemoryFileObject( nameStr, CharBuffer.wrap( content ) );
//        }
//        else {
//            return new MemoryFileObject( nameStr, (CharBuffer)CharBuffer.allocate( length + 1 ).append( content ).append( ' ' ).flip() );
//        }
//        
//    }            
    
    public static String stripExtension( String fileName ) {        
        int dot = fileName.lastIndexOf(".");
        return (dot == -1 ? fileName : fileName.substring(0, dot));
    }    
    
    
//    /**
//     * Returns the name of JavaFileObject, similar to
//     * {@link java.io.File#getName}
//     */
//    public static String getName (final JavaFileObject fo, final boolean noExt) {
//        assert fo != null;
//        if (fo instanceof Base) {
//            Base baseFileObject = (Base) fo;
//            if (noExt) {
//                return baseFileObject.getName();
//            }
//            else {                
//                StringBuilder sb = new StringBuilder ();
//                sb.append (baseFileObject.getName());
//                sb.append('.'); //NOI18N
//                sb.append(baseFileObject.getExt());
//                return sb.toString();
//            }
//        }
//        try {
//            final URL url = fo.toUri().toURL();
//            String path = url.getPath();
//            int index1 = path.lastIndexOf('/');
//            int len;
//            if (noExt) {
//               final int index2 = path.lastIndexOf('.');
//               if (index2>index1) {
//                   len = index2;
//               }
//               else {
//                   len = path.length();
//               }
//            }
//            else {
//                len = path.length();
//            }
//            path = path.substring(index1+1,len);
//            return path;
//        } catch (MalformedURLException e) {
//            return null;
//        }        
//    }
    /**
     * Returns the name of JavaFileObject, similar to
     * {@link java.io.File#getName}
     */
    public static String getName (final /*Java*/FileObject fo, final boolean noExt) {
        assert fo != null;
        return noExt ? fo.getName() : fo.getNameExt();
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
                
    public static String getBinaryName (final File file, final File root) {
        assert file != null && root != null;
        String fileName = FileObjects.getRelativePath (root, file);
        int index = fileName.lastIndexOf('.');  //NOI18N
        if (index > 0) {
            fileName = fileName.substring(0,index);
        }        
        return fileName.replace(File.separatorChar,'.');   //NOI18N        
    }
    
    public static String getSimpleName( /*Java*/FileObject fo ) {
        
        String name = getName(fo,true);
        int i = name.lastIndexOf( '$' );
        if ( i == -1 ) {
            return name;
        }
        else {
            return name.substring( i + 1 );
        }        
    }
    
    public static String getSimpleName( String fileName ) {
        
        String name = getBaseName( fileName );
        
        int i = name.lastIndexOf( '$' );
        if ( i == -1 ) {
            return name;
        }
        else {
            return name.substring( i + 1 );
        }
        
    }
    
    public static String convertPackage2Folder( String packageName ) {
        return packageName.replace( '.', '/' );
    }    
    
    
    public static String convertFolder2Package (String packageName) {
        return convertFolder2Package (packageName, '/');    //NOI18N
    }
    
    public static String convertFolder2Package( String packageName, char folderSeparator ) {
        return packageName.replace( folderSeparator, '.' );
    }
    
    
    public static String getRelativePath (final String packageName, final String relativeName) {
        StringBuilder relativePath = new StringBuilder ();
        relativePath.append(packageName.replace('.','/'));
        relativePath.append(relativeName);
        return relativePath.toString();
    }
    
    public static String[] getParentRelativePathAndName (final String className) {
        if (className.charAt(className.length()-1) == '.') {
            return null;
        }
        final int index = className.lastIndexOf('.');
        if (index<0) {
            return new String[] {
                "",     //NOI18N
                className
            };
        }
        else {
            return new String[] {
                className.substring(0,index).replace('.','/'),      //NOI18N
                className.substring(index+1)
            };
        }
    }
    
    
    public static File getRootFile (final URL url) {
        File rootFile;
        if ("jar".equals(url.getProtocol())) {  //NOI18N
            rootFile = new File (URI.create(FileUtil.getArchiveFile(url).toExternalForm()));
        }
        else {
            rootFile = new File (URI.create(url.toExternalForm()));
        }
        return rootFile;
    }
    
    public static void deleteRecursively (final File folder) {
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
    
    // Private methods ---------------------------------------------------------
    
    // Innerclasses ------------------------------------------------------------
    
//    public static abstract class Base implements JavaFileObject {
//
//        protected final JavaFileObject.Kind kind;
//        protected final String pkgName;
//        protected final String nameWithoutExt;
//        protected final String ext;        
//        
//        protected Base (final String pkgName, final String name) {
//            assert pkgName != null;
//            assert name != null;
//            this.pkgName = pkgName;
//            String[] res = getNameExtPair(name);
//            this.nameWithoutExt = res[0];
//            this.ext = res[1];
//            if (FileObjects.JAVA.equalsIgnoreCase(ext)) { //NOI18N
//                this.kind = Kind.SOURCE;
//            }
//            else if (FileObjects.CLASS.equalsIgnoreCase(ext) || "sig".equals(ext)) {   //NOI18N
//                this.kind = Kind.CLASS;
//            }
//            else if (FileObjects.HTML.equalsIgnoreCase(ext)) {    //NOI18N
//                this.kind = Kind.HTML;
//            }
//            else {
//                this.kind = Kind.OTHER;
//            }
//        }
//        
//        public JavaFileObject.Kind getKind() {
//            return this.kind;
//        }
//        
//        public boolean isNameCompatible (String simplename, JavaFileObject.Kind k) {
//            if (this.kind != k) {
//                return false;
//            }
//	    return nameWithoutExt.equals(simplename);
//	}        
//        
//        public NestingKind getNestingKind() {
//            return null;
//        }
//        
//        public Modifier getAccessLevel() {
//            return null;
//        }
//    
//        @Override
//        public String toString() {
//            return this.toUri().toString();
//        }
//        
//        public String getPackage () {
//            return this.pkgName;
//        }
//        
//        public String getNameWithoutExtension () {
//            return this.nameWithoutExt;
//        }
//        
//        public String getName () {
//            return this.nameWithoutExt + '.' + ext;
//        }
//        
//        public String getExt () {
//            return this.ext;
//        }        
//        
//        private static String[] getNameExtPair (String name) {
//            int index = name.lastIndexOf ('.');            
//            String namenx;
//            String ext;
//            if (index <= 0) {
//                namenx =name;
//                ext = "";   //NOI18N
//            }
//            else {
//                namenx = name.substring(0,index);
//                if (index == name.length()-1) {
//                    ext = "";
//                }
//                else {
//                    ext = name.substring(index+1);
//                }
//            }
//            return new String[] {
//              namenx,
//              ext
//            };
//        }
//    }
//    
//    public static abstract class FileBase extends Base {
//        
//        protected final File f;
//        
//        protected FileBase (final File file, final String pkgName, final String name) {
//            super (pkgName, name);
//            assert file != null;
//            assert file.equals(FileUtil.normalizeFile(file));
//            this.f = file;
//        }
//        
//        public File getFile () {
//            return this.f;
//        }
//    }
    
    
    public static class InvalidFileException extends IOException {
        
        public InvalidFileException () {
            super ();
        }
        
        public InvalidFileException (final FileObject fo) {
            super (NbBundle.getMessage(FileObjects.class,"FMT_InvalidFile",FileUtil.getFileDisplayName(fo)));
        }
    }
    
    
    public static String getRelativePath (final File root, final File fo) {
        final String rootPath = root.getAbsolutePath();
        final String foPath = fo.getAbsolutePath();
        assert foPath.startsWith(rootPath);
        int index = rootPath.length();
        if (rootPath.charAt(index-1)!=File.separatorChar) {
            index++;
        }            
        int foIndex = foPath.length();
        if (foIndex <= index) {
            return "";  //NOI18N
        }
        return foPath.substring(index);
    }           
//    
//    private static class RegularFileObject extends FileBase {
//        
//        private URI uriCache;
//        private final JavaFileFilterImplementation filter;
//
//	public RegularFileObject(final File f, final String packageName, final String baseName, final JavaFileFilterImplementation filter) {
//            super (f, packageName, baseName);
//            this.filter = filter;
//	}               
//
//        public InputStream openInputStream() throws IOException {
//	    return new FileInputStream(f);
//	}
//
//	public Reader openReader (boolean b) throws IOException {
//	    throw new UnsupportedOperationException();
//	}
//
//	public OutputStream openOutputStream() throws IOException {
//	    return new FileOutputStream(f);
//	}
//
//	public Writer openWriter() throws IOException {
//	    //FIX: consider using encoding here
//	    return new OutputStreamWriter(new FileOutputStream(f));
//	}
//
//	public @Override boolean isNameCompatible(String simplename, JavaFileObject.Kind kind) {
//	    boolean res = super.isNameCompatible(simplename, kind);
//            if (res) {
//                return res;
//            }
//            else if (Utilities.isWindows()) {
//                return nameWithoutExt.equalsIgnoreCase(simplename);
//            }
//            else {
//                return false;
//            }
//	} 	   
//        
//        public URI toUri () {
//            if (this.uriCache == null) {
//                this.uriCache = f.toURI();
//            }
//            return this.uriCache;
//        }
//
//        public long getLastModified() {
//	    return f.lastModified();
//	}
//
//	public boolean delete() {
//	    return f.delete();
//	}
//
//	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
//            
//            char[] result;
//            InputStreamReader in = new InputStreamReader (new FileInputStream(this.f), encodingName);
//            try {
//                int len = (int)this.f.length();
//                result = new char [len+1];
//                int red = 0, rv;	    
//                while ((rv=in.read(result,red,len-red))>0 && (red=red+rv)<len);
//            } finally {
//                in.close();
//            }
//            result[result.length-1]='\n'; //NOI18N
//            CharSequence buffer = CharBuffer.wrap (result);
//            if (this.filter != null) {
//                buffer = this.filter.filterCharSequence(buffer);
//            }
//            return buffer;
//	}
//
//	@Override
//	public boolean equals(Object other) {
//	    if (!(other instanceof RegularFileObject))
//		return false;
//	    RegularFileObject o = (RegularFileObject) other;
//	    return f.equals(o.f);
//	}
//
//	@Override
//	public int hashCode() {
//	    return f.hashCode();
//	}
//        
//    }    
//
//    /** A subclass of FileObject representing zip entries.
//     * XXX: What happens when the archive is deleted or rebuilt?
//     */
//    private abstract static class ZipFileBase extends Base {
//        
//        protected final long mtime;
//        protected final String resName;
//        
//        public ZipFileBase (final String folderName, final String baseName, long mtime) {
//            super (convertFolder2Package(folderName),baseName);
//            this.mtime = mtime;
//            if (folderName.length() == 0) {
//                this.resName = baseName;
//            }
//            else {
//                StringBuilder resName = new StringBuilder (folderName);
//                resName.append('/');        //NOI18N
//                resName.append(baseName);
//                this.resName = resName.toString();
//            }
//        }
//        
//        public OutputStream openOutputStream() throws IOException {
//	    throw new UnsupportedOperationException();
//	}
//
//	public Reader openReader(boolean b) throws IOException {
//	    throw new UnsupportedOperationException();
//	}
//
//        public Writer openWriter() throws IOException {
//	    throw new UnsupportedOperationException();
//	}
//        
//        public long getLastModified() {
//	    return mtime;
//	}
//
//	public boolean delete() {
//	    throw new UnsupportedOperationException();
//	}
//
//	public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
//	    throw new UnsupportedOperationException();
//	}
//        
//        public final URI toUri () {
//            URI  zdirURI = this.getArchiveURI();
//            return URI.create ("jar:"+zdirURI.toString()+"!/"+resName);  //NOI18N
//        }
//        
//        @Override
//	public int hashCode() {
//	    return this.resName.hashCode();
//	}                
//        
//	@Override
//	public boolean equals(Object other) {
//	    if (!(other instanceof ZipFileBase))
//		return false;
//	    ZipFileBase o = (ZipFileBase) other;
//	    return getArchiveURI().equals(o.getArchiveURI()) && resName.equals(o.resName);
//	}
//        
//        protected abstract URI getArchiveURI ();
//        
//    }
//    
//    private static class ZipFileObject extends ZipFileBase {
//	
//
//	/** The zipfile containing the entry.
//	 */
//	private final File archiveFile;
//        
//
//        ZipFileObject(final File archiveFile, final String folderName, final String baseName, long mtime) {
//            super (folderName,baseName,mtime);
//            assert archiveFile != null : "archiveFile == null";   //NOI18N
//	    this.archiveFile = archiveFile;
//            
//	}
//
//        public InputStream openInputStream() throws IOException {            
//            class ZipInputStream extends InputStream {
//
//                private ZipFile zipfile;
//                private InputStream delegate;
//
//                public ZipInputStream (ZipFile zf) throws IOException {
//                    this.zipfile = zf;
//                    this.delegate = zf.getInputStream(new ZipEntry(resName));
//                }
//
//                public int read() throws IOException {
//                    throw new java.lang.UnsupportedOperationException("Not supported yet.");
//                }
//
//                public int read(byte b[], int off, int len) throws IOException {
//                    return delegate.read(b, off, len);
//                }
//
//                public int available() throws IOException {
//                    return this.delegate.available();
//                }
//
//                public void close() throws IOException {
//                    try {
//                        this.delegate.close();
//                    } finally {
//                        this.zipfile.close();
//                    }
//                }
//
//
//            };
//            ZipFile zf = new ZipFile (archiveFile);
//            return new ZipInputStream (zf);
//	}
//        
//        public URI getArchiveURI () {
//            return this.archiveFile.toURI();
//        }
//    }
//    
//    private static class CachedZipFileObject extends ZipFileBase {
//        
//        private ZipFile zipFile;
//        
//        CachedZipFileObject(final ZipFile zipFile, final String folderName, final String baseName, long mtime) {
//            super (folderName,baseName,mtime);
//            assert zipFile != null : "archiveFile == null";   //NOI18N
//	    this.zipFile = zipFile;            
//	}
//        
//        public InputStream openInputStream() throws IOException {
//            return this.zipFile.getInputStream(new ZipEntry (this.resName));
//	}
//        
//        public URI getArchiveURI () {
//            return new File (this.zipFile.getName()).toURI();
//        }
//    }
//    
//    
//    /** Temporay FileObject for parsing input stream.
//     */    
//    private static class MemoryFileObject extends Base {
//        
//        private String fileName;
//        private CharBuffer cb;
//        
//        public MemoryFileObject( String fileName, CharBuffer cb ) {            
//            super ("",fileName);    //NOI18N
//            this.cb = cb;
//            this.fileName = fileName;
//        }                
//        
//
//        /**
//         * Get the character content of the file, if available.
//         * @param ignoreEncodingErrors if true, encoding errros will be replaced by the 
//         * default translation character; otherwise they should be reported as diagnostics.
//         * @throws UnsupportedOperationException if character access is not supported
//         */
//        public java.nio.CharBuffer getCharContent(boolean ignoreEncodingErrors) throws java.io.IOException {
//            return cb;
//        }
//
//        public boolean delete() {
//            // Do nothing
//            return false;
//        }        
//
//        public URI toUri () {
//            return URI.create (this.nameWithoutExt);
//        }
//
//        public long getLastModified() {
//            return System.currentTimeMillis(); // XXX
//        }
//
//        /**
//         * Get an InputStream for this object.
//         * 
//         * @return an InputStream for this  object.
//         * @throws UnsupportedOperationException if the byte access is not supported
//         */
//        public InputStream openInputStream() throws java.io.IOException {
//            return new ByteArrayInputStream(cb.toString().getBytes("UTF-8"));
//        }
//
//        /**
//         * Get an OutputStream for this object.
//         * 
//         * @return an OutputStream for this  object.
//         * @throws UnsupportedOperationException if byte access is not supported
//         */
//        public java.io.OutputStream openOutputStream() throws java.io.IOException {
//            throw new UnsupportedOperationException();
//        }
//
//        /**
//         * Get a reader for this object.
//         * 
//         * @return a Reader for this file object.
//         * @throws UnsupportedOperationException if character access is not supported
//         * @throws IOException if an error occurs while opening the reader
//         */
//        public java.io.Reader openReader (boolean b) throws java.io.IOException {
//            throw new UnsupportedOperationException();
//        }
//
//        /**
//         * Get a writer for this object.
//         * @throws UnsupportedOperationException if character access is not supported
//         * @throws IOException if an error occurs while opening the writer
//         */
//        public java.io.Writer openWriter() throws java.io.IOException {
//            throw new UnsupportedOperationException();
//        }
//        
//    }    
    
    private static class SimpleNameStringComparator implements Comparator<String> {
        
        public int compare( String o1, String o2 ) {
            return getSimpleName( o1 ).compareTo( getSimpleName( o2 ) );
        }
                        
    }
    
    private static class SimpleNameFileObjectComparator implements Comparator</*Java*/FileObject> {
        
        public int compare( /*Java*/FileObject o1, /*Java*/FileObject o2 ) {
            
            String n1 = getSimpleName( o1 );
            String n2 = getSimpleName( o2 );
                        
            return n1.compareTo( n2 );
        }
                        
    }
    
    static final String encodingName = new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();            
    
}
