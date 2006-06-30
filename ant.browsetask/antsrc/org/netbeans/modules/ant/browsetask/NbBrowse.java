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
