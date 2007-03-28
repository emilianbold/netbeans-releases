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
package org.netbeans.modules.websvc.rest.spi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * All development project type supporting REST framework should provide
 * one instance of this in project lookup.
 *
 * @author Nam Nguyen
 */
public abstract class RestSupport {
    public static final String SWDP_LIBRARY = "swdp"; //NOI18N
    public static final String PROP_SWDP_CLASSPATH = "libs.swdp.classpath"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_DIR = "restbeans.test.dir";
    public static final String PROP_RESTBEANS_TEST_FILE = "restbeans.test.file";
    public static final String PROP_RESTBEANS_TEST_URL = "restbeans.test.url";
    public static final String PROP_BASE_URL_TOKEN = "base.url.token";
    public static final String BASE_URL_TOKEN = "___BASE_URL___";
    public static final String RESTBEANS_TEST_DIR = "nbproject/private/restbeans";
    public static final String COMMAND_TEST_RESTBEANS = "test.restbeans";
    public static final String REST_SUPPORT_ON = "rest.support.on";
    public static final String TEST_RESBEANS = "test-resbeans.html";
    
    AntProjectHelper helper;

    /** Creates a new instance of RestSupport */
    public RestSupport(AntProjectHelper helper) {
        this.helper = helper;
    }

    /**
     * Ensure the project is ready for REST development.
     * Typical implementation would need to invoke addSwdpLibraries
     * REST development with servlet container would need to add servlet adaptor
     * to web.xml.
     */
    public abstract void ensureRestDevelopmentReady() throws IOException;

    /**
     * Cleanup the project from previously added REST development support artifacts.
     * This should not remove any user source code.
     */
    public abstract void cleanupRestDevelopment();

    /**
     * Is the REST development setup ready: SWDP library added, REST adaptors configured, 
     * generic test client created ?
     */
    public abstract boolean isReady();

    /**
     * Generates test client.  Typically RunTestClientAction would need to call 
     * this before invoke the build script target.
     * 
     * @param destDir directory to write test client files in.
     * @return test file object, containing token BASE_URL_TOKEN whether used or not.
     */
    public FileObject generateTestClient(File testdir) throws IOException {        
        if (! testdir.isDirectory()) {
            testdir.mkdirs();
        }
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject testFO = dir.getFileObject(TEST_RESBEANS);
        if (testFO == null) {
            testFO = dir.createData(TEST_RESBEANS);
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = testFO.lock();
            OutputStream os = testFO.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os));
            InputStream is = RestSupport.class.getResourceAsStream("resources/"+TEST_RESBEANS);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String lineSep = "\n";//Unix
            if(File.separatorChar == '\\')//Windows
                lineSep = "\r\n";
            while((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (lock != null) lock.releaseLock();
            if (reader != null) {
                reader.close();
            }
        }
        copyFile("resources/expand.gif", testdir, "expand.gif");
        copyFile("resources/collapse.gif", testdir, "collapse.gif");
        copyFile("resources/item.gif", testdir, "item.gif");
        return testFO;
    }

    private void copyFile(String path, File testdir, String name) throws IOException {
        File df = new File(testdir, name);
        if(!df.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = RestSupport.class.getResourceAsStream(path);
                os = new FileOutputStream(df);
                byte[] bytes = new byte[1000];
                while (is.read(bytes) != -1) {
                    os.write(bytes);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }
    
    /**
     *  Add SWDP library for given source file on specified class path types.
     * 
     *  @param source source file object for which the libraries is added.
     *  @param classPathTypes types of class path to add ("javac.compile",...)
     */
    public void addSwdpLibrary(String[] classPathTypes) throws IOException {
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            throw new IllegalStateException("SWDP library not found");
        }

        Project project = getProject();
        if (project == null) {
            return;
        }
        
        // check if swdp is already part of classpath
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        FileObject restClass = classPath.findResource("com/sun/ws/rest/api/UriTemplate.class"); // NOI18N
        if (restClass != null) {
            return;
        }
        
        ProjectClassPathExtender pce = project.getLookup().lookup(ProjectClassPathExtender.class);
        ProjectClassPathModifierImplementation pcm = project.getLookup().lookup(ProjectClassPathModifierImplementation.class);
        if (pcm != null) {
            for (String type : classPathTypes) {
                ProjectClassPathModifier.addLibraries(new Library[] { swdpLibrary }, sourceRoot, type);
            }
        } else if (pce != null) {
            pce.addLibrary(swdpLibrary);
        } else{
            throw new IllegalStateException("Current project does not have support " +
                    "for ProjectClassPathModifier or ProjectClassPathExtender");
        }
    }

    protected Project getProject() {
        return FileOwnerQuery.getOwner(helper.getProjectDirectory());
    }

    public boolean hasSwdpLibrary() {
        String v = helper.getStandardPropertyEvaluator().getProperty(PROP_SWDP_CLASSPATH);
        return v != null;
    }
    
    protected void setProjectProperty(String name, String value) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(REST_SUPPORT_ON, "true");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }

    protected String getProjectProperty(String name) {
        return helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(name);
    }
}

