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

package org.netbeans.modules.java.j2seproject.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author Petr Hrebejk
 */
public class ClassPathSupport {
                
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
    public Iterator /*<Item>*/ itemsIterator( String propertyValue ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue ).iterator();
    }
    
    public List /*<Item>*/ itemsList( String propertyValue ) {    
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List items = new ArrayList( pe.length );        
        for( int i = 0; i < pe.length; i++ ) {
            Item item;

            // First try to find out whether the item is well known classpath
            if ( isWellKnownPath( pe[i] ) ) {
                // Some well know classpath
                item = Item.create( pe[i] );
            } 
            else if ( isLibrary( pe[i] ) ) {
                //Library from library manager
                String libraryName = pe[i].substring( libraryPrefix.length(), pe[i].lastIndexOf('.') ); //NOI18N
                Library library = LibraryManager.getDefault().getLibrary( libraryName );
                item = Item.create( library, pe[i] );
            } 
            else if ( isAntArtifact( pe[i] ) ) {
                // Ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);
                item = Item.create( (AntArtifact)ret[0], (URI)ret[1], pe[i] );
            } else {
                // Standalone jar or property
                String eval = evaluator.evaluate( pe[i] );
                File f = null;
                if (eval != null) {
                    f = antProjectHelper.resolveFile( eval );
                }                    
                item = Item.create( f, pe[i] );
            }
            
            items.add( item );
           
        }

        return items;
        
    }
    
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings( Iterator /*<Item>*/ classpath ) {
        
        ArrayList result = new ArrayList();
        
        while( classpath.hasNext() ) {

            Item item = (Item)classpath.next();
            String reference = null;
            
            switch( item.getType() ) {

                case Item.TYPE_JAR:
                    reference = item.getReference();
                    if (reference == null) {
                        // New file
                        File file = item.getFile();
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReference(file, null);
                    }
                    break;
                case Item.TYPE_LIBRARY:
                    Library library = item.getLibrary();                   
                    reference = item.getReference();                    
                    if (reference == null) {
                        if ( library == null ) {
                            break;
                        }
                        reference = getLibraryReference( item );
                    }
                    break;    
                case Item.TYPE_ARTIFACT:
                    reference = item.getReference();
                    AntArtifact artifact = (AntArtifact)item.getArtifact();                                       
                    if ( reference == null) {
                        if ( artifact == null ) {
                            break;
                        }
                        reference = referenceHelper.addReference( item.getArtifact(), item.getArtifactURI());
                    }
                    break;
                case Item.TYPE_CLASSPATH:
                    reference = item.getReference();
                    break;
            }
            
            if ( reference != null ) {
                result.add( reference );
            }

        }

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
        
    // Innerclasses ------------------------------------------------------------
    
    /** Item of the classpath.
     */    
    public static class Item {
        
        // Types of the classpath elements
        public static final int TYPE_JAR = 0;
        public static final int TYPE_LIBRARY = 1;
        public static final int TYPE_ARTIFACT = 2;
        public static final int TYPE_CLASSPATH = 3;

        
        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        
        private Item( int type, Object object, String property ) {
            this.type = type;
            this.object = object;
            this.property = property;
        }
        
        private Item( int type, Object object, URI artifactURI, String property ) {
            this( type, object, property );
            this.artifactURI = artifactURI;
        }
              
        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property ) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
            return new Item( TYPE_LIBRARY, library, property );
        }
        
        public static Item create( AntArtifact artifact, URI artifactURI, String property ) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item( TYPE_ARTIFACT, artifact, artifactURI, property );
        }
        
        public static Item create( File file, String property ) {
            if ( file == null ) {
                throw new IllegalArgumentException( "file must not be null" ); // NOI18N
            }
            return new Item( TYPE_JAR, file, property );
        }
        
        public static Item create( String property ) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
            }
            return new Item ( TYPE_CLASSPATH, null, property );
        }
        
        // Instance methods ----------------------------------------------------
        
        public int getType() {
            return type;
        }
        
        public Library getLibrary() {
            if ( getType() != TYPE_LIBRARY ) {
                throw new IllegalArgumentException( "Item is not of required type - LIBRARY" ); // NOI18N
            }
            return (Library)object;
        }
        
        public File getFile() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            return (File)object;
        }
        
        public AntArtifact getArtifact() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
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
        
        public int hashCode() {
        
            int hash = getType();

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
    
        public boolean equals( Object object ) {

            if ( !( object instanceof Item ) ) {
                return false;
            }
            
            Item item = (Item)object;

            if ( getType() != item.getType() ) {
                return false;
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
                    return object.equals( item.object );
            }

        }
                
    }
            
}
