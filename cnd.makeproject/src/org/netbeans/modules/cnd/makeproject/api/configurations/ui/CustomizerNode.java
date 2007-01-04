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

package org.netbeans.modules.cnd.makeproject.api.configurations.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.openide.nodes.Sheet;

public class CustomizerNode {
    public static String iconbase = "org/netbeans/modules/cnd/makeproject/ui/resources/general"; // NOI18N
    public static String icon = "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif"; // NOI18N

    public String name;
    public String displayName;
    public boolean advanced;
    public CustomizerNode[] children;
        
    public CustomizerNode(String name, String displayName, boolean advanced, CustomizerNode[] children) {
        this.name = name;
        this.displayName = displayName;
        this.advanced = advanced;
        this.children = children;
    }
    
    public CustomizerNode(String name, String displayName, CustomizerNode[] children) {
        this(name, displayName, false, children);
    }

    public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
	return null;
    }
}
