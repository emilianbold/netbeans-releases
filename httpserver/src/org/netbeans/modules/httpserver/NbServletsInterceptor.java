package org.netbeans.modules.httpserver;

import org.apache.tomcat.core.*;
import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.StringTokenizer;

import org.openide.TopManager;

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
        HttpServerSettings op = HttpServerSettings.OPTIONS;
        
        ctx.getServletLoader().setParentLoader(TopManager.getDefault().systemClassLoader());
        
        ServletWrapper sw;
	//sw=addServlet( ctx, "NotFoundServlet", "org.netbeans.modules.httpserver.NotFoundServlet");
	//ctx.addServletMapping("/*", "NotFoundServlet");
        
	sw=addServlet( ctx, "RepositoryServlet", "org.netbeans.modules.httpserver.RepositoryServlet");
	ctx.addServletMapping(op.getRepositoryBaseURL() + "*", "RepositoryServlet");
        
	sw=addServlet( ctx, "ClasspathServlet", "org.netbeans.modules.httpserver.ClasspathServlet");
	ctx.addServletMapping(op.getClasspathBaseURL() + "*", "ClasspathServlet");
        
	sw=addServlet( ctx, "JavadocServlet", "org.netbeans.modules.httpserver.JavadocServlet");
	ctx.addServletMapping(op.getJavadocBaseURL() + "*", "JavadocServlet");
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbServletsInterceptor - init  " + ctx.getPath() + " " + ctx.getDocBase() );
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    addNbServlets( ctx );

	} catch (Exception e) {
            String msg = "NbServletsInterceptor failed";
	    System.out.println(msg);
	}

    }

}

