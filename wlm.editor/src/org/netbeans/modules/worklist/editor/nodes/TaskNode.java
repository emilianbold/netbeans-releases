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

package org.netbeans.modules.worklist.editor.nodes;

import org.netbeans.modules.worklist.editor.nodes.children.TaskChildren;
import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import org.netbeans.modules.worklist.editor.utils.StringUtils;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.worklist.editor.nodes.properties.NameProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class TaskNode extends WLMNode<TTask> {

    private String cachedName = null;

    public TaskNode(TTask task, Lookup lookup) {
        this(task, new TaskChildren(task, lookup), lookup);
    }

    public TaskNode(TTask task, Children children, Lookup lookup) {
        super(task, children, lookup);
        updateDisplayName();
    }

    @Override
    public void updateDisplayName() {
        TTask task = getWLMComponent();

        String name = task.getName();
        if (name == null) {
            name = "";
        } else {
            name = name.trim();
        }

        if (!StringUtils.equals(name, cachedName)) {
            cachedName = name;
            
            DisplayNameBuilder builder = new DisplayNameBuilder();

            if (name.length() > 0) {
                builder.append(name);
            } else {
                builder.startColor("#888888");
                builder.append("Unnamed task");
                builder.endColor();
            }

            setDisplayName(builder);
        }
    }

    @Override
    public WLMNodeType getType() {
        return WLMNodeType.TASK;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property nameProperty = new NameProperty(getWLMComponent());
        set.put(nameProperty);

        sheet.put(set);

        return sheet;
    }


    @Override
    protected void fireWLMPropertiesChanged() {
        String newName = getWLMComponent().getName();
        firePropertyChange(NameProperty.NAME, "", newName); // NOI18N
    }
}
