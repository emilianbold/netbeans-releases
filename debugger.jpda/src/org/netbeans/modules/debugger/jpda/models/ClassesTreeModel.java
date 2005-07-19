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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class ClassesTreeModel implements TreeModel {

    
    private static boolean      verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('s') >= 0);
    
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector              listeners = new Vector ();
    
    
    public ClassesTreeModel(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public Object[] getChildren (Object o, int from, int to) 
    throws UnknownTypeException {
        try {
            Object[] r = null;
            if (o.equals (ROOT)) {
                r = getLoaders ();
            } else
            if (o instanceof Object[]) {
                r = getChildren ((Object[]) o);
            } else
            if (o instanceof ClassLoaderReference) {
                r = getPackages ((ClassLoaderReference) o);
            } else
            if (o == NULL_CLASS_LOADER) {
                r = getPackages (null);
            } else
            if (o instanceof ReferenceType) {
                r = ((ReferenceType) o).nestedTypes ().toArray ();
            } else
            throw new UnknownTypeException (o);
            Object[] rr = new Object [to - from];
            System.arraycopy (r, from, rr, 0, to - from);
            return rr;
        } catch (VMDisconnectedException ex) {
            return new Object [0];
        }
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        try {
            if (node.equals (ROOT))
                return getLoaders ().length;
            if (node instanceof Object[])
                return getChildren ((Object[]) node).length;
            if (node instanceof ClassLoaderReference)
                return getPackages ((ClassLoaderReference) node).length;
            if (node == NULL_CLASS_LOADER)
                return getPackages (null).length;
            if (node instanceof ReferenceType) {
                return ((ReferenceType) node).nestedTypes ().size ();
            }
            throw new UnknownTypeException (node);
        } catch (VMDisconnectedException ex) {
            return 0;
        }
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o.equals (ROOT)) return false;
        if (o instanceof Object[]) return false;
        if (o instanceof ReferenceType) return false;
        if (o instanceof ClassLoaderReference) return false;
        if (o == NULL_CLASS_LOADER) return false;
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
        if (listener == null)
            listener = new Listener (this, debugger);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
        if (listeners.size () == 0) {
            listener.destroy ();
            listener = null;
        }
    }
    
    public void fireTreeChanged () {
        classes = null;
        names = null;
        cache = new HashMap ();
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (null);
    }

    
    // private methods .........................................................

    private List classes = null; // name of class -> ReferenceType
    private List names = null; // list on names of all clases
    private HashMap cache = new HashMap ();
        // null => list of class loaders
        // ClassLoaderReference -> list of packages & ReferenceTypes
        // package name -> list of packages & ReferenceTypes
    private Comparator comparator = new PackageComparator ();
    private Comparator comparator1 = new ClassLoaderComparator ();
    static final Integer NULL_CLASS_LOADER = new Integer (11);
    
    
    private List getNames () {
        if (classes == null) {
	    VirtualMachine vm = debugger.getVirtualMachine ();
            List referenceTypes = new ArrayList ();
            if (vm != null) 
                referenceTypes = vm.allClasses ();
            int i, k = referenceTypes.size ();
            names = new ArrayList ();
            classes = new ArrayList ();
            for (i = 0; i < k; i++) {
                ReferenceType rt = (ReferenceType) referenceTypes.get (i);
                if (rt instanceof ArrayType) continue;
                names.add (rt.name ());
                classes.add (rt);
            }
        }
        return names;
    }
    
    private Object[] getLoaders () {
        Object[] ch = (Object[]) cache.get (null);
        if (ch != null) return ch;
        List names = getNames ();
        Set loaders = new TreeSet (comparator1);
        int i, k = names.size ();
        for (i = 0; i < k; i++) {
            try {
            String name = (String) names.get (i);
            ReferenceType rt = (ReferenceType) classes.get (i);
            ClassLoaderReference clr = rt.classLoader ();
            if (clr == null)
                loaders.add (NULL_CLASS_LOADER);
            else
            if (clr.referenceType ().name ().equals ("sun.reflect.DelegatingClassLoader"))
                continue;
            else
                loaders.add (clr);
            } catch (ObjectCollectedException ex) {
            }
        }
        ch = loaders.toArray ();
        cache.put (null, ch);
        return ch;
    }
    
    private Object[] getPackages (ClassLoaderReference clr) {
        Object[] ch = clr == null ?
            (Object[]) cache.get (NULL_CLASS_LOADER) :
            (Object[]) cache.get (clr);
        if (ch != null) return ch;
        List names = getNames ();
        Set objects = new TreeSet (comparator);
        int i, k = names.size ();
        for (i = 0; i < k; i++) {
            String name = (String) names.get (i);
            ReferenceType rt = (ReferenceType) classes.get (i);
            if (rt.classLoader () != clr) continue;
            int start = 0;
            int end = name.indexOf ('.', start);
            if (end < 0) {
                // ReferenceType found
                if (name.indexOf ('$', start) < 0) {
                    ReferenceType tr = (ReferenceType) classes.get (i);
                    objects.add (tr);
                }
            } else
                objects.add (new Object[] {name.substring (0, end), clr});
        }
        ch = objects.toArray ();
        if (clr == null)
            cache.put (NULL_CLASS_LOADER, ch);
        else
            cache.put (clr, ch);
        return ch;
    }
    
    private Object[] getChildren (Object[] parent) {
        Object[] ch = (Object[]) cache.get (parent);
        if (ch != null) return ch;
        List names = getNames ();
        Set objects = new TreeSet (comparator);
        int i, k = names.size ();
        for (i = 0; i < k; i++) {
            String name = (String) names.get (i);
            ReferenceType rt = (ReferenceType) classes.get (i);
            if (rt.classLoader () != parent [1]) continue;
            String parentN = ((String) parent [0]) + '.';
            if (!name.startsWith (parentN)) continue;
            int start = (parentN).length ();
            int end = name.indexOf ('.', start);
            if (end < 0) {
                // ReferenceType found
                if (name.indexOf ('$', start) < 0) {
                    objects.add (rt);
                }
            } else
                objects.add (new Object[] {name.substring (0, end), rt.classLoader ()});
        }
        ch = objects.toArray ();
        cache.put (parent, ch);
        return ch;
    }
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    private static String shortName (String name) {
        int i = name.lastIndexOf ('.');
        if (i < 0) return name;
        return name.substring (i + 1);
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference model;
        
        public Listener (
            ClassesTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference (tm);
            debugger.addPropertyChangeListener (this);
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println ("ClTM cancel old task " + task);
                task = null;
            }
        }
        
        private ClassesTreeModel getModel () {
            ClassesTreeModel tm = (ClassesTreeModel) model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( ( (e.getPropertyName () == 
                     debugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                   //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == debugger.PROP_STATE)
                 ) && (debugger.getState () == debugger.STATE_STOPPED)
            ) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
                final ClassesTreeModel ltm = getModel ();
                if (ltm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println ("ClTM cancel old task " + task);
                    task = null;
                }
                task = RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        if (debugger.getState () != debugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println ("ClTM cancel started task " + task);
                            return;
                        }
                        if (verbose)
                            System.out.println ("ClTM do task " + task);
                        ltm.fireTreeChanged ();
                    }
                }, 500);
                if (verbose)
                    System.out.println ("ClTM  create task " + task);
            } else
            if ( (e.getPropertyName () == debugger.PROP_STATE) &&
                 (debugger.getState () != debugger.STATE_STOPPED) &&
                 (task != null)
            ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel ();
                if (verbose)
                    System.out.println ("ClTM cancel task " + task);
                task = null;
            }
        }
    }
    
    private static class PackageComparator implements Comparator {
        public PackageComparator () {}
        
        public int compare (Object o1, Object o2) {
            if (o1 instanceof Object[]) {
                if (o2 instanceof Object[])
                    return ((String) ((Object[]) o1) [0]).compareTo (((Object[]) o2) [0]);
                return -1;
            } 
            if (o2 instanceof Object[])
                return 1;
            return shortName (((ReferenceType) o1).name ()).compareTo (
                shortName (((ReferenceType) o2).name ())
            );
        }
    }
    
    private static class ClassLoaderComparator implements Comparator {
        public ClassLoaderComparator () {}
        
        public int compare (Object o1, Object o2) {
            if (o1 == NULL_CLASS_LOADER) {
                if (o2 == NULL_CLASS_LOADER)
                    return 0;
                return -1;
            } 
            if (o2 == NULL_CLASS_LOADER)
                return 1;
            return ((ClassLoaderReference) o1).toString ().compareTo (
                ((ClassLoaderReference) o2).toString ()
            );
        }
    }
}
