/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import java.util.ArrayList;
import org.netbeans.test.editor.app.core.TestGroup;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Vector;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.cookies.LoggingCookie;
import org.netbeans.test.editor.app.core.cookies.PackCookie;
import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TestPackAction;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestStep extends TestGroup {
    
    /** Creates new TestStep */
    public TestStep(int num) {
        this("step"+Integer.toString(num));
    }
    
    public TestStep(String name) {
        super(name);
    }
    
    public TestStep(Element node) {
        super(node);
    }
    
    public Element toXML(Element node) {
        return super.toXML(node);
    }
    
    public void startLogging() {
        System.err.println("Start Logging");
        getLogger().clear();
        getLogger().startLogging();
    }
    
    public void stopLogging() {
        System.err.println("Stop Logging");
        getLogger().stopLogging();
        TestNode[] nodes=getLogger().saveActions(this);
        getLogger().clear();
        firePropertyChange(ADD_CHILDS,null,nodes);
    }
    
    public boolean isLogging() {
        return getLogger().isLogging();
    }
    
    public void perform() {
        isPerforming=true;
        for(int i=0;i < getChildCount();i++) {
            if (!isPerforming) break;
            get(i).perform();
        }
        isPerforming=false;
    }
    
    public void stop() {
        isPerforming=false;
    }
    
    public void delete() {
        TestNode n;
        TestCallAction ca;
        
        for(int i=0;i < owner.getChildCount();i++) {
            n=owner.get(i);
            if (n instanceof TestCallAction) {
                ca=(TestCallAction)n;
                if (name.compareTo(ca.getToCall()) == 0) {
                    ca.setToCall("");
                }
                if (name.compareTo(ca.getToSet()) == 0) {
                    ca.setToSet("");
                }
            }
        }
        owner.remove(this);
    }
    
    public boolean isPacked() {
        TestNode tn;
        for (int i=0;i < getChildCount();i++) {
            tn=(TestNode)(get(i));
            if (tn instanceof TestLogAction && tn.getName().compareTo(TestStringAction.STRINGED_NAME) == 0) {
                return false;
            }
        }
        return true;
    }
    
    public void pack() {
        TestAction[] tas=TestStringAction.generate(getChildNodes());
        removeAll();
        addNodes(tas);
    }
    
    protected void registerNewTypes() {
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestLogAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestStringAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestCompletionAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetKitAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetIEAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetJavaIEAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetCompletionAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestAddAbbreviationAction.class);
    }
    
    protected void registerCookies() {
        getCookieSet().put(PerformCookie.class,new PerformCookie() {
            public void perform() {
                TestStep.this.perform();
            }
            
            public boolean isPerforming() {
                return TestStep.this.isPerforming;
            }
        });
        getCookieSet().put(LoggingCookie.class,new LoggingCookie() {
            public void start() {
                TestStep.this.startLogging();
            }
            public void stop() {
                TestStep.this.stopLogging();
            }
            public boolean isLogging() {
                return TestStep.this.isLogging();
            }
        });
        getCookieSet().put(PackCookie.class,new PackCookie() {
            public void pack() {
                TestStep.this.pack();
            }
            public boolean isPacked() {
                return TestStep.this.isPacked();
            }
        });
    }
}
