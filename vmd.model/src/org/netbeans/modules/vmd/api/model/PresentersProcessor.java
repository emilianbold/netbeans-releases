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
 *
 */

package org.netbeans.modules.vmd.api.model;

import org.openide.util.Lookup;

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public abstract class PresentersProcessor {

    private static final Lookup.Result<PresentersProcessor> result = Lookup.getDefault ().lookupResult (PresentersProcessor.class);

    private String projectType;

    protected PresentersProcessor (String projectType) {
        this.projectType = projectType;
    }

    final String getProjectType () {
        return projectType;
    }

    protected abstract void postProcessPresenters (DesignDocument document, ComponentDescriptor descriptor, ArrayList<Presenter> presenters);

    static void postProcessDescriptor (String projectType, DesignDocument document, ComponentDescriptor descriptor, ArrayList<Presenter> presenters) {
        assert Debug.isFriend (DesignComponent.class, "setComponentDescriptor"); // NOI18N
        for (PresentersProcessor processor : result.allInstances ())
            if (projectType.equals (processor.getProjectType ()))
                processor.postProcessPresenters (document, descriptor, presenters);
    }

}
