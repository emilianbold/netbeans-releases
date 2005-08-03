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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.*;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Handles module events distributed by NetBeans module
 * framework.
 *
 * <p>It's registered and instantiated from module manifest.
 *
 * @author Petr Kuzel
 * @author Maros Sandor
 */
public final class ModuleLifecycleManager extends ModuleInstall implements ErrorHandler {

    static final String [] vcsGenericModules = {
        "org.netbeans.modules.vcs.advanced",
    };
    
    public void restored() {
        disableOldModules();
    }

    private void disableOldModules() {
        Runnable runnable = new Runnable() {
            public void run() {
                boolean notified = false;
                for (int i = 0; i < vcsGenericModules.length; i++) {
                    FileLock lock = null;
                    OutputStream os = null;
                    try {
                        String newModule = vcsGenericModules[i];
                        String newModuleXML = "Modules/" + newModule.replace('.', '-') + ".xml";
                        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(newModuleXML);
                        if (fo == null) continue;
                        Document document = readModuleDocument(fo);

                        NodeList list = document.getDocumentElement().getElementsByTagName("param");
                        int n = list.getLength();
                        for (int j = 0; j < n; j++) {
                            Element node = (Element) list.item(j);
                            if ("enabled".equals(node.getAttribute("name"))) {
                                Text text = (Text) node.getChildNodes().item(0);
                                String value = text.getNodeValue();
                                if ("true".equals(value)) {
                                    text.setNodeValue("false");
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                        if (!notified) {
                            JOptionPane.showMessageDialog(null, 
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"), 
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"), 
                                                          JOptionPane.WARNING_MESSAGE);
                            notified = true;
                        }
                        lock = fo.lock();
                        os = fo.getOutputStream(lock);
                        
                        XMLUtil.write(document, os, "UTF-8");
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        if (os != null) try { os.close(); } catch (IOException ex) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setErrorHandler(this);
        InputStream is = fo.getInputStream();
        Document document = parser.parse(is);
        is.close();
        return document;
    }

    public void uninstalled() {
        CvsVersioningSystem.getInstance().shutdown();
    }

    public void error(SAXParseException exception) throws SAXException {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void warning(SAXParseException exception) throws SAXException {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }
}
