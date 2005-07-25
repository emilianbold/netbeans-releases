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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;


/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public abstract class LookupSensitiveAction extends BasicAction implements LookupListener {
    
    private Lookup lookup;    
    private Class[] watch;
    private Lookup.Result results[];
    private boolean needsRefresh = true;
    private boolean initialized = false;
        
    private static boolean refreshing = false;
    
    /** Formats the name with following 
     */    
    /*
    public LookupSensitiveAction(String iconResource, Lookup lookup) {
        this( iconResource == null ? null : new ImageIcon( Utilities.loadImage( iconResource ) ), lookup );
    }
    */
        
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public LookupSensitiveAction(Icon icon, Lookup lookup, Class[] watch ) {
        super( null, icon );
        if (lookup == null) {
            lookup = Utilities.actionsGlobalContext();
        }
        this.lookup = lookup;
        this.watch = watch;
    }
    
    private void init () {
        if (initialized) {
            return ;
        }
        assert EventQueue.isDispatchThread () : "Cannot be called outside EQ!";
        this.results = new Lookup.Result[watch.length];
        // Needs to listen on changes in results
        for ( int i = 0; i < watch.length; i++ ) {
            results[i] = lookup.lookup( new Lookup.Template( watch[i] ) );
            results[i].allItems();
            LookupListener resultListener = (LookupListener)WeakListeners.create( LookupListener.class, this, results[i] );
            results[i].addLookupListener( resultListener ); 
        }
        
    }
    
    /** Needs to override getValue in order to force refresh
     */
    public Object getValue( String key ) {
        init ();
        if ( needsRefresh ) {
            doRefresh();
        }
        return super.getValue( key );
    }
    
    /** Needs to override isEnabled in order to force refresh
     */
    public boolean isEnabled() {
        init ();
        if ( needsRefresh ) {
            doRefresh();
        }
        return super.isEnabled();
    }
    
    public final void actionPerformed( ActionEvent e ) {
        init ();
        actionPerformed( lookup );
    }
    
    protected final Lookup getLookup() {
        return lookup;
    }
        
    private void doRefresh() {
        refreshing = true;
        refresh( lookup );
        refreshing = false;
        needsRefresh = false;
    }
    
    // Abstract methods --------------------------------------------------------
    
    /** Called when the action is performed
     */
    protected abstract void actionPerformed( Lookup context );
           
    /** Place where to change properties (enablement/name) when
     *  the set of current projects changes.
     */
    protected abstract void refresh( Lookup context );
    
    // Implementation of LookupListener ----------------------------------------
    
    public void resultChanged( LookupEvent e ) {
        if ( refreshing ) {
            return;
        }        
        else if ( getPropertyChangeListeners().length == 0 ) {        
            needsRefresh = true;
        }
        else {
            doRefresh();
        }
    }
    
}