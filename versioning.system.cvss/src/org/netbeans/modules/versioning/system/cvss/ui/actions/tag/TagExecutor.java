/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.add.AddInformation;

import java.util.*;
import java.io.File;

/**
 * Executes a given 'tag' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class TagExecutor extends ExecutorSupport {
    
    public TagExecutor(CvsVersioningSystem cvs, TagCommand cmd) {
        this(cvs, cmd, null);
    }
    
    public TagExecutor(CvsVersioningSystem cvs, TagCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        
        Set parents = new HashSet();
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            DefaultFileInfoContainer info = (DefaultFileInfoContainer) i.next();
            File file = info.getFile();
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            parents.add(file.getParentFile());
        }
        toRefresh.clear();
        
        for (Iterator i = parents.iterator(); i.hasNext();) {
            File dir = (File) i.next();
            cache.refreshCached(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);            
        }
    }
}
