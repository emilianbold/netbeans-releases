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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.DirectoryChooserPanel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;

public class VectorNodeProp extends PropertySupport {

    private VectorConfiguration vectorConfiguration;
    private BooleanConfiguration inheritValues;
    private String baseDir;
    private String[] texts;
    boolean addPathPanel;
    private HelpCtx helpCtx;

    public VectorNodeProp(VectorConfiguration vectorConfiguration, BooleanConfiguration inheritValues, String baseDir, String[] texts, boolean addPathPanel, HelpCtx helpCtx) {
        super(texts[0], List.class, texts[1], texts[2], true, true);
        this.vectorConfiguration = vectorConfiguration;
        this.inheritValues = inheritValues;
        this.baseDir = baseDir;
        this.texts = texts;
        this.addPathPanel = addPathPanel;
        this.helpCtx = helpCtx;
    }

    @Override
    public String getHtmlDisplayName() {
        if (vectorConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    public Object getValue() {
        return vectorConfiguration.getValue();
    }

    public void setValue(Object v) {
        vectorConfiguration.setValue((List) v);
    }

    @Override
    public void restoreDefaultValue() {
        vectorConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return vectorConfiguration.getValue().size() == 0;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new DirectoriesEditor((List) ((ArrayList) vectorConfiguration.getValue()).clone());
    }

    /*
    public Object getValue(String attributeName) {
    if (attributeName.equals("canEditAsText")) // NOI18N
    return Boolean.FALSE;
    return super.getValue(attributeName);
    }
     */
    private class DirectoriesEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private List value;
        private PropertyEnv env;

        public DirectoriesEditor(List value) {
            this.value = value;
        }

        @Override
        public void setAsText(String text) {
            List newList = new ArrayList();
            StringTokenizer st = new StringTokenizer(text, File.pathSeparator); // NOI18N
            while (st.hasMoreTokens()) {
                newList.add(st.nextToken());
            }
            super.setValue(newList);
        }

        @Override
        public String getAsText() {
            boolean addSep = false;
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                if (addSep) {
                    ret.append(File.pathSeparator);
                }
                ret.append((String) value.get(i));
                addSep = true;
            }
            return ret.toString();
        }

        @Override
        public java.awt.Component getCustomEditor() {
            String text = null;
            if (inheritValues != null) {
                text = texts[3];
            }
            return new DirectoryChooserPanel(baseDir, (String[]) value.toArray(new String[value.size()]), addPathPanel, inheritValues, text, this, env, helpCtx);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
