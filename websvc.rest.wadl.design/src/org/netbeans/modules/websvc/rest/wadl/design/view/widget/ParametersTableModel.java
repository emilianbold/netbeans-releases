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

package org.netbeans.modules.websvc.rest.wadl.design.view.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class ParametersTableModel implements TableModel<Param> {
    
    private transient List<Param> params;
    private WadlModel wadlModel;
    private Object[] paramStyles;
    
    /**
     *
     * @param method
     */
    public ParametersTableModel(Collection<Param> params, Object[] paramStyles, WadlModel wadlModel) {
        this.params = new ArrayList<Param>();
        this.paramStyles = paramStyles;
        this.params.addAll(params);
        this.wadlModel = wadlModel;
    }
    
    public int getRowCount() {
        return params.size();
    }
    
    public int getColumnCount() {
        return 5;
    }
    
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
        case 0:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Name");
        case 1:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Type");
        case 2:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Style");
        case 3:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Fixed");
        case 4:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Default");
        default:
            throw new IllegalArgumentException("");
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return true;
        case 1:
            return true;
        case 2:
            return true;
        case 3:
            return true;
        case 4:
            return true;
        default:
            return false;
        }
    }
    
    public String getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            Param p = getUserObject(rowIndex);
            switch(columnIndex) {
            case 0:
                return p.getName();
            case 1:
                QName type = p.getType();
                String aValue = type.getLocalPart();
                int ndx = aValue.indexOf(":");
                if(aValue != null && ndx != -1) {
                    aValue = aValue.substring(ndx+1);
                }
                return ParamType.fromValue(aValue).value();
            case 2:
                return p.getStyle();
            case 3:
                return p.getFixed()!=null?p.getFixed():null;
            case 4:
                return p.getDefault()!=null?p.getDefault():null;
            default:
                throw new IllegalArgumentException("");
            }
        }
        return null;
    }
    
    public void setValueAt(String aValue, int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            try {
                wadlModel.startTransaction();
                Param p = getUserObject(rowIndex);
                switch (columnIndex) {
                    case 0:
                        p.setName(aValue);
                        break;
                    case 1:
                        QName prevType = p.getType();
                        String prefix = prevType.getPrefix();
                        int ndx = aValue.indexOf(":");
                        if(aValue != null && ndx != -1) {
                            prefix = aValue.substring(0, ndx);
                            aValue = aValue.substring(ndx+1);
                        }
                        if(prefix == null || prefix.equals("")) {
                            ndx = prevType.getLocalPart().indexOf(":");
                            if(ndx != -1)
                                prefix = prevType.getLocalPart().substring(0, ndx);
                        }
                        p.setType(new QName(prevType.getNamespaceURI(), 
                                ParamType.fromValue(aValue).value(), prefix));
                        break;
                    case 2:
                        p.setStyle(aValue);
                        break;
                    case 3:
                        p.setFixed(aValue);
                        break;
                    case 4:
                        p.setDefault(aValue);
                        break;
                    default:
                        throw new IllegalArgumentException("");
                }
            } finally {
                wadlModel.endTransaction();
            }
        }
    }

    public Object[] getTypeAt(int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            Param p = getUserObject(rowIndex);
            switch(columnIndex) {
            case 0:
                return null;
            case 1:
                return ParamType.values(false);
            case 2:
                return paramStyles;
            case 3:
                return null;
            case 4:
                return null;
            default:
                throw new IllegalArgumentException("");
            }
        }
        return null;
    }
    
    public Param getUserObject(int rowIndex) {
        return params.get(rowIndex);
    }

    public void addParameter(Param param) {
        params.add(param);
    }

    void removeParameter(Param param) {
        params.remove(param);
    }
}
