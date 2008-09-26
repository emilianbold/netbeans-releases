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

package org.netbeans.modules.uml.documentation.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *  Displays the Describe documentation editor.
 *
 * @author  Darshan
 * @version 1.0
 */
public class DocumentViewAction extends AbstractAction
{
   private static DocumentViewAction instance = null;
   public DocumentViewAction()
   {
      putValue(Action.NAME, NbBundle.getMessage(DocumentViewAction.class, "Action.Doc.Title"));
      putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/uml/documentation/ui/resources/DocPane.gif"))); // NOI18N
//		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift D")); //#NOI18N
   }

   public static synchronized DocumentViewAction getInstance() {
       if (instance == null)
           instance = new DocumentViewAction();
       return instance;
   }

    /**
     * Returns the help context for this action. Currently, no help is provided.
     * @return <code>null</code> always.
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     *  Returns the (display) name of this action.
     *
     * @return The <code>MessagesBundle.document</code> value for
     *         <code>Action.DocView.Title</code>
     */
    public String getName() {
        return (String)getValue(Action.NAME);
    }

    public void actionPerformed(ActionEvent e) {
        DocumentationTopComponnet topC = DocumentationTopComponnet.getInstance();
        if (topC != null) 
        {
            topC.open();
            topC.requestActive();
        }
    }
}
