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
package org.netbeans.modules.visualweb.dataconnectivity.naming;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.naming.ProjectContextManager;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 * The factory that creates Creator's InitialContext
 *
 * @author John Kline
 */
//public class DesignTimeInitialContextFactory implements InitialContextFactory, ProjectChangeListener{
public class DesignTimeInitialContextFactory implements InitialContextFactory {
    private static DesignTimeContext prjContext = null;
    
    public static void setInitialContextFactoryBuilder() {
        try {
            javax.naming.spi.NamingManager.setInitialContextFactoryBuilder(
                    new javax.naming.spi.InitialContextFactoryBuilder() {
                public InitialContextFactory createInitialContextFactory(Hashtable env) {
                    return new DesignTimeInitialContextFactory();
                }
            }
            );
        } catch (NamingException e) {
        }
    }
    
    private static Context ctx = null;
    private static Hashtable env = null;
    
    public DesignTimeInitialContextFactory() {
    }
    
    public synchronized Context getInitialContext(Hashtable environment)
    throws NamingException {
        env = environment;
        String otherName = (String)environment.get(Context.INITIAL_CONTEXT_FACTORY);
        
        if (otherName != null && !getClass().getName().equals(otherName)) {
            try {
                InitialContextFactory otherFactory = (InitialContextFactory)Thread.currentThread().
                        getContextClassLoader().loadClass(otherName).newInstance();
                
                return otherFactory.getInitialContext(environment);
            } catch (Exception e) {
                // No NB ErrorManager here!
                e.printStackTrace();
            }
        }
        
        // If no projects open in the IDE then return null,
        // else if there is at least one open project then make sure that a context for
        // the project hasn't been created before creating a context
        Project currentProj = null; 
        if (CurrentProject.getInstance() == null)
            currentProj = OpenProjects.getDefault().getMainProject();
        else
            currentProj = CurrentProject.getInstance().getProject();
        
        // Handle IDE startup use case
        if (currentProj == null) {
            currentProj = CurrentProject.getInstance().getOpenedProject();
        }
        
        //Setup cache of projects per context
        ProjectContextManager prjCtxManager = ProjectContextManager.getInstance();
               
        // Construct a new context object for the current project
        prjContext = (DesignTimeContext)DesignTimeContext.createDesignTimeContext(currentProj, environment);                         
        prjCtxManager.addEntry(currentProj, prjContext);
        
        return prjContext;
    }
}
