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

package org.netbeans.modules.beans.beaninfo;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.*;

/** Implements children for basic source code patterns
 * 
 * @author Petr Hrebejk
 */
public class BiChildren extends Children.Keys {

    /** The class element its subelements are represented */
    protected ClassElement        classElement;

    /** Main storage of nodes */
    private Collection[]          cpl;

    /** Object for finding patterns in class */
    private BiAnalyser       biAnalyser;


    // Constructors -----------------------------------------------------------------------

    /** Create pattern children. The children are initilay unfiltered.
     * @param elemrent the atteached class. For this class we recognize the patterns 
     */ 

    public BiChildren ( BiAnalyser biAnalyser, Class[] keys ) {
        super();
        this.biAnalyser = biAnalyser;
        setKeys( keys );
    }

    /** Called when the preparetion of nodes is needed
     */
    protected void addNotify() {
        //refreshAllKeys ();
    }

    /** Called when all children are garbage collected */
    protected void removeNotify() {
        setKeys( java.util.Collections.EMPTY_SET );
    }

    /** Gets the pattern analyser which manages the patterns */

    BiAnalyser getBiAnalyser( ) {
        return biAnalyser;
    }

    // Children.keys implementation -------------------------------------------------------

    /** Creates nodes for given key.
    */
    protected Node[] createNodes( final Object key ) {
        if ( key == BiFeature.Descriptor.class )
            return createNodesFromFeatures( biAnalyser.getDescriptor() );
        if ( key == BiFeature.Property.class )
            return createNodesFromFeatures( biAnalyser.getProperties() );
        if ( key == BiFeature.IdxProperty.class )
            return createNodesFromFeatures( biAnalyser.getIdxProperties() );
        if ( key == BiFeature.EventSet.class )
            return createNodesFromFeatures( biAnalyser.getEventSets() );
        if ( key == BiFeature.Method.class )
            return createNodesFromFeatures( biAnalyser.getMethods() );


        /*
        if (key instanceof IdxPropertyPattern)
          return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, true) };
        if (key instanceof PropertyPattern) 
          return new Node[] { new PropertyPatternNode((PropertyPattern)key, true) };
        if (key instanceof EventSetPattern)
          return new Node[] { new EventSetPatternNode((EventSetPattern)key, true) };
        */
        // Unknown pattern
        return new Node[0];
    }

    // Utility methods --------------------------------------------------------------------

    Node[] createNodesFromFeatures( Collection col ) {

        Iterator it = col.iterator();

        Node[] nodes = new Node[ col.size() ];

        for ( int i = 0; it.hasNext() && i < nodes.length; i++ )
            nodes[i] = new BiFeatureNode( (BiFeature) it.next(), biAnalyser );

        return nodes;
    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $ 
 */ 