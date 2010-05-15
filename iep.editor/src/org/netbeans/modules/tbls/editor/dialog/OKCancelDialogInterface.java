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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.tbls.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionListener;

/**
 * This interface exists make dialog creation more generic.
 *
 * @author Bing Lu
 */
public interface OKCancelDialogInterface {

    /**
     * Gets the actionListener attribute of the OKCancelDialogInterface object
     *
     * @return The actionListener value
     */
    public ActionListener getActionListener();

    /**
     * Gets the innerPane attribute of the OKCancelDialogInterface object
     *
     * @return The innerPane value
     */
    public Component getInnerPane();

    /**
     * Gets the title attribute of the OKCancelDialogInterface object
     *
     * @return The title value
     */
    public String getTitle();
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
