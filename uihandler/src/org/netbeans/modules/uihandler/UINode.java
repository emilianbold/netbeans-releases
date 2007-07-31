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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import org.netbeans.lib.uihandler.Decorable;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach
 */
final class UINode extends AbstractNode implements VisualData, Decorable {
    private static final SimpleFormatter FORMATTER = new SimpleFormatter();
    private LogRecord log;
    private String htmlKey;

    private UINode(LogRecord r, Children ch) {
        super(ch, Lookups.fixed(r));
        log = r;

        LogRecords.decorate(r, this);
        
        Sheet.Set s = Sheet.createPropertiesSet();
        s.put(createPropertyDate(this));
        s.put(createPropertyLogger(this));
        s.put(createPropertyMessage(this));
        getSheet().put(s);

        if (r.getParameters() != null && r.getParameters().length > 0) {
            Sheet.Set paramSheet = new Sheet.Set();
            paramSheet.setName("parameters"); // NOI18N
            paramSheet.setDisplayName(NbBundle.getMessage(UINode.class, "MSG_DisplayNameParameters"));
            for (int i = 0; i < r.getParameters().length; i++) {
                paramSheet.put(createProperty(i, getParam(r, i, Object.class)));
            }
            getSheet().put(paramSheet);
        }
    }

    public long getMillis() {
        return log.getMillis();
    }
    
    public String getLoggerName() {
        return log.getLoggerName();
    }
    
    public String getMessage() {
        return FORMATTER.format(log);
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (htmlKey == null) {
            return null;
        } else {
            return NbBundle.getMessage(UINode.class, htmlKey, getDisplayName());
        }
    }
    
    static String getParam(LogRecord r, int index) {
        Object[] arr = r.getParameters();
        if (arr == null || arr.length <= index || !(arr[index] instanceof String)) {
            return "";
        }
        return (String)arr[index];
    }
    
    static Node create(LogRecord r) {
        Children ch;
        if (r.getThrown() != null) {
            ch = new StackTraceChildren(r.getThrown());
        } else if ("UI_ENABLED_MODULES".equals(r.getMessage()) || 
            "UI_DISABLED_MODULES".equals(r.getMessage())) {
            ch = new ModulesChildren(r.getParameters());
        } else {
            ch = Children.LEAF;
        }
        
        
        return new UINode(r, ch);
    }
    
    static Node.Property createPropertyDate(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<Date> {
            public NP() {
                super(
                    "date", Date.class, 
                    NbBundle.getMessage(UINode.class, "MSG_DateDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_DateShortDescription")
                );
            }

            public Date getValue() throws IllegalAccessException, InvocationTargetException {
                return source == null ? null : new Date(source.getMillis());
            }
            
            public int hashCode() {
                return getClass().hashCode();
            }
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }

    static Node.Property createPropertyLogger(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "logger", String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_LoggerDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_LoggerShortDescription")
                );
            }

            public String getValue() throws IllegalAccessException, InvocationTargetException {
                if (source == null) {
                    return null;
                }
                String full = source.getLoggerName();
                if (full == null) {
                    return null;
                }
                if (full.startsWith("org.netbeans.ui")) {
                    if (full.equals("org.netbeans.ui")) {
                        return "UI General";
                    }
                    
                    return full.substring("org.netbeans.ui.".length());
                }
                return full;
            }
            
            public int hashCode() {
                return getClass().hashCode();
            }
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }
    static Node.Property createPropertyMessage(final VisualData source) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "message", String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_MessageDisplayName"),
                    NbBundle.getMessage(UINode.class, "MSG_MessageShortDescription")
                );
            }

            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return source == null ? null : source.getMessage();
            }
            
            public int hashCode() {
                return getClass().hashCode();
            }
            public boolean equals(Object o) {
                return o != null && o.getClass().equals(getClass());
            }
        }
        return new NP();
    }
    private Node.Property<?> createProperty(final int index, final Object object) {
        class NP extends PropertySupport.ReadOnly<String> {
            public NP() {
                super(
                    "param #" + index, String.class, 
                    NbBundle.getMessage(UINode.class, "MSG_ParameterDisplayName", index, object),
                    NbBundle.getMessage(UINode.class, "MSG_ParameterShortDescription", index, object)
                );
            }

            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return object == null ? null : object.toString();
            }
            
            private int getIndex() {
                return index;
            }
            
            public int hashCode() {
                return getClass().hashCode();
            }
            public boolean equals(Object o) {
                if (o == null || !o.getClass().equals(getClass())) {
                    return false;
                }
                NP np = (NP)o;
                return getIndex() == np.getIndex();
            }
        }
        return new NP();
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
            an.setIconBaseWithExtension("org/netbeans/modules/uihandler/stackframe.gif"); // NOI18N
            return new Node[] { an };
        }
        
    } // end of StackTraceElement

    
    private static final class ModulesChildren extends Children.Keys<Object> {
        private Object[] modules;
        public ModulesChildren(Object[] m) {
            modules = m;
        }
        
        protected void addNotify() {
            setKeys(modules);
        }
        
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName((String)key);
            an.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif"); // NOI18N
            return new Node[] { an };
        }
        
    } // end of StackTraceElement
    
    private static String afterLastDot(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }

    private static <T> T getParam(LogRecord r, int index, Class<T> type) {
        if (r == null || r.getParameters() == null || r.getParameters().length <= index) {
            return null;
        }
        Object o = r.getParameters()[index];
        return type.isInstance(o) ? type.cast(o) : null;
    }

}
