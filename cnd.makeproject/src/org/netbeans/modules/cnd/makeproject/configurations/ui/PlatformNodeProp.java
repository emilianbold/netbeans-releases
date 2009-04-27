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
import org.netbeans.modules.cnd.makeproject.api.configurations.PlatformConfiguration;
import org.openide.nodes.Node;

public class PlatformNodeProp extends Node.Property {
    
    private PlatformConfiguration platformConfiguration;
    private PlatformEditor editor;
    private boolean canWrite;
    private String name;
    private String description;

    @SuppressWarnings("unchecked")
    public PlatformNodeProp(PlatformConfiguration platformConfiguration, boolean canWrite, String name, String description) {
        super(Integer.class);
        this.platformConfiguration = platformConfiguration;
        this.canWrite = canWrite;
        this.name = name;
        this.description = description;
        platformConfiguration.setPlatformNodeProp(this);
        editor = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return description;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (platformConfiguration.getModified()) {
            return platformConfiguration.isConfigured() ? "<b>" + getDisplayName() : getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }
    
    public Object getValue() {
        return Integer.valueOf(platformConfiguration.getValue());
    }
    
    public void setValue(Object v) {
        platformConfiguration.setValue((String)v);
    }
    
    @Override
    public void restoreDefaultValue() {
        platformConfiguration.reset();
    }
    
    @Override
    public boolean supportsDefaultValue() {
        return true;
    }
    
    @Override
    public boolean isDefaultValue() {
        return !platformConfiguration.getModified();
    }

    public boolean canWrite() {
        return canWrite;
    }
    
    public boolean canRead() {
        return true;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (editor == null) {
            editor = new PlatformEditor();
        }
        return editor;
    }
    
    public void repaint() {
        ((PlatformEditor) getPropertyEditor()).repaint();
    }

    private class PlatformEditor extends PropertyEditorSupport {
        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        @Override
        public String getAsText() {
            return platformConfiguration.getName();
        }
        
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(text);
        }
        
        @Override
        public String[] getTags() {
            return platformConfiguration.getNames();
        }
        
        public void repaint() {
            firePropertyChange();
        }
    }
}
