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

package org.netbeans.modules.editor.options;

import java.beans.*;

/** BeanInfo for common options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AllOptionsBeanInfo extends BaseOptionsBeanInfo {

    public static final String EDITOR_STATE_PROP = "editorState"; // NOI18N

    public static final String[] PROP_NAMES = new String[] {
                BaseOptions.KEY_BINDING_LIST_PROP,
                EDITOR_STATE_PROP,
                BaseOptions.OPTIONS_VERSION_PROP
    };

    public AllOptionsBeanInfo() {
        super("/org/netbeans/modules/editor/resources/allOptions", "base_"); // NOI18N
    }
    
    protected Class getBeanClass() {
        return AllOptions.class;
    }

    protected String[] getPropNames() {
        return PROP_NAMES;
    }

    protected void updatePropertyDescriptors() {
        super.updatePropertyDescriptors();

        setHidden(new String[] {
            EDITOR_STATE_PROP,
            BaseOptions.OPTIONS_VERSION_PROP
        });
    }

}
