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

import org.openide.util.Lookup;

/** Interceptor which tells the server about the classloader to be used by the
 *  server's servlet loaders.
 *
 * @author petr.jiricka@czech.sun.com
 */
public class NbLoaderInterceptor extends BaseInterceptor {

    public NbLoaderInterceptor() {
    }

    private void setNbLoader( ContextManager cm ) throws TomcatException {
	ClassLoader cl = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        cm.setParentClassLoader(cl);
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbLoaderInterceptor - init"); // NOI18N
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    setNbLoader( cm );

            // here we set values normally supplied by WebXmlReader that we want to remove
            // exluding welcome files and JspServlet so only session timeout is here
            ctx.setSessionTimeOut (30);
	} catch (Exception e) {
            String msg = "NbLoaderInterceptor failed";  // NOI18N
	    System.out.println(msg);
	}

    }

}

