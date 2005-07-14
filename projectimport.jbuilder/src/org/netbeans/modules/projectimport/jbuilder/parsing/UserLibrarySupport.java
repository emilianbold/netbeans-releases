/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.List;
import java.util.Collections;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.LoggerFactory;
import org.openide.ErrorManager;
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
public final class UserLibrarySupport {
    private static final String ROOT_ELEMENT = "library";//NOI18N
    private static final String FULLNAME_ELEMENT = "fullname";//NOI18N
    private static final String CLASS_ELEMENT = "class";//NOI18N
    private static final String PATH_ELEMENT = "path";//NOI18N
    
    private static File installDirLib;// = new File();
    private static File userHomeLib;// = new File();
    
    private File library;
    private String  libraryName;
    
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(UserLibrarySupport.class);
    
    public static AbstractProject.UserLibrary getInstance(String libraryName, File projectDir)  {
        File[] folders = new File[] {projectDir, getUserHomeLib(),getInstallDirLib()};
        UserLibrarySupport uSupport = UserLibrarySupport.getInstance(libraryName, folders);
        return (uSupport != null) ? uSupport.getLibrary() : null;
    }
    
    public static File getUserHomeLib() {
        if (userHomeLib == null) {
            String home = System.getProperty("user.home", "");//NOI18N
            
            if (home.length() > 0) {
                userHomeLib = new File(home, ".jbuilder2005");//NOI18N
                if (!userHomeLib.exists()) {
                    logger.finest("Not valid user.home.lib: " + userHomeLib);//NOI18N
                    userHomeLib = null;
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
        for (int i = 0; i < folders.length; i++) {
            String fileName = libraryName.trim()+".library";//NOI18N
            File library = new File(folders[i], fileName);
            if (!library.exists() && library.getParentFile() != null) {
                File[] allChildren = library.getParentFile().listFiles();
                for (int j = 0; j < allChildren.length; j++) {
                    if (allChildren[j].getName().equalsIgnoreCase(fileName)) {
                        UserLibrarySupport instance = new UserLibrarySupport(libraryName, allChildren[j]);
                        AbstractProject.UserLibrary ul = instance.getLibrary();
                        if (ul != null) {
                            library = allChildren[j];
                        }
                    }
                }
            }
            
            if (library.exists()) {
                UserLibrarySupport instance = new UserLibrarySupport(libraryName, library);
                return instance;
            } else {
                logger.finest("library: "+libraryName + " with path: " + library.getAbsolutePath() + " doesn't exists");//NOI18N
            }
        }
        
        return null;
    }
    /** Creates a new instance of JBLibraries */
    private UserLibrarySupport(String  libraryName, File library) {
        this.libraryName = libraryName;
        this.library = library;
    }
    
    AbstractProject.UserLibrary getLibrary()  {
        try {
            return buildLibrary();
        } catch (IOException iex) {
            ErrorManager.getDefault().notify(iex);
        } catch (SAXException sax) {
            ErrorManager.getDefault().notify(sax);
        }
        
        return null;
    }
    
    
    private AbstractProject.UserLibrary buildLibrary() throws IOException, SAXException {
        AbstractProject.UserLibrary retval = new AbstractProject.UserLibrary(libraryName);
        InputStream jprIs = new BufferedInputStream(new FileInputStream(library));
        try {
            Document doc = XMLUtil.parse(new InputSource(jprIs), false, false, null, null);
            Element docEl = getRootElement(doc);
            
            String fullName = getFullName(docEl);
            if (!fullName.equals(libraryName)) {
                return null;
            }
            
            Element classElem = Util.findElement(docEl, CLASS_ELEMENT,null);
            List/*<Element>*/ pathElems = (classElem != null) ? Util.findSubElements(classElem) : Collections.EMPTY_LIST;
            for (int i = 0; i < pathElems.size(); i++) {
                String path = getPath((Element)pathElems.get(i));
                if (path != null) {
                    AbstractProject.Library lEntry = createLibraryEntry(path);
                    if (lEntry != null) {
                        retval.addLibrary(lEntry);
                    }
                    
                }
            }
        } finally {
            if (jprIs != null) {
                jprIs.close();
            }
        }
        
        return retval;
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
            Element fullNameElement = Util.findElement(docEl, FULLNAME_ELEMENT,null);
            fullName = (fullNameElement != null) ? Util.findText(fullNameElement) : null;
        }
        
        return fullName;
    }
    
    
    private String getPath(Element pathElem) {
        String path = null;
        
        if (pathElem != null && pathElem.getNodeName().equals(PATH_ELEMENT)) {
            path = Util.findText(pathElem);
            
        }
        
        return path;
    }
    
}
