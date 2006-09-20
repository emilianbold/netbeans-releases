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

package org.netbeans.modules.palette;

import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Palette settings to be remembered over IDE restarts.
 * There's an instance of these settings for each palette model instance.
 *
 * @author S. Aubrecht
 */
public final class DefaultSettings implements Settings, ModelListener, CategoryListener, Runnable {
    
    private static final String SETTINGS_ROOT_FOLDER = "PaletteSettings";
    private static final String NODE_ATTR_PREFIX = "psa_";
    
    private static final String NULL_VALUE = "null";
    
    private static final String XML_ROOT = "root";
    private static final String XML_CATEGORY = "category";
    private static final String XML_ITEM = "item";
    
    private static final String XML_ATTR_NAME = "name";
    
    private static final String[] KNOWN_PROPERTIES = new String[] {
        NODE_ATTR_PREFIX + PaletteController.ATTR_ICON_SIZE,
        NODE_ATTR_PREFIX + PaletteController.ATTR_IS_EXPANDED,
        NODE_ATTR_PREFIX + PaletteController.ATTR_IS_VISIBLE,
        NODE_ATTR_PREFIX + PaletteController.ATTR_SHOW_ITEM_NAMES
    };
    
    private static final int ICON_SIZE_ATTR_INDEX = 0;
    private static final int IS_EXPANDED_ATTR_INDEX = 1;
    private static final int IS_VISIBLE_ATTR_INDEX = 2;
    private static final int SHOW_ITEM_NAMES_ATTR_INDEX = 3;
    
    private Model model;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private FileLock settingsFileLock;
    
    private RequestProcessor requestProcessor = new RequestProcessor( "PaletteSettings" ); //NOI18N
    
    private static Logger ERR = Logger.getLogger("org.netbeans.modules.palette"); // NOI18N

    
    public DefaultSettings( Model model ) {
        this.model = model;
        model.addModelListener( this );
        Category[] categories = model.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            categories[i].addCategoryListener( this );
        }
        load();
    }
    

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }

    public boolean isVisible(Item item) {
        Node node = getNode( item.getLookup() );
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }

    public void setVisible(Item item, boolean visible ) {
        Node node = getNode( item.getLookup() );
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }

    public boolean isVisible( Category category ) {
        Node node = getNode( category.getLookup() );
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }

    public void setVisible( Category category, boolean visible ) {
        Node node = getNode( category.getLookup() );
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }
    
    public boolean isNodeVisible( Node node ) {
        return get( node, PaletteController.ATTR_IS_VISIBLE, true );
    }
    
    public void setNodeVisible( Node node, boolean visible ) {
        set( node, PaletteController.ATTR_IS_VISIBLE, visible, true );
    }

    public boolean isExpanded( Category category ) {
        Node node = getNode( category.getLookup() );
        return get( node, PaletteController.ATTR_IS_EXPANDED, false );
    }

    public void setExpanded( Category category, boolean expanded ) {
        Node node = getNode( category.getLookup() );
        set( node, PaletteController.ATTR_IS_EXPANDED, expanded, false );
    }

    public int getIconSize() {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_ICON_SIZE, BeanInfo.ICON_COLOR_16x16 );
    }

    public void setIconSize( int iconSize ) {
        Node node = getNode( model.getRoot() );
        set( node, PaletteController.ATTR_ICON_SIZE, iconSize, BeanInfo.ICON_COLOR_16x16 );
    }

    public void setShowItemNames( boolean showNames ) {
        Node node = getNode( model.getRoot() );
        set( node, PaletteController.ATTR_SHOW_ITEM_NAMES, showNames, true );
    }

    public boolean getShowItemNames() {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_SHOW_ITEM_NAMES, true );
    }
    
    private Node getNode( Lookup lkp ) {
        return (Node)lkp.lookup( Node.class );
    }
    
    private boolean get( Node node, String attrName, boolean defaultValue ) {
        Object value = get( node, attrName, Boolean.valueOf( defaultValue ) );
        return null == value ? defaultValue : Boolean.valueOf( value.toString() ).booleanValue();
    }

    private int get( Node node, String attrName, int defaultValue ) {
        Object value = get( node, attrName, Integer.valueOf( defaultValue ) );
        try {
            if( null != value )
                return Integer.parseInt( value.toString() );
        } catch( NumberFormatException nfE ) {
            //ignore
        }
        return defaultValue;
    }
    
    private Object get( Node node, String attrName, Object defaultValue ) {
        Object res = null;
        if( null != node ) {
            res = node.getValue( NODE_ATTR_PREFIX+attrName );
            if( null == res || NULL_VALUE.equals( res ) ) {
                res = getNodeDefaultValue( node, attrName );
            }
        }
        if( null == res ) {
            res = defaultValue;
        }
        return res;
    }
    
    private Object getNodeDefaultValue( Node node, String attrName ) {
        Object res = node.getValue( attrName );
        if( null == res ) {
            DataObject dobj = (DataObject)node.getCookie( DataObject.class );
            if( null != dobj ) {
                res = dobj.getPrimaryFile().getAttribute( attrName );
            }
        }
        return res;
    }
    
    private void set( Node node, String attrName, boolean newValue, boolean defaultValue ) {
        set( node, attrName, Boolean.valueOf( newValue ), Boolean.valueOf( defaultValue ) );
    }
    
    private void set( Node node, String attrName, int newValue, int defaultValue ) {
        set( node, attrName, Integer.valueOf( newValue ), Integer.valueOf( defaultValue ) );
    }
    
    private void set( Node node, String attrName, Object newValue, Object defaultValue ) {
        if( null == node )
            return;
        Object oldValue = get( node, attrName, defaultValue );
        if( oldValue.equals( newValue ) ) {
            return;
        }
        node.setValue( NODE_ATTR_PREFIX+attrName, newValue );
        requestProcessor.post( this );
        propertySupport.firePropertyChange( attrName, oldValue, newValue );
    }

    public void categoryModified( Category src ) {
        requestProcessor.post( this );
    }
    
    public void run() {
        store();
    }

    public void categoriesRemoved( Category[] removedCategories ) {
        for( int i=0; i<removedCategories.length; i++ ) {
            removedCategories[i].removeCategoryListener( this );
        }
        requestProcessor.post( this );
    }

    public void categoriesAdded( Category[] addedCategories ) {
        for( int i=0; i<addedCategories.length; i++ ) {
            addedCategories[i].addCategoryListener( this );
        }
        requestProcessor.post( this );
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        //not interested
    }

    public void categoriesReordered() {
        //not interested
    }

    private void load() {
        try {
            FileObject fo = findSettingsFile(); 
            if( null == fo || !fo.isValid() || !fo.canRead() )
                return;
            XMLReader reader = XMLUtil.createXMLReader();
            SettingsHandler handler = new SettingsHandler();
            reader.setContentHandler(handler);
            InputStream stream = null;
            try {
                stream = fo.getInputStream();
            } catch( FileNotFoundException fnfE ) {
            }
            if( null == stream )
                return;
            InputSource input = new InputSource( stream );
            
            reader.parse(input);
            stream.close();
            
        } catch( SAXException saxE ) {
            ERR.log( Level.INFO, Utils.getBundleString("Err_LoadSettings"), saxE ); //NOI18N
        } catch( IOException ioE ) {
            ERR.log( Level.INFO, Utils.getBundleString("Err_LoadSettings"), ioE ); //NOI18N
        }
    }
    
    private void store() {
        Node root = (Node)model.getRoot().lookup( Node.class );
        assert null != root;

        try {
            FileObject fo = findSettingsFile(); 
            if( null == fo )
                fo = createSettingsFile();
            if( null == fo )
                return;
            synchronized( this ) {
                if( null == settingsFileLock ) {
                    try {
                        settingsFileLock = fo.lock();
                    } catch( FileAlreadyLockedException falE ) {
                        //settings are being stored already at the moment
                        return;
                    }
                } else {
                    //settings are being stored already at the moment
                    return;
                }
            }
            PrintWriter writer = new PrintWriter( fo.getOutputStream( settingsFileLock ) );
            writer.print( "<root " );
            printAttributes( writer, root );
            writer.println( '>' );
            
            Node[] categories = root.getChildren().getNodes();
            for( int i=0; i<categories.length; i++ ) {
                printCategory( writer, categories[i] );
            }
            
            writer.println( "</root>" );
            writer.close();
        } catch( IOException ioE ) {
            ERR.log( Level.INFO, Utils.getBundleString("Err_StoreSettings"), ioE ); //NOI18N
        } finally {
            if( null != settingsFileLock )
                settingsFileLock.releaseLock();
            settingsFileLock = null;
        }
    }
    
    private void printCategory( PrintWriter writer, Node category ) {
        writer.print( "\t<category " );
        printAttributes( writer, category );
        writer.println( '>' );
        
        Node[] items = category.getChildren().getNodes();
        for( int i=0; i<items.length; i++ ) {
            printItem( writer, items[i] );
        }
        writer.println( "\t</category>" );
    }
    
    private void printItem( PrintWriter writer, Node item ) {
        boolean hasAttributes = false;
        for( int i=0; i<KNOWN_PROPERTIES.length; i++ ) {
            if( null != item.getValue( KNOWN_PROPERTIES[i] ) ) {
                hasAttributes = true;
                break;
            }
        }
        if( !hasAttributes )
            return; //nothing to store
        
        writer.print( "\t\t<item " );
        printAttributes( writer, item);
        
        writer.println( " />" );
    }
    
    private void printAttributes( PrintWriter writer, Node node ) {
        writer.print( " name=\"" );
        writer.print( node.getName() );
        writer.print( "\" " );
        
        for( int i=0; i<KNOWN_PROPERTIES.length; i++ ) {
            Object value = node.getValue( KNOWN_PROPERTIES[i] );
            if( null != value && !NULL_VALUE.equals( value ) ) {
                writer.print( KNOWN_PROPERTIES[i] );
                writer.print( "=\"" );
                writer.print( value.toString() );
                writer.print( "\" " );
            }
        }
    }
    
    private FileObject findSettingsFile() throws IOException {
        FileObject settingsRoot = findOrCreateSettingsFolder();
        return settingsRoot.getFileObject( model.getName(), "settings" );
    }

    private FileObject createSettingsFile() throws IOException {
        FileObject settingsRoot = findOrCreateSettingsFolder();
        String modelName = model.getName();
        FileObject res = settingsRoot.getFileObject( modelName, "settings" );
        if( null == res ) {
            try {
                res = settingsRoot.createData( modelName, "settings" );
            } catch( FileNotFoundException fnfE ) {
                return null;
            }
        }
        return res;
    }
    
    private FileObject findOrCreateSettingsFolder() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject settingsRoot = fs.findResource( SETTINGS_ROOT_FOLDER );
        if( null == settingsRoot ) {
            settingsRoot = fs.getRoot().createFolder( SETTINGS_ROOT_FOLDER );
        }
        return settingsRoot;
    }

    public int getItemWidth() {
        Node node = getNode( model.getRoot() );
        return get( node, PaletteController.ATTR_ITEM_WIDTH, -1 );
    }

    public void reset() {
        Node root = (Node)model.getRoot().lookup( Node.class );
        clearAttributes( root );
        Category[] categories = model.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            Node cat = (Node)categories[i].getLookup().lookup( Node.class );
            clearAttributes( cat );
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                Node it = (Node)items[j].getLookup().lookup( Node.class );
                clearAttributes( it );
            }
        }
        requestProcessor.post( this );
    }

    private void clearAttributes( Node node ) {
        for( int i=0; i<KNOWN_PROPERTIES.length; i++ ) {
            node.setValue( KNOWN_PROPERTIES[i], NULL_VALUE );
        }
    }

    private class SettingsHandler extends DefaultHandler {
        private Node currentCategory = null;
        private Node rootNode;
        
        public void startElement(String uri, String localName, String qName, Attributes attributes) 
            throws SAXException {

            if( XML_ROOT.equals( qName ) ) {
                
                Node root = getRoot();
                extractAttribute( root, SHOW_ITEM_NAMES_ATTR_INDEX, attributes );
                extractAttribute( root, ICON_SIZE_ATTR_INDEX, attributes );
                
            } else if( XML_CATEGORY.equals( qName ) ) {
                
                String catName = attributes.getValue( XML_ATTR_NAME );
                if( null != catName ) {
                    Node root = getRoot();
                    Node category = root.getChildren().findChild( catName );
                    if( null != category ) {
                        currentCategory = category;
                        extractAttribute( category, IS_EXPANDED_ATTR_INDEX, attributes );
                        extractAttribute( category, IS_VISIBLE_ATTR_INDEX, attributes );
                    }
                }
                
            } else if( XML_ITEM.equals( qName ) ) {
                
                String itemName = attributes.getValue( XML_ATTR_NAME );
                if( null != itemName && null != currentCategory ) {
                    Node item = currentCategory.getChildren().findChild( itemName );
                    if( null != item ) {
                        extractAttribute( item, IS_VISIBLE_ATTR_INDEX, attributes );
                    }
                }
            }
        }
            
        private void extractAttribute( Node node, int attrIndex, Attributes attributes ) {
            if( null == node ) {
                return;
            }
            String attrName = KNOWN_PROPERTIES[attrIndex];
            String value = attributes.getValue( attrName );
            if( null != value ) {
                node.setValue( attrName, value );
            }
        }
        
        private Node getRoot() {
            if( null == rootNode ) {
                rootNode = (Node)model.getRoot().lookup( Node.class );
            }
            return rootNode;
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if( XML_CATEGORY.equals( qName ) ) {
                currentCategory = null;
            }
        }
    }
}
