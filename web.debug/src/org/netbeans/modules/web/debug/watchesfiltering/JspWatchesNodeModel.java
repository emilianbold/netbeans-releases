/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;

/**
 * Node model for JSP EL watches.
 * 
 * @author Maros Sandor
 */
public class JspWatchesNodeModel implements NodeModel {

    private static final String ICON_BASE ="org/netbeans/modules/debugger/resources/watchesView/Watch";
    
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        JspElWatch watch = (JspElWatch) node;
        return watch.getExpression();
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        return ICON_BASE;
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        JspElWatch watch = (JspElWatch) node;
        
        String t = watch.getType ();
        String e = watch.getExceptionDescription ();
        if (e != null) {
            return watch.getExpression() + " = >" + e + "<";
        }
        if (t == null) {
            return watch.getExpression() + " = " + watch.getValue();
        } else {
            try {
                return watch.getExpression() + " = (" + watch.getType () + ") " + watch.getToStringValue();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
}
