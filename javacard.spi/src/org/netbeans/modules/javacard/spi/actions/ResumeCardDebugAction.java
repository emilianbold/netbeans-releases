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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.spi.actions;

import java.util.Collection;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.spi.actions.ContextAction;
import org.openide.util.NbBundle;

/**
 *
 */
final class ResumeCardDebugAction extends ContextAction<ResumeCapability> {
    private final RunMode mode;
    ResumeCardDebugAction(RunMode mode) {
        super (ResumeCapability.class, mode == RunMode.RUN ? NbBundle.getMessage(ResumeCardDebugAction.class,
                "ACTION_RESUME_CARD") : NbBundle.getMessage(ResumeCardDebugAction.class, //NOI18N
                "ACTION_RESUME_CARD_MODAL", mode), null); //NOI18N
        this.mode = mode;
    }

    @Override
    protected void actionPerformed(Collection<? extends ResumeCapability> targets) {
        for (ResumeCapability c : targets) {
            c.resume(mode);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ResumeCardDebugAction && ((ResumeCardDebugAction)o).mode == mode;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (11 * mode.hashCode());
    }
}
