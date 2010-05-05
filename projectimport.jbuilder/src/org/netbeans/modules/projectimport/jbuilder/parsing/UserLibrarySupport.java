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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.LoggerFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Radek Matous
 */
public final class UserLibrarySupport {
    private static final String ROOT_ELEMENT = "library";//NOI18N
    private static final String FULLNAME_ELEMENT = "fullname";//NOI18N
    private static final String CLASS_ELEMENT = "class";//NOI18N
    private static final String PATH_ELEMENT = "path";//NOI18N
    private static final String REQUIRED_LIB = "required";//NOI18N    
    
    private static File installDirLib;// = new File();
    private static File userHomeLib;// = new File();
    
    private File library;
    private String  libraryName;
    
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(UserLibrarySupport.class);
    
    public static AbstractProject.UserLibrary getInstance(String libraryName, File projectDir)  {
        File[] folders = new File[] {projectDir, getUserHomeLib(),getInstallDirLib()};
        Set checkCyclicDeps = new HashSet();
        UserLibrarySupport uSupport = UserLibrarySupport.getInstance(libraryName, folders);
        return (uSupport != null) ? uSupport.getLibrary(folders, checkCyclicDeps) : null;
    }
    
    public static File getUserHomeLib() {
        if (userHomeLib == null) {
            String home = System.getProperty("user.home", "");//NOI18N
            
            if (home.length() > 0) {
                userHomeLib = new File(home, ".jbuilder2006");//NOI18N
                if (!userHomeLib.exists()) {
                    userHomeLib = new File(home, ".jbuilder2005");//NOI18N
                    if (!userHomeLib.exists()) {
                        logger.finest("Not valid user.home.lib: " + userHomeLib);//NOI18N
                        userHomeLib = null;
                    }
                }
            } else {
                logger.finest("Not valid user.home: ");//NOI18N
            }
        }
        
        return userHomeLib;
    }
    
    public static File getInstallDirLib() {
        return installDirLib;
    }
    
    
    
    public static void setUserHomeLib(final File uHomeDirLib) {
        userHomeLib = uHomeDirLib;
    }
    
    
    public static void setInstallDirLib(final File iDirLib) {
        installDirLib = iDirLib;
    }
    
    private static UserLibrarySupport getInstance(String libraryName, File[] folders)  {
        final String fileName = libraryName.trim()+".library";//NOI18N        
        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null) continue;
            File library = new File(folders[i], fileName);
            if (library.exists()) {
                return new UserLibrarySupport(libraryName, library);
            }
        }
        
        for (int i = 0; i < folders.length; i++) {
            if (folders[i] == null) continue;
            final File[] allChildren = folders[i].listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return f.isFile() && f.getName().endsWith(".library");//NOI18N
                }
            });
            if (allChildren == null) continue;
            for (int j = 0; j < allChildren.length; j++) {
                UserLibrarySupport result = resolveLibrary(libraryName, allChildren[j], folders, new HashSet());
                if (result != null) {
                    return result;
                }
            }
        }
        
        logger.finest("library: "+libraryName + " doesn't exists");//NOI18N
        return null;
    }

    private static UserLibrarySupport resolveLibrary(final String libraryName, final File libFile, final File[] folders,final Set checkCyclicDeps) {
        UserLibrarySupport instance = new UserLibrarySupport(libraryName, libFile);
        AbstractProject.UserLibrary ul = instance.getLibrary(folders, checkCyclicDeps);
        return ul != null ? instance : null;
    }
    
    /** Creates a new instance of JBLibraries */
    private UserLibrarySupport(String  libraryName, File library) {
        this.libraryName = libraryName;
        this.library = library;
    }
    
    private AbstractProject.UserLibrary getLibrary(File[] folders, Set checkCyclicDeps)  {
        try {
            return buildLibrary(folders, checkCyclicDeps);
        } catch (IOException iex) {
            ErrorManager.getDefault().notify(iex);
        } catch (SAXException sax) {
            ErrorManager.getDefault().notify(sax);
        }
        
        return null;
    }
    
    
    private AbstractProject.UserLibrary buildLibrary(File[] folders, Set checkCyclicDeps) throws IOException, SAXException {
        AbstractProject.UserLibrary retval = new AbstractProject.UserLibrary(libraryName);
        boolean isthere = checkCyclicDeps.add(libraryName);
        assert isthere : libraryName;
        InputStream jprIs = new BufferedInputStream(new FileInputStream(library));
        try {
            Document doc = XMLUtil.parse(new InputSource(jprIs), false, false, null, null);
            Element docEl = getRootElement(doc);
            
            String fullName = getFullName(docEl);
            if (!fullName.equals(libraryName)) {
                return null;
            }

            List<Element> reqElems = XMLUtil.findSubElements(docEl);
            for (int i = 0; i < reqElems.size(); i++) {
                Element elem = (Element)reqElems.get(i);
                String classElem = getClassElement(elem);
                if (classElem != null) {
                    resolvePath(folders, retval, elem);
                } else {
                    String requiredLibrary = getRequiredLibrary(elem);
                    if (requiredLibrary != null) {
                        if (checkCyclicDeps.contains(requiredLibrary)) {
                            AbstractProject.UserLibrary uL = new AbstractProject.UserLibrary(requiredLibrary, false);
                            retval.addDependency(uL);
                        } else {
                            UserLibrarySupport uS = UserLibrarySupport.getInstance(requiredLibrary, folders);
                            if (uS != null) {
                                AbstractProject.UserLibrary uL = uS.getLibrary(folders, checkCyclicDeps);
                                if (uL != null) {
                                    retval.addDependency(uL);
                                }
                            }
                        }
                    }
                }
            }
            
            //Element classElem = Util.findElement(docEl, CLASS_ELEMENT,null);
            
        } catch (Exception ex) {            
            System.out.println("libraryName: " + libraryName);
            return null;
        } finally {
            if (jprIs != null) {
                jprIs.close();
            }
        }
        
        return retval;
    }

    private void resolvePath(final File[] folders, final AbstractProject.UserLibrary retval, final Element classElem) throws IllegalArgumentException {
        List<Element> pathElems = (classElem != null) ? XMLUtil.findSubElements(classElem) : Collections.EMPTY_LIST;
        for (int i = 0; i < pathElems.size(); i++) {
            String path = getPath((Element)pathElems.get(i));
            if (path != null) {
                AbstractProject.Library lEntry = createLibraryEntry(path);
                if (lEntry != null) {
                    retval.addLibrary(lEntry);
                }                    
            }
        }
    }
    
    private Element getRootElement(Document doc) throws IOException {
        Element docEl = doc.getDocumentElement();
        
        if (!docEl.getTagName().equals(ROOT_ELEMENT)) { // NOI18N
            String message = NbBundle.getMessage(UserLibrarySupport.class,"ERR_WrongRootElement",docEl.getTagName());// NOI18N
            throw new IOException(message);
        }
        
        return docEl;
    }
    
    private AbstractProject.Library createLibraryEntry(String encodedPath) {
        String decodedPath = encodedPath.replaceAll("^\\[", "");//NOI18N
        decodedPath = decodedPath.replaceAll("]", "");//NOI18N
        decodedPath = decodedPath.replaceAll("\\%\\|", ":");//NOI18N
        File f = new File(decodedPath);
        if (!f.exists()) {
            f = new File(library.getParentFile(), decodedPath);
        }
        f = FileUtil.normalizeFile(f);
        if (!f.exists()) {
            logger.finest(encodedPath+ " converted into file: " + f.getAbsolutePath() );//NOI18N
        }
        return (f.exists()) ? new AbstractProject.Library(f) : null;
    }
    
    private String getFullName(Element docEl) {
        String fullName = null;
        
        if (docEl != null) {
            try {XMLUtil.write(docEl.getOwnerDocument(), System.out, "UTC-8"); } catch (Exception e) {}
            Element fullNameElement = XMLUtil.findElement(docEl, FULLNAME_ELEMENT,null);
            fullName = (fullNameElement != null) ? XMLUtil.findText(fullNameElement) : null;
        }
        
        return fullName;
    }
    
    
    private String getPath(Element pathElem) {
        return getElement(pathElem, PATH_ELEMENT);
    }

    private String getRequiredLibrary(Element pathElem) {
        return getElement(pathElem, REQUIRED_LIB);
    }
    
    private String getClassElement(Element pathElem) {
        return getElement(pathElem, CLASS_ELEMENT);
    }
    
    
    private String getElement(final Element pathElem, String name) {
        String path = null;
        
        if (pathElem != null && pathElem.getNodeName().equals(name)) {
            path = XMLUtil.findText(pathElem);
            
        }
        
        return path;
    }

    
}
