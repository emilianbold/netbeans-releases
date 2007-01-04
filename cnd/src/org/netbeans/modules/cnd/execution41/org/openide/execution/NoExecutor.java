/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.execution41.org.openide.execution;

import org.openide.execution.ExecutorTask;

import org.openide.util.NbBundle;

/** default executor type if none is registered. */
final class NoExecutor extends Executor {

    private static Executor NO_EXECUTOR;

    private static final long serialVersionUID = -7574239908797899512L;

    private NoExecutor () {
    }

    protected String displayName () {
        return NbBundle.getBundle (NoExecutor.class).getString ("LAB_NoExecutor"); // NOI18N
    }

    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx (NoExecutor.class);
    }

    public ExecutorTask execute (ExecInfo info) throws java.io.IOException {
        throw new java.io.IOException (NbBundle.getMessage (NoExecutor.class,
		"EXC_NoExecutor", info.getClassName ())); // NOI18N
    }

    private Object readResolve () throws java.io.ObjectStreamException {
        return getInstance();
    }

    /** get default instance */
    public synchronized static Executor getInstance() {
        if (NO_EXECUTOR == null) {
            NO_EXECUTOR = new NoExecutor();
        }
        return NO_EXECUTOR;
    }
}
