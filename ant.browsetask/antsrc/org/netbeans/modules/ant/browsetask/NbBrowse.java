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

package org.netbeans.modules.ant.browsetask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.*;

import org.openide.awt.HtmlBrowser;

/**
 * Opens a web browser.
 * @author Jesse Glick
 */
public class NbBrowse extends Task {
    
    private String url;
    public void setUrl(String s) {
        url = s;
    }
    
    private File file;
    public void setFile(File f) {
        file = f;
    }
    
    public void execute() throws BuildException {
        if (url != null ^ file == null) throw new BuildException("You must define the url or file attributes", getLocation());
        if (url == null) {
            url = file.toURI().toString();
        }
        log("Browsing: " + url);
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(url));
        } catch (MalformedURLException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
