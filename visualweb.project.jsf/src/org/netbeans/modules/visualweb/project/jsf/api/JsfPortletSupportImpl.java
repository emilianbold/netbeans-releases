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


package org.netbeans.modules.visualweb.project.jsf.api;

import org.netbeans.modules.visualweb.api.portlet.dd.PortletModeType;
import org.netbeans.modules.visualweb.api.portlet.dd.PortletDDHelper;
import org.netbeans.modules.visualweb.project.jsf.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * This class implements the portlet support for a project.
 * @author David Botterill
 */
public class JsfPortletSupportImpl implements JsfPortletSupport {
    private Project project;

    /**
     * This constructor needs to be as FAST as possible since it will be called
     * by a static method each time someone simply CHECKS for portlet support on
     * a project.
     * @param project The Project that this class will provide portlet support for.
     */
    public JsfPortletSupportImpl(Project project) {
        this.project = project;
    }

    /**
     * This method will set the portlet name
     * @param oldName the current name of the portlet to change.
     * @param newName the new name to give to the portlet.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if oldName or newName == null
     */
    public void setPortletName(String oldName, String newName) throws JsfPortletSupportException {
        if(null == oldName || null == newName ) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_NAMES_NULL"));
        }
        /**
         * Now find the portlet.xml file.
         */

        File portletDDFile = getPortletDD();

        /**
         * Create a helper and set the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);

        ddHelper.setPortletName(oldName,newName);
    }
    /**
     * This method will set the initial page for the portlet.
     * @param inMode The PortletModeType to set the initial page for
     * @param inFilePath The path of the file using the context relative path.  The path should include the leading "/".
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode or inFilePath == null
     */
    public void setInitialPage(PortletModeType inMode, String inFilePath) throws JsfPortletSupportException {

        File initialFile = new File(inFilePath);
        if(null == initialFile || null == inMode) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }

        /**
         * Now find the portlet.xml file.
         */

        File portletDDFile = getPortletDD();

        /**
         * Create a helper and set the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);
        ddHelper.setInitialPage(inMode,inFilePath);

    }

    /**
     * This method will set the initial VIEW page for the portlet.
     * @param inMode The PortletModeType to set the initial page for
     * @param inFileObject The FileObject of the file to set as the inital VIEW Page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inFileObject == null
     */
    public void setInitialPage(PortletModeType inMode, FileObject inFileObject) throws JsfPortletSupportException {
        if(null == inFileObject ) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        if(null == inMode) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_MODE_NULL"));
        }
        
        /**
         * First get the file to be made the initial page.
         */
        FileObject initialFO= inFileObject;
        File initialFile = FileUtil.toFile(initialFO);
        if(null == initialFile || null == initialFile.getName() || initialFile.getName().equals("")) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        
        /**
         * Now find the portlet.xml file.
         */
        
        File portletDDFile = getPortletDD();
        
        /**
         * Create a helper and set the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);

        String initialPageName = initialFile.getName();
        String initialPath = getPortletPageFolderPath(initialFO);
       this.setInitialPage(inMode,initialPath + "/" + initialPageName);
        
    }
    /**
     * This method will unset the given initial page to no initial page.
     * @param inFileObject The FileObject of the file to set as the inital VIEW Page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inFileObject == null
     */
    public void unsetInitialPage(FileObject inFileObject) throws JsfPortletSupportException {
        if(null == inFileObject ) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        /**
         * First get the file to be made the initial page.
         */
        FileObject initialFO= inFileObject;
        File initialFile = FileUtil.toFile(initialFO);
        if(null == initialFile || null == initialFile.getName() || initialFile.getName().equals("")) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        
        /**
         * Now find the portlet.xml file.
         */
        
        File portletDDFile = getPortletDD();
        
        /**
         * Create a helper and set the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);
        String initialPageName = initialFile.getName();
        String initialPath = getPortletPageFolderPath(initialFO);        
        ddHelper.unsetInitialPage(initialPath + "/" + initialPageName);
        
    }
    
    /**
     * This method will check whether a given page is the initial page for a given mode.
     * @param inMode The PortletModeType to check for the initial page.
     * @param inFileObject The FileObject of the file to set check as the initial page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inFileObject == null
     */
    public boolean isInitialPage(PortletModeType inMode, FileObject inFileObject) throws JsfPortletSupportException {
        if(null == inFileObject ) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        if(null == inMode) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_MODE_NULL"));
        }
        
        /**
         * First get the file to be made checked as initial page.
         */
        FileObject initialFO= inFileObject;
        
        File initialFile = FileUtil.toFile(initialFO);
        if(null == initialFile || null == initialFile.getName() || initialFile.getName().equals("")) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        
        /**
         * Now find the portlet.xml file.
         */
        
        File portletDDFile = getPortletDD();
        
        /**
         * Create a helper and set check the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);
        String initialPageName = initialFile.getName();
        String initialPath = getPortletPageFolderPath(initialFO);
        return ddHelper.isInitialPage(inMode,initialPath + "/" + initialPageName);
        
    }
    
    /**
     * This method will check whether a given page is an initial page for any mode.
     * @param inFileObject The FileObject of the file to set check as the initial page.
     * @return the PortletModeType the page is the initial page for.  If the page is not an initial page, 
     * null is returned.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inFileObject == null
     */
    public PortletModeType getPortletMode(FileObject inFileObject) throws JsfPortletSupportException {
        if(null == inFileObject ) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        
        /**
         * First get the file to be made checked as initial page.
         */
        FileObject initialFO= inFileObject;
        
        File initialFile = FileUtil.toFile(initialFO);
        if(null == initialFile || null == initialFile.getName() || initialFile.getName().equals("")) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_DATAOBJECT_NULL"));
        }
        
        /**
         * Now find the portlet.xml file.
         */
        
        File portletDDFile = getPortletDD();
        
        /**
         * Create a helper and set check the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);
        String initialPageName = initialFile.getName();
        String initialPath = getPortletPageFolderPath(initialFO);
        return ddHelper.isInitialPage(initialPath + "/" + initialPageName);
        
    }
    /**
     * This method return the page for the given initial mode.
     * @param inMode The PortletModeType to check for the initial page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode == null
     */
    public String getInitialPage(PortletModeType inMode) throws JsfPortletSupportException {
        
        if(null == inMode) {
            throw new JsfPortletSupportException(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_MODE_NULL"));
        }
                
        /**
         * Now find the portlet.xml file.
         */
        
        File portletDDFile = getPortletDD();
        
        /**
         * Create a helper and set the inital page.
         */
        PortletDDHelper ddHelper = new PortletDDHelper(portletDDFile);        
        return ddHelper.getInitialPage(inMode);
        
    }
    
    /**
     * This method returns the portlet.xml file for the current project.
     * @return File representing the "portlet.xml" file for the project.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if the portlet.xml file can not be found.
     */
    
    public File getPortletDD() throws JsfPortletSupportException {
        FileObject portletDDFO = project.getProjectDirectory().getFileObject(JsfProjectConstants.PATH_WEB_INF + "/portlet.xml");  // NOI18N
        if(null == portletDDFO) {
            String message = MessageFormat.format(NbBundle.getMessage(JsfPortletSupportImpl.class, "MSG_JsfPortletSupportImpl_PORTLETDDNOTFOUND"),
                    new Object[] {project.getProjectDirectory().getName()});
                    throw new JsfPortletSupportException(message);
        }
        
        File portletDDFile = FileUtil.toFile(portletDDFO);
        
        return portletDDFile;
    }
    
    /**
     * This method returns the relative path of the folder for the given portlet page.  
     * @param inFO is the FileObject to get the relative Folder path for.
     * @returns String representing the relative path of the folder for the given portlet relative to the 
     * portlet project. For example, if the inFO has a path of "/home/david/Creator/Projects/Portlet1/web/edit/EditPage.jsp"
     * the folder "/edit" will be returned.
     */
    public String getPortletPageFolderPath(FileObject inFO) {
        String returnPath = null;
        FileObject webRoot = project.getProjectDirectory().getFileObject(JsfProjectConstants.PATH_DOC_ROOT);
        if(webRoot == null) {
            return null;
        }
        String webRootPath = webRoot.getPath();
        String jspPath = inFO.getParent().getPath();
        if(jspPath.startsWith(webRootPath)) {
            returnPath = jspPath.substring(webRootPath.length());
        }
        
        return returnPath;
    }
    
}
