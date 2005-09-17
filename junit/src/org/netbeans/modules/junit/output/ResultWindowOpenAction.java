/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * Action which opens the JUnit Test Results window.
 *
 * @see  ResultWindow
 * @author  Marian Petras
 */
public final class ResultWindowOpenAction extends AbstractAction {
    
    /**
     * Creates an instance of this action.
     */
    public ResultWindowOpenAction() {
        String name = NbBundle.getMessage(ResultWindowOpenAction.class,
                                          "TITLE_TEST_RESULTS");        //NOI18N
        putValue(NAME, name);
        putValue("iconBase",                                            //NOI18N
               "org/netbeans/modules/junit/output/res/testResults.png");//NOI18N
    }
    
    /**
     * Opens and activates the JUnit Test Results window.
     *
     * @param  e  event that caused this action to be called
     */
    public void actionPerformed(ActionEvent e) {
        ResultWindow resultWindow = ResultWindow.getInstance();
        resultWindow.open();
        resultWindow.requestActive();
    }
    
}
