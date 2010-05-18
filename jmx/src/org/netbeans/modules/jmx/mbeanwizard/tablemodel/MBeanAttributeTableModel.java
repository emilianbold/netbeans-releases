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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.tablemodel;
import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.MBeanAttribute;


/**
 * Class implementing the table model for the mbean attribute table
 *
 */
public class MBeanAttributeTableModel extends AbstractJMXTableModel {

    public static final int IDX_ATTR_NAME        = 0;
    public static final int IDX_ATTR_TYPE        = 1;
    public static final int IDX_ATTR_ACCESS      = 2;
    public static final int IDX_ATTR_DESCRIPTION = 3;
    
    protected int lastIndex;
    
    /**
     * Constructor
     */
    public MBeanAttributeTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanAttributeTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[4];
        
        String sn = bundle.getString("LBL_AttrName");// NOI18N
        
        String st = bundle.getString("LBL_AttrType");// NOI18N
        String sa = bundle.getString("LBL_AttrAccess");// NOI18N
        String sd = bundle.getString("LBL_AttrDescription");// NOI18N
        
        columnNames[IDX_ATTR_NAME]          = sn;
        columnNames[IDX_ATTR_TYPE]          = st;
        columnNames[IDX_ATTR_ACCESS]        = sa;
        columnNames[IDX_ATTR_DESCRIPTION]   = sd;
        
        lastIndex = 0;
    }
    
    /**
     * Creates a new MBean Attribute object
     * @return MBeanAttribute the created Attribute with default values
     */
    public MBeanAttribute createNewAttribute() {
        
        MBeanAttribute attribute = new MBeanAttribute(
                WizardConstants.ATTR_NAME_DEFVALUE + lastIndex,
                WizardConstants.STRING_OBJ_FULLNAME,
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                WizardConstants.ATTR_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.ATTR_DESCR_DEFVALUE_SUFFIX, null);
        lastIndex++;
        return attribute;
    }
    
    public void clear() {
        data.clear();
    }
    
    /**
     * Returns an attribute accordind to his index in the attribute list
     * @param index the index of the attribute
     * @return MBeanAttribute the attribute
     */
    public MBeanAttribute getAttribute(int index) {
        return (MBeanAttribute)data.get(index);
    }
    
    /**
     * Overridden method from superclass 
     */
    public Object getValueAt(int row, int col) {
        MBeanAttribute attr = (MBeanAttribute)data.get(row);
        switch(col) {
            case 0: return attr.getName();
            case 1: return attr.getTypeName();
            case 2: return attr.getAccess();
            case 3: return attr.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanAttributeTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanAttribute attr = (MBeanAttribute)data.get(rowIndex);
            switch(columnIndex) {
                case 0: attr.setName((String)aValue);
                break;
                case 1: attr.setTypeName((String)aValue);
                break;
                case 2: attr.setAccess((String)aValue);
                break;
                case 3: attr.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanAttributeTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    public void addRow(MBeanAttribute mba) {
       MBeanAttribute newAttr = new MBeanAttribute(
               mba.getName(),
               mba.getTypeName(),
               mba.getAccess(),
               mba.getDescription(), mba.getTypeMirror());
       data.add(mba); 
       //table is informed about the change to update the view
       this.fireTableDataChanged();
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanAttribute mba = createNewAttribute();
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
