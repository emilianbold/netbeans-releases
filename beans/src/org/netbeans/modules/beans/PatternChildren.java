/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.*;
import org.openide.src.nodes.ClassChildren;
import org.openide.src.nodes.ElementNodeFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;

/** Implements children for basic source code patterns
* 
* @author Petr Hrebejk, Jan Jancura
*/
public class PatternChildren extends ClassChildren {

    static final RequestProcessor ANALYZER = new RequestProcessor("Bean patterns analyser", 1);

    private boolean wri = true;

    private Listener elementListener = new Listener();
    
    private PropertyChangeListener weakMethodListener = WeakListener.propertyChange( elementListener, null);
    private PropertyChangeListener weakFieldListener = WeakListener.propertyChange( elementListener, null);

    private PropertyChangeListener weakStyleListener = WeakListener.propertyChange( elementListener, PropertyActionSettings.getDefault());
    
    private RequestProcessor.Task   refreshTask;
    
    static {
        Integer i = new Integer (PatternFilter.METHOD | PatternFilter.PROPERTY |
                                 PatternFilter.IDXPROPERTY | PatternFilter.EVENT_SET
                                );
        propToFilter.put (ElementProperties.PROP_METHODS, i);
        i = new Integer (PatternFilter.FIELD | PatternFilter.PROPERTY |
                         PatternFilter.IDXPROPERTY | PatternFilter.EVENT_SET
                        );
        propToFilter.put (ElementProperties.PROP_FIELDS, i);
    }

    /** Object for finding patterns in class */
    private PatternAnalyser       patternAnalyser;

    // Constructors -----------------------------------------------------------------------

    /** Create pattern children. The children are initilay unfiltered.
     * @param elemrent the atteached class. For this class we recognize the patterns 
     */ 

    public PatternChildren (ClassElement classElement) {
        super (classElement);
        patternAnalyser = new PatternAnalyser( classElement );
        PropertyActionSettings.getDefault().addPropertyChangeListener(weakStyleListener);
    }

    public PatternChildren (ClassElement classElement, boolean isWritable ) {
        this (classElement);
        wri = isWritable;
    }

    /** Create pattern children. The children are initilay unfiltered.
     * @param elemrent the atteached class. For this class we recognize the patterns 
     */ 

    public PatternChildren (ElementNodeFactory factory, ClassElement classElement) {
        super (factory, classElement);
        patternAnalyser = new PatternAnalyser( classElement );
    }

    public PatternChildren (ElementNodeFactory factory, ClassElement classElement, boolean isWritable ) {
        this (factory, classElement);
        wri = isWritable;
    }

    /*
    PatternChildren cloneChildren() {
      return.clone();
      System.out.println ( "CLONING CHILDREN" );
      return new PatternChildren( patternAnalyser.getClassElement() );
}
    */

    // FilterCookie implementation --------------------------------------------------------
    
    private boolean addNotifyRefresh;
    
    /**
     * HACK -- this disables synchronous node refresh done from ClassChildren.addNotify(). 
     * It would be way better to change ClassChildren.addNotify() so it does not force a refresh,
     * but God knows who actually subclasses that stuff and for what purposes.
     */
    protected void addNotify() {
	try {
	    addNotifyRefresh = true;
	    super.addNotify();
	} finally {
	    addNotifyRefresh = false;
	}
    }

    /** Updates all the keys (elements) according to the current filter &
    * ordering.
    */
    protected void refreshAllKeys () {
        cpl = new Collection [getOrder ().length];
	if (addNotifyRefresh) {
	    scheduleRefresh();
	} else {
    	    refreshKeys (PatternFilter.ALL);
	}
    }
    
    private synchronized void scheduleRefresh() {
        if (refreshTask == null) {
            refreshTask = ANALYZER.create(elementListener);
        }
        refreshTask.schedule(0);
    }

    /** Updates all the keys with given filter. Overriden to provide package access tothis method.
    */
    protected void refreshKeys (int filter) {

        // Method is added or removed ve have to re-analyze the pattern abd to
        // registrate Children as listener
        reassignMethodListener();
        reassignFieldListener();
        patternAnalyser.analyzeAll();
        try{
            //temporary solution, probably bug in java module
            super.refreshKeys (filter);
        } catch(Exception ex){
        }
        //Thread.dumpStack();
    }

    /** @return The class of currently associated filter or null
     * if no filter is associated with these children 
     */
    public Class getFilterClass () {
        return PatternFilter.class;
    }

    /** Gets the pattern analyser which manages the patterns */
    PatternAnalyser getPatternAnalyser( ) {
        return patternAnalyser;
    }


    // Children.keys implementation -------------------------------------------------------

    /** Creates node for given key.
    */
    protected Node[] createNodes (Object key ) {

        if (key instanceof IdxPropertyPattern)
            return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, wri) };
        if (key instanceof PropertyPattern)
            return new Node[] { new PropertyPatternNode((PropertyPattern)key, wri) };
        if (key instanceof EventSetPattern)
            return new Node[] { new EventSetPatternNode((EventSetPattern)key, wri) };

        // Unknown pattern
        return super.createNodes (key);
    }


    // Utility methods --------------------------------------------------------------------

    protected Collection getKeysOfType (int elementType) {

        LinkedList keys = (LinkedList) super.getKeysOfType (elementType);
        LinkedList temp = null;

        if ((elementType & PatternFilter.PROPERTY) != 0)  {
            temp = new LinkedList();
            temp.addAll( patternAnalyser.getPropertyPatterns() );
            Collections.sort( temp, new PatternComparator() );
            keys.addAll( temp );
            //keys.addAll( patternAnalyser.getPropertyPatterns() );
        }
        if ((elementType & PatternFilter.IDXPROPERTY) != 0) {
            temp = new LinkedList();
            temp.addAll( patternAnalyser.getIdxPropertyPatterns() );
            Collections.sort( temp, new PatternComparator() );
            keys.addAll( temp );
            //keys.addAll( patternAnalyser.getIdxPropertyPatterns() );
        }
        if ((elementType & PatternFilter.EVENT_SET) != 0) {
            temp = new LinkedList();
            temp.addAll( patternAnalyser.getEventSetPatterns() );
            Collections.sort( temp, new PatternComparator() );
            keys.addAll( temp );
            //keys.addAll( patternAnalyser.getEventSetPatterns() );
        }

        //    if ((filter == null) || filter.isSorted ())
        //      Collections.sort (keys, comparator);
        return keys;
    }

    /** Method for removing method listener */
    private void reassignMethodListener() {
        MethodElement[] methods = element.getMethods();
        for ( int i = 0; i < methods.length ; i++ ) {
            methods[i].removePropertyChangeListener( weakMethodListener );
            methods[i].addPropertyChangeListener( weakMethodListener );
        }
    }


    /** Method for removing field listener */
    private void reassignFieldListener() {
        FieldElement[] fields = element.getFields();
        for ( int i = 0; i < fields.length ; i++ ) {
            fields[i].removePropertyChangeListener( weakFieldListener );
            fields[i].addPropertyChangeListener( weakFieldListener );
        }
    }


    // Inner classes ----------------------------------------------------------------------

    /** The listener of method changes temporary used in PatternAnalyser to
     * track changes in 
     */

    final class Listener implements PropertyChangeListener, Runnable {
        public void propertyChange ( PropertyChangeEvent e ) {
            Object src = e.getSource();
            String name = e.getPropertyName();
            
            if( src instanceof org.netbeans.modules.java.JavaDataObject ) //ignore
                return;
            if(PropertyActionSettings.getDefault() == src &&
                !PropertyActionSettings.PROP_STYLE.equals(name) ) {
                return;
            }
            if (name.equals(ElementProperties.PROP_JAVADOC) || 
                name.equals(ElementProperties.PROP_INIT_VALUE) ||
                name.equals(ElementProperties.PROP_EXCEPTIONS) ||
                name.equals(ElementProperties.PROP_BODY))
                return;
            scheduleRefresh();
        }
        
        public void run() {
            refreshKeys(PatternFilter.ALL);
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
