/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import org.apache.tomcat.core.*;
import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.StringTokenizer;

import org.openide.TopManager;
import org.openide.util.SharedClassObject;

/**
 * @author costin@dnt.ro
 */
public class NbServletsInterceptor extends BaseInterceptor {

    public NbServletsInterceptor() {
    }

    private ServletWrapper addServlet( Context ctx, String name, String classN )
	throws TomcatException {
	ServletWrapper sw=new ServletWrapper();
	sw.setContext(ctx);
	sw.setServletName( name );
	sw.setServletClass( classN);
	ctx.addServlet( sw );
	sw.setLoadOnStartUp(0);
	return sw;
    }
    
    private void addNbServlets( Context ctx ) throws TomcatException {
        HttpServerSettings op = (HttpServerSettings)SharedClassObject.findObject (HttpServerSettings.class, true);
        ServletWrapper sw;
	//sw=addServlet( ctx, "NotFoundServlet", "org.netbeans.modules.httpserver.NotFoundServlet");
	//ctx.addServletMapping("/*", "NotFoundServlet");
        
	sw=addServlet( ctx, "RepositoryServlet", "org.netbeans.modules.httpserver.RepositoryServlet");  // NOI18N
	ctx.addServletMapping(op.getRepositoryBaseURL() + "*", "RepositoryServlet");                    // NOI18N
        
	sw=addServlet( ctx, "ClasspathServlet", "org.netbeans.modules.httpserver.ClasspathServlet");    // NOI18N
	ctx.addServletMapping(op.getClasspathBaseURL() + "*", "ClasspathServlet");                      // NOI18N
        
	sw=addServlet( ctx, "JavadocServlet", "org.netbeans.modules.httpserver.JavadocServlet");        // NOI18N
	ctx.addServletMapping(op.getJavadocBaseURL() + "*", "JavadocServlet");                          // NOI18N
        
	sw=addServlet( ctx, "WrapperServlet", "org.netbeans.modules.httpserver.WrapperServlet");        // NOI18N
	ctx.addServletMapping(op.getWrapperBaseURL () + "*", "WrapperServlet");                         // NOI18N
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbServletsInterceptor - init  " + ctx.getPath() + " " + ctx.getDocBase() );  // NOI18N
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    addNbServlets( ctx );

	} catch (Exception e) {
            // PENDING: use ErrorManager to report this
            String msg = "NbServletsInterceptor failed";
	    System.out.println(msg);
	}

    }

}

