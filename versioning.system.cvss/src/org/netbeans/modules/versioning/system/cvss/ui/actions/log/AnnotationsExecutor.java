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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateCommand;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateInformation;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateLine;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;


/**
 * Executes a given 'annotate' command.
 * 
 * @author Petr Kuzel
 */
public class AnnotationsExecutor extends ExecutorSupport {

    private final List listeners = new ArrayList(1);

    public AnnotationsExecutor(CvsVersioningSystem cvs, AnnotateCommand cmd) {
        this(cvs, cmd, null);
    }
    
    public AnnotationsExecutor(CvsVersioningSystem cvs, AnnotateCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        AnnotateInformation ai = (AnnotateInformation) e.getInfoContainer();
        Iterator it = listeners.iterator();
        List lines = new ArrayList(100);
        AnnotateLine al = ai.getFirstLine();
        if (al != null) {
            lines.add(al);
            while (true) {
                al = ai.getNextLine();
                if (al == null) break;
                lines.add(al);
            }
            File localFile = e.getInfoContainer().getFile();
            while (it.hasNext()) {
                LogOutputListener listener = (LogOutputListener) it.next();
                listener.annotationLines(localFile, lines);
            }
        }
    }

    public void addLogOutputListener(LogOutputListener listener) {
        listeners.add(listener);
    }

    protected boolean logCommandOutput() {
        return false;
    }
}
