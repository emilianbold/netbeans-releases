/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
/** 
*
* @author Milan Kuchtiak
*/
public final class SetExecutionUriAction extends NodeAction {
    public static final String ATTR_EXECUTION_URI = "execution.uri"; //NOI18N
    
    /** Creates and starts a thread for generating documentation
    */
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject)activatedNodes[0].getCookie(DataObject.class);
                if (data != null) {
                    FileObject servletFo = data.getPrimaryFile();
                    WebModule webModule = WebModule.getWebModule(servletFo);
                    String[] urlPatterns = getServletMappings(webModule, servletFo);
                    if (urlPatterns!=null && urlPatterns.length>0) {
                        String oldUri = (String)servletFo.getAttribute(ATTR_EXECUTION_URI);
                        ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,oldUri,false);
                        DialogDescriptor desc = new DialogDescriptor(uriPanel,
                            NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                        Object res = DialogDisplayer.getDefault().notify(desc);
                        if (res.equals(NotifyDescriptor.YES_OPTION)) {
                            try {
                                servletFo.setAttribute(ATTR_EXECUTION_URI,uriPanel.getServletUri());
                            } catch (java.io.IOException ex){}
                        } else return;
                    } else {
                        String mes = java.text.MessageFormat.format (
                                NbBundle.getMessage (SetExecutionUriAction.class, "TXT_missingServletMappings"),
                                new Object [] {servletFo.getName()}); //NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                }
            }
        }
    }
    
    protected boolean enable (Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject)activatedNodes[0].getCookie(DataObject.class);
                if (data != null) {
                    String mimetype = data.getPrimaryFile().getMIMEType();
                    Boolean servletAttr = (Boolean)data.getPrimaryFile().getAttribute("org.netbeans.modules.web.IsServletFile"); //NOI18N
                    return "text/x-java".equals(mimetype) && Boolean.TRUE.equals(servletAttr); //NOI18N
                }
            }
        }
        return false;
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SetExecutionUriAction.class);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getMessage(SetExecutionUriAction.class, "LBL_serveltExecutionUriAction");
    }
    
    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "org/netbeans/modules/web/project/ui/resources/servletUri.gif"; // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public static String[] getServletMappings(WebModule webModule, FileObject javaClass) {
        if (webModule==null) return null;
        FileObject webDir = webModule.getDocumentBase ();
        if (webDir==null) return null;
        FileObject fo = webDir.getFileObject("WEB-INF/web.xml"); //NOI18N
        if (fo==null) return null;
        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        String className = classPath.getResourceName(javaClass,'.',false);
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet[] servlets = webApp.getServlet();
            java.util.List mappingList = new java.util.ArrayList();
            for (int i=0;i<servlets.length;i++) {
                if (className.equals(servlets[i].getServletClass())) {
                    String servletName=servlets[i].getServletName();
                    ServletMapping[] maps  = webApp.getServletMapping();
                    for (int j=0;j<maps.length;j++) {
                        if (maps[j].getServletName().equals(servletName)) {
                            String urlPattern = maps[j].getUrlPattern();
                            if (urlPattern!=null)
                                mappingList.add(urlPattern);
                        }
                    }
                }
            }
            String[] mappings = new String[mappingList.size()];
            mappingList.toArray(mappings);
            return mappings;
        } catch (java.io.IOException ex) {return null;}  
    }
    
}
