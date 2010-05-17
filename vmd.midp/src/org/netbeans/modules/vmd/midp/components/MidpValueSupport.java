/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public final class MidpValueSupport {

    public static String getHumanReadableString (PropertyValue value) {
        if (value == null)
            return NbBundle.getMessage(MidpValueSupport.class, "LBL_not_set"); // NOI18N
        switch (value.getKind ()) {
            case ENUM:
            case VALUE:
                return value.getPrimitiveValue ().toString ();
            case USERCODE:
                return NbBundle.getMessage(MidpValueSupport.class, "LBL_custom_code"); // NOI18N
            case NULL:
                return NbBundle.getMessage(MidpValueSupport.class, "LBL_null"); // NOI18N
            case ARRAY:
                return NbBundle.getMessage(MidpValueSupport.class, "LBL_array"); // NOI18N
            case REFERENCE:
                return NbBundle.getMessage(MidpValueSupport.class, "LBL_component", value.getComponent ().getComponentID ()); // NOI18N // TODO - use InfoPresenter
            default:
                throw new IllegalStateException ();
        }
    }
    
    public static String getHumanReadableString (DesignComponent component) {
        InfoPresenter infoPresenter = component.getPresenter(InfoPresenter.class);
        return infoPresenter != null ? infoPresenter.getDisplayName(InfoPresenter.NameType.PRIMARY) : null;
    }
    
}
