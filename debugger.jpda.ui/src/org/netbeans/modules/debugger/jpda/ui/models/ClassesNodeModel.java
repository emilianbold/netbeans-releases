/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
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
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (ClassesNodeModel.class).getString
                ("CTL_ClassesModel_Column_Name_Name");
        if (o instanceof Object[]) {
            String name = (String) ((Object[]) o) [0];
            int i = name.lastIndexOf ('.');
            if (i >= 0)
                name = name.substring (i + 1);
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
