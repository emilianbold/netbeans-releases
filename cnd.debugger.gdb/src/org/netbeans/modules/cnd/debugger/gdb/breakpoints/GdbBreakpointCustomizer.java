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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.Customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.modules.cnd.debugger.gdb.models.BreakpointsActionsProvider;
import org.netbeans.spi.debugger.ui.Controller;

/**
 *
 * @author martin
 */
public class GdbBreakpointCustomizer extends JPanel implements Customizer, Controller {
    
    private JComponent c;
    
    public void setObject(Object bean) {
        if (!(bean instanceof CndBreakpoint)) {
            throw new IllegalArgumentException(bean.toString());
        }
        init((CndBreakpoint) bean);
    }
    
    private void init(CndBreakpoint b) {
        c = BreakpointsActionsProvider.getCustomizerComponent(b);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(c, gbc);
    }

    public boolean ok() {
        return ((Controller) c).ok();
    }

    public boolean cancel() {
        return ((Controller) c).cancel();
    }

}
