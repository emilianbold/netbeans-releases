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
public class TestSubTest extends Test {
    
    public static final String OWNLOGGER="Own_logger";
    
    /** Creates new TestSubTest */
    public TestSubTest(int num,Logger logr) {
        this("subTest"+Integer.toString(num));
        logger=logr;
    }
    
    public TestSubTest (String name) {
        super(name);
    }
    
    public TestSubTest(Element node) {
        super(node);
        if (node.getAttribute(OWNLOGGER).equals("true")) {        
            logger = new Logger(Main.editor);
        } else {
            logger=null;
        }
    }
    
    public Element toXML(Element node) {
        super.toXML(node);
        node.setAttribute(OWNLOGGER, (logger != owner.getLogger()) ? "true" : "false");        
        return node;
    }
    
    public String getAuthor() {
        return ((Test)owner).getAuthor();
    }

    public String getVersion() {
        return ((Test)owner).getVersion();
    }
    
    public void setOwnLogger(boolean b) {
        if (b && logger == owner.getLogger()) {
            logger=new Logger(Main.editor);
            firePropertyChange (OWNLOGGER,null,new Boolean(b));
        } else {
            if (!b && logger != owner.getLogger()) {
                logger=owner.getLogger();
                firePropertyChange (OWNLOGGER,null,new Boolean(b));
            }
        }
    }
    
    public boolean getOwnLogger() {
        return (logger != owner.getLogger());
    }
    
    public void perform() {
        Main.log("\nSub Test: "+getName()+" starts execution.");
        isPerforming=true;        
        for(int i=0;i < getChildCount();i++) {
            if (!isPerforming) break;
            if (get(i) instanceof TestCallAction) {
                ((TestCallAction)get(i)).performAndWait();
            }
        }
        isPerforming=false;
    }
    
    public boolean isPerfoming() {
        return isPerforming;
    }
    
    public void stop() {
        if (getLogger().isPerforming())
            getLogger().stopPerforming();
        isPerforming=false;
    }    
    
    protected Collection getNewTypes() {
        Vector newTypes = new Vector();
        newTypes.add(new NewType () {
            public void create () {
                addNode(
                new TestStep(getNameCounter()));
            }
            
            public String getName() {
                return "Step";
            }
            
            public org.openide.util.HelpCtx getHelpCtx() {
                return null;
            }
        });
        
        newTypes.add(new NewType () {
            public void create () {
                addNode(
                new TestCallAction(getNameCounter()));
            }
            
            public String getName() {
                return "Call action";
            }
            
            public org.openide.util.HelpCtx getHelpCtx() {
                return null;
            }
        });
        newTypes.addAll(generateSetNewTypes());
        return newTypes;
    }

}
