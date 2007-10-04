/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * made subject to such option by the copyright holder.n
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

