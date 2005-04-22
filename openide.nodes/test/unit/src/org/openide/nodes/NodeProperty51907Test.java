/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Tests for issue 51907. For more information see the
 * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=51907">
 * descrition in issuezilla</a>
 *
 * @author mkrauskopf
 */
public class NodeProperty51907Test extends NbTestCase {
    
    public NodeProperty51907Test(String name) {
        super(name);
    }
    
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", "org.openide.nodes.NodeProperty51907Test$Lkp");
        assertNotNull("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
        ErrManager.resetMessages();
    }
    
    
    /**
     * Note that for this test it doesn't matter what isDefaultValue() methods
     * return.
     */
    public void testThatWarningIsLoggedForOldModulesProperty() {
        System.out.println("testThatWarningIsLoggedForOldModulesProperty");
        
        Node.Property property = new OldModulePropertyWithSDVReturningTrue();
        // ErrorManager should log warning
        property.isDefaultValue();
        String messages = ErrManager.messages.toString();
        String className = property.getClass().getName();
        assertTrue("There should be WARNING message logged by the ErrorManager",
                messages.startsWith(ErrManager.WARNING_MESSAGE_START));
        assertTrue("The WARNING message should contain name of the property" +
                "class - " + className, messages.indexOf(className) >= 0);
        assertTrue("There should be exactly one message logged, but is " +
                ErrManager.nOfMessages, ErrManager.nOfMessages == 1);
        
        // ErrorManager shouldn't log warning more than once per property
        property.isDefaultValue();
        assertTrue("There should be exactly one message logged, but is " +
                ErrManager.nOfMessages, ErrManager.nOfMessages == 1);
        
        Node.Property otherInstance = new OldModulePropertyWithSDVReturningTrue();
        otherInstance.isDefaultValue();
        assertTrue("There should be exactly one message logged, but is " +
                ErrManager.nOfMessages, ErrManager.nOfMessages == 1);
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithBothMethodsOverrided() {
        System.out.println("testThatWarningIsNotLoggedForPropertyWithBothMethodOverrided");
        
        Node.Property property = new BothMethodsOverridedProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertTrue("There shouldn't be any WARNING message logged by the ErrorManager",
                ErrManager.messages.length() == 0);
        assertTrue("There shouldn't be any messages logged, but is " +
                ErrManager.nOfMessages, ErrManager.nOfMessages == 0);
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithNoneMethodOverrided() {
        System.out.println("testThatWarningIsNotLoggedForPropertyWithNoneMethodOverrided");
        
        Node.Property property = new DefaultTestProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertTrue("There shouldn't be any WARNING message logged by the ErrorManager",
                ErrManager.messages.length() == 0);
        assertTrue("There shouldn't be any messages logged, but is " +
                ErrManager.nOfMessages, ErrManager.nOfMessages == 0);
    }
    
    /**
     * Simulates property for old modules which didn't know about
     * isDefaultValue() method but could overrode restoreDefaultValue() to 
     * returns true. Warning has to be logged for such properties.
     */
    private static final class OldModulePropertyWithSDVReturningTrue
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
    }
    
    /**
     * Simulates correctly implemented property which override both methods.
     */
    private static final class BothMethodsOverridedProperty
            extends DefaultTestProperty {
        public boolean supportsDefaultValue()  {
            return true;
        }
        public boolean isDefaultValue() {
            return false;
        }
    }
    
    /**
     * Simulates correctly implemented property which doesn't override any of
     * the methods (supportsDefaultValue(), isDefaultValue()).
     */
    private static class DefaultTestProperty extends Node.Property {
        /** We don't need any of these method (or constructor) for our testing. */
        public DefaultTestProperty() { super(Object.class); }
        public void setValue(Object val) {}
        public Object getValue() { return null; }
        public boolean canWrite() { return false; }
        public boolean canRead() { return false; }
    }
    
    
    
    public static final class Lkp extends AbstractLookup {
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new ErrManager());
        }
    }
    
    private static final class ErrManager extends ErrorManager {
        static final StringBuffer messages = new StringBuffer();
        static int nOfMessages;
        static final String DELIMITER = ": ";
        static final String WARNING_MESSAGE_START = WARNING + DELIMITER;
        
        static void resetMessages() {
            messages.delete(0, ErrManager.messages.length());
            nOfMessages = 0;
        }
        
        public void log(int severity, String s) {
            nOfMessages++;
            messages.append(severity + DELIMITER + s);
            messages.append('\n');
        }
        
        public Throwable annotate(Throwable t, int severity,
                String message, String localizedMessage,
                Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance(String name) {
            return this;
        }
        
        public void notify(int severity, Throwable t) {}
    }
}
