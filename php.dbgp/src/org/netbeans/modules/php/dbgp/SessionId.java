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
package org.netbeans.modules.php.dbgp;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.dbgp.SessionProgress;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;


/**
 * This class is used for session identifying between IDE and Debugger.
 * Session id is based on file requested for debug.
 * It is used for mapping remote files to local files based on
 * session information.
 *
 *
 * @author ads
 *
 */
public class SessionId {
    private static final String PREFIX      = "netbeans-xdebug";         // NOI18N
    private static final String SLASH       = "/";                     // NOI18N
    private static final String BACK_SLASH  = "\\";                    // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SessionId.class.getName());

    //keep synchronized with org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants
    private static final String SOURCES_TYPE_PHP = "PHPSOURCE"; // NOI18N


    public SessionId( FileObject fileObject ) {
        myId = PREFIX;//+ mySessionsCount;
        mySessionsCount++;
        myFileObject = fileObject;
        myDebugFile = fileObject;
    }

    public String getId() {
        return myId;
    }

    public FileObject getSessionFileObject(){
        return myFileObject;
    }

    public Project getProject(){
        return FileOwnerQuery.getOwner( myFileObject );
    }

    public String getSessionPrefix() {
        return PREFIX;
    }

    public synchronized void setFileUri( String uri ) {
        if ( myFileUri == null ) {
            myFileUri = uri;
            computeBases();
        }
        notifyAll();
        SessionProgress s = SessionProgress.forSessionId(this);
        if (s != null) {
            s.notifyConnectionFinished();
        }
    }

    public synchronized String waitServerFile( boolean  wait ) {
        String retval = getFileUri();
        if (  retval != null ) {
            return retval;
        } else {
            if ( !wait ) {
                return null;
            }
            try {
                wait( );
            }
            catch (InterruptedException e) {
                return getFileUri();
            }
        }
        return getFileUri();
    }

    /**
     * @param localFileName file name on the local filesystem
     * @return remote uri respectively given <code>localFileName</code>
     */
    public String getFileUriByLocal( String localFileName ) {
        File file = new File( localFileName );
        if ( !file.exists() ) {
            return null;
        }
        return getFileUriByLocal(
                FileUtil.toFileObject(FileUtil.normalizeFile( file)) );
    }

    /**
     * @param localFile file name on the local filesystem
     * @return remote uri respectively given <code>localFile</code>
     */
    public String getFileUriByLocal( FileObject localFile ) {
        if ( localFile == null ) {
            return null;
        }
        String retval = getFileUri();
        if ( retval != null && localFile.equals( getDebugFile() )) {
            // trivial case , could be quickly handled
            return retval;
        } else {
            retval = null;
        }
        if ( myLocalBase != null && myRemoteBase != null ) {
            String relativeFile = FileUtil.getRelativePath( myLocalBase, localFile);
            if (relativeFile != null) {
                relativeFile = relativeFile.replace(File.separator, myRemoteSeparator);
                retval = myRemoteBase + relativeFile;
            }
        }
        if (retval == null) {
            File file = FileUtil.toFile(localFile);
            if (file != null) {
                try {
                    URL u = file.toURI().toURL();
                    if ("file".equals(u.getProtocol())) {//NOI18N
                        retval = u.toExternalForm();
                        if (retval.startsWith("file:/") && !retval.startsWith("file:///")) {//NOI18N
                            retval = retval.replaceFirst("/", "///");//NOI18N
                        }
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return retval;
    }

    public DataObject getDataObjectByRemote( String uri ) {
        try {
            FileObject fileObject = getFileObjectByRemote(uri);
            if (fileObject == null) {
                return null;
            }
            return DataObject.find(fileObject);
        }
        catch (DataObjectNotFoundException e) {
            return null;
        }
    }

    private FileObject convertToFileObject(String uri) {
        if (uri != null) {
            try {
                URL url = new URI(uri).toURL();
                if ("file".equals(url.getProtocol())) { //NOI18N
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        return fo;
                    }
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public FileObject getFileObjectByRemote( String uri ){
        FileObject retval = null;
        if (uri != null) {
            if (uri.equals(getFileUri())) {
                retval = getDebugFile();
            } else if (myRemoteBase != null && myLocalBase != null && uri.startsWith(myRemoteBase)) {
                String relativePath = uri.substring(myRemoteBase.length());
                relativePath = relativePath.replace(myRemoteSeparator, File.separator);
                retval = myLocalBase.getFileObject(relativePath);
            }
        }
        if (retval == null) {
            retval = convertToFileObject(uri);
        }
        return retval;
    }

    private String getFileUri() {
        return myFileUri;
    }


    private void computeBases() {
        int indx;

        int slashIndex = getFileUri().lastIndexOf( SLASH );
        if ( slashIndex!=-1 ) {
            indx = slashIndex;
            myRemoteSeparator = SLASH;
        }
        else if( getFileUri().lastIndexOf( BACK_SLASH ) != -1 ) {
            indx = getFileUri().lastIndexOf( BACK_SLASH );
            myRemoteSeparator = BACK_SLASH;
        }
        else {
            assert false;
            return;
        }

        if ( indx == getFileUri().length() -1 ) {
            /* This should never happend.  getFileUri() should represent
             * path to file, not folder.
             */
            assert false;
            return ;
        }
        String fileName = getFileUri().substring( indx +1 );
        String remoteFolderName = getFileUri().substring( 0, indx );
        FileObject localFolder;

        if ( getSessionFileObject().isFolder() ) {
            // fileName in this case is directory index file ( f.e. index.php(html) )
            localFolder = getSessionFileObject();
            myDebugFile = getSessionFileObject().getFileObject( fileName );
        }
        else {
            String name = getSessionFileObject().getNameExt();
            if ( !name.equals( fileName )) {
                String fileUri = getFileUri();
                FileObject tmpDebugFile = fileUri != null ? convertToFileObject(fileUri) : null;
                myDebugFile = tmpDebugFile != null ? tmpDebugFile : myDebugFile;
                assert tmpDebugFile != null : fileUri;
                return;
            }
            localFolder = getSessionFileObject().getParent();
        }
        setupBases( localFolder , remoteFolderName, fileName );
    }

    private void setupBases( FileObject localFolder, String remoteFolder, String fileName)
    {
        Project project = getProject();
        FileObject projectDirectory = (project != null) ? project.getProjectDirectory() : null;
        if (projectDirectory == null /*fos test purposes*/) {
            assert "On".equals(System.getProperty("TestRun", "Off"));
            setRemoteBase(  remoteFolder );
            myLocalBase =  localFolder;
            myDebugFile = myLocalBase.getFileObject( fileName );
            return;
        }
        if (localFolder.equals(projectDirectory)) {
            //TODO: review this code  -just hot fix - for debugging project (debug not debug.single)
            setRemoteBase(  remoteFolder );
            myLocalBase = getSourceRoot();
            if (myDebugFile == null) {
                myDebugFile = getSourceRoot().getFileObject( fileName );
            }
            return;
        }
        if ( localFolder.equals( getSourceRoot() ) ) {
            setRemoteBase(  remoteFolder );
            myLocalBase = localFolder;
            return;
        }
        int indx = remoteFolder.lastIndexOf( myRemoteSeparator );
        if ( indx == -1 ) {
            setRemoteBase( "" );
            myLocalBase = localFolder;
            return;
        }
        assert indx+1 < remoteFolder.length();
        setupBases( localFolder.getParent() , remoteFolder.substring( 0, indx), fileName);
    }

    private void setRemoteBase( String base ) {
        if ( base.endsWith( myRemoteSeparator)) {
            myRemoteBase = base ;
        }
        else {
            myRemoteBase = base +myRemoteSeparator;
        }
    }

    private FileObject getDebugFile(){
        return myDebugFile;
    }

    private FileObject getSourceRoot() {
	final FileObject[] sourceObjects = getSourceObjects(getProject());
        return (sourceObjects != null && sourceObjects.length > 0) ? sourceObjects[0] : null;
    }

    public static SourceGroup[] getSourceGroups(Project phpProject) {
        Sources sources = ProjectUtils.getSources(phpProject);
        SourceGroup[] groups = sources.getSourceGroups(SOURCES_TYPE_PHP);//NOI18N
        return groups;
    }

    public static FileObject[] getSourceObjects(Project phpProject) {
        SourceGroup[] groups = getSourceGroups(phpProject);
        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    private String myId;

    private static int mySessionsCount = 1;

    private String myFileUri;

    private final FileObject myFileObject;

    private FileObject myLocalBase;

    private String myRemoteBase;

    private String myRemoteSeparator;

    private FileObject myDebugFile;

}
