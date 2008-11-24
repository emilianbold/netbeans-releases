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

package org.netbeans.modules.ide.ergonomics.debugger;

import javax.swing.JComponent;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Flaska
 */
public class GdbAttachTypeProxy extends AttachType {
    private static final String NAME = NbBundle.getMessage(GdbAttachTypeProxy.class, "CTL_GdbAttachPanel_name"); // NOI18N
    private AttachType delegate;
    private boolean isVisible;
    
    public GdbAttachTypeProxy() {
        this.delegate = null;
        this.isVisible = true;
    }

    @Override
    public String getTypeDisplayName() {
        if (!isVisible) {
            return null;
        } else {
            if (getAttachType() != null) {
                isVisible = false;
                return null;
            }
        }
        if (delegate != null) {
            return delegate.getTypeDisplayName();
        }
        return NAME;
    }

    @Override
    public JComponent getCustomizer() {
        if (delegate == null) {
            return new DebuggerConfigurationPanel(this);
        } else {
            return delegate.getCustomizer();
        }
    }

    @Override
    public Controller getController() {
        if (delegate == null) {
            return super.getController();
        } else {
            return delegate.getController();
        }
    }

    public void invalidate() {
        isVisible = false;
        delegate = getAttachType();
    }

    AttachType getAttachType() {
        for (AttachType type : DebuggerManager.getDebuggerManager().lookup(null, AttachType.class)) {
            if (type.getClass().equals(GdbAttachTypeProxy.class)) {
                continue;
            } else if (type.getTypeDisplayName().equals(NAME)) {
                return type;
            }
        }
        return null;
    }

}
