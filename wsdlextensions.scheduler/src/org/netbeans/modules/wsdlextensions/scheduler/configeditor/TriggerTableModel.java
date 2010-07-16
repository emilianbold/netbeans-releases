/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants.TriggerType;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 * Implements the Scheduler Trigger Swing table model.
 * 
 * @author sunsoabi_edwong
 */
public class TriggerTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -236884653410013559L;
    
    private SchedulerModel schedulerModel;
    
    private static final String[] COLUMN_NAMES = {
        NbBundle.getMessage(TriggerTableModel.class, "HDR_NAME"),       //NOI18N
        NbBundle.getMessage(TriggerTableModel.class, "HDR_TYPE"),       //NOI18N
        NbBundle.getMessage(TriggerTableModel.class, "HDR_ENABLED"),    //NOI18N
        NbBundle.getMessage(TriggerTableModel.class, "HDR_DESCRIPTION"),//NOI18N
    };
    private static final int NAME_COL = 0;
    private static final int TYPE_COL = NAME_COL + 1;
    private static final int ENABLED_COL = TYPE_COL + 1;
    private static final int DESCRIPTION_COL = ENABLED_COL + 1;
    
    public TriggerTableModel() {
        super();
    }
    
    public TriggerTableModel(SchedulerModel schedulerModel) {
        this();
        this.schedulerModel = schedulerModel;
    }
    
    public void setSchedulerModel(SchedulerModel schedulerModel) {
        this.schedulerModel = schedulerModel;
    }

    public int getRowCount() {
        if (schedulerModel != null) {
            return schedulerModel.getTriggers().size();
        }
        return 0;
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (ENABLED_COL == columnIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if (schedulerModel != null) {
            SchedulerModel.TriggerDetail td =
                    schedulerModel.getTriggers().get(rowIndex);
            switch (columnIndex) {
            case NAME_COL:
                result = td.getName();
                break;
            case TYPE_COL:
                if (td.getType() != null) {
                    TriggerType type = TriggerType.toEnum(td.getType());
                    result = (type != null) ? type.getI18nName() : "?"; //NOI18N
                }
                break;
            case ENABLED_COL:
                result = Boolean.valueOf(td.isEnabled());
                break;
            case DESCRIPTION_COL:
                if (td.getDescription() != null) {
                    result = td.getDescription();
                } else {
                    result = "";                                        //NOI18N
                }
                break;
            default:
                break;
            }
        }
        return result;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (null == schedulerModel) {
            return;
        }
        SchedulerModel.TriggerDetail trigger =
                schedulerModel.getTriggers().get(rowIndex);
        
        switch (columnIndex) {
        case NAME_COL:
            if (aValue instanceof String) {
                String nuName = Utils.trim((String) aValue);
                if (!Utils.equals(nuName, Utils.trim(trigger.getName()))) {
                    trigger.setName(nuName);
                }
            }
            break;
        case ENABLED_COL:
            if (aValue instanceof Boolean) {
                boolean nuEnabled = ((Boolean) aValue).booleanValue();
                if (nuEnabled != trigger.isEnabled()) {
                    trigger.setEnabled(nuEnabled);
                }
            }
            break;
        case DESCRIPTION_COL:
            if (aValue instanceof String) {
                String nuDescription = Utils.trim((String) aValue);
                if (!Utils.equals(nuDescription,
                        Utils.trim(trigger.getDescription()))) {
                    trigger.setDescription(nuDescription);
                }
            }
            break;
        default:
            super.setValueAt(aValue, rowIndex, columnIndex);
            break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class result = super.getColumnClass(columnIndex);
        if (getRowCount() > 0) {
            Object value = getValueAt(0, columnIndex);
            if (value != null) {
                result = value.getClass();
            }
        }
        return result;
    }
}
