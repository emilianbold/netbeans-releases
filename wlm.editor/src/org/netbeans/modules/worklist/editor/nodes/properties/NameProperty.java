/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMNameable;
import org.netbeans.modules.worklist.editor.nodes.WLMNode;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NameProperty extends Node.Property<String> {

    private WLMNameable wlmNameable;

    public NameProperty(WLMNameable wlmNameable) {
        super(String.class);
        this.wlmNameable = wlmNameable;
        setName(NAME);

        setDisplayName(NbBundle.getMessage(getClass(), 
                "NAME_PROPERTY_DISPLAY_NAME")); // NOI18N
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public String getValue() throws IllegalAccessException,
            InvocationTargetException
    {
        String value = wlmNameable.getName();
        return value;
    }

    @Override
    public boolean canWrite() {
        WLMModel model = wlmNameable.getModel();
        ModelSource modelSource = (model == null) ? null
                : model.getModelSource();
        return (modelSource != null) && modelSource.isEditable();
    }

    @Override
    public void setValue(String value) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException
    {
        if (value == null) {
            value = "";
        } else {
            value = value.trim();
        }
        
        WLMModel model = wlmNameable.getModel();
        model.startTransaction();
        try {
            wlmNameable.setName(value);
        } finally {
            model.endTransaction();
        }
    }

    public static final String NAME = "nameProperty";
}
