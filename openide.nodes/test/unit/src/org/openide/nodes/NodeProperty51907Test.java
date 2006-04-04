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
import java.util.logging.Level;
import org.netbeans.junit.Log;
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
        
    /**
     * Note that for this test it doesn't matter what isDefaultValue() methods
     * return.
     */
    public void testThatWarningIsLoggedForOldModulesProperty() {
        CharSequence log = Log.enable("", Level.WARNING);
        Node.Property property = new OldModulePropertyWithSDVReturningTrue();
        // ErrorManager should log warning
        property.isDefaultValue();
        String className = property.getClass().getName();
        assertTrue("The WARNING message should contain name of the property" +
                "class - " + className + " was log:\n" + log, log.toString().indexOf(className) >= 0);


        int len = log.length();
        
        // ErrorManager shouldn't log warning more than once per property
        property.isDefaultValue();
        assertEquals("No other message logged", len, log.length());

        Node.Property otherInstance = new OldModulePropertyWithSDVReturningTrue();
        otherInstance.isDefaultValue();
        assertEquals("No other message logged2", len, log.length());
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithBothMethodsOverrided() {
        CharSequence log = Log.enable("", Level.WARNING);
        
        Node.Property property = new BothMethodsOverridedProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertEquals("There shouldn't be any WARNING message logged by the ErrorManager", 0, log.length());
    }
    
    public void testThatWarningIsNotLoggedForPropertyWithNoneMethodOverrided() {
        CharSequence log = Log.enable("", Level.WARNING);
        
        Node.Property property = new DefaultTestProperty();
        // ErrorManager shouldn't log warning for correct implementations
        property.isDefaultValue();
        assertEquals("There shouldn't be any WARNING message logged by the ErrorManager", 0, log.length());
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
    
}
