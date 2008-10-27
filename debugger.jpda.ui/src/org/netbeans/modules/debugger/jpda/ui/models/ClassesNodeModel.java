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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author   Jan Jancura
 */
public class ClassesNodeModel implements NodeModel {

    private static final String CLASS =
        "org/netbeans/modules/debugger/jpda/resources/class";
    private static final String INTERFACE =
        "org/netbeans/modules/debugger/jpda/resources/interface";
    private static final String PACKAGE =
        "org/netbeans/modules/debugger/jpda/resources/package";
    private static final String FIELD =
        "org/netbeans/modules/debugger/jpda/resources/field";
    private static final String CLASS_LOADER =
        "org/netbeans/modules/debugger/jpda/resources/classLoader";
    
    private Properties classesProperties = Properties.getDefault().
            getProperties("debugger").getProperties("classesView"); // NOI18N
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (ClassesNodeModel.class).getString
                ("CTL_ClassesModel_Column_Name_Name");
        if (o instanceof Object[]) {
            String name = (String) ((Object[]) o) [0];
            boolean flat = classesProperties.getBoolean("flat", true);
            if (!flat) {
                int i = name.lastIndexOf ('.');
                if (i >= 0)
                    name = name.substring (i + 1);
            }
            return name;
        }
        if (o instanceof ReferenceType) {
            String name = ((ReferenceType) o).name ();
            int i = name.lastIndexOf ('.');
            if (i >= 0)
                name = name.substring (i + 1);
            i = name.lastIndexOf ('$');
            if (i >= 0)
                name = name.substring (i + 1);
            return name;
        }
        if (o instanceof ClassLoaderReference) {
            String name = ((ClassLoaderReference) o).referenceType ().name ();
            if (name.endsWith ("AppClassLoader"))
                return NbBundle.getBundle (ClassesNodeModel.class).getString
                    ("CTL_ClassesModel_Column_Name_AppClassLoader");
            return java.text.MessageFormat.format (NbBundle.getBundle
                (ClassesNodeModel.class).getString (
                    "CTL_ClassesModel_Column_Name_ClassLoader"), 
                    new Object [] {name}
                );
        }
        if (o instanceof Integer) {
            return NbBundle.getBundle (ClassesNodeModel.class).getString 
                ("CTL_ClassesModel_Column_Name_SystemClassLoader");
        }
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (ClassesNodeModel.class).getString
                ("CTL_ClassesModel_Column_Name_Desc");
        if (o instanceof Object[])
            return java.text.MessageFormat.format (NbBundle.getBundle
                (ClassesNodeModel.class).getString (
                    "CTL_ClassesModel_Column_Name_Package"), 
                (Object []) o
            );
        if (o instanceof ReferenceType) {
            String format = (o instanceof ClassType) ?
                    NbBundle.getBundle (ClassesNodeModel.class).getString
                        ("CTL_ClassesModel_Column_Name_Class") :
                    NbBundle.getBundle (ClassesNodeModel.class).getString
                        ("CTL_ClassesModel_Column_Name_Interface");
            String name = java.text.MessageFormat.format (
                format, 
                new Object [] {((ReferenceType) o).name ()}
            );
            ClassLoaderReference cl = ((ReferenceType) o).classLoader ();
            if (cl != null) {
                name += " " + java.text.MessageFormat.format (
                    NbBundle.getBundle (ClassesNodeModel.class).getString (
                    "CTL_ClassesModel_Column_Name_LoadedBy"), 
                    new Object [] {cl.referenceType ().name ()}
                );
            }
            return name;
        }
        if (o instanceof ClassLoaderReference)
            return null;
        if (o instanceof Integer)
            return null;
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return CLASS;
        if (o instanceof Object[])
            return PACKAGE;
        if (o instanceof ClassType)
            return CLASS;
        if (o instanceof InterfaceType)
            return INTERFACE;
        if (o instanceof ClassLoaderReference)
            return CLASS_LOADER;
        if (o instanceof Integer)
            return CLASS_LOADER;
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
    
    
    // ColumnModels ............................................................
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static class DefaultClassesColumn extends 
    SourcesModel.AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultClassesColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultClassesColumn.class).
                getString ("CTL_ClassesModel_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        @Override
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultClassesColumn.class).getString
                ("CTL_ClassesModel_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
}
