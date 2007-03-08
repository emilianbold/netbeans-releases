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

package org.netbeans.spi.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.palette.ui.PalettePanel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * A class that listens to changes to the set of opened TopComponents and to the
 * set of activated Nodes to show/hide the palette window when a TopComponent that
 * supports the palette is activated/deactivated.
 *
 * @author S. Aubrecht
 */
final class PaletteSwitch implements Runnable, LookupListener {
    
    static final String PROP_PALETTE_CONTENTS = "component_palette_contents"; //NOI18N
    
    private static PaletteSwitch theInstance;
    
    private PropertyChangeListener registryListener;
    
    private PropertyChangeSupport propertySupport;
    
    private PaletteController currentPalette;
    private boolean isGroupOpen = false;
    private Lookup.Result lookupRes;
    
    /** Creates a new instance of PaletteSwitcher */
    private PaletteSwitch() {
        
        propertySupport = new PropertyChangeSupport( this );
        currentPalette = findPalette();
    }
    
    public synchronized static PaletteSwitch getDefault() {
        if( null == theInstance ) {
            theInstance = new PaletteSwitch();
        }
        return theInstance;
    }
    
    public void startListening() {
        synchronized( theInstance ) {
            if( null == registryListener ) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener( registryListener );
                switchLookupListener();
                run();
            }
        }
    }
    
    public void stopListening() {
        synchronized( theInstance ) {
            if( null != registryListener ) {
                TopComponent.getRegistry().removePropertyChangeListener( registryListener );
                registryListener = null;
                currentPalette = null;
            }
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }
    
    public PaletteController getCurrentPalette() {
        return currentPalette;
    }
    
    public void run() {
        if( !SwingUtilities.isEventDispatchThread() ) {
            SwingUtilities.invokeLater( this );
            return;
        }
        final PaletteController oldPalette = currentPalette;
        currentPalette = findPalette();

        showHidePaletteTopComponent( oldPalette, currentPalette );

        propertySupport.firePropertyChange( PROP_PALETTE_CONTENTS, oldPalette, currentPalette );
    }
    
    private PaletteController findPalette() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        
        PaletteController palette;
        TopComponent activeTc = registry.getActivated();
        palette = getPaletteFromTopComponent( activeTc, true );
        
        ArrayList<PaletteController> availablePalettes = new ArrayList<PaletteController>(3);
        if( null == palette ) {
            Set openedTcs = registry.getOpened();
            for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
                TopComponent tc = (TopComponent)i.next();
                
                palette = getPaletteFromTopComponent( tc, true );
                if( null != palette ) {
                    availablePalettes.add( palette );
                }
            }
            if( null != currentPalette && (availablePalettes.contains( currentPalette ) || isPaletteMaximized()) )
                palette = currentPalette;
            else if( availablePalettes.size() > 0 )
                palette = availablePalettes.get( 0 );
        }
        return palette;
    }
    
    private boolean isPaletteMaximized() {
        boolean isMaximized = true;
        boolean currentPaletteStillAvailable = false;
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set openedTcs = registry.getOpened();
        for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
            TopComponent tc = (TopComponent)i.next();

            if( tc.isShowing() && !(tc instanceof PaletteTopComponent) ) {
                //other window(s) than the Palette are showing
                isMaximized = false;
                break;
            }
            if( !currentPaletteStillAvailable ) {
                //check whether the window with the current palette controller wasn't closed
                PaletteController palette = getPaletteFromTopComponent( tc, false );
                if( null != palette && palette == currentPalette ) {
                    currentPaletteStillAvailable = true;
                }
            }
        }
        return isMaximized && currentPaletteStillAvailable;
    }
    
    PaletteController getPaletteFromTopComponent( TopComponent tc, boolean mustBeShowing ) {
        if( null == tc || (!tc.isShowing() && mustBeShowing) )
            return null;
        
        PaletteController pc = (PaletteController)tc.getLookup().lookup( PaletteController.class );
        if( null == pc && WindowManager.getDefault().isEditorTopComponent( tc ) ) {
            //check if there's any palette assigned to TopComponent's mime type
            Node[] activeNodes = tc.getActivatedNodes();
            if( null != activeNodes && activeNodes.length > 0 ) {
                DataObject dob = activeNodes[0].getLookup().lookup( DataObject.class );
                if( null != dob ) {
                    while( dob instanceof DataShadow ) {
                        dob = ((DataShadow)dob).getOriginal();
                    }
                    FileObject fo = dob.getPrimaryFile();
                    if( !fo.isVirtual() ) {
                        String mimeType = fo.getMIMEType();
                        pc = getPaletteFromMimeType( mimeType );
                    }
                }
            }
        }
        return pc;
    }
    
    /** 
     * Finds appropriate PaletteController for given mime type.
     *
     * @param mimeType Mime type to check for associated palette content.
     * 
     * @return PaletteController that is associated with the given mime type and that should
     * be displayed in the Common Palette when an editor window with the given mime type is activated.
     * @since 1.10
     */
    PaletteController getPaletteFromMimeType( String mimeType ) {
        MimePath path = MimePath.get( mimeType );
        Lookup lkp = MimeLookup.getLookup( path );
        return lkp.lookup( PaletteController.class );
    }
    
    private void showHidePaletteTopComponent( PaletteController prevPalette, PaletteController newPalette ) {
        if( prevPalette == newPalette && null != newPalette )
            return;
        
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup( "commonpalette" ); // NOI18N
        if( null == group )
            return; // group not found (should not happen)
        
        if( null == prevPalette && null != newPalette ) {
            group.open();
            isGroupOpen = true;
        } else if( ((null != prevPalette && null == newPalette) 
            || (null == prevPalette && null == newPalette))
            && isGroupOpen ) {
            PalettePanel.getDefault().setContent( null, null, null );
            group.close();
            isGroupOpen = false;
        }
    }
    
    /**
     * multiview components do not fire events when switching their inner tabs
     * so we have to listen to changes in lookup contents
     */
    private void switchLookupListener() {
        TopComponent active = TopComponent.getRegistry().getActivated();
        if( null != lookupRes ) {
            lookupRes.removeLookupListener( PaletteSwitch.this );
            lookupRes = null;
        }
        if( null != active ) {
            lookupRes = active.getLookup().lookup( new Lookup.Template<PaletteController>( PaletteController.class ) );
            lookupRes.addLookupListener( PaletteSwitch.this );
            lookupRes.allItems();
        }
    }
    
    private PropertyChangeListener createRegistryListener() {
        return new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                if( TopComponent.Registry.PROP_CURRENT_NODES.equals( evt.getPropertyName() )
                    || TopComponent.Registry.PROP_OPENED.equals( evt.getPropertyName() )
                    || TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {

                    if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {
                        //switch lookup listener for the activated TC
                        switchLookupListener();
                    }
                    run();
                }
            }
        };
    }

    public void resultChanged(LookupEvent ev) {
        run();
    }
}
