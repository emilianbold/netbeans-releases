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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.openide.ErrorManager;

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
            ErrorManager em = ErrorManager.getDefault();
            em.annotate(e, "NbServletsInterceptor failed"); // NOI18N
	    em.notify(ErrorManager.INFORMATIONAL, e);
	}

    }

}

