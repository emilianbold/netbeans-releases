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

import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Vector;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestSubTest extends Test {
    
    public static final String OWNLOGGER="OwnLogger";
    
    /** Creates new TestSubTest */
    public TestSubTest(int num,Logger logr) {
        this("subTest"+Integer.toString(num));
        logger=logr;
    }
    
    public TestSubTest(String name) {
        super(name);
    }
    
    public TestSubTest(Element node) {
        super(node);
        if (node.getAttribute(OWNLOGGER).equals("true")) {
            logger = new Logger(Main.frame.getEditor());
        } else {
            logger=null;
        }
    }
    
    public Element toXML(Element node) {
        super.toXML(node);
        node.setAttribute(OWNLOGGER, (logger != owner.getLogger()) ? "true" : "false");
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        if (node.getAttribute(OWNLOGGER).equals("true")) {
            logger = new Logger(Main.frame.getEditor());
        } else {
            logger=null;
        }
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(OWNLOGGER, new BooleanProperty((logger != getOwner().getLogger())));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(OWNLOGGER) == 0) {
            return new BooleanProperty((logger != getOwner().getLogger()));
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(OWNLOGGER) == 0) {
            if (((BooleanProperty)(value)).getValue()) {
                logger = new Logger(Main.frame.getEditor());
            } else {
                logger=null;
            }
        } else {
            super.setProperty(name, value);
        }
    }
    
    public String getAuthor() {
        return ((Test)owner).getAuthor();
    }
    
    public String getVersion() {
        return ((Test)owner).getVersion();
    }
    
    public void setOwnLogger(boolean b) {
        if (b && logger == owner.getLogger()) {
            logger=new Logger(Main.frame.getEditor());
            firePropertyChange(OWNLOGGER,null,b ? Boolean.TRUE : Boolean.FALSE);
        } else {
            if (!b && logger != owner.getLogger()) {
                logger=owner.getLogger();
                firePropertyChange(OWNLOGGER,null,b ? Boolean.TRUE : Boolean.FALSE);
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
    
    protected void registerNewTypes() {
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestStep.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestCallAction.class);
    }
}
