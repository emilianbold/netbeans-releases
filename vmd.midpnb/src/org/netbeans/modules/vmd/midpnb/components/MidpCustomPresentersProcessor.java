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

package org.netbeans.modules.vmd.midpnb.components;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class MidpCustomPresentersProcessor extends PresentersProcessor {

    public MidpCustomPresentersProcessor () {
        super (MidpDocumentSupport.PROJECT_TYPE_MIDP);
    }

    protected void postProcessPresenters (DesignDocument document, ComponentDescriptor descriptor, ArrayList<Presenter> presenters) {
        if (document.getDescriptorRegistry ().isInHierarchy (ResourcesCategoryCD.TYPEID, descriptor.getTypeDescriptor ().getThisType ())) {
            presenters.add (new AcceptTypePresenter (SimpleCancellableTaskCD.TYPEID));
            presenters.add (new AcceptTypePresenter (SimpleTableModelCD.TYPEID));
        }
    }

}
