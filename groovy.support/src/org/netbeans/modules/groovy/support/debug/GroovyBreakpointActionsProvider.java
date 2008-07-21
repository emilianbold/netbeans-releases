/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.support.debug;

import javax.swing.Action;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 * @author Martin Adamek
 */
public class GroovyBreakpointActionsProvider implements NodeActionsProviderFilter {
    
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction (
        NbBundle.getMessage(GroovyBreakpointActionsProvider.class, "LBL_Action_Go_To_Source"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                goToSource ((GroovyLineBreakpoint) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        NbBundle.getMessage(GroovyBreakpointActionsProvider.class, "LBL_Action_Customize"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return false;
            }
            public void perform (Object[] nodes) {
//                customize ((Breakpoint) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    
    
    public Action[] getActions (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (!(node instanceof GroovyLineBreakpoint))
            return original.getActions (node);
        
        Action[] oas = original.getActions (node);
        if (node instanceof GroovyLineBreakpoint) {
            Action[] as = new Action [oas.length + 3];
            as [0] = GO_TO_SOURCE_ACTION;
            as [1] = null;
            System.arraycopy (oas, 0, as, 2, oas.length);
            as [as.length - 1] = CUSTOMIZE_ACTION;
            return as;
        }
        Action[] as = new Action [oas.length + 1];
        System.arraycopy (oas, 0, as, 0, oas.length);
        as [as.length - 1] = CUSTOMIZE_ACTION;
        return as;
    }
    
    public void performDefaultAction (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof GroovyLineBreakpoint)
            goToSource ((GroovyLineBreakpoint) node);
        else
            original.performDefaultAction (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

//    private static void customize (Breakpoint b) {
//        JComponent c = null;
//        if (b instanceof GroovyLineBreakpoint) {
//            c = new JspBreakpointPanel((GroovyLineBreakpoint) b);
//        }
//
//        DialogDescriptor descriptor = new DialogDescriptor (
//            c,
//            NbBundle.getMessage (
//                GroovyBreakpointActionsProvider.class,
//                "CTL_Breakpoint_Customizer_Title" // NOI18N
//             // NOI18N
//            )
//
//        );
//
//        JButton bOk = null;
//        JButton bClose = null;
//        descriptor.setOptions (new JButton[] {
//            bOk = new JButton (NbBundle.getMessage (
//                GroovyBreakpointActionsProvider.class,
//                "CTL_Ok" // NOI18N
//             // NOI18N
//            )),
//            bClose = new JButton (NbBundle.getMessage (
//                GroovyBreakpointActionsProvider.class,
//                "CTL_Close" // NOI18N
//             // NOI18N
//            ))
//
//        });
//        HelpCtx helpCtx = HelpCtx.findHelp (c);
//        if (helpCtx == null)
//            helpCtx = new HelpCtx ("debug.add.breakpoint");;
//        descriptor.setHelpCtx (helpCtx);
//        bOk.getAccessibleContext ().setAccessibleDescription (
//            NbBundle.getMessage (
//                GroovyBreakpointActionsProvider.class,
//                "ACSD_CTL_Ok" // NOI18N
//             // NOI18N
//            )
//
//        );
//        bOk.setMnemonic(NbBundle.getMessage(GroovyBreakpointActionsProvider.class, "CTL_Ok_MNEM").charAt(0)); // NOI18N
//        bClose.getAccessibleContext ().setAccessibleDescription (
//            NbBundle.getMessage (
//                GroovyBreakpointActionsProvider.class,
//                "ACSD_CTL_Close" // NOI18N
//             // NOI18N
//            )
//
//        );
//        bClose.setMnemonic(NbBundle.getMessage(GroovyBreakpointActionsProvider.class, "CTL_Close_MNEM").charAt(0)); // NOI18N
//        descriptor.setClosingOptions (null);
//        Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
//        d.pack ();
//        d.setVisible (true);
//        if (descriptor.getValue () == bOk) {
//            ((Controller) c).ok ();
//        }
//    }
    
    private static void goToSource (GroovyLineBreakpoint b) {
        Context.showSource (b);
    }
}
