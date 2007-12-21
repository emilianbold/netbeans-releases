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

package org.netbeans.modules.bpel.debugger.ui.callstack;

import java.util.Vector;
import javax.swing.JToolTip;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Table model supporting the BPEL Partner Links view.
 * 
 * @author Kirill Sorokin
 */
public class CallStackTableModel implements TableModel, Constants {
    
    private BpelDebugger myDebugger;
    
    private Vector<ModelListener> myListeners = new Vector<ModelListener>();
    
    /**{@inheritDoc}*/
    public CallStackTableModel(
            final ContextProvider contextProvider) {
        
        myDebugger = (BpelDebugger) contextProvider.lookupFirst(
                null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return "";
        }
        
        if (object instanceof JToolTip) {
            final Object realObject = ((JToolTip) object).
                    getClientProperty("getShortDescription"); // NOI18N
                    
            return getValueAt(realObject, column);
        }
        
        if (object instanceof PemEntity) {
            final PemEntity pemEntity = (PemEntity) object;
            final PsmEntity psmEntity = pemEntity.getPsmEntity();
            
            if (column.equals(CallStackColumnModel_Line.COLUMN_ID)) {
                final String url = ModelUtil.getUrl(
                        psmEntity.getModel().getProcessQName());
                
                if (url == null) {
                    return "";
                }
                
                final BpelModel model = EditorUtil.getBpelModel(url);
                
                if (model == null) {
                    return null;
                }
                
                final int lineNumber = ModelUtil.getLineNumber(
                        model, psmEntity.getXpath());
                
                final int slashIndex = url.lastIndexOf("/");
                if (slashIndex == -1) {
                    return url + ":" + lineNumber;
                } else {
                    return url.substring(slashIndex + 1) + ":" + lineNumber;
                }
            }
            
            if (column.equals(CallStackColumnModel_XPath.COLUMN_ID)) {
                return psmEntity.getXpath();
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        if (object instanceof PemEntity) {
            if (column.equals(CallStackColumnModel_Line.COLUMN_ID)) {
                return;
            }
            
            if (column.equals(CallStackColumnModel_XPath.COLUMN_ID)) {
                return;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object instanceof PemEntity) {
            if (column.equals(CallStackColumnModel_Line.COLUMN_ID)) {
                return true;
            }
            
            if (column.equals(CallStackColumnModel_XPath.COLUMN_ID)) {
                return true;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
    }
}
