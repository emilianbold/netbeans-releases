/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug;
import org.openide.nodes.Node;

/**
 *
 * @author  pj97932
 */
public interface JSPServletFinder extends Node.Cookie {
    
    public static final String SERVLET_FINDER_CHANGED = "servlet-finder-changed"; // NOI18N
    
    public File getServletTempDirectory();
    
    public String getServletResourcePath(String jspResourcePath);
    
    public String getServletEncoding(String jspResourcePath);
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    public OldJSPDebug.JspSourceMapper getSourceMapper(String jspResourcePath);
    
}
