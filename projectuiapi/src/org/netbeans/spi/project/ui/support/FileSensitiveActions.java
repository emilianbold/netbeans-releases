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

package org.netbeans.spi.project.ui.support;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Factory for creating file-sensitive actions.
 * @author Petr Hrebejk
 */
public class FileSensitiveActions {

    private FileSensitiveActions() {}

    /**
     * Creates an action sensitive to the set of currently selected files.
     * When performed the action will call the given command on the {@link org.netbeans.spi.project.ActionProvider} of
     * the selected project(s) and pass the proper context to it. Enablement of the
     * action depends on the behavior of the project's action provider.<BR>
     * Shortcuts for actions are shared according to command, i.e. actions based on the same command
     * will have the same shortcut.
     * @param command the command which should be invoked when the action is
     *        performed
     * @param namePattern pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link java.text.MessageFormat}: <code>{0}</code> - number of selected projects;
     *        <code>{1}</code> - name of the first project.
     * @param icon icon of the action (or null)
     * @return newly created file-sensitive action
     */    
    public static Action fileCommandAction( String command, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().fileCommandAction( command, namePattern, icon );
    }
    
}
