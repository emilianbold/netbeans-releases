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

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;
import org.netbeans.modules.vmd.api.screen.display.injector.ScreenButtonInjectorPresenter;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.openide.util.NbBundle;

import javax.swing.*;

/**
 * @author David Kaspar
 */
public class ImageItemInjectorPresenter extends ScreenButtonInjectorPresenter.Static {

    public ImageItemInjectorPresenter () {
        super (NbBundle.getMessage(ImageItemInjectorPresenter.class, "DISP_AssignNewImage"), null, 10); // NOI18N
    }

    public boolean isEnabled () {
        return getComponent ().readProperty (ImageItemCD.PROP_IMAGE).getKind () == PropertyValue.Kind.NULL;
    }

    protected void actionPerformed () {
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                PropertiesSupport.showCustomPropertyEditor (getComponent (), ImageItemCD.PROP_IMAGE);
            }
        });
    }

}
