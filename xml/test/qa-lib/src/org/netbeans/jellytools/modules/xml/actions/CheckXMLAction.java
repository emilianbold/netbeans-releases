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
package org.netbeans.jellytools.modules.xml.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** CheckXMLAction class
 * @author <a href="mailto:mschovanek@netbeans.org">Martin Schovanek</a> */
public class CheckXMLAction extends ActionNoBlock {

    private static final String popup =
    Bundle.getStringTrimmed("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Check_XML");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F9, KeyEvent.ALT_MASK);

    /** creates new CheckXMLAction instance */    
    public CheckXMLAction() {
        super(null, popup, "org.netbeans.modules.xml.tools.actions.CheckAction", shortcut);
    }
}