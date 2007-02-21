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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connection;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connections;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ConnectionsImpl extends JBIComponentImpl implements Connections {
    
    /** Creates a new instance of ConnectionsImpl */
    public ConnectionsImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public ConnectionsImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.CONNECTIONS));
    }

    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }

    public List<Connection> getConnections() {
        return getChildren(Connection.class);        
    }

    public void removeConnection(Connection connection) {
        removeChild(CONNECTION_PROPERTY, connection);
    }

    public void addConnection(int index, Connection connection) {
        insertAtIndex(CONNECTION_PROPERTY, connection, index, Connection.class);
    }
}
