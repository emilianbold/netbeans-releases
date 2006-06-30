/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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


package org.netbeans.modules.properties;


import java.text.MessageFormat;

import org.openide.actions.RenameAction;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * Renames a <code>PropertiesLocaleNode</code> node,
 * i.e. one locale (=locale suffix of one properties file)
 * belonging to certain bundle of properties files.
 *
 * @author   Petr Jiricka
 * @see Node#setName
 */
public class LangRenameAction extends RenameAction {

    /** Generated serial version UID. */
    static final long serialVersionUID =-6548687347804513177L;
    

    /** Performs action. Overrides superclass method. */
    protected void performAction (Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated
        Node.Cookie cake = n.getCookie(PropertiesLocaleNode.class);
        PropertiesLocaleNode pln = (PropertiesLocaleNode)cake;

        String lang = Util.getLocaleSuffix(pln.getFileEntry());
        if (lang.length() > 0)
            if (lang.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                lang = lang.substring(1);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
		NbBundle.getMessage(LangRenameAction.class,
				    "LBL_RenameLabel"),			//NOI18N
		NbBundle.getMessage(LangRenameAction.class,
				    "LBL_RenameTitle"));		//NOI18N
        
        dlg.setInputText(lang);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            try {
                pln.setName(Util.assembleName (pln.getFileEntry().getDataObject().getPrimaryFile().getName(), dlg.getInputText()));
            }
            catch (IllegalArgumentException e) {
                // catch & report badly formatted names
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    MessageFormat.format(
                        NbBundle.getBundle("org.openide.actions.Bundle").getString("MSG_BadFormat"),
                        new Object[] {pln.getName()}),
                    NotifyDescriptor.ERROR_MESSAGE);
                        
                DialogDisplayer.getDefault().notify(msg);
            }
        }
    }
}
