/*
 * TargetModuleConverter.java
 *
 * Created on August 21, 2003, 7:13 AM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import org.netbeans.spi.settings.DOMConvertor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.filesystems.*;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.*;

/**
 *
 * @author  nn136682
 */
public class TargetModuleConverter extends DOMConvertor {
    
    private static final String PUBLIC_ID = "-//org_netbeans_modules_j2ee//DTD TargetModule 1.0//EN"; // NOI18N
    private static final String SYSTEM_ID = "nbres:/org/netbeans/modules/j2ee/deployment/impl/target-module.dtd"; // NOI18N
    private static final String E_TARGET_MODULE_LIST = "target-module-list";

    private static final String E_TARGET_MODULE = "target-module";
    private static final String A_ID = "id";
    private static final String A_INSTANCE_URL = "instance-url";
    private static final String A_TARGET_NAME = "target-name";
    private static final String A_TIMESTAMP = "timestamp";
    private static final String A_CONTENT_DIR = "content-dir";
    private static final String A_CONTEXT_ROOT = "context-root";
    
    public static DOMConvertor create() {
        return new TargetModuleConverter();
    }
    
    /** Creates a new instance of TargetModuleConverter */
    protected TargetModuleConverter() {
        super(PUBLIC_ID, SYSTEM_ID, E_TARGET_MODULE_LIST);
    }
    
    protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        NodeList targetModuleElements =  element.getElementsByTagName(E_TARGET_MODULE);
        TargetModule[] targetModules = new TargetModule[targetModuleElements.getLength()];
        for (int i=0; i<targetModules.length; i++) {
            Element te = (Element) targetModuleElements.item(i);
            String id = te.getAttribute(A_ID);
            String url = te.getAttribute(A_INSTANCE_URL);
            String targetName = te.getAttribute(A_TARGET_NAME);
            String timestamp = te.getAttribute(A_TIMESTAMP);
            String contentDir = te.getAttribute(A_CONTENT_DIR);
            String contextRoot = te.getAttribute(A_CONTEXT_ROOT);

            if (id == null || url == null || targetName == null)
                throw new IOException(NbBundle.getMessage(TargetModuleConverter.class, "MSG_TargetModuleParseError"));
                
            try {
                targetModules[i] = new TargetModule(id, url, targetName, Long.parseLong(timestamp), contentDir, contextRoot);
            } catch (NumberFormatException nfe) {
                throw (IOException) ErrorManager.getDefault().annotate(new IOException(), nfe);
            }
        }
        return new TargetModule.List(targetModules);
    }
    
    protected void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element element, Object obj) throws IOException, DOMException {
        if (obj == null)
            return;
        
        if (! (obj instanceof TargetModule.List))
            throw new DOMException(
            DOMException.NOT_SUPPORTED_ERR, 
            NbBundle.getMessage(TargetModuleConverter.class, "MSG_NotSupportedObject", obj.getClass()));
        
        TargetModule.List tmList = (TargetModule.List) obj;
        TargetModule[] targetModules = tmList.getTargetModules();
        for (int i=0; i<targetModules.length; i++) {
            Element tmElement = doc.createElement (E_TARGET_MODULE);
            tmElement.setAttribute(A_ID, targetModules[i].getId());
            tmElement.setAttribute(A_INSTANCE_URL, targetModules[i].getInstanceUrl());
            tmElement.setAttribute(A_TARGET_NAME, targetModules[i].getTargetName());
            tmElement.setAttribute(A_TIMESTAMP, String.valueOf(targetModules[i].getTimestamp()));
            tmElement.setAttribute(A_CONTENT_DIR, targetModules[i].getContentDirectory());
            tmElement.setAttribute(A_CONTEXT_ROOT, targetModules[i].getContextRoot());
            element.appendChild (tmElement);
        }
    }

    public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of TargetModule
    }
    public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of TargetModule
    }

    private static final String DIR_TARGETMODULES = "TargetModules";
    private static FileObject targetModulesDir = null;
    private static FileObject getTargetModulesDir() throws IOException {
        if (targetModulesDir == null) {
            FileObject j2eeDir = Repository.getDefault().getDefaultFileSystem().findResource("/J2EE");
            targetModulesDir = j2eeDir.getFileObject(DIR_TARGETMODULES);
            if (targetModulesDir == null)
                targetModulesDir = j2eeDir.createFolder(DIR_TARGETMODULES);
        }
        return targetModulesDir;
    }

    public static boolean writeTargetModule(TargetModule instance, String managerDir, String targetDir, String tmFileName) {
        FileLock lock = null;
        Writer writer = null;
        try {
            FileObject managerDirFO = getTargetModulesDir().getFileObject(managerDir);
            if (managerDirFO == null)
                managerDirFO = getTargetModulesDir().createFolder(managerDir);
            FileObject targetDirFO = managerDirFO.getFileObject(targetDir);
            if (targetDirFO == null)
                targetDirFO = managerDirFO.createFolder(targetDir);
            FileObject fo = FileUtil.createData(targetDirFO, tmFileName);
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            create().write(writer, new TargetModule.List(instance));
            return true;
            
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
            return false;
        }
        finally {
            try {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
            } catch (Exception e) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, e);
            }
        }
    }

    public static TargetModule readTargetModule(String managerDir, String targetDir, String tmFileName) {
        Reader reader = null;
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject (targetDir);
                if (dir != null) {
                    FileObject fo = dir.getFileObject(tmFileName);
                    if (fo != null) {
                        reader = new InputStreamReader(fo.getInputStream());
                        TargetModule.List tml = (TargetModule.List) create().read(reader);
                        if (tml == null || tml.getTargetModules().length < 1)
                            return null;
                        return tml.getTargetModules()[0];
                    }
                }
            }
            return null;
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
            return null;
        } finally {
            try {  if (reader != null) reader.close(); } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }

    public static List getTargetModulesByContextRoot(String managerDir, String targetDir, String contextRoot) {
        Reader reader = null;
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject (targetDir);
                if (dir != null) {
                    java.util.Enumeration fos = dir.getChildren(false);
                    ArrayList result = new ArrayList();
                    while (fos.hasMoreElements()){
                        FileObject fo = (FileObject) fos.nextElement();
                        reader = new InputStreamReader(fo.getInputStream());
                        TargetModule.List tml = (TargetModule.List) create().read(reader);
                        if (tml != null && tml.getTargetModules().length > 0) {
                            TargetModule tm = tml.getTargetModules()[0];
                            if (contextRoot.equals(tm.getContextRoot()))
                                result.add(tm);
                        }
                    }
                    return result;
                }
            }
            return Collections.EMPTY_LIST;
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
            return Collections.EMPTY_LIST;
        } finally {
            try {  if (reader != null) reader.close(); } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }

    public static TargetModule remove(String managerDir, String targetDir, String tmFileName) {
        FileLock lock = null;
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject (targetDir);
                if (dir != null) {
                    FileObject fo = dir.getFileObject(tmFileName);
                    if (fo != null) {
                        lock = fo.lock();
                        fo.delete(lock);
                    }
                }
            }
            return null;
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
            return null;
        } finally {
            try {  if (lock != null) lock.releaseLock(); } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }

}
