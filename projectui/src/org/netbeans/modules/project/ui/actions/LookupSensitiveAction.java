/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/** Action sensitive to current project
 *
 * @author Pet Hrebejk
 */
public abstract class LookupSensitiveAction extends BasicAction implements LookupListener {
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions"); // NOI18N
    
    private Lookup lookup;
    private Class<?>[] watch;
    private Lookup.Result results[];
    private boolean needsRefresh = true;
    private boolean initialized = false;
        
    private boolean refreshing = false;
    
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
            results[i] = lookup.lookupResult(watch[i]);
            results[i].allItems();
            LookupListener resultListener = (LookupListener)WeakListeners.create( LookupListener.class, this, results[i] );
            results[i].addLookupListener( resultListener ); 
        }
        initialized = true;
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
        
        if (UILOG.isLoggable(Level.FINE)) {
            LogRecord r;
            boolean isKey;
            if (e.getSource() instanceof JMenuItem) {
                isKey = false;
            } else if (e.getSource() instanceof JButton) {
                isKey = false;
            } else {
                isKey = true;
            }
            
            if (!isKey) {
                r = new LogRecord(Level.FINE, "UI_ACTION_BUTTON_PRESS"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(LookupSensitiveAction.class));
                r.setParameters(new Object[] { 
                    e.getSource(), 
                    e.getSource().getClass().getName(), 
                    this, 
                    getClass().getName(), 
                    getValue(NAME) 
                });
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }
        
        actionPerformed( lookup );
    }
    
    protected final Lookup getLookup() {
        return lookup;
    }
        
    private void doRefresh() {
        refreshing = true;
        try {
            refresh( lookup );
        } finally {
            refreshing = false;
        }
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