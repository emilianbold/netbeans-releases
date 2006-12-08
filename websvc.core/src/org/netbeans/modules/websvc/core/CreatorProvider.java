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

package org.netbeans.modules.websvc.core;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class CreatorProvider {

    private static final Lookup.Result<ClientCreatorProvider> clientProviders =
        Lookup.getDefault().lookup(new Lookup.Template<ClientCreatorProvider>(ClientCreatorProvider.class));

    private static final Lookup.Result<ServiceCreatorProvider> serviceProviders =
        Lookup.getDefault().lookup(new Lookup.Template<ServiceCreatorProvider>(ServiceCreatorProvider.class));
     
    private static final Lookup.Result<HandlerCreatorProvider> handlerProviders =
        Lookup.getDefault().lookup(new Lookup.Template<HandlerCreatorProvider>(HandlerCreatorProvider.class));

    public static ClientCreator getClientCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends ClientCreatorProvider> instances = clientProviders.allInstances();
        for (ClientCreatorProvider impl: instances) {
            ClientCreator creator = impl.getClientCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }
    
    public static ServiceCreator getServiceCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends ServiceCreatorProvider> instances = serviceProviders.allInstances();
        for (ServiceCreatorProvider impl: instances) {
            ServiceCreator creator = impl.getServiceCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }
    
    public static HandlerCreator getHandlerCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends HandlerCreatorProvider> instances = handlerProviders.allInstances();
        for (HandlerCreatorProvider impl: instances) {
            HandlerCreator creator = impl.getHandlerCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }   
}
