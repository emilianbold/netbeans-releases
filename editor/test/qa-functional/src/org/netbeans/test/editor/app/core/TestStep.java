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

import org.netbeans.test.editor.app.gui.Main;
import org.netbeans.test.editor.app.core.TestGroup;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Vector;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

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
        initialize();
    }
    
    public TestStep(Element node) {
        super(node);
        initialize();
    }
    
    public Element toXML(Element node) {
        return super.toXML(node);
    }
    
    private void initialize() {
        getCookieSet().add(new LoggingCookie() {
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
    }
    
    public void startLogging() {
//        Main.editor.lock(getLogger());
        getLogger().clear();
        getLogger().startLogging();
    }
    
    public void stopLogging() {
        getLogger().stopLogging();
        getLogger().saveActions(this);
        getLogger().clear();
//        Main.editor.unlock(getLogger());
        firePropertyChange (CHANGE_CHILD,null,null);
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
    
    public boolean isToCall() {
        if (getChildCount() == 0)
            return true;
        if (get(0) instanceof TestSetAction)
            return false;
        return true;
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

    protected Collection getNewTypes() {
        return generateSetNewTypes();
    }

}
