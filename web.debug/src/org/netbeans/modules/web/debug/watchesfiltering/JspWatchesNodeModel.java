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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
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

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }
}
