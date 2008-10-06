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

package org.netbeans.modules.uml.ui.addins.associateDialog;
import java.util.Hashtable;
import javax.swing.Icon;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.swing.preferencedialog.ISwingPreferenceTableModel;

/**
 * @author sumitabhk
 *
 */
public class AssociateTableModel extends AbstractTableModel implements ISwingPreferenceTableModel
{
    private AssociateDialogUI m_AssociateControl = null;
    private ETList< Object > m_collection = null;
    
    private Hashtable <Integer, String> m_ColNameMap = new Hashtable <Integer, String>();
    
    public AssociateTableModel()
    {
        super();
    }
    
    public AssociateTableModel(AssociateDialogUI control)
    {
        super();
        m_AssociateControl = control;
        buildColumnMap();
    }
    public AssociateTableModel(AssociateDialogUI control, ETList<Object> values)
    {
        super();
        m_AssociateControl = control;
        m_collection = values;
        buildColumnMap();
    }
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
    public int getRowCount()
    {
        int retVal = 0;
        if (m_collection != null)
        {
            retVal = retVal + m_collection.size();
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
    public int getColumnCount()
    {
        int count = 0;
        ETList<String> strs = AssociateUtilities.buildColumns();
        if (strs != null)
        {
            count = strs.size();
        }
        return count;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
    public String getColumnName(int arg0)
    {
        String dispColName = "";
        Integer i = new Integer(arg0);
        String colName = m_ColNameMap.get(i);
        if (colName != null && colName.length() > 1)
        {
            dispColName = AssociateUtilities.translateString(colName);
        }
        return dispColName;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
    public Class getColumnClass(int arg0)
    {
        if ("IDS_ICON".equals(m_ColNameMap.get(arg0)))
            return Icon.class;
        return String.class;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
    public boolean isCellEditable(int arg0, int arg1)
    {
        return false;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
    public Object getValueAt(int row, int col)
    {
        Object retObj = null;
        if (m_collection != null)
        {
            int count = m_collection.size();
            if (row <= count)
            {
                for (int i=0; i<count; i++)
                {
                    Object objVal = m_collection.get(i);
                    IElement pEle = null;
                    INamedElement namedEle = null;
                    IProxyDiagram pDiag = null;
                    if (objVal instanceof IElement)
                    {
                        pEle = (IElement)objVal;
                    }
                    if (objVal instanceof INamedElement)
                    {
                        namedEle = (INamedElement)objVal;
                    }
                    if (objVal instanceof IProxyDiagram)
                    {
                        pDiag = (IProxyDiagram)objVal;
                    }
                    if (i == row)
                    {
                        Integer theInt = new Integer(col);
                        String colName = m_ColNameMap.get(theInt);
                        //if (colName == null || colName.length() == 1)
                        if (colName.equals("IDS_ICON"))
                        {
                            // not displaying the Icon title
                            CommonResourceManager mgr = CommonResourceManager.instance();
                            retObj = mgr.getIconForDisp(objVal);
                        }
                        else if (colName.equals("IDS_NAME"))
                        {
                            if (namedEle != null)
                            {
                                retObj = namedEle.getName();
                            }
                            else if (pDiag != null)
                            {
                                retObj = pDiag.getName();
                            }
                        }
                        else if (colName.equals("IDS_ALIAS"))
                        {
                            if (namedEle != null)
                            {
                                retObj = namedEle.getAlias();
                            }
                            else if (pDiag != null)
                            {
                                retObj = pDiag.getAlias();
                            }
                        }
                        else if (colName.equals("IDS_TYPE"))
                        {
                            if (pEle != null)
                            {
                                retObj = pEle.getExpandedElementType();
                            }
                            else if (pDiag != null)
                            {
                                retObj = pDiag.getDiagramKindName();
                            }
                        }
                        else if (colName.equals("IDS_FULLNAME"))
                        {
                            if (namedEle != null)
                            {
                                retObj = namedEle.getQualifiedName2();
                            }
                            else if (pDiag != null)
                            {
                                retObj = pDiag.getQualifiedName();
                            }
                        }
                        else if (colName.equals("IDS_PROJECT"))
                        {
                            if (pEle != null)
                            {
                                IProject pProj = pEle.getProject();
                                if (pProj != null)
                                {
                                    retObj = pProj.getName();
                                }
                            }
                            else if (pDiag != null)
                            {
                                IProject pProj = pDiag.getProject();
                                if (pProj != null)
                                {
                                    retObj = pProj.getName();
                                }
                            }
                        }
                        else if (colName.equals("IDS_ID"))
                        {
                            if (pEle != null)
                            {
                                retObj = pEle.getXMIID();
                            }
                            else if (pDiag != null)
                            {
                                retObj = pDiag.getXMIID();
                            }
                        }
                    }
                }
            }
        }
        return retObj;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
    public void setValueAt(Object arg0, int arg1, int arg2)
    {
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
         */
    public void addTableModelListener(TableModelListener arg0)
    {
    }
    
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
         */
    public void removeTableModelListener(TableModelListener arg0)
    {
    }
    public IElement getElementAtRow(int row)
    {
        IElement retObj = null;
        if (m_collection != null)
        {
            int count = m_collection.size();
            if (row <= count)
            {
                for (int i=0; i<count; i++)
                {
                    if (i == row)
                    {
                        Object obj = m_collection.get(i);
                        if (obj instanceof IElement)
                        {
                            retObj = (IElement)obj;
                        }
                        break;
                    }
                }
            }
        }
        return retObj;
    }
    public IProxyDiagram getDiagramAtRow(int row)
    {
        IProxyDiagram retObj = null;
        if (m_collection != null)
        {
            int count = m_collection.size();
            if (row <= count)
            {
                for (int i=0; i<count; i++)
                {
                    if (i == row)
                    {
                        Object obj = m_collection.get(i);
                        if (obj instanceof IProxyDiagram)
                        {
                            retObj = (IProxyDiagram)obj;
                        }
                        break;
                    }
                }
            }
        }
        return retObj;
    }
    private void buildColumnMap()
    {
        ETList<String> strs = AssociateUtilities.buildColumns();
        if (strs != null)
        {
            int count = strs.size();
            for (int x = 0; x < count; x++)
            {
                String colName = strs.get(x);
                if (colName != null && colName.length() > 1)
                {
                    Integer i = new Integer(x);
                    m_ColNameMap.put(i, colName);
                }
            }
        }
    }
}
