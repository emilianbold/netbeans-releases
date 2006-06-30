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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
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

