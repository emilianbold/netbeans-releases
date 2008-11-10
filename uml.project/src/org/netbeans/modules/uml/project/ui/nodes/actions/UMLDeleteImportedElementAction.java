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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.project.ui.nodes.actions;

import java.awt.Dialog;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.project.ui.cookies.ImportedElementCookie;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExtendedDelete;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

@org.openide.util.lookup.ServiceProvider(service=org.openide.explorer.ExtendedDelete.class)
public final class UMLDeleteImportedElementAction extends CookieAction implements ExtendedDelete
{

    public boolean delete(final Node[] nodes) throws IOException
    {
        if (enable(nodes))
        {
            if (java.awt.EventQueue.isDispatchThread())
            {
                performAction(nodes);
            } else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        performAction(nodes);
                    }
                });
            }
            return true;
        } else
        {
            return false;
        }
    }

    protected void performAction(Node[] activatedNodes)
    {
        if (activatedNodes.length == 0)
        {
            return;
        }
        
        ImportedElementDeletePanel panel = new ImportedElementDeletePanel(activatedNodes);
        String title = NbBundle.getMessage(UMLDeleteImportedElementAction.class,
                "TITLE_ConfirmDeleteImportedElement");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title,
                true, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.YES_OPTION, null);
        dialogDescriptor.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(title);

        try
        {
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() == DialogDescriptor.YES_OPTION)
            {
                boolean deleteOriginal = panel.getDeleteFromOriginal();

                for (Node node : activatedNodes)
                {
                    if (deleteOriginal)
                    {
                        node.destroy();
                    } else
                    {
                        ImportedElementCookie cookie = node.getLookup().lookup(ImportedElementCookie.class);
                        cookie.removeImportedElement();
                    }
                }
            }
        } catch (Exception e)
        {
            Logger.getLogger(UMLDeleteImportedElementAction.class.getName()).log(Level.SEVERE,
                    e.getLocalizedMessage());
        } finally
        {
            dialog.dispose();
        }
    }

    protected int mode()
    {
        return CookieAction.MODE_ALL;
    }

    public String getName()
    {
        return NbBundle.getMessage(UMLDeleteImportedElementAction.class,
                "CTL_RemoveFromImport");
    }

    protected Class[] cookieClasses()
    {
        return new Class[]{ImportedElementCookie.class};
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }
}