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
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.TransferResource;

/**
 *
 * @author mkleint
 * @author anuradha
 */
public class ProgressTransferListener implements TransferListener {
    
    private static ThreadLocal<Integer> lengthRef = new ThreadLocal<Integer>();
    private static ThreadLocal<Integer> countRef = new ThreadLocal<Integer>();
    private static ThreadLocal<ProgressContributor> contribRef = new ThreadLocal<ProgressContributor>();
    private static ThreadLocal<ProgressContributor> pomcontribRef = new ThreadLocal<ProgressContributor>();
    private static ThreadLocal<Integer> pomCountRef = new ThreadLocal<Integer>();
    private static ThreadLocal<Stack<ProgressContributor>> contribStackRef = new ThreadLocal<Stack<ProgressContributor>>();
    private static ThreadLocal<AggregateProgressHandle> handleRef = new ThreadLocal<AggregateProgressHandle>();
    private static final ThreadLocal<AtomicBoolean> cancel = new ThreadLocal<AtomicBoolean>();
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
        cancel.remove();
    }

    /**
     * Produces a token which may be passed to {@link AggregateProgressFactory#createHandle}
     * in order to permit progress to be canceled.
     * If an event is received after a cancel request has been made, {@link ThreadDeath} will
     * be thrown (which you probably also want to catch and handle gracefully).
     * Must be called by the same thread as will call {@link #setAggregateHandle} and runs the process.
     * @return a cancellation token
     */
    public static Cancellable cancellable() {
        final AtomicBoolean b = new AtomicBoolean();
        cancel.set(b);
        return new Cancellable() {
            public @Override boolean cancel() {
                return b.compareAndSet(false, true);
            }
        };
    }

    private static void checkCancel() {
        AtomicBoolean b = cancel.get();
        if (b != null && b.get()) {
            throw new ThreadDeath();
        }
    }

    private String getResourceName(TransferResource res) {
        int lastSlash = res.getResourceName().lastIndexOf("/"); //NOI18N
        return lastSlash > -1 ? res.getResourceName().substring(lastSlash + 1) : res.getResourceName();
    }
    


    @Override
    public void transferInitiated(TransferEvent te) throws TransferCancelledException {
        if (handleRef.get() == null || contribStackRef.get() == null) {
            //maybe log?
            return;
        }
        assert handleRef.get() != null;
        assert contribStackRef.get() != null;
        
        TransferResource res = te.getResource();
        String resName = getResourceName(res);
        if (!resName.endsWith(".pom")) { //NOI18N
            Stack<ProgressContributor> stack = contribStackRef.get();
            ProgressContributor pc = stack != null && !stack.empty() ? stack.pop() : null;
            if (pc == null) {
                String name = (te.getRequestType() == TransferEvent.RequestType.GET
                        ? NbBundle.getMessage(ProgressTransferListener.class, "TXT_Download", resName)
                        : NbBundle.getMessage(ProgressTransferListener.class, "TXT_Uploading", resName));
                pc = AggregateProgressFactory.createProgressContributor(name);
                handleRef.get().addContributor(pc);
            }
            contribRef.set(pc);
        } else {
            String name = (te.getRequestType() == TransferEvent.RequestType.GET
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

    @Override
    public void transferStarted(TransferEvent te) throws TransferCancelledException {
        if (contribRef.get() == null || handleRef.get() == null) {
            return;
        }
        TransferResource res = te.getResource();
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

    @Override
    public void transferProgressed(TransferEvent te) throws TransferCancelledException {
         checkCancel();
        if (contribRef.get() == null) {
            return;
        }
        long cnt = (long)countRef.get();
        if (te.getDataLength() > 0) {
            cnt = cnt + te.getDataLength();
        }
        cnt = Math.min((long)Integer.MAX_VALUE, cnt);
        if (lengthRef.get() < 0) {
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferring", getResourceName(te.getResource())));
        } else {
            cnt = Math.min(cnt, (long)lengthRef.get());
            contribRef.get().progress(NbBundle.getMessage(ProgressTransferListener.class, "TXT_Transferred", getResourceName(te.getResource()), cnt), (int)cnt);
        }
        countRef.set((int)cnt);
    }

    @Override
    public void transferCorrupted(TransferEvent te) throws TransferCancelledException {
       if (contribRef.get() == null) {
            return;
        }
        contribRef.get().finish();
        contribRef.remove();
    }

    @Override
    public void transferSucceeded(TransferEvent te) {
        if (contribRef.get() == null) {
            return;
        }
        contribRef.get().finish();
        contribRef.remove();
    }

    @Override
    public void transferFailed(TransferEvent te) {
        if (contribRef.get() == null) {
            return;
        }
        contribRef.get().finish();
        contribRef.remove();
    }

}
