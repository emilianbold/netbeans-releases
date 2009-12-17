/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openide.util.lookup;

import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup.Storage;

public class ProxyLookup173975Test {

    public ProxyLookup173975Test() {
    }

    boolean called = false;

    @Test
    public void testAbstractLookupWithoutAllInstances() {
        registerLookupListenerAndAddSomething(false, false, false);
    }

    @Test
    public void testAbstractLookupWithAllInstances() {
        registerLookupListenerAndAddSomething(false, true, false);
    }

    @Test
    public void testAbstractLookupInheritanceTreeWithoutAllInstances() {
        registerLookupListenerAndAddSomething(false, false, true);
    }

    @Test
    public void testAbstractLookupInheritanceTreeWithAllInstances() {
        registerLookupListenerAndAddSomething(false, true, true);
    }

    @Test
    public void testProxyLookupWithoutAllInstances() {
        registerLookupListenerAndAddSomething(true, false, false);
    }

    @Test
    public void testProxyLookupWithAllInstances() {
        registerLookupListenerAndAddSomething(true, true, false);
    }

    @Test
    public void testProxyLookupInheritanceTreeWithoutAllInstances() {
        registerLookupListenerAndAddSomething(true, false, true);
    }

    @Test
    public void testProxyLookupInheritanceTreeWithAllInstances() {
        registerLookupListenerAndAddSomething(true, true, true);
    }

    private void registerLookupListenerAndAddSomething(boolean useProxy, boolean callAllInstances, boolean inheritanceTree) {
        called = false;
        InstanceContent aInstanceContent = new InstanceContent();
        Storage<?> s = inheritanceTree ? new InheritanceTree() : new ArrayStorage();
        Lookup aLookup = new AbstractLookup(aInstanceContent, s);
        if (useProxy) {
            aLookup = new ProxyLookup(aLookup);
        }
        Lookup.Result<ObjectInLookup> result = aLookup.lookupResult(ObjectInLookup.class);
        if (callAllInstances) {
            result.allInstances(); // TO GET SUCCESS
        }
        result.addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                Lookup.Result aResult = (Lookup.Result) ev.getSource();
                Collection c = aResult.allInstances();
                if (!c.isEmpty()) {
                    called = true;
                }
            }
        });

        aInstanceContent.set(Collections.singleton(
                new ObjectInLookup("Set Object in Lookup)")), null);
        Assert.assertTrue("Listener was notified", called);
    }

    public class ObjectInLookup {

        private final String name;

        public ObjectInLookup(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "objectinlookup:" + getName();
        }
    }
}
