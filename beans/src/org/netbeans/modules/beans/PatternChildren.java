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

package org.netbeans.modules.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.lang.ref.WeakReference;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.api.mdr.events.MDRChangeListener;
import org.netbeans.api.mdr.events.MDRChangeEvent;
import org.netbeans.api.mdr.events.MDRChangeSource;
import org.netbeans.api.mdr.events.AttributeEvent;

import javax.jmi.reflect.JmiException;

/** Implements children for basic source code patterns
* 
* @author Petr Hrebejk, Jan Jancura
*/
public final class PatternChildren extends Children.Keys {

    static final RequestProcessor ANALYZER = new RequestProcessor("Bean patterns analyser", 1); // NOI18N

    private boolean wri = true;

    private Listener elementListener = new Listener(this);
    
    private RequestProcessor.Task   refreshTask;
    
    /** Object for finding patterns in class */
    private PatternAnalyser       patternAnalyser;
    
    private final JavaClass classElement;
    
    // Constructors -----------------------------------------------------------------------

    /** Create pattern children. The children are initilay unfiltered.
     * @param classElement the atteached class. For this class we recognize the patterns
     */ 

    public PatternChildren (JavaClass classElement) {
        this(classElement, true);
    }

    public PatternChildren (JavaClass classElement, boolean isWritable ) {
        this.classElement = classElement;
        patternAnalyser = new PatternAnalyser( classElement );
        PropertyActionSettings.getDefault().addPropertyChangeListener(elementListener);
        wri = isWritable;
    }

    protected void addNotify() {
        super.addNotify();
        refreshAllKeys();
    }

    /** Updates all the keys (elements) according to the current filter &
    * ordering.
    */
    protected void refreshAllKeys () {
            scheduleRefresh();
    }
    
    private synchronized void scheduleRefresh() {
        if (refreshTask == null) {
            refreshTask = ANALYZER.create(elementListener);
        }
        refreshTask.schedule(200);
    }

    /** Updates all the keys with given filter. Overriden to provide package access tothis method.
    */
    protected void refreshKeys (int filter) {

        // Method is added or removed ve have to re-analyze the pattern abd to
        // registrate Children as listener
        JMIUtils.beginTrans(false);
        try {
            try {
                elementListener.unregisterAll();
                elementListener.reassignMethodListener(this.classElement);
                elementListener.reassignFieldListener(this.classElement);
                elementListener.assignFeaturesListener(this.classElement);
                patternAnalyser.analyzeAll();
            } finally {
                JMIUtils.endTrans();
            }
        } catch (JmiException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        
        setKeys(collectAllKeys());
    }
    
    private Collection collectAllKeys() {
        List keys = new LinkedList();
        keys.addAll(getKeysOfType(PatternFilter.PROPERTY));
        keys.addAll(getKeysOfType(PatternFilter.IDXPROPERTY));
        keys.addAll(getKeysOfType(PatternFilter.EVENT_SET));
        return keys;
    }

    /** Gets the pattern analyser which manages the patterns */
    PatternAnalyser getPatternAnalyser( ) {
        return patternAnalyser;
    }

    public void removeAll() {
        elementListener.unregisterAll();
        setKeys(Collections.EMPTY_LIST);
    }
    
    // Children.keys implementation -------------------------------------------------------

    /** Creates node for given key.
    */
    protected Node[] createNodes (Object key) {
        return new Node[] {createPatternNode(key)};
    }
    
    private Node createPatternNode(Object key) {

        if (key instanceof IdxPropertyPattern)
            return new IdxPropertyPatternNode((IdxPropertyPattern) key, wri);
        if (key instanceof PropertyPattern)
            return new PropertyPatternNode((PropertyPattern) key, wri);
        if (key instanceof EventSetPattern)
            return new EventSetPatternNode((EventSetPattern) key, wri);

        // Unknown pattern
        ErrorManager.getDefault().notify(ErrorManager.WARNING, new IllegalStateException("Unknown key: " + key)); // NOI18N
        return null;
    }


    // Utility methods --------------------------------------------------------------------

    protected Collection getKeysOfType (int elementType) {

        List keys = null;

        if ((elementType & PatternFilter.PROPERTY) != 0)  {
            keys = new ArrayList(patternAnalyser.getPropertyPatterns());
            Collections.sort( keys, new PatternComparator() );
        }
        if ((elementType & PatternFilter.IDXPROPERTY) != 0) {
            keys = new ArrayList(patternAnalyser.getIdxPropertyPatterns());
            Collections.sort( keys, new PatternComparator() );
        }
        if ((elementType & PatternFilter.EVENT_SET) != 0) {
            keys = new ArrayList(patternAnalyser.getEventSetPatterns());
            Collections.sort( keys, new PatternComparator() );
        }

        //    if ((filter == null) || filter.isSorted ())
        //      Collections.sort (keys, comparator);
        return keys;
    }



    // Inner classes ----------------------------------------------------------------------

    /** The listener of method changes temporary used in PatternAnalyser to
     * track changes in 
     */
    static final class Listener extends WeakReference implements MDRChangeListener, PropertyChangeListener, Runnable {
        List/*<? extends MDRChangeSource>*/	knownFields, knownMethods;
        JavaClass classElement;
        
        private Listener(PatternChildren owner) {
            super(owner, Utilities.activeReferenceQueue());
        }
        
        private PatternChildren getChildren() {
            PatternChildren o = (PatternChildren) get();
            if (o != null)
                return o;
            unregisterAll();
            PropertyActionSettings.getDefault().removePropertyChangeListener(this);
            return null;
        }
        
        /** Method for removing method listener */
        private void reassignMethodListener(JavaClass el) throws JmiException {
            assert JMIUtils.isInsideTrans();
            List/*<Methods>*/ methods = JMIUtils.getMethods(el);
            for (Iterator it = methods.iterator(); it.hasNext();) {
                MDRChangeSource method = (MDRChangeSource) it.next();
                method.addListener(this);
            }
            knownMethods = methods;
        }
        
        /** Method for removing field listener */
        private void reassignFieldListener(JavaClass el) throws JmiException {
            assert JMIUtils.isInsideTrans();
            List fields = JMIUtils.getFields(el);
            for (Iterator it = fields.iterator(); it.hasNext();) {
                MDRChangeSource field = (MDRChangeSource) it.next();
                field.addListener(this);
            }
            knownFields = fields;
        }
        
        private void assignFeaturesListener(JavaClass el) throws JmiException {
            assert JMIUtils.isInsideTrans();
            ((MDRChangeSource) el).addListener(this);
            this.classElement = el;
        }
        
        synchronized void unregisterAll() {
            // unregister us from everywhere:
            List fields = knownFields;
            if (fields != null) {
                for (Iterator it = fields.iterator(); it.hasNext();) {
                    MDRChangeSource field = (MDRChangeSource) it.next();
                    field.removeListener(this);
                    knownFields = null;
                }
            }
            List/*<Methods>*/ methods = knownMethods;
            if (methods != null) {
                for (Iterator it = methods.iterator(); it.hasNext();) {
                    MDRChangeSource method = (MDRChangeSource) it.next();
                    method.removeListener(this);
                    knownMethods = null;
                }
            }
            if (this.classElement != null) {
                ((MDRChangeSource) this.classElement).removeListener(this);
                this.classElement = null;
            }
        }
        
        public void propertyChange ( PropertyChangeEvent e ) {
            Object src = e.getSource();
            String name = e.getPropertyName();
            
            if(PropertyActionSettings.getDefault() == src &&
                    !PropertyActionSettings.PROP_STYLE.equals(name) ) {
                return;
            }
            PatternChildren pch = getChildren();
            if (pch != null)
                pch.scheduleRefresh();
        }
        
        /**
         * This method is called from either scheduled refresh, or from
         * the Active Reference Queue, when the owning children are GCed.
         */
        public void run() {
            PatternChildren pch = getChildren();
            if (pch != null)
                pch.refreshKeys(PatternFilter.ALL);
        }

        public void change(MDRChangeEvent e) {
            boolean refresh = false;
            try {
                JMIUtils.beginTrans(false);
                try {
                    refresh = checkChangeEvent(e);
                } finally {
                    JMIUtils.endTrans();
                }
            } catch (JmiException e1) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e1);
            }
            
            if (refresh) {
                PatternChildren pch = getChildren();
                if (pch != null)
                    pch.refreshAllKeys();
            }
        }
        private boolean checkChangeEvent(MDRChangeEvent e) {
            Object src = e.getSource();
            if (src instanceof Element && ((Element) src).isValid()) {
                if (e instanceof AttributeEvent) {
                    AttributeEvent ae = (AttributeEvent) e;
                    String name = ae.getAttributeName();
                    if (src instanceof JavaClass) {
                        if ("contents".equals(name)) { // NOI18N
                            return true;
                        }
                    } else if ("name".equals(name) || "modifiers".equals(name) || // NOI18N
                            "typeName".equals(name) || "parameters".equals(name)) { // NOI18N
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static final class PatternComparator implements Comparator {

        public int compare( Object a, Object b ) {
            return ((Pattern)a).getName().compareTo( ((Pattern)b ).getName() );
        }

        public boolean equals( Object c ) {
            return c instanceof PatternComparator;
        }
    }
}
