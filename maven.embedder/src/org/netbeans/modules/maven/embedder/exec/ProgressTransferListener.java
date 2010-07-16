/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.Stack;
import org.apache.maven.repository.ArtifactTransferEvent;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.repository.ArtifactTransferResource;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ProgressTransferListener implements ArtifactTransferListener {
    
    private static ThreadLocal<Integer> lengthRef = new ThreadLocal<Integer>();
    private static ThreadLocal<Integer> countRef = new ThreadLocal<Integer>();
    private static ThreadLocal<ProgressContributor> contribRef = new ThreadLocal<ProgressContributor>();
    private static ThreadLocal<ProgressContributor> pomcontribRef = new ThreadLocal<ProgressContributor>();
    private static ThreadLocal<Integer> pomCountRef = new ThreadLocal<Integer>();
    private static ThreadLocal<Stack<ProgressContributor>> contribStackRef = new ThreadLocal<Stack<ProgressContributor>>();
    private static ThreadLocal<AggregateProgressHandle> handleRef = new ThreadLocal<AggregateProgressHandle>();
    private static final int POM_MAX = 20;
    /** Creates a new instance of ProgressTransferListener */
    public ProgressTransferListener() {
    }
    
    public static void setAggregateHandle(AggregateProgressHandle hndl) {
        handleRef.set(hndl);
        contribStackRef.set(new Stack<ProgressContributor>());
        ProgressContributor pc = AggregateProgressFactory.createProgressContributor("Pom files");
        hndl.addContributor(pc);
        pc.start(POM_MAX);
        pomCountRef.set(new Integer(0));
        pomcontribRef.set(pc);
    }
    
    public static void clearAggregateHandle() {
        handleRef.remove();
        contribRef.remove();
        contribStackRef.remove();
        pomcontribRef.remove();
        pomCountRef.remove();
    }

    private String getResourceName(ArtifactTransferResource res) {
        int lastSlash = res.getName().lastIndexOf("/"); //NOI18N
        return lastSlash > -1 ? res.getName().substring(lastSlash + 1) : res.getName();
    }
    
    public void transferInitiated(ArtifactTransferEvent ate) {
        if (handleRef.get() == null || contribStackRef.get() == null) {
            //maybe log?
            return;
        }
        assert handleRef.get() != null;
        assert contribStackRef.get() != null;
        
        ArtifactTransferResource res = ate.getResource();
        String resName = getResourceName(res);
        if (!resName.endsWith(".pom")) { //NOI18N
            Stack<ProgressContributor> stack = contribStackRef.get();
            ProgressContributor pc = stack != null && !stack.empty() ? stack.pop() : null;
            if (pc == null) {
                String name = (ate.getRequestType() == ArtifactTransferEvent.REQUEST_GET
                        ? NbBundle.getMessage(ProgressTransferListener.class, "TXT_Download", resName)
                        : NbBundle.getMessage(ProgressTransferListener.class, "TXT_Uploading", resName));
                pc = AggregateProgressFactory.createProgressContributor(name);
                handleRef.get().addContributor(pc);
            }
            contribRef.set(pc);
        } else {
            String name = (ate.getRequestType() == ArtifactTransferEvent.REQUEST_GET
                    ? NbBundle.getMessage(ProgressTransferListener.class, "TXT_Download", resName)
                    : NbBundle.getMessage(ProgressTransferListener.class, "TXT_Uploading", resName));
            ProgressContributor pc = AggregateProgressFactory.createProgressContributor(name);
            contribStackRef.get().add(pc);
            handleRef.get().addContributor(pc);
            int count = pomCountRef.get();
            if (count < POM_MAX - 1) {
                count = count + 1;
                pomcontribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Started", resName), count);
                pomCountRef.set(new Integer(count));
            } else {
                pomcontribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Started", resName));
            }
        }
    }
    
    public void transferStarted(ArtifactTransferEvent ate) {
//        String smer = transferEvent.getRequestType() == TransferEvent.REQUEST_GET ?
//                              "Downloading: " : "Uploading: "; //NOI18N - ends up in the maven output.
//        System.out.println(smer + transferEvent.getWagon().getRepository().getUrl() + "/" + transferEvent.getResource().getName()); //NOI18N
        if (contribRef.get() == null || handleRef.get() == null) {
            return;
        }
        ArtifactTransferResource res = ate.getResource();
        int total = (int)Math.min((long)Integer.MAX_VALUE, res.getContentLength());
        if (total < 0) {
            contribRef.get().start(0);
        } else {
            contribRef.get().start(total);
        }
        lengthRef.set(total);
        countRef.set(0);
        contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Started", getResourceName(res)));
    }
    
    public void transferProgress(ArtifactTransferEvent ate) {
        if (contribRef.get() == null) {
            return;
        }
        long cnt = (long)countRef.get();
        if (ate.getDataLength() > 0) {
            cnt = cnt + ate.getDataLength();
        }
        cnt = Math.min((long)Integer.MAX_VALUE, cnt);
        if (lengthRef.get() < 0) {
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferring", getResourceName(ate.getResource())));
        } else {
            cnt = Math.min(cnt, (long)lengthRef.get());
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferred", getResourceName(ate.getResource()), cnt), (int)cnt);
        }
        countRef.set((int)cnt);
    }
    
    public void transferCompleted(ArtifactTransferEvent ate) {
        if (contribRef.get() == null) {
            return;
        }
        contribRef.get().finish();
        contribRef.remove();
    }
    
    public boolean isShowChecksumEvents() {
        return false;
    }

    public void setShowChecksumEvents(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
