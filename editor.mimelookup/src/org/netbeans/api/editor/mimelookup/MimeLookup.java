/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.mimelookup;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mime Lookup provides lookup mechanism for mime specific objects. 
 * It can be used for example for retrieving mime specific 
 * editor settings, actions, etc. 
 * <p>
 * For obtaining mime-type specific lookup 
 * {@link #getMimeLookup(String) getMimeLookup(String mimeType)} static method can be used.
 * <p>
 * Because embeded languages (like JSP) can contain embeded mime types, the mime 
 * lookup also provides the functionality of lookuping over these embeded
 * mime types.
 * <p>
 * For obtaining mime-type specific embeded lookup
 * {@link #childLookup(String) childLookup(String mimeType)} method can be used.
 * <p>
 * Clients can listen on the lookup result for lookup changes. Because lookup results
 * are held weakly, client is responsible for reference holding during 
 * life-time frame. Listeners added to lookup result should be weak. As a pattern
 * for that requirements, following code snippet can be used:
 * <pre>
 *
 *      public class MyClass {
 *          private Lookup.Result result;
 *          private LookupListener weakLookupListener;
 *          private LookupListener lookupListener
 *
 *          public MyClass(){
 *              MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java"); //use your content type here
 *              result = lookup.lookup(new Lookup.Template(YourObjectHere.class));
 *              result.allInstances(); // allInstances should be called, otherwise event will
 *                                     // not be fired - optimalization in ProxyLookup
 *              lookupListener = new LookupListener(){
 *                 public void resultChanged(LookupEvent ev) {
 *                     // handle your code
 *                 }
 *              };
 *
 *              weakLookupListener = (LookupListener) WeakListeners.create(
 *                      LookupListener.class, lookupListener, result);
 *
 *              result.addLookupListener(weakLookupListener);
 *          }
 *
 *      }
 *
  * </pre>
 * 
 *  @author Miloslav Metelka, Martin Roskanin
 */
final public class MimeLookup extends Lookup {
    
    private static DelegatingResult rootInitializers;
    private static final Map mime2lookup = new HashMap();
    
    private MimeLookup parent;
    private DelegatorLookup delegator;
    private final List initializersListeners = new ArrayList();
    private final List initializersList = new ArrayList();
    private final Map mime2childLookup = new HashMap();    
    
    /** Creates a new instance of MimeLookup 
     *
     *  @param parent   parent of this MimeLookup. Can be null in case of constructing 
     *                  the root MimeLookup
     *  @param mimeType non-null mime-type string representation, e.g. "text/x-java"
     */
    private MimeLookup(MimeLookup parent, String mimeType) {
        this.parent = parent;
        this.delegator = new DelegatorLookup();
        initMimeLookup(mimeType);
    }
    
    /** Gets mime-type specific lookup.
     *
     *  @param mimeType non-null mime-type string representation, e.g. "text/x-java"
     *  @return non-null mime-type specific lookup
     */
    public static MimeLookup getMimeLookup(String mimeType) {
        if (mimeType == null) {
            throw new NullPointerException("mimeType must not be null"); // NOI18N
        }
        MimeLookup lookup;
        synchronized (mime2lookup) {
            Reference ref = (Reference) mime2lookup.get(mimeType);
            lookup = (ref == null) ? null : (MimeLookup) ref.get();

            if (lookup == null) {
                lookup = new MimeLookup(null, mimeType);
                mime2lookup.put(mimeType, new java.lang.ref.WeakReference(lookup));
            }
        }
        
        return lookup;
    }
    

    /** Gets mime-type specific child (embeded) lookup. Child mime-type content can be embeded into parent
     *  mime-type content in various embeded languages. In this case mime-type lookup child is 
     *  specified as subelement of parent lookup i.e.: MimeLookup("text/x-jsp") can have
     *  a child MimeLookup("text/x-java") in a case of a jsp scriplet.
     *
     *  @param mimeType non-null mime-type string representation
     *  @return non-null mime-type specific child (embeded) lookup
     */
    public MimeLookup childLookup(String mimeType){
        if (mimeType == null) {
            throw new NullPointerException("mimeType must not be null"); // NOI18N
        }
        MimeLookup lookup;
        synchronized (mime2childLookup){
            Reference ref = (Reference) mime2childLookup.get(mimeType);
            lookup = (ref == null) ? null : (MimeLookup) ref.get();
            
            if (lookup == null) {
                lookup = new MimeLookup(this, mimeType);
                mime2childLookup.put(mimeType, new java.lang.ref.WeakReference(lookup));
            }
        }
        return lookup;
    }

    /** Look up an object matching a given interface.
     * This is the simplest method to use.
     * If more than one object matches, one will be returned arbitrarily.
     * The template class may be a class or interface; the instance is
     * guaranteed to be assignable to it.
     *
     * @param clazz class of the object we are searching for
     * @return an object implementing the given class or <code>null</code> if no such
     *         implementation is found
     */
    public Object lookup(Class clazz){
        return delegator.lookup(clazz);
    }

    /** The general lookup method. Callers can get list of all instances and classes
     * that match the given <code>template</code> and attach a listener to
     * this be notified about changes. The general interface does not
     * specify whether subsequent calls with the same template produce new
     * instance of the {@link org.openide.util.Lookup.Result} or return shared instance. The
     * prefered behaviour however is to return shared one.
     *
     * @param template a template describing the services to look for
     * @return an object containing the results
     */
    public Result lookup(Template template) {
        return delegator.lookup(template);
    }
    
    private void initMimeLookup(String mimeType){
        Iterator initializersIterator = (parent == null) ? 
            getRootInitializers().allInstances().iterator() :
            parent.initializersList.iterator();
        
        while (initializersIterator.hasNext()){
            MimeLookupInitializer initializer
                    = (MimeLookupInitializer)initializersIterator.next();
            Lookup.Result children = initializer.child(mimeType);
            Iterator childrenIt = children.allInstances().iterator();
            while (childrenIt.hasNext()){
                initializersList.add(childrenIt.next());
            }
            initializersListeners.add(new InitializersListener(children));
        }
        
        if (parent == null){
            initializersListeners.add(new InitializersListener(rootInitializers));
        }
        
        rebuildLookups();
    }
    
    private static DelegatingResult getRootInitializers() {
        if (rootInitializers == null) {
            rootInitializers = new DelegatingResult(Lookup.getDefault().lookup(
                    new Lookup.Template(MimeLookupInitializer.class)));
        }
        return rootInitializers;
    }
    
    
    
    private void rebuildLookups() {
        List allLookups = new ArrayList();
        for (Iterator it = initializersListeners.iterator();
            it.hasNext();) {
            InitializersListener l = (InitializersListener)it.next();
            allLookups.addAll(l.getLookups());
        }
        Lookup[] all = new Lookup[allLookups.size()];
        allLookups.toArray(all);
        delegator.setDelegatorLookups(all);
    }
    
    private static class DelegatingResult extends Lookup.Result{
        
        private final Lookup.Result delegator;
        private final Set listeners = new WeakSet(10); // Set<LookupListener>
        
        
        public DelegatingResult(Lookup.Result delegator){
            this.delegator = delegator;
        }
        
        public void removeLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }

        public void addLookupListener(LookupListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }

        public Collection allInstances() {
            return delegator.allInstances();
        }
        
        public void resultChanged() {
            LookupListener[] _listeners;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                _listeners = (LookupListener[])listeners.toArray(new LookupListener[listeners.size()]);
            }
            LookupEvent ev = new LookupEvent(this);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].resultChanged(ev);
            }
        }
        
    }
    
    private class DelegatorLookup extends ProxyLookup{
        public DelegatorLookup(){
        }
        
        public void setDelegatorLookups(Lookup[] lookups) {
            setLookups(lookups);
        }
    }

    private final class InitializersListener implements LookupListener {
        
        private Lookup.Result initializersResult;
        private List lookupList = Collections.EMPTY_LIST;
        private Map initializer2lookup = new HashMap();
        
        public InitializersListener(Lookup.Result initializersResult) {
            this.initializersResult = initializersResult;
            initializersResult.addLookupListener(this);
            rebuild(initializersResult.allInstances());
        }
        
        public synchronized List getLookups() {
            return lookupList;
        }

        private void rebuild(Collection newInitializers) {
            List lookups = new ArrayList();
            initializer2lookup.keySet().retainAll(newInitializers);
            for (Iterator it = newInitializers.iterator(); it.hasNext();) {
                Object initializer = it.next();
                initializer2lookup.remove(initializer);
                Lookup lookup = ((MimeLookupInitializer)initializer).lookup();
                if (lookup == null) {
                    continue;
                }
                initializer2lookup.put(initializer, lookup);
                lookups.add(lookup);
            }
            synchronized (this) {
                lookupList = lookups;
            }
        }

        public void resultChanged(LookupEvent ev) {
            Lookup.Result initializersResult = ((Lookup.Result)ev.getSource());
            rebuild(initializersResult.allInstances());
            rebuildLookups();
        }
        
    }
}
