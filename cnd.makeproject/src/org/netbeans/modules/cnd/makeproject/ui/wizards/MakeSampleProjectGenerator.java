/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
//import javax.swing.text.html.parser.Element;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;

/**
 * Create a sample web project by unzipping a template into some directory
 */
public class MakeSampleProjectGenerator {

    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/make-project/1"; // NOI18N
    private static final String PROJECT_CONFIGURATION_FILE = "nbproject/configurations.xml"; // NOI18N

    private MakeSampleProjectGenerator() {}

    public static Set createProjectFromTemplate(final FileObject template, File projectLocation, final String name) throws IOException {
	String mainProject = (String)template.getAttribute("mainProjectLocation"); // NOI18N
	String subProjects = (String)template.getAttribute("subProjectLocations"); // NOI18N
	if (mainProject != null) {
            File mainProjectLocation = new File(projectLocation.getPath() + File.separator + mainProject);
            File[] subProjectLocations = null;
            if (subProjects != null) {
                Vector subProjectsFiles = new Vector();
                StringTokenizer st = new StringTokenizer(subProjects, ","); // NOI18N
                while (st.hasMoreTokens()) {
                    subProjectsFiles.add(new File(projectLocation.getPath() + File.separator + st.nextToken()));
                }
                subProjectLocations = (File[])subProjectsFiles.toArray(new File[subProjectsFiles.size()]);
            }
	    return createProjectFromTemplate(template.getInputStream(), projectLocation, mainProjectLocation, subProjectLocations, name);
	}
	else {
	    return createProjectFromTemplate(template.getInputStream(), projectLocation, name);
	}
    }

    public static Set createProjectFromTemplate(final URL template, File projectLocation, final String name) throws IOException {
	return createProjectFromTemplate(template.openStream(), projectLocation, name);
    }

    public static Set createProjectFromTemplate(InputStream inputStream, File projectLocation, final String name) throws IOException {
        FileObject prjLoc = null;
        //if (template.getExt().endsWith("zip")) {  // NOI18N
            unzip(inputStream, projectLocation);
            // update project.xml
            try {
                prjLoc = FileUtil.toFileObject(projectLocation);

		// Change project name in 'project.xml'
                File projXml = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH));
                Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
		changeXmlFileByNameNS(doc, PROJECT_CONFIGURATION_NAMESPACE, "name", name, null); // NOI18N
                saveXml(doc, prjLoc, AntProjectHelper.PROJECT_XML_PATH);

		// Change working dir and default conf in 'projectDescriptor.xml'
		String workingDir = projectLocation.getPath();
		String systemOs = getCurrentSystemOs();
                projXml = FileUtil.toFile(prjLoc.getFileObject(PROJECT_CONFIGURATION_FILE));
                doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
		//changeXmlFileByTagName(doc, "buildCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
		//changeXmlFileByTagName(doc, "cleanCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
		//changeXmlFileByTagName(doc, "executablePath", workingDir, "X-PROJECTDIR-X"); // NOI18N
		//changeXmlFileByTagName(doc, "folderPath", workingDir, "X-PROJECTDIR-X"); // NOI18N
		changeXmlFileByTagName(doc, "defaultConf", systemOs, "X-DEFAULTCONF-X"); // NOI18N
                //saveXml(doc, prjLoc, "nbproject/projectDescriptor.xml"); // NOI18N
                saveXml(doc, prjLoc, PROJECT_CONFIGURATION_FILE);

            } catch (Exception e) {
                throw new IOException(e.toString());
            }
            
            prjLoc.refresh(false);
        //}
            
        return Collections.singleton(DataObject.find(prjLoc));
    }
    
    private static void addToSet(Vector set, File projectFile) throws IOException {
        try {
            FileObject prjLoc = null;
	    prjLoc = FileUtil.toFileObject(projectFile);
            prjLoc.refresh(false);
            set.add(DataObject.find(prjLoc));
        }
	catch (Exception e) {
	    throw new IOException(e.toString());
        }
    }

    public static Set createProjectFromTemplate(InputStream inputStream, File projectLocation, File mainProjectLocation, File[] subProjectLocations, String name) throws IOException {
        Vector set = new Vector();
        unzip(inputStream, projectLocation);
        addToSet(set, mainProjectLocation); 
        if (subProjectLocations != null) {
            for (int i = 0; i < subProjectLocations.length; i++)
                addToSet(set, subProjectLocations[i]);
        }
        return new LinkedHashSet(set);
    }

    private static void changeXmlFileByNameNS(Document doc, String tagNameNS, String tagName, String newText, String regex) throws IOException {
	NodeList nlist = doc.getElementsByTagNameNS(tagNameNS, tagName); // NOI18N
	changeXmlFileByNodeList(nlist, newText, regex);
    }

    private static void changeXmlFileByTagName(Document doc, String tagName, String newText, String regex) throws IOException {
	NodeList nlist = doc.getElementsByTagName(tagName); // NOI18N
	changeXmlFileByNodeList(nlist, newText, regex);
    }

    private static void changeXmlFileByNodeList(NodeList nlist, String newText, String regex) throws IOException {
	if (nlist != null) {
	    for (int i=0; i < nlist.getLength(); i++) {
		Node n = nlist.item(i);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
		    continue;
		}
		Element e = (Element)n;
    
		replaceText(e, newText, regex);
	    }
	}
    }
    
    private static void unzip(InputStream source, File targetFolder) throws IOException {
        //installation
        ZipInputStream zip=new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                File f = new File(targetFolder, ent.getName());
                if (ent.isDirectory()) {
                    f.mkdirs();
                } else {
                    f.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(f);
                    try {
                        FileUtil.copy(zip, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    private static void replaceText(Element parent, String name, String regex) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
		if (regex != null) {
		    String s = text.getNodeValue();
		    text.setNodeValue(s.replaceAll(regex, name));
		}
		else {
		    text.setNodeValue(name);
		}
                return;
            }
        }
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    /*
     * "0" = solaris/sparc
     * "1" = solaris/x86
     * "2" = Linux
     */
    private static String getCurrentSystemOs() {
	// FIXUP: needs improvement...
	String osname = System.getProperty("os.name"); // NOI18N
	String osarch = System.getProperty("os.arch"); // NOI18N

	if (osname.toLowerCase().indexOf("linux") >= 0) // NOI18N
	    return "2"; // NOI18N
	else if (osarch.indexOf("86") >= 0) // NOI18N
	    return "1"; // NOI18N
	else
	    return "0"; // NOI18N
    }
}
