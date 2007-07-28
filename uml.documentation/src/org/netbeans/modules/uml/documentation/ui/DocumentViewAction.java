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

package org.netbeans.modules.uml.documentation.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.openide.util.HelpCtx;
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
      putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("org/netbeans/modules/uml/documentation/ui/resources/DocPane.gif"))); // NOI18N
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
