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
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

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
            return "Name";
        if (o instanceof String) {
            String name = (String) o;
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
                return "Application Class Loader";
            return "Class Loader " + name;
        }
        if (o instanceof Integer) {
            String name = "System Class Loader";
            return name;
        }
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return null;
        if (o instanceof String)
            return "Package " + o;
        if (o instanceof ReferenceType) {
            String name = (o instanceof ClassType) ?
                "Class " + ((ReferenceType) o).name () :
                "Interface " + ((ReferenceType) o).name ();
            ClassLoaderReference clr = ((ReferenceType) o).classLoader ();
            if (clr != null)
                name += " loaded by " + clr.referenceType ().name ();
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
        if (o instanceof String)
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
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
}
