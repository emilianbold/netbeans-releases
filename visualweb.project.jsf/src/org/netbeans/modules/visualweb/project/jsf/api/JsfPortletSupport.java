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
import org.netbeans.modules.visualweb.project.jsf.*;
import java.io.File;
import org.openide.filesystems.FileObject;

/**
 *
 * @author dey, David Botterill
 */
public interface JsfPortletSupport {


    /**
     * This method will set the initial page for the portlet.
     * @param inMode The PortletModeType to set the initial page for
     * @param inFileObject The FileObject of the file to set as the inital VIEW Page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode or inFileObject == null
     */
    public void setInitialPage(PortletModeType inMode, FileObject inFileObject) throws JsfPortletSupportException;
    /**
     * This method will set the initial page for the portlet.
     * @param inMode The PortletModeType to set the initial page for
     * @param inFilePath The path of the file using the context relative path.  The path should include the leading "/".
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode or inFilePath == null
     */
    public void setInitialPage(PortletModeType inMode, String inFilePath) throws JsfPortletSupportException;

    /**
     * This method will set the portlet name
     * @param oldName the current name of the portlet to change.
     * @param newName the new name to give to the portlet.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if oldName or newName == null
     */
    public void setPortletName(String oldName, String newName) throws JsfPortletSupportException;

    /**
     * This method will unset the given initial page to no initial page.
     * @param inFileObject The FileObject of the file to set as the inital VIEW Page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inFileObject == null
     */
    public void unsetInitialPage(FileObject inFileObject) throws JsfPortletSupportException;

    /**
     * This method will check whether a given page is the initial page for a given mode.
     * @param inMode The PortletModeType to check for the initial page.
     * @param inFileObject The FileObject of the file to set check as the initial page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode or inFileObject == null
     * @return returns boolean true= the portlet page is the initial page for the given mode,
     * false= the portlet page is <b>NOT</b? the initial page for the given mode.
     */
    public boolean isInitialPage(PortletModeType inMode, FileObject inFileObject) throws JsfPortletSupportException;

    /**
     * This method will check whether a given page is an initial page for any mode.
     * @param inFileObject The FileObject of the file to set check as the initial page.
     * @return the PortletModeType the page is the initial page for.  If the page is not an initial page,
     * null is returned.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode or inFileObject == null
     */
    public PortletModeType getPortletMode(FileObject inFileObject) throws JsfPortletSupportException;

    /**
     * This method return the page for the given initial mode.
     * @param inMode The PortletModeType to check for the initial page.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if inMode == null
     * @return returns a String representing the portlet page for the given mode.  For example,
     * if "PortletPage1.jsp" is the page for a given mode, "/PortletPage1.jsp" would be returned.
     */
    public String getInitialPage(PortletModeType inMode) throws JsfPortletSupportException;


    /**
     * This method returns the portlet.xml file for the current project.
     * @return File representing the "portlet.xml" file for the project.
     * @throws org.netbeans.modules.visualweb.project.jsf.JsfPortletSupportException thrown if the portlet.xml file can not be found.
     */
    public File getPortletDD() throws JsfPortletSupportException;

    /**
     * This method returns the relative path of the folder for the given portlet page.  
     * @param inFO is the FileObject of the JSP page to get the relative Folder path for.
     * @returns String representing the relative path of the folder for the given portlet relative to the 
     * portlet project. For example, if the inFO has a path of "/home/david/Creator/Projects/Portlet1/web/edit/EditPage.jsp"
     * the folder "/edit" will be returned.
     */
    public String getPortletPageFolderPath(FileObject inFO);    
    
}
