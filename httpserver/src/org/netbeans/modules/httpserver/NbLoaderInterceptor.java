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

import org.openide.util.SharedClassObject;
import org.openide.TopManager;

/** Interceptor which tells the server about the classloader to be used by the 
 *  server's servlet loaders.
 *
 * @author petr.jiricka@czech.sun.com
 */
public class NbLoaderInterceptor extends BaseInterceptor {

    public NbLoaderInterceptor() {
    }

    private void setNbLoader( ContextManager cm ) throws TomcatException {
        HttpServerSettings op = (HttpServerSettings)SharedClassObject.findObject (HttpServerSettings.class, true);
        
        cm.setParentClassLoader(TopManager.getDefault().systemClassLoader());
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( ctx.getDebug() > 0 ) ctx.log("NbLoaderInterceptor - init"); // NOI18N
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Default init
	    setNbLoader( cm );

	} catch (Exception e) {
            String msg = "NbLoaderInterceptor failed";  // NOI18N
	    System.out.println(msg);
	}

    }

}

