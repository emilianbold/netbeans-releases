/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.test.dataprovider.cachedrowsetdataprovider;

import java.awt.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.test.dataprovider.common.*;
        
public class TextFieldPageBeanDataProvider extends JSFComponent {
    public TextFieldPageBeanDataProvider() {
        this("textField1"); // default Text Field id
    }
    
    public TextFieldPageBeanDataProvider(String componentID) {
        componentName = COMPONENT_TEXT_FIELD_NAME;
        this.componentID = componentID;
        dbTableName = DB_TABLE_PERSON;    
    }
    
    public String makeTextField() {
        String errMsg = null;
        try {
            putVisualComponentOnDesigner(250, 25); 
            // now jSF component is selected in Navigator
            String newID = TestPropertiesHandler.getTestProperty(
                "ID_TextField_DataBinding");
            changeComponentID(newID); // component should be selected
            componentPoint = getComponentPoint(); // component should be selected
            Utils.doSaveAll();
            putDBTableOnComponent();
            Utils.doSaveAll();
        } catch (Exception e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    @Override
    protected void putDBTableOnComponent() { 
        String 
            dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL"),
            rowSetName = TestPropertiesHandler.getTestProperty("ID_RowSet_TextField_DataBinding"),
            dataProviderName = TestPropertiesHandler.getTestProperty("ID_DataProvider_TextField_DataBinding");

        Utils.putDBTableOnComponent(dbURL, dbTableName, componentPoint, rowSetName, 3, 2);
        checkRowSetAppearance(NAVIGATOR_TREE_NODE_SESSION_PREFIX + rowSetName);
        checkDataProviderAppearance(NAVIGATOR_TREE_NODE_PAGE_PREFIX + dataProviderName);
    }
}
