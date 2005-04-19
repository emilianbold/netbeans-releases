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

package org.netbeans.modules.j2ee.deployment.impl;

import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import java.io.*;

import org.netbeans.spi.settings.DOMConvertor;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.filesystems.*;

/**
 * @author  nn136682
 */
public class ServerStringConverter extends org.netbeans.spi.settings.DOMConvertor {
    
    private static final String E_SERVER_STRING = "server-string";
    private static final String E_TARGET = "target";
    private static final String A_PLUGIN = "plugin";
    private static final String A_URL = "url";
    private static final String A_NAME = "name";
    private static final String PUBLIC_ID = "-//org_netbeans_modules_j2ee//DTD ServerString 1.0//EN"; // NOI18N
    private static final String SYSTEM_ID = "nbres:/org/netbeans/modules/j2ee/deployment/impl/server-string.dtd"; // NOI18N
    
    public static boolean writeServerInstance(ServerString instance, String destDir, String destFile) {
        FileLock lock = null;
        Writer writer = null;
        try {
            FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(destDir);
            FileObject fo = FileUtil.createData(dir, destFile);
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            create().write(writer, instance);
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

    public static ServerString readServerInstance(String fromDir, String fromFile) {
        Reader reader = null;
        try {
            FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(fromDir);
            FileObject fo = dir.getFileObject (fromFile);
            if (fo == null)
                return null;
            
            reader = new InputStreamReader(fo.getInputStream());
            return (ServerString) create().read(reader);
        } catch(Exception ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.WARNING, ioe);
            return null;
        } finally {
            try {  if (reader != null) reader.close(); } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }
    
    public static DOMConvertor create() {
        return new ServerStringConverter();
    }
    
    /** Creates a new instance of ServerStringConverter */
    protected ServerStringConverter() {
        super(PUBLIC_ID, SYSTEM_ID, E_SERVER_STRING);
    }
    
    protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        NodeList targetElements =  element.getElementsByTagName(E_TARGET);
        String[] targets = new String[targetElements.getLength()];
        for (int i=0; i<targets.length; i++) {
            Element te = (Element) targetElements.item(i);
            targets[i] = te.getAttribute(A_NAME);
            if (targets[i] == null)
                throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", E_TARGET));
        }
        String plugin = element.getAttribute(A_PLUGIN);
        if (plugin == null)
            throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", A_PLUGIN));
        
        String url = element.getAttribute(A_URL);
        //if (plugin == null)
        //    throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_ServerStringParseError", A_URL));

        return new ServerString(plugin, url, targets);
    }
    
    public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of ServerName
    }
    public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of ServerName
    }
    protected void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element element, Object obj) throws IOException, DOMException {
        if (obj == null)
            return;
        
        if (! (obj instanceof ServerString))
            throw new DOMException(
            DOMException.NOT_SUPPORTED_ERR, 
            NbBundle.getMessage(ServerStringConverter.class, "MSG_NotSupportedObject", obj.getClass()));
        
        ServerString ss = (ServerString) obj;
        if (ss.getPlugin() == null)
            throw new IOException(NbBundle.getMessage(ServerStringConverter.class, "MSG_BadServerString", ss));

        String[] targets = ss.getTargets();
        if (targets == null)
            targets = new String[0];
        
        for (int i=0; i<targets.length; i++) {
            Element targetElement = doc.createElement (E_TARGET);
            targetElement.setAttribute(A_NAME, targets[i]);
            element.appendChild (targetElement);
        }
        String url = ss.getUrl();
        if (url == null)
            url = "";
        element.setAttribute(A_URL, url);
        element.setAttribute(A_PLUGIN, ss.getPlugin());
    }
}
