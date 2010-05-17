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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
