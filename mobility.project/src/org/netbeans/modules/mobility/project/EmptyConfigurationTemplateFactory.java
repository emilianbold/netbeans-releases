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

package org.netbeans.modules.mobility.project;

import java.util.Map;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class EmptyConfigurationTemplateFactory implements ProjectConfigurationFactory, ProjectConfigurationFactory.ConfigurationTemplateDescriptor{

    public EmptyConfigurationTemplateFactory() {
    }

    public CategoryDescriptor getRootCategory() {
        return null;
    }

    public String getCfgName() {
        return ""; //NOI18N
    }

    public Map<String, String> getProjectConfigurationProperties() {
        return null;
    }

    public Map<String, String> getProjectGlobalProperties() {
        return null;
    }

    public Map<String, String> getPrivateProperties() {
        return null;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EmptyConfigurationTemplateFactory.class, "EmptyConfigurationTemplate"); //NOI18N
    }

}
