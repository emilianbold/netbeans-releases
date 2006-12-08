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

package org.netbeans.modules.websvc.core.dev.wizard;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.modules.websvc.core.HandlerCreatorProvider;
import org.openide.WizardDescriptor;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxWsHandlerCreatorProvider implements HandlerCreatorProvider {

    public JaxWsHandlerCreatorProvider() {
    }
    
    public HandlerCreator getHandlerCreator(Project project, WizardDescriptor wiz) {
        //String jaxVersion = (String) wiz.getProperty(WizardProperties.JAX_VERSION);
        //if (jaxVersion.equals(WizardProperties.JAX_WS)) {
            return new JaxWsHandlerCreator(project, wiz);
        //}
        //return null;
    }

}
