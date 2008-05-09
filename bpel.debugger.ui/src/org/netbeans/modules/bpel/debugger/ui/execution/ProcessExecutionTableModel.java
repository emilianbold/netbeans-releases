/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JToolTip;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Model describing the table structure in the Process Execution View. Since 
 * the view contains only the tree column, this is an empty stub, which only 
 * performs some basic type verification.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessExecutionTableModel implements TableModel {
    private Map<PsmEntity, String> psm2Line = new HashMap<PsmEntity, String>();
    
    /**{@inheritDoc}*/
    public ProcessExecutionTableModel(
            final ContextProvider contextProvider) {
        // Does nothing
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object stuff, 
            final String column) throws UnknownTypeException {
        
        Object object = stuff;
        boolean isTooltip = false;
        
        if (stuff instanceof JToolTip) {
            isTooltip = true;
            object = ((JToolTip) stuff).
                        getClientProperty("getShortDescription");
        }
        
        if (object instanceof ProcessExecutionTreeModel.Dummy) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return "";
            }
            
            if (column.equals(ProcessExecutionColumnModel_Line.COLUMN_ID)) {
                return "";
            }
            
            if (column.equals(ProcessExecutionColumnModel_XPath.COLUMN_ID)) {
                return "";
            }
        }
        
        if (object instanceof PsmEntity) {
            final PsmEntity psmEntity = (PsmEntity) object;
            
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return "";
            }
            
            if (column.equals(ProcessExecutionColumnModel_Line.COLUMN_ID)) {
                if (psm2Line.get(psmEntity) == null) {
                    final String url = ModelUtil.getUrl(
                            psmEntity.getModel().getProcessQName());
                    
                    if (url == null) {
                        return "";
                    }
                    
                    final BpelModel model = EditorUtil.getBpelModel(url);
                    
                    if (model == null) {
                        return "";
                    }
                    
                    final int lineNumber = ModelUtil.getLineNumber(
                                model, psmEntity.getXpath());
                    
                    final String line;
                    final int slashIndex = url.lastIndexOf("/");
                    if (slashIndex == -1) {
                        line = url + ":" + lineNumber;
                    } else {
                        line = url.substring(slashIndex + 1) + ":" + lineNumber;
                    }
                    
                    psm2Line.put(psmEntity, line);
                    
                    return line;
                }
                
                return psm2Line.get(psmEntity);
            }
            
            if (column.equals(ProcessExecutionColumnModel_XPath.COLUMN_ID)) {
                return psmEntity.getXpath();
            }
        }
        
        if (object instanceof PemEntity) {
            final PemEntity pemEntity = (PemEntity) object;
            
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                final String branchId = ((PemEntity) object).getBranchId();
                
                final String tagName = ((PemEntity) object).
                        getPsmEntity().getTag();
                
                if (tagName.equals("onEvent") || 
                        tagName.equals("eventHandlers")) {
                    return "";
                }
                
                return branchId == null ? "" : branchId;
            }
            
            if (column.equals(ProcessExecutionColumnModel_Line.COLUMN_ID)) {
                return getValueAt(pemEntity.getPsmEntity(), column);
            }
            
            if (column.equals(ProcessExecutionColumnModel_XPath.COLUMN_ID)) {
                return getValueAt(pemEntity.getPsmEntity(), column);
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        // Does nothing
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        return true;
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(final ModelListener listener) {
        // Does nothing
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(final ModelListener listener) {
        // Does nothing
    }
}
