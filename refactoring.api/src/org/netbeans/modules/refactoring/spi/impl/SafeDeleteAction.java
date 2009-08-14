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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.explorer.ExtendedDelete;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** 
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.explorer.ExtendedDelete.class)
public class SafeDeleteAction extends RefactoringGlobalAction implements ExtendedDelete {
    
    private static final Logger LOGGER = Logger.getLogger(SafeDeleteAction.class.getName());

    /**
     * Creates a new instance of SafeDeleteAction
     */
    public SafeDeleteAction() {
        super (NbBundle.getMessage(SafeDeleteAction.class, "LBL_SafeDel_Action"), null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public final void performAction(Lookup context) {
        ActionsImplementationFactory.doDelete(context);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "SafeDeleteAction.performAction", new Exception());
        }
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Lookup context) {
        return ActionsImplementationFactory.canDelete(context); 
    }
    
    @Override
    protected Lookup getLookup(Node[] n) {
        Lookup l = super.getLookup(n);
        if (regularDelete) {
            ExplorerContext con = l.lookup(ExplorerContext.class);
            if (con!=null) {
                con.setDelete(true);
            } else {
                con = new ExplorerContext();
                con.setDelete(true);
                return new ProxyLookup(l, Lookups.singleton(con));
            }
        }
        return l;
    }
    
    private boolean regularDelete = false;
    public boolean delete(final Node[] nodes) {
        if (nodes.length < 2 && enable(nodes)) {
            if (java.awt.EventQueue.isDispatchThread()) {
                regularDelete = true;
                performAction(nodes);
                regularDelete = false;
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        regularDelete = true;
                        performAction(nodes);
                        regularDelete = false;
                    }
                    
                });
            }
            return true;
        } else {
            return false;
        }
    }
}
