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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.event;

import java.util.EventObject;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLObject;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLDataEvent extends EventObject {

    private SQLCanvasObject canvasObj;
    private SQLObject chldObj;

    public SQLDataEvent(Object source, SQLCanvasObject obj) {
        super(source);
        this.canvasObj = obj;
    }

    public SQLDataEvent(Object source, SQLCanvasObject obj, SQLObject chldObj) {
        super(source);
        this.canvasObj = obj;
        this.chldObj = chldObj;
    }

    public SQLCanvasObject getCanvasObject() {
        return canvasObj;
    }

    public SQLObject getChildObject() {
        return chldObj;
    }

}

