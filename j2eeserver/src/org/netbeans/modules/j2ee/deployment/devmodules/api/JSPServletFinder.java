/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.OldJSPDebug;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Jiricka
 */
public final class JSPServletFinder {
    
    public static final String SERVLET_FINDER_CHANGED = "servlet-finder-changed"; // NOI18N
    
    private Project project;

    /** Returns JSPServletFinder for the project that contains given file.
     * @return null if the file is not in any project
     */
    public static JSPServletFinder findJSPServletFinder(FileObject f) {
        Project prj = FileOwnerQuery.getOwner (f);
        return prj == null ? null : new JSPServletFinder(prj);
    }
    
    /** Creates a new instance of JspServletFinderImpl */
    private JSPServletFinder (Project project) {
        this.project = project;
    }
    
    private J2eeModuleProvider getProvider() {
        return (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
    }
    
    /** Returns the server instance currently selected for the module associated with this JSPServletFinder.
     * May return null.
     */
    private ServerString getServerString() {
        J2eeModuleProvider dl = getProvider ();
        if (dl == null)
            return null;
        ServerInstance instance = ServerRegistry.getInstance ().getServerInstance (dl.getServerInstanceID ());
        return instance == null ? null : new ServerString (instance);
    }
    
    
    private String getWebURL() {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        return provider.getConfigSupport().getWebContextRoot();
    }
    
    /** Returns the FindJSPServlet class associated with this JSPServletFinder.
     * May return null.
     */
    private FindJSPServlet getServletFinder() {
        ServerString serverS = getServerString();
        if (serverS == null)
            return null;
        ServerInstance inst = serverS.getServerInstance();
        if (inst == null)
            return null;
        return inst.getFindJSPServlet();
    }
    
    public File getServletTempDirectory () {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        //TargetModuleID moduleID = getTargetModuleID();
        //if (moduleID == null)
        //    return null;
        return find.getServletTempDirectory(webURL);
        
/*        try {
            J2eeDeploymentLookup dl = getDeploymentLookup();
            J2eeProfileSettings settings = dl.getJ2eeProfileSettings();
            DeploymentTargetImpl target = new DeploymentTargetImpl(settings, dl);
            ServerString serverS = target.getServer();
            ServerInstance inst = serverS.getServerInstance();
            DeploymentManager dm = inst.getDeploymentManager();
System.out.println("getting servlet temp directory - dm is  " + dm);
            TargetModuleID mod[] = dm.getAvailableModules(ModuleType.WAR, serverS.toTargets());
            TargetModuleID mod0 = null; // PENDING - find by web URI
            FindJSPServlet find = inst.getFindJSPServlet();
System.out.println("getting servlet temp directory - find is " + find);
            return find.getServletTempDirectory(mod0);
        }
        catch (TargetException e) {
            // PENDING
            return null;
        }*/
    }
 
    public String getServletResourcePath(String jspResourcePath) {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        return find.getServletResourcePath(webURL, jspResourcePath);
    }
 
    public String getServletEncoding(String jspResourcePath) {
        FindJSPServlet find = getServletFinder();
        if (find == null)
            return null;
        String webURL = getWebURL();
        if (webURL == null)
            return null;
        return find.getServletEncoding(webURL, jspResourcePath);
    }
 
    public OldJSPDebug.JspSourceMapper getSourceMapper(String jspResourcePath) {
        // PENDING
        return null;
    }
 
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // PENDING
    }
 
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // PENDING
    }
}
