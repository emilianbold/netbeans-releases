/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.actions.RenameAction;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


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
        if (!(n instanceof PropertiesLocaleNode))
            throw new IllegalStateException("Resource Bundle: Node is not PropertiesLocaleNode (renaming language)"); // NOI18N
        
        PropertiesLocaleNode pln = (PropertiesLocaleNode)n;

        String lang = Util.getLocalePartOfFileName (pln.getFileEntry());
        if (lang.length() > 0)
            if (lang.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                lang = lang.substring(1);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
            NbBundle.getBundle("org.openide.actions.Bundle").getString("CTL_RenameLabel"),
            NbBundle.getBundle("org.openide.actions.Bundle").getString("CTL_RenameTitle"));
        
        dlg.setInputText(lang);
        if (NotifyDescriptor.OK_OPTION.equals(TopManager.getDefault().notify(dlg))) {
            try {
                pln.setName(Util.assembleName (pln.getFileEntry().getDataObject().getPrimaryFile().getName(), dlg.getInputText()));
            }
            catch (IllegalArgumentException e) {
                // catch & report badly formatted names
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    MessageFormat.format(
                        NbBundle.getBundle("org.openide.actions.Bundle").getString("MSG_BadFormat"),
                        new Object[] {n.getName()}),
                    NotifyDescriptor.ERROR_MESSAGE);
                        
                TopManager.getDefault().notify(msg);
            }
        }
    }
}
