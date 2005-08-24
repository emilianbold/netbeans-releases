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

package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;

/**
 *
 * @author Petr Hrebejk
 * @author Andrei Badea
 */
public class ClassPathSupport {
    
    public final static String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private Set /*<String>*/ wellKnownPaths;
    private String libraryPrefix;
    private String librarySuffix;
    private String antArtifactPrefix;
        
    /** Creates a new instance of ClassPathSupport */
    public  ClassPathSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              AntProjectHelper antProjectHelper,
                              String wellKnownPaths[],
                              String libraryPrefix,
                              String librarySuffix,
                              String antArtifactPrefix ) {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.wellKnownPaths = wellKnownPaths == null ? null : new HashSet( Arrays.asList( wellKnownPaths ) );
        this.libraryPrefix = libraryPrefix;
        this.librarySuffix = librarySuffix;
        this.antArtifactPrefix = antArtifactPrefix;
    }
    
    /** Creates list of <CODE>Items</CODE> from given property.
     */    
    public Iterator /*<Item>*/ itemsIterator( String propertyValue, String includedLibrariesElement ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue, includedLibrariesElement ).iterator();
    }
    
    public List /*<Item>*/ itemsList( String propertyValue, String includedLibrariesElement ) {    
        
        // Get the list of items which are included in deployment
        List includedItems = ( includedLibrariesElement != null ) ? 
            getIncludedLibraries( antProjectHelper, includedLibrariesElement ) : 
            Collections.EMPTY_LIST;
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List items = new ArrayList( pe.length );        
        for( int i = 0; i < pe.length; i++ ) {
            String property = EjbJarProjectProperties.getAntPropertyName( pe[i] );
            Item item;

            // First try to find out whether the item is well known classpath
            if ( isWellKnownPath( pe[i] ) ) {
                // Some well know classpath
                item = Item.create( pe[i], false );
            } 
            else if ( isLibrary( pe[i] ) ) {
                //Library from library manager
                String libraryName = pe[i].substring( libraryPrefix.length(), pe[i].lastIndexOf('.') ); //NOI18N
                Library library = LibraryManager.getDefault().getLibrary( libraryName );
                if ( library == null ) {
                    item = Item.createBroken( Item.TYPE_LIBRARY, pe[i], includedItems.contains( property ) );
                }
                else {
                    item = Item.create( library, pe[i], includedItems.contains( property ) );
                }               
            } 
            else if ( isAntArtifact( pe[i] ) ) {
                // Ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);
                if ( ret[0] == null || ret[1] == null ) {
                    item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i], includedItems.contains ( property ) );
                }
                else {
                    //fix of issue #55391
                    AntArtifact artifact = (AntArtifact)ret[0];
                    URI uri = (URI)ret[1];
                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
                    File artifactFile = new File (artifact.getScriptLocation().toURI().resolve(uri).normalize());
                    if (usedFile.equals(artifactFile)) {
                        item = Item.create( artifact, uri, pe[i], includedItems.contains ( property ) );
                    }
                    else {
                        item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i], includedItems.contains ( property ) );
                    }
                }
            } else {
                // Standalone jar or property
                String eval = evaluator.evaluate( pe[i] );
                File f = null;
                if (eval != null) {
                    f = antProjectHelper.resolveFile( eval );
                }                    
                
                if ( f == null || !f.exists() ) {
                    item = Item.createBroken( Item.TYPE_JAR, pe[i], includedItems.contains ( property ) );
                }
                else {
                    item = Item.create( f, pe[i], includedItems.contains ( property ) );
                }
            }
            
            items.add( item );
           
        }

        return items;
        
    }
      
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     * !! This method may add <included-library> items to project.xml !!
     */
    public String[] encodeToStrings( Iterator /*<Item>*/ classpath, String includedLibrariesElement ) {
        
        ArrayList result = new ArrayList();
        ArrayList includedLibraries = new ArrayList();
        
        List cp = new LinkedList();
        
        while( classpath.hasNext() ) {

            Item item = (Item)classpath.next();
            cp.add(item);
            String reference = null;
            
            switch( item.getType() ) {

                case Item.TYPE_JAR:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    if (reference == null) {
                        // New file
                        File file = item.getFile();
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReference(file, null);
                        item.setReference(reference);
                    }
                    break;
                case Item.TYPE_LIBRARY:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }                    
                    Library library = item.getLibrary();                                       
                    if (reference == null) {
                        if ( library == null ) {
                            break;
                        }
                        reference = getLibraryReference( item );
                        item.setReference(reference);
                    }
                    break;    
                case Item.TYPE_ARTIFACT:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    AntArtifact artifact = (AntArtifact)item.getArtifact();                                       
                    if ( reference == null) {
                        if ( artifact == null ) {
                            break;
                        }
                        reference = referenceHelper.addReference( item.getArtifact(), item.getArtifactURI());
                        item.setReference(reference);
                    }
                    break;
                case Item.TYPE_CLASSPATH:
                    reference = item.getReference();
                    break;
            }
            
            if ( reference != null ) {
                result.add( reference );
                
                // Add the item to the list of items included in deployment
                if ( includedLibrariesElement != null && item.isIncludedInDeployment() ) {
                    includedLibraries.add( EjbJarProjectProperties.getAntPropertyName( reference ) );
                }
            }
            
        }
        
        if ( includedLibrariesElement != null )
            putIncludedLibraries( includedLibraries, cp, antProjectHelper, includedLibrariesElement );

        String[] items = new String[ result.size() ];
        for( int i = 0; i < result.size(); i++) {
            if ( i < result.size() - 1 ) {
                items[i] = result.get( i ) + ":";
            }
            else  {       
                items[i] = (String)result.get( i );    //NOI18N
            }
        }
        
        return items;
    }
    
    public String getLibraryReference( Item item ) {
        if ( item.getType() != Item.TYPE_LIBRARY ) {
            throw new IllegalArgumentException( "Item must be of type LIBRARY" );
        }
        return libraryPrefix + item.getLibrary().getName() + librarySuffix;        
    }
    
    // Private methods ---------------------------------------------------------

    private boolean isWellKnownPath( String property ) {
        return wellKnownPaths == null ? false : wellKnownPaths.contains( property );
    }
    
    private boolean isAntArtifact( String property ) {        
        return antArtifactPrefix == null ? false : property.startsWith( antArtifactPrefix );
    }
    
    private boolean isLibrary( String property ) {
        if ( libraryPrefix != null && property.startsWith( libraryPrefix ) ) {
            return librarySuffix == null ? true : property.endsWith( librarySuffix );
        }
        else {
            return false;
        }
        
    }
    
    // Private static methods --------------------------------------------------
    
    /** 
     * Returns a list with the classpath items which are to be included 
     * in deployment.
     */
    private static List /*<String>*/ getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List libraries = new ArrayList( libs.getLength() );
        for ( int i = 0; i < libs.getLength(); i++ ) {
            Element item = (Element)libs.item( i );
            libraries.add( findText( item ));
        }
        return libraries;
    }
    
    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries( List /*<String>*/ libraries, List /*<Item>*/ classpath, AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert libraries != null;
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        for (Iterator i = libraries.iterator(); i.hasNext();) {
            String libraryName = (String)i.next();
            //find a correcponding classpath item for the library
            for(int idx = 0; idx < classpath.size(); idx++ ) {
                ClassPathSupport.Item item = (ClassPathSupport.Item)classpath.get(idx);
                String libraryPropName = "${" + libraryName + "}";
                if(libraryPropName.equals(item.getReference()))
                    data.appendChild(createLibraryElement(doc, libraryName, item, includedLibrariesElement));
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(Document doc, String pathItem, Item item, String includedLibrariesElement ) {
        Element libraryElement = doc.createElementNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        ArrayList files = new ArrayList ();
        ArrayList dirs = new ArrayList ();
        EjbJarProjectProperties.getFilesForItem(item, files, dirs);
        if (files.size() > 0) {
            libraryElement.setAttribute(ATTR_FILES, "" + files.size());
        }
        if (dirs.size() > 0) {
            libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size());
        }
        
        libraryElement.appendChild( doc.createTextNode( pathItem ) );
        return libraryElement;
    }
       
    /**
     * Extracts <b>the first</b> nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    private static String findText( Element parent ) {
        NodeList l = parent.getChildNodes();
        for ( int i = 0; i < l.getLength(); i++ ) {
            if ( l.item(i).getNodeType() == Node.TEXT_NODE ) {
                Text text = (Text)l.item( i );
                return text.getNodeValue();
            }
        }
        return null;
    }
        
    // Innerclasses ------------------------------------------------------------
    
    /** Item of the classpath.
     */    
    public static class Item {
        
        // Types of the classpath elements
        public static final int TYPE_JAR = 0;
        public static final int TYPE_LIBRARY = 1;
        public static final int TYPE_ARTIFACT = 2;
        public static final int TYPE_CLASSPATH = 3;

        // Reference to a broken object
        private static final String BROKEN = "BrokenReference"; // NOI18N
        
        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        private boolean includedInDeployment;
        private String raw;
        
        private Item( int type, Object object, String property, boolean included, String raw ) {
            this.type = type;
            this.object = object;
            this.property = property;
            this.includedInDeployment = included;
            this.raw = raw;
        }
        
        private Item( int type, Object object, URI artifactURI, String property, boolean included ) {
            this( type, object, property, included,null );
            this.artifactURI = artifactURI;
        }
              
        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property, boolean included ) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
            String libraryName = library.getName();
            return new Item( TYPE_LIBRARY, library, property, included, EjbJarProjectProperties.LIBRARY_PREFIX + libraryName + EjbJarProjectProperties.LIBRARY_SUFFIX);
        }
        
        public static Item create( AntArtifact artifact, URI artifactURI, String property, boolean included ) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item( TYPE_ARTIFACT, artifact, artifactURI, property, included );
        }
        
        public static Item create( File file, String property, boolean included ) {
            if ( file == null ) {
                throw new IllegalArgumentException( "file must not be null" ); // NOI18N
            }
            return new Item( TYPE_JAR, file, property, included, null );
        }
        
        public static Item create( String property, boolean included ) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
            }
            return new Item ( TYPE_CLASSPATH, null, property, included, null );
        }
        
        public static Item createBroken( int type, String property, boolean included ) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            return new Item( type, BROKEN, property, included, null );
        }
        
        // Instance methods ----------------------------------------------------
        
        public String getRaw() {
            return raw;
        }
        
        public int getType() {
            return type;
        }
        
        public Library getLibrary() {
            if ( getType() != TYPE_LIBRARY ) {
                throw new IllegalArgumentException( "Item is not of required type - LIBRARY" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (Library)object;
        }
        
        public File getFile() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (File)object;
        }
        
        public AntArtifact getArtifact() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (AntArtifact)object;
        }
        
        public URI getArtifactURI() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            return artifactURI;
        }
        
        
        public String getReference() {
            return property;
        }
        
        public void setReference(String property) {
            this.property = property;
        }

        public boolean isIncludedInDeployment() {
            boolean result = includedInDeployment;
            if (getType() == TYPE_JAR) {
                // at the moment we can't include folders in deployment
//                FileObject fo = FileUtil.toFileObject(getFile());
//                if (fo == null || fo.isFolder())
//                    return false;
            }
            return includedInDeployment;
        }
        
        public void setIncludedInDeployment(boolean includedInDeployment) {
            this.includedInDeployment = includedInDeployment;
        }
        
        public boolean isBroken() {
            return object == BROKEN;
        }
        
        public String toString() {
            return "artifactURI=" + artifactURI
                    + ", type=" + type 
                    + ", property=" + property
                    + ", includedInDeployment=" + includedInDeployment
                    + ", raw=" + raw
                    + ", object=" + object;
        }
        
        public int hashCode() {
        
            int hash = getType();

            if ( object == BROKEN ) {
                return BROKEN.hashCode();
            }
            
            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    hash += getArtifact().getType().hashCode();                
                    hash += getArtifact().getScriptLocation().hashCode();
                    hash += getArtifactURI().hashCode();
                    break;
                case TYPE_CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash += object.hashCode();
            }

            return hash;
        }
    
        public boolean equals( Object itemObject ) {

            if ( !( itemObject instanceof Item ) ) {
                return false;
            }
            
            Item item = (Item)itemObject;

            if ( getType() != item.getType() ) {
                return false;
            }
            
            if ( isBroken() != item.isBroken() ) {
                return false;
            }
            
            if ( isBroken() ) {
                return getReference().equals( item.getReference() );
            }

            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    if ( getArtifact().getType() != item.getArtifact().getType() ) {
                        return false;
                    }

                    if ( !getArtifact().getScriptLocation().equals( item.getArtifact().getScriptLocation() ) ) {
                        return false;
                    }

                    if ( !getArtifactURI().equals( item.getArtifactURI() ) ) {
                        return false;
                    }
                    return true;
                case TYPE_CLASSPATH:
                    return property.equals( item.property );
                default:
                    return this.object.equals( item.object );
            }

        }
                
    }
            
}

