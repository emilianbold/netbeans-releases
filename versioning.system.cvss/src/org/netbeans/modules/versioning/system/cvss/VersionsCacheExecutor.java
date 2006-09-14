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
package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.PipedFileInformation;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.openide.filesystems.FileUtil;

import java.io.File;

/**
 * Fetches given revision.
 *
 * @author Petr Kuzel
 */
final class VersionsCacheExecutor extends ExecutorSupport {

    private File checkedOutVersion;

    public VersionsCacheExecutor(CheckoutCommand cmd, GlobalOptions options, boolean quiet) {
        super(CvsVersioningSystem.getInstance(), cmd, options);
        setNonInteractive(quiet);
    }

    protected synchronized void commandFinished(ClientRuntime.Result result) {
        if (checkedOutVersion == null) {
            // typical for dead files
            // System.err.println("CVS: " + cmd.getCVSCommand() + " misses piped response!");
        }
        notifyAll();
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        PipedFileInformation info = (PipedFileInformation) e.getInfoContainer();
        checkedOutVersion = FileUtil.normalizeFile(info.getTempFile());
    }

    public File getCheckedOutVersion() {
        return checkedOutVersion;
    }

    protected boolean logCommandOutput() {
        return ((CheckoutCommand)cmd).isPipeToOutput() == false;
    }


}
