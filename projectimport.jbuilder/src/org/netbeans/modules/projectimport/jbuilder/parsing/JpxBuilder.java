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

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.LoggerFactory;
import org.netbeans.modules.projectimport.j2seimport.ProjectModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Radek Matous
 */
final class JpxBuilder extends ProjectBuilder {
    private static final String ROOT_ELEMENT = "project";//NOI18N
    private static final String SYS_CATEGORY = "sys";//NOI18N
    
    public static final String SOURCE_PATH = "SourcePath";//NOI18N
    public static final String TEST_PATH = "TestPath";//NOI18N
    public static final String LIBRARIES = "Libraries";//NOI18N
    public static final String JDK = "JDK";//NOI18N
    
    private static final String ELEMENT = "property";//NOI18N
    
    private static final String CATEGORY_ATTR = "category";//NOI18N
    private static final String NAME_ATTR = "name";//NOI18N
    private static final String VALUE_ATTR = "value";//NOI18N
    
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(JpxBuilder.class);
    
    private String extension;
    
    /** Creates a new instance of JpxResourceType */
    public JpxBuilder(final String extension) {
        this.extension = extension;
    }
    
    protected String getSupportedExtension() {
        return extension;
    }
    
    protected final Collection/*<ProjectModel>*/ buildImpl(final File file) {
        Collection retval = new HashSet();
        try {
            retval.add(parseAndBuild(file));
        } catch (IOException iex) {
            ErrorManager.getDefault().notify(iex);
        } catch (SAXException sax) {
            ErrorManager.getDefault().notify(sax);
        }
        
        assert retval.size() > 0;
        return retval;
    }
    
    private ProjectModel parseAndBuild(final File jpxFile) throws IOException, SAXException {
        String sourcePath = "";//NOI18N
        String testPath = "";//NOI18N
        String libraries = "";//NOI18N
        String jdk = "";//NOI18N
        
        InputStream jprIs = new BufferedInputStream(new FileInputStream(jpxFile));
        try {
            Document doc = XMLUtil.parse(new InputSource(jprIs),
                    false, false, null, null);
            Element docEl = getRootElement(doc);
            
            
            NodeList nList = docEl.getElementsByTagName(ELEMENT);//NOI18N
            for (int i = 0; i < nList.getLength(); i++) {
                String category = null;
                String name = null;
                String value = null;
                
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element property = (Element)node;
                    category = property.getAttribute(CATEGORY_ATTR);
                    name = property.getAttribute(NAME_ATTR);
                    value = property.getAttribute(VALUE_ATTR);
                    
                    if (category != null && name != null && value != null) {
                        if (SOURCE_PATH.equals(name)) {
                            sourcePath = value;
                        }  if (TEST_PATH.equals(name)) {
                            testPath = value;
                        } else if (LIBRARIES.equals(name)) {
                            libraries = value;
                        } else if (JDK.equals(name)) {
                            jdk = value;
                        }
                    }
                }
            }
        } finally {
            if (jprIs != null) {
                jprIs.close();
            }
        }
        
        Enumeration sourcePathEn = new StringTokenizer(sourcePath,";");//NOI18N
        Enumeration testPathEn = new StringTokenizer(testPath,";");//NOI18N
        Enumeration librariesEn = new StringTokenizer(libraries,";");//NOI18N
        
        return createProjectModel(jpxFile, sourcePathEn,testPathEn, librariesEn, jdk);
    }
    
    private ProjectModel createProjectModel(final File jpxFile, Enumeration sourcePathEn,
            Enumeration testPathEn, Enumeration libraryEn, String jdkId) {
        File projectDir = jpxFile.getParentFile();
        assert projectDir.exists();
        assert projectDir.isDirectory();
        
        
        FileObject prjDirFo = FileUtil.toFileObject(projectDir);
        assert prjDirFo != null;
        
        AbstractProject project = new AbstractProject(jpxFile.getParentFile().getName(), prjDirFo);
        
        Set allTestPaths = new HashSet();
        while (testPathEn.hasMoreElements()) {
            String testPath = (String)testPathEn.nextElement();
            File testFile = FileUtil.normalizeFile(new File(projectDir, testPath));
            AbstractProject.SourceRoot asr;
            asr = new AbstractProject.SourceRoot(testFile.getName(), testFile);
            allTestPaths.add(asr);
        }
        
        while (sourcePathEn.hasMoreElements()) {
            String sourcePath = (String)sourcePathEn.nextElement();
            File sourceFile = FileUtil.normalizeFile(new File(projectDir, sourcePath));
            AbstractProject.SourceRoot asr;
            asr = new AbstractProject.SourceRoot(sourceFile.getName(), sourceFile);
            project.addSourceRoot(asr);
        }
        
        while (libraryEn.hasMoreElements()) {
            String libraryElement = (String)libraryEn.nextElement();
            File prjOrArchiv = FileUtil.normalizeFile(new File(projectDir, libraryElement));
            FileObject fo = FileUtil.toFileObject(prjOrArchiv);
            
            boolean isProject = ProjectBuilder.isProjectFile(prjOrArchiv) && fo != null;
            boolean isArchiv = (isProject) ? false : (fo != null && FileUtil.isArchiveFile(fo));
            boolean isUserLib = (isProject || isArchiv) ? false : !prjOrArchiv.exists();
            isUserLib = (isUserLib) ? (libraryElement.indexOf('/') == -1 ||
                    libraryElement.indexOf('\\') == -1) : false;
            
            
            if (isProject) {
                ProjectBuilder ftype = ProjectBuilder.getProvider(prjOrArchiv);
                
                if (ftype instanceof JpxBuilder) {
                    Collection subPrjs = ftype.buildProjectModels(prjOrArchiv);
                    for (Iterator it = subPrjs.iterator(); it.hasNext(); ) {
                        AbstractProject subPrjDef = (AbstractProject)it.next();
                        if (subPrjDef != null) {
                            project.addDependency(subPrjDef);
                        }
                    }
                }
                
            } else if (isArchiv) {
                
                prjOrArchiv = FileUtil.normalizeFile(prjOrArchiv);
                project.addLibrary(new AbstractProject.Library(prjOrArchiv));
                
            } else if (isUserLib) {
                
                final String libraryName = libraryElement;
                AbstractProject.UserLibrary aulib = UserLibrarySupport.getInstance(libraryName,projectDir);
                
                aulib = (aulib != null) ? aulib : new AbstractProject.UserLibrary(libraryName, true);
                project.addUserLibrary(aulib);
            }
        }
        
        if (jdkId != null) {
            project.setJdkId(jdkId);
            File jdkFolder = JdkSupport.getJKDDirectory(jdkId, projectDir);
            if (jdkFolder != null) {
                project.setJDKDirectory(jdkFolder);
            } else {
                project.setInvalidJDK(jdkId);
            }
            
        } else {
            logger.finest("no JDK found");//NOI18N
        }
        
        
        
        return project;
    }
    
    private Element getRootElement(Document doc) throws IOException {
        Element docEl = doc.getDocumentElement();
        
        if (!docEl.getTagName().equals(ROOT_ELEMENT)) { // NOI18N
            String message = NbBundle.getMessage(UserLibrarySupport.class,"ERR_WrongRootElement",docEl.getTagName());// NOI18N
            throw new IOException(message);
        }
        
        return docEl;
    }
}
