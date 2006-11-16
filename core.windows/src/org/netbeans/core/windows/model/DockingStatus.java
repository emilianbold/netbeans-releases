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

package org.netbeans.core.windows.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/**
 * This class stores the snapshot of the docking status (docked/slided-out) of TopComponents 
 * when switching to or from maximized mode.
 *
 * @author S. Aubrecht
 */
public class DockingStatus {
    
    protected Model model;
    protected List<String> docked = new ArrayList<String>(10);
    protected List<String> slided = new ArrayList<String>(10);
    
    /** Creates a new instance of DockingStatus */
    DockingStatus( Model model ) {
        this.model = model;
    }
    
    /**
     * Remember which TopComponents are docked and which are slided.
     */
    public void mark() {
        Set<ModeImpl> modes = model.getModes();
        for( Iterator<ModeImpl> i=modes.iterator(); i.hasNext(); ) {
            ModeImpl modeImpl = i.next();
            if( modeImpl.getState() == Constants.MODE_STATE_SEPARATED )
                continue;
            
            List<String> views = model.getModeOpenedTopComponentsIDs( modeImpl );
            if( modeImpl.getKind() == Constants.MODE_KIND_VIEW ) {
                docked.addAll( views );
                slided.removeAll( views );
            } else if( modeImpl.getKind() == Constants.MODE_KIND_SLIDING ) {
                docked.removeAll( views );
                slided.addAll( views );
            }
        }
    }
    
    /**
     * @return True if the TopComponent should switch to docked status
     * (Used when switching to/from maximized mode)
     */
    public boolean shouldDock( String tcID ) {
        return null != tcID && docked.contains( tcID );
    }
    
    /**
     * @return True if the TopComponent should slide-out
     * (Used when switching to/from maximized mode)
     */
    public boolean shouldSlide( String tcID ) {
        return null != tcID && slided.contains( tcID );
    }
    
    /**
     * Adds 'docked' TopComponent (used when the window system loads)
     */
    public void addDocked( String tcID ) {
        if( null != tcID ) {
            docked.add( tcID );
            slided.remove( tcID );
        }
    }
    
    /**
     * Adds 'slided-out' TopComponent (used when the window system loads)
     */
    public void addSlided( String tcID ) {
        if( null != tcID ) {
            slided.add( tcID );
            docked.remove( tcID );
        }
    }
    
    /**
     * (Used when the window system gets stored)
     * @return True if the given TopComponent was docked when its snapshot was taken.
     */
    public boolean isDocked( String tcID ) {
        return null != tcID && docked.contains( tcID );
    }
    
    /**
     * (Used when the window system gets stored)
     * @return True if the given TopComponent was slided when its snapshot was taken.
     */
    public boolean isSlided( String tcID ) {
        return null != tcID && slided.contains( tcID );
    }
    
    /**
     * Reset to defaults
     */
    void clear() {
        docked.clear();
        slided.clear();
    }
}
