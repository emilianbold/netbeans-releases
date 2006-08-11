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

package org.netbeans.modules.uihandler;

import java.util.logging.LogRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
final class UINode extends AbstractNode {
    private LogRecord log;

    private UINode(LogRecord r, Children ch) {
        super(ch);
        log = r;
        setName(r.getMessage());
        try {
            Sheet.Set s = new Sheet.Set();
            s.setName(Sheet.PROPERTIES);
            s.put(new PropertySupport.Reflection<Long>(log, long.class, "millis")); // NOI18N
            
            getSheet().put(s);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    static Node create(LogRecord r) {
        Children ch;
        if (r.getThrown() != null) {
            ch = new StackTraceChildren(r.getThrown());
        } else {
            ch = Children.LEAF;
        }
        
        
        return new UINode(r, ch);
    }
    
    private static final class StackTraceChildren extends Children.Keys<StackTraceElement> {
        private Throwable throwable;
        public StackTraceChildren(Throwable t) {
            throwable = t;
        }
        
        protected void addNotify() {
            setKeys(throwable.getStackTrace());
        }
        
        protected Node[] createNodes(StackTraceElement key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.getClassName() + "." + key.getMethodName());
            an.setDisplayName(NbBundle.getMessage(UINode.class, "MSG_StackTraceElement", 
                new Object[] { 
                    key.getFileName(),
                    key.getClassName(),
                    key.getMethodName(),
                    key.getLineNumber(),
                    afterLastDot(key.getClassName()),
                }
            ));
            return new Node[] { an };
        }
        
        private static String afterLastDot(String s) {
            int index = s.lastIndexOf('.');
            if (index == -1) {
                return s;
            }
            return s.substring(index + 1);
        }
    
    } // end of StackTraceElement
    
}
