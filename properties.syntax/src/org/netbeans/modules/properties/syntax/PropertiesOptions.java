/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.properties.syntax;


import java.awt.Color;

import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Options for the properties editor kit.
 *
 * @author Libor Kramolis
 */
public class PropertiesOptions extends BaseOptions {

    /** Generated serial version UID. */
    static final long serialVersionUID = 2347735706857337892L;

    /** Name of property. */
    public static final String PROPERTIES = "properties"; // NOI18N
    
    /** Name of property. */
    public static final String SHADOW_TABLE_CELL_PROP = "shadowTableCell"; // NOI18N
    
    /** Array of prperty names. */
    static final String[] PROPERTIES_PROP_NAMES = OptionSupport.mergeStringArrays(
        BaseOptions.BASE_PROP_NAMES,
        new String[] {
            SHADOW_TABLE_CELL_PROP
        }
    );

    
    /** Constructor. */
    public PropertiesOptions() {
        super(PropertiesKit.class, PROPERTIES);
    }

    
    /**
     * Gets localized string. 
     * @return localized string */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(PropertiesOptions.class).getString(s);
        return (res == null) ? super.getString(s) : res;
        } catch (Exception e) {
            return super.getString(s);
        }
    }

    /** Gets <code>SHADOW_TABLE_CELL_PROP</code> property. */
    public Color getShadowTableCell() {
        return (Color) getSettingValue(SHADOW_TABLE_CELL_PROP);
    }

    /** Sets <code>SHADOW_TABLE_CELL_PROP</code> property.*/
    public void setShadowTableCell(Color color) {
        setSettingValue(SHADOW_TABLE_CELL_PROP, color);
    }
    
    /** Gets help context. Overrides superclass method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("editing.editor.properties"); // NOI18N
    }

    protected String getContentType() {
        return PropertiesKit.PROPERTIES_MIME_TYPE;
    }
    
}
