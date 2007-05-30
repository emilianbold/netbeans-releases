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

package org.netbeans.modules.xml.schema.ui.basic.navigator;

import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorPanel;

/**
 * An implementation of NavigatorPanel for XML Schema navigator.
 *
 * @author Marek Fukala
 * @author Nathan Fiedler
 * @author Samaresh Panda
 */
public class SchemaNavigatorPanel extends AbstractXMLNavigatorPanel {

    /**
     * Public nullary constructor needed for system to instantiate the provider.
     */
    public SchemaNavigatorPanel() {
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(SchemaNavigatorPanel.class,
                "LBL_SchemaNavigatorPanel_Hint");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SchemaNavigatorPanel.class,
                "LBL_SchemaNavigatorPanel_Name");
    }

    protected  AbstractXMLNavigatorContent getNavigatorUI() {
	if (navigator == null) {
	    navigator = new SchemaNavigatorContent();
	}
	return navigator;
    }

    }
