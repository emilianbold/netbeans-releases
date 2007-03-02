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
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import javax.swing.JPanel;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetCallback;

public abstract class DataBindingPanel extends JPanel {

    protected BindingTargetCallback bindingCallback;
    protected DesignProperty designProperty;
    protected FacesDesignProperty facesDesignProperty;
    public DataBindingPanel(BindingTargetCallback bindingCallback, DesignProperty designProperty) {
        this.bindingCallback = bindingCallback;
        this.designProperty = designProperty;
        if (designProperty instanceof FacesDesignProperty) {
            facesDesignProperty = (FacesDesignProperty)designProperty;
        }
    }
    public abstract String getDataBindingTitle();
}
