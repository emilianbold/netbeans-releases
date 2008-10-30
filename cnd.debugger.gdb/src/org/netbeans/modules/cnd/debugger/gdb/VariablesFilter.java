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

package org.netbeans.modules.cnd.debugger.gdb;

import javax.swing.Action;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * VariablesFilter.java
 *
 * This filter allows to change nodes in Locals View and Watches View for
 * some concrete variable types. For example it allows to define "logical" view
 * for java.util.Hashtable.
 *
 * Instances of this filter should be registerred in: "
 * "META-INF\debugger\netbeans-JPDADebuggerEngine\org.netbeans.spi.debugger.jpda.VariablesFilter"
 * file.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public abstract class VariablesFilter {
    

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedTypes ();

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedAncestors ();
    
    /** 
     * Returns filtered children for given variable on given indexes.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public abstract Object[] getChildren (
        TreeModel      original,
        Variable       variable, 
        int            from, 
        int            to
    ) throws UnknownTypeException;
    
    /** 
     * Returns number of filtered children for given variable.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  number of filtered children for given variable
     */
    public abstract int getChildrenCount (
        TreeModel      original,
        Variable       variable
    ) throws UnknownTypeException;
    
    /**
     * Returns true if variable is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (
        TreeModel      original,
        Variable       variable
    ) throws UnknownTypeException;
    
    
    // NodeModelFilter
    
    /**
     * Returns filterred display name for given variable.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred icon for given variable.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given variable.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    
    // NodeActionsProviderFilter
    
    /**
     * Returns set of actions for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  set of actions for given variable
     */
    public abstract Action[] getActions (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    /**
     * Performs default action for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    
    // TableModelFilter
    
    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and for variable <code>variable</code>. Column ID is defined in by 
     * {@link org.netbeans.spi.viewmodel.ColumnModel#getID}, and variables are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param   original the original table model
     * @param   variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException if there is no TableModel defined for given
     *          parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    public abstract Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Filters original isReadOnly value from given table model.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public abstract boolean isReadOnly (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * for variable <code>variable</code>. Column ID is defined in by 
     * {@link org.netbeans.spi.viewmodel.ColumnModel#getID}, and variable are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public abstract void setValueAt (
        TableModel original, 
        Variable variable, 
        String columnID, 
        Object value
    ) throws UnknownTypeException;
}
