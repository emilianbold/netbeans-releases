/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which opens the Search Results window.
 *
 * @see  ResultView
 * @author  Marian Petras
 */
public class ResultViewOpenAction extends AbstractAction {
    
    /**
     * Creates an instance of this action.
     */
    public ResultViewOpenAction() {
        String name = NbBundle.getMessage(
                ResultViewOpenAction.class,
                "TEXT_ACTION_SEARCH_RESULTS");                          //NOI18N
        putValue(NAME, name);
        putValue("iconBase",                                            //NOI18N
                 "org/netbeans/modules/search/res/find.gif");           //NOI18N
    }
    
    /**
     * Opens and activates the Search Results window.
     *
     * @param  e  event that caused this action to be called
     */
    public void actionPerformed(ActionEvent e) {
        ResultView resultView = ResultView.getInstance();
        resultView.open();
        resultView.requestActive();
    }
    
}
