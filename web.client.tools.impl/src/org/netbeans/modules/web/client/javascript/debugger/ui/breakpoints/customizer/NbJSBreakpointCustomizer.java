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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.customizer;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Joelle Lam
 */
public class NbJSBreakpointCustomizer  extends JPanel implements Customizer, Controller{
    Breakpoint b;



    public static void customize(Breakpoint b) {
        JComponent c = getCustomizerPanel(b);
        HelpCtx helpCtx = HelpCtx.findHelp(c);
        if (helpCtx == null) {
            helpCtx = new HelpCtx("debug.add.breakpoint");  // NOI18N
        }
        final Controller[] cPtr = new Controller[]{(Controller) c        };
        final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
        final Dialog[] dialogPtr = new Dialog[1];
        ActionListener buttonsActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                    boolean ok = cPtr[0].ok();
                    if (ok) {
                        dialogPtr[0].setVisible(false);
                    }
                } else {
                    dialogPtr[0].setVisible(false);
                }
            }
        };
        DialogDescriptor descriptor = new DialogDescriptor(
                c,
                NbBundle.getMessage(
                NbJSBreakpointCustomizer.class,
                "CTL_Breakpoint_Customizer_Title" // NOI18N
                ),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                buttonsActionListener);
        descriptor.setClosingOptions(new Object[]{});
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.pack();
        descriptorPtr[0] = descriptor;
        dialogPtr[0] = d;
        d.setVisible(true);
    }


    JComponent customizerComponent;
    public void setObject(Object bean) {
        if( bean instanceof Breakpoint ){
            b = (Breakpoint)bean;
            init(b);
        }
    }
    
    private void init(Breakpoint b) {
        customizerComponent = getCustomizerPanel(b);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(customizerComponent, gbc);
    }

    public boolean ok() {
        return ((Controller) customizerComponent).ok();
    }

    public boolean cancel() {
        return ((Controller) customizerComponent).cancel();
    }
    
    public static JComponent getCustomizerPanel(Breakpoint b) {
        JComponent c = null;
        if ( b == null ){
            c = new NbJSBreakpointPanel();
        } else if (b instanceof NbJSBreakpoint) {
            c = new NbJSBreakpointPanel((NbJSBreakpoint)b);
        }
        c.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NbJSBreakpointCustomizer.class, "ACSD_Breakpoint_Customizer_Dialog")); // NOI18N
        c.setMinimumSize(new Dimension(300,100));
        return c;
    }
}
