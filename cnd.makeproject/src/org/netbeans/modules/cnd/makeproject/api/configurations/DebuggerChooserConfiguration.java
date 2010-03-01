/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.PrioritizedCustomizerNode;
import org.openide.util.Lookup;

public class DebuggerChooserConfiguration {

    private static List<CustomizerNode>  nodes = null;
    private static String[] names;
    private static int def;

    private int value;
    private boolean modified;
    private boolean dirty = false;

    public DebuggerChooserConfiguration(Lookup lookup) {
        init(lookup);
        reset();
    }

    public DebuggerChooserConfiguration(DebuggerChooserConfiguration conf) {
        value = conf.value;
        setModified(false);
    }

    private static void init(Lookup lookup) {
        if (nodes == null) {
            nodes = CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Debug", lookup); // NOI18N
            String[] defnames = new String[] { "" };

            if (nodes.size() >= 1) {
                int priority = PrioritizedCustomizerNode.DEFAULT_PRIORITY;
                int idx = 0;
                List<String> n = new ArrayList<String>();
                for (CustomizerNode node : nodes) {
                    if (node instanceof PrioritizedCustomizerNode) {
                        if (((PrioritizedCustomizerNode) node).getPriority() > priority) {
                            priority = ((PrioritizedCustomizerNode) node).getPriority();
                            idx = n.size();
                        }
                    } else if (node.getClass().getName().toLowerCase().contains("dbx")) { // NOI18N
                        priority = PrioritizedCustomizerNode.MAX_PRIORITY;
                        idx = n.size();
                    }
                    n.add(node.getDisplayName());
                }
                names = n.toArray(defnames);
                def = idx;
            } else {
                names = defnames;
                def = 0;
            }
        }
    }

    public void setValue(int value) {
        this.value = value;
        setModified(true);
    }

    public void setValue(String s) {
        if (s != null) {
            for (int i = 0; i < names.length; i++) {
                if (s.equals(names[i])) {
                    setValue(i);
                    break;
                }
            }
        }
    }
    
    public int getValue() {
        return value;
    }

    public final void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }
    
    public int getDefault() {
        return def;
    }

    public final void reset() {
        value = getDefault();
        setModified(false);
    }

    public String getName() {
        if (getValue() < names.length) {
            return names[getValue()];
        } else {
            return "???"; // FIXUP // NOI18N
        }
    }

    public CustomizerNode getNode() {
        if (getValue() < nodes.size()) {
            return nodes.get(getValue());
        } else {
            return null;
        }
    }

    public String[] getNames() {
        return names;
    }

    // Clone and Assign
    public void assign(DebuggerChooserConfiguration conf) {
        dirty = getValue() != conf.getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public DebuggerChooserConfiguration clone() {
        return new DebuggerChooserConfiguration(this);
    }
}
