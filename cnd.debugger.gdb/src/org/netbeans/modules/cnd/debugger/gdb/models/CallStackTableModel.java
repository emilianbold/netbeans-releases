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

package org.netbeans.modules.cnd.debugger.gdb.models;

import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;

/**
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackTableModel implements TableModel, Constants {
    
    public Object getValueAt(Object row, String columnID) throws UnknownTypeException {
        if (row instanceof GdbCallStackFrame) {
            if (columnID.equals(CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
                GdbCallStackFrame csf = (GdbCallStackFrame) row;
                if (csf.getFullname() != null) {
                    return csf.getFullname() + ":" + csf.getLineNumber(); // NOI18N
                } else if (csf.getFrom() != null) {
                    return csf.getFrom();
                } else {
                    return "";
                }
            }
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly(Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof GdbCallStackFrame) {
            if (columnID.equals(CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
		return true;
	    }
        }
        throw new UnknownTypeException(row);
    }
    
    public void setValueAt(Object row, String columnID, Object value) throws UnknownTypeException {
        throw new UnknownTypeException(row);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
    }
}
