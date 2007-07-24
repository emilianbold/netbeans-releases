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

package org.netbeans.modules.vmd.midp.screen.display.injector;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.injector.ScreenButtonInjectorPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class TickerInjectorPresenter extends ScreenButtonInjectorPresenter.Static {

    public TickerInjectorPresenter () {
        super (NbBundle.getMessage(TickerInjectorPresenter.class, "DISP_AssignNewTicker"), null, 10); // NOI18N
    }

    public boolean isEnabled () {
        return getComponent ().readProperty (DisplayableCD.PROP_TICKER).getKind() == PropertyValue.Kind.NULL;
    }

    protected void actionPerformed () {
        DesignDocument document = getComponent ().getDocument ();
        DesignComponent resources = MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID);
        DesignComponent ticker = document.createComponent (TickerCD.TYPEID);
        resources.addComponent (ticker);
        getComponent ().writeProperty (DisplayableCD.PROP_TICKER, PropertyValue.createComponentReference (ticker));
    }

}
