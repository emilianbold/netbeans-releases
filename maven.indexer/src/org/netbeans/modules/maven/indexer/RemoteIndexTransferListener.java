/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Anuradha G
 */
public class RemoteIndexTransferListener implements TransferListener {

    private ProgressHandle handle;
    private RepositoryInfo info;
    private int lastunit;/*last work unit*/
    /*Debug*/

    private boolean debug;
    private InputOutput io;
    private OutputWriter writer;

    public RemoteIndexTransferListener(RepositoryInfo info) {

        this.info = info;


        if (debug) {
            io = IOProvider.getDefault().getIO(NbBundle.getMessage(RemoteIndexTransferListener.class, "LBL_Transfer_TAG")//NII18N
                    + (info.getName()), true);
            writer = io.getOut();
        }
    }

    public void transferInitiated(TransferEvent arg0) {/*EMPTY*/

    }

    public void transferStarted(TransferEvent arg0) {
        long contentLength = arg0.getResource().getContentLength();
        this.handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RemoteIndexTransferListener.class, "LBL_Transfer_TAG")//NII18N
                + info.getName());
        handle.start((int) contentLength / 1024);
        if (debug) {
            writer.println("File Size :" + (int) contentLength / 1024);//NII18N

        }
    }

    public void transferProgress(TransferEvent arg0, byte[] arg1, int arg2) {
        int work = arg2 / 1024;
        if (handle != null) {
            handle.progress(lastunit += work);
        }
        if (debug) {
            writer.println("Units completed :" + lastunit);//NII18N

        }
    }

    public void transferCompleted(TransferEvent arg0) {
        if (handle != null) {
            handle.finish();
        }
        if (debug) {
            writer.println("Completed");//NII18N

        }
    }

    public void transferError(TransferEvent arg0) {

        if (debug) {
            writer.println("Finish with Errors");//NII18N

        }
    }

    public void debug(String arg0) {
        if (debug) {
            writer.println(arg0);
        }
    }
}
