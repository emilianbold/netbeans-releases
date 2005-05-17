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
 * Microsystems, Inc. All Rights Reserved.n
 */

package org.netbeans.modules.web.wizards;

import java.io.IOException; 

import org.openide.filesystems.FileObject;

import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

//import org.netbeans.api.java.classpath.ClassPath;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** 
* Generic methods for evaluating the input into the wizards. 
* 
* @author Ana von Klopp 
*/

abstract class DeployData { 

    WebApp webApp = null; 
    String className = null; 
    boolean makeEntry = true; 
    FileObject ddObject = null; 

    final static boolean debug = false; 

    // This is the web app file object
    void setWebApp(FileObject fo) { 
	if(debug) log("::setWebApp()"); 
	if(fo == null) { 
	    ddObject = null; 
	    webApp = null; 
	    return;
	} 

	ddObject = fo; 

	try { 
	    webApp = DDProvider.getDefault().getDDRoot(fo);
	    if(debug) log(webApp.toString()); 
	}
	catch(IOException ioex) {
	    if(debug) { 
		log("Couldn't get the web app!");  
		ioex.printStackTrace(); // XXX this is not an exception handling
	    }
	} 
	catch(Exception ex) {
	    if(debug) { 
		log("Couldn't get the web app!");  
		ex.printStackTrace();  // XXX this is not an exception handling
	    }
	} 
    } 

    String getClassName() { 
	if(className == null) return ""; 
	return className; 
    } 

    void setClassName(String name) { 
	this.className = name; 
    } 

    boolean makeEntry() { 
	return makeEntry; 
    } 

    void setMakeEntry(boolean makeEntry) { 
	this.makeEntry = makeEntry; 
    } 

    void writeChanges() throws IOException { 

	if(debug) log("::writeChanges()"); //NOI18N
	if(webApp == null) return; 
	if(debug) log("now writing..."); //NOI18N
        webApp.write(ddObject);
    }

    abstract boolean isValid();
    // This must invoke write changes at the end 
    abstract void createDDEntries(); 
    abstract String getErrorMessage(); 
    abstract void log(String s);
    abstract void setAddToDD(boolean addToDD);
    abstract boolean isAddToDD();
    
    public static FileObject getWebAppFor(FileObject folder) {
        if (folder==null) return null;
        WebModule webModule = WebModule.getWebModule(folder);
        if (webModule==null) return null;
        return webModule.getDeploymentDescriptor ();
    }
    
    public boolean hasDD() {
        return webApp!=null;
    }
}

