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

package org.netbeans.spi.palette;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.modules.palette.DefaultModel;
import org.netbeans.modules.palette.DefaultSettings;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.RootNode;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.PaletteSwitch;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;


/**
 * <p>PaletteFactory creating new PaletteController instances.</p>
 *
 * <p><b>Important: This SPI is still under development.</b></p>
 *
 * @author S. Aubrecht
 */
public final class PaletteFactory {
    
    /** 
     * Do not allow instances of this class.
     */
    private PaletteFactory() {
    }

    /**
     * Create a new palette from the given folder.
     *
     * @param rootFolderName Name of palette's root folder, its sub-folders are categories.
     * @param customActions Import actions for palette customizer.
     */
    public static PaletteController createPalette( String rootFolderName, PaletteActions customActions ) 
            throws IOException {
        return createPalette( rootFolderName, customActions, null, null );
    }
    
    
    /**
     * Create a new palette from the given folder.
     *
     * @param rootFolderName Name of palette's root folder, its sub-folders are categories.
     * @param customActions Import actions for palette customizer.
     * @param filter A filter that can dynamically hide some categories and items.
     * @param customizer Add custom DataFlavors to the Transferable of items being dragged from
     * the palette to editor window. Can be null to use the default PaletteController.ITEM_DATA_FLAVOR.
     */
    public static PaletteController createPalette( String rootFolderName, 
                                                   PaletteActions customActions,
                                                   PaletteFilter filter,
                                                   TransferableCustomizer customizer ) 
            throws IOException {
        
        assert null != rootFolderName;
        
        DataFolder paletteFolder = DataFolder.findFolder( getPaletteFolder( rootFolderName ) );
        return createPalette( paletteFolder.getNodeDelegate(), customActions, filter, customizer );
    }
    
    
    /**
     * Create a new palette from the given root Node.
     *
     * @param paletteRoot Palette's root <code>Node</code>, its children are categories, 
     * their children are palette items.
     * @param customActions Import actions for palette customizer.
     */
    public static PaletteController createPalette( Node paletteRoot, PaletteActions customActions )
            throws IOException {
        return createPalette( paletteRoot, customActions, null, null );
    }
    
    /**
     * Create a new palette from the given root Node.
     *
     * @param paletteRoot Palette's root <code>Node</code>, its children are categories, 
     * their children are palette items.
     * @param customActions Import actions for palette customizer.
     * @param filter A filter that can dynamically hide some categories and items. Can be null.
     * @param customizer Add custom DataFlavors to the Transferable of items being dragged from
     * the palette to editor window. Can be null to use the default PaletteController.ITEM_DATA_FLAVOR.
     * Can be null.
     */
    public static PaletteController createPalette( Node paletteRoot, 
                                                   PaletteActions customActions,
                                                   PaletteFilter filter,
                                                   TransferableCustomizer customizer )
            throws IOException {
        
        assert null != paletteRoot;
        assert null != customActions;
        
        ArrayList lookupObjects = new ArrayList(3);
        lookupObjects.add( customActions );
        if( null != filter )
            lookupObjects.add( filter );
        if( null != customizer )
            lookupObjects.add( customizer );
        
        RootNode root = new RootNode( paletteRoot, Lookups.fixed( lookupObjects.toArray() ) );
        Model model = createModel( root );
        Settings settings = new DefaultSettings( model );
        
        PaletteSwitch.getDefault().startListening();
        
        return new PaletteController( model, settings );
    }
    
    private static Model createModel( RootNode root ) {
        return new DefaultModel( root );
    }
    
    private static FileObject getPaletteFolder( String folderName ) throws IOException {
        FileObject paletteFolder = null;
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        paletteFolder = fs.findResource( folderName );
        if (paletteFolder == null) { // not found, cannot continue
            throw new FileNotFoundException( folderName );
        }
        return paletteFolder;
    }
}
