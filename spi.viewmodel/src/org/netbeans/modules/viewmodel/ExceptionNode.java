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

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.lang.IllegalAccessException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class ExceptionNode extends AbstractNode {

    
    private Exception exception;
    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public ExceptionNode ( 
        Exception exception
    ) {
        super (
            Children.LEAF,
            Lookups.singleton (exception)
        );
        this.exception = exception;
        setIconBase ("org/openide/resources/actions/empty");
    }
    
    public String getName () {
        return exception.getLocalizedMessage ();
    }
    
    public String getDisplayName () {
        return exception.getLocalizedMessage ();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (getClass ());
    }
}

