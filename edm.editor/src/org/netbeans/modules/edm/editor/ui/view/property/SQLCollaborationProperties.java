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
package org.netbeans.modules.edm.editor.ui.view.property;

import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDefinition;
import java.beans.PropertyEditor;
import java.util.Vector;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.property.impl.DefaultPropertyEditor;
import org.openide.nodes.Node;

/**
 * @author Ahimanikya Satapathy
 */
public class SQLCollaborationProperties {

    SQLDefinition sqlDef;

    public SQLCollaborationProperties(SQLDefinition def) {
        this.sqlDef = def;
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("sourceConnections")) {
            Vector str = new Vector();
            for (SQLDBModel dbmodel : sqlDef.getSourceDatabaseModels()) {
                str.add(dbmodel.getModelName());
            }
            return new DefaultPropertyEditor.ListEditor(str);
        }
        return null;
    }
    
    /**
     * Gets display name of this table.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return sqlDef.getDisplayName();
    }

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        this.sqlDef.setDisplayName(newName);
    }
    

    public String getSourceConnections() throws EDMException {
        String conNames = "";
        //Check if the list is zero
        int i = 0;
        for (SQLDBModel dbmodel : sqlDef.getSourceDatabaseModels()) {
            if (i++ > 0) {
                conNames += "," + dbmodel.getModelName();
            } else {
                conNames += dbmodel.getModelName();
            }
        }
        return conNames;
    }

    protected void setDirty(boolean dirty) {
        
    }
    
    public String getResponseType() {
        return this.sqlDef.getResponseType();
    }
    
    public void setResponseType(String type) {
        this.sqlDef.setResponseType(type);
    }
}

