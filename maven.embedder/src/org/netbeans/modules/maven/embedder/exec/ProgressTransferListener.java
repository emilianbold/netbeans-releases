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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.embedder.exec;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ProgressTransferListener implements TransferListener {
    
    private static ThreadLocal<Integer> lengthRef = new ThreadLocal<Integer>();
    private static ThreadLocal<Integer> countRef = new ThreadLocal<Integer>();
    private static ThreadLocal<ProgressContributor> contribRef = new ThreadLocal<ProgressContributor>();
    private static ThreadLocal<AggregateProgressHandle> handleRef = new ThreadLocal<AggregateProgressHandle>();
    /** Creates a new instance of ProgressTransferListener */
    public ProgressTransferListener() {
    }
    
    public static void setAggregateHandle(AggregateProgressHandle hndl) {
        handleRef.set(hndl);
    }
    
    public static void clearAggregateHandle() {
        handleRef.remove();
        contribRef.remove();
    }

    private String getResourceName(Resource res) {
        int lastSlash = res.getName().lastIndexOf("/"); //NOI18N
        return lastSlash > -1 ? res.getName().substring(lastSlash + 1) : res.getName();
    }
    
    public void transferInitiated(TransferEvent transferEvent) {
        Resource res = transferEvent.getResource();
        String resName = getResourceName(res);
        if (!resName.endsWith(".pom")) { //NOI18N
            String name = (transferEvent.getRequestType() == TransferEvent.REQUEST_GET
                              ? NbBundle.getMessage(ProgressTransferListener.class, "TXT_Download", resName) 
                              : NbBundle.getMessage(ProgressTransferListener.class, "TXT_Uploading", resName));
            contribRef.set(AggregateProgressFactory.createProgressContributor(name));
        }
    }
    
    public void transferStarted(TransferEvent transferEvent) {
//        String smer = transferEvent.getRequestType() == TransferEvent.REQUEST_GET ?
//                              "Downloading: " : "Uploading: "; //NOI18N - ends up in the maven output.
//        System.out.println(smer + transferEvent.getWagon().getRepository().getUrl() + "/" + transferEvent.getResource().getName()); //NOI18N
        if (contribRef.get() == null || handleRef.get() == null) {
            return;
        }
        Resource res = transferEvent.getResource();
        int total = (int)Math.min((long)Integer.MAX_VALUE, res.getContentLength());
        handleRef.get().addContributor(contribRef.get());
        if (total < 0) {
            contribRef.get().start(0);
        } else {
            contribRef.get().start(total);
        }
        lengthRef.set(total);
        countRef.set(0);
        contribRef.get().progress(org.openide.util.NbBundle.getMessage(ProgressTransferListener.class, "TXT_Started", getResourceName(res)));
    }
    
    public void transferProgress(TransferEvent transferEvent, byte[] b, int i) {
        if (contribRef.get() == null) {
            return;
        }
        long cnt = (long)countRef.get();
        if (i > 0) {
            cnt = cnt + i;
        }
        cnt = Math.min((long)Integer.MAX_VALUE, cnt);
        if (lengthRef.get() < 0) {
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferring", getResourceName(transferEvent.getResource())));
        } else {
            cnt = Math.min(cnt, (long)lengthRef.get());
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferred", getResourceName(transferEvent.getResource()), cnt), (int)cnt);
        }
        countRef.set((int)cnt);
    }
    
    public void transferCompleted(TransferEvent transferEvent) {
        if (contribRef.get() == null) {
            return;
        }
        contribRef.get().finish();
        contribRef.remove();
    }
    
    public void transferError(TransferEvent transferEvent) {
        transferCompleted(transferEvent);
        //TODO some reporting??
    }
    
    public void debug(String string) {
    }
    
}
