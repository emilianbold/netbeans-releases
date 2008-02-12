/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.clientproject.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Petr Hrebejk
 */
public class ClassPathSupport {
     
    public final static String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private Set<String> wellKnownPaths;
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
        this.wellKnownPaths = wellKnownPaths == null ? null : new HashSet<String>( Arrays.asList( wellKnownPaths ) );
        this.libraryPrefix = libraryPrefix;
        this.librarySuffix = librarySuffix;
        this.antArtifactPrefix = antArtifactPrefix;
    }
    
    /** Creates list of <CODE>Items</CODE> from given property.
     */    
    public Iterator<Item> itemsIterator( String propertyValue, String includedLibrariesElement ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue, includedLibrariesElement ).iterator();
    }
    
    public List<Item> itemsList( String propertyValue, String includedLibrariesElement ) {
        
        // Get the list of items which are included in deployment
        List<String> includedItems = (includedLibrariesElement != null) ?
            getIncludedLibraries( antProjectHelper, includedLibrariesElement ) : 
            Collections.<String>emptyList();
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List<Item> items = new ArrayList<Item>( pe.length );
        for( int i = 0; i < pe.length; i++ ) {
            String property = getAntPropertyName( pe[i] );
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
                    item = Item.createBroken( f, pe[i], includedItems.contains ( property ) );
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
        
        List<String> result = new ArrayList<String>();
        List<String> includedLibraries = new ArrayList<String>();
        
        List<Item> cp = new LinkedList<Item>();
        
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
                    AntArtifact artifact = item.getArtifact();                                       
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
                    includedLibraries.add( getAntPropertyName( reference ) );
                }
            }
            
        }
        
        if ( includedLibrariesElement != null ) {
            putIncludedLibraries( includedLibraries, cp, antProjectHelper, includedLibrariesElement );
        }

        String[] items = new String[ result.size() ];
        for( int i = 0; i < result.size(); i++) {
            if ( i < result.size() - 1 ) {
                items[i] = result.get( i ) + ":"; // NOI18N
            }
            else  {       
                items[i] = result.get( i );    //NOI18N
            }
        }
        
        return items;
    }
    
    public String getLibraryReference( Item item ) {
        if ( item.getType() != Item.TYPE_LIBRARY ) {
            throw new IllegalArgumentException( "Item must be of type LIBRARY" ); // NOI18N
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
    private static List<String> getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<String> libraries = new ArrayList<String>(libs.getLength());
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
    private static void putIncludedLibraries( List<String> libraries, List<Item> classpath,
            AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert libraries != null;
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        for (String libraryName : libraries) {
            //find a correcponding classpath item for the library
            for (ClassPathSupport.Item item : classpath) {
                String libraryPropName = "${" + libraryName + "}"; // NOI18N
                if(libraryPropName.equals(item.getReference())) {
                    data.appendChild(createLibraryElement(doc, libraryName, item, includedLibrariesElement));
                }
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(Document doc, String pathItem, Item item, String includedLibrariesElement ) {
        Element libraryElement = doc.createElementNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<File> files = new ArrayList<File>();
        List<File> dirs = new ArrayList<File>();
        AppClientProjectProperties.getFilesForItem(item, files, dirs);
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

        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        private boolean includedInDeployment;
        private String raw;
        private final boolean broken;
        
        private Item(int type, Object object, String property, boolean included, String raw, boolean broken) {
            this.type = type;
            this.object = object;
            this.property = property;
            this.includedInDeployment = included;
            this.raw = raw;
            this.broken = broken;
        }
        
        private Item( int type, Object object, URI artifactURI, String property, boolean included ) {
            this(type, object, property, included, null, false);
            this.artifactURI = artifactURI;
        }
        
        private Item(int type, Object object, String property, boolean included, String raw) {
            this(type, object, property, included, null, false);
        }
              
        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property, boolean included ) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
            String libraryName = library.getName();
            return new Item( TYPE_LIBRARY, library, property, included, AppClientProjectProperties.LIBRARY_PREFIX + libraryName + AppClientProjectProperties.LIBRARY_SUFFIX);
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
            return new Item(type, null, property, included, null, true);
        }
        
        public static Item createBroken(File file, String property, boolean included) {
            return new Item(TYPE_JAR, file, property, included, null, true);
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
            // for broken item: one will get java.io.File or null (#113390)
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
//            boolean result = includedInDeployment;
//            if (getType() == TYPE_JAR) {
                // at the moment we can't include folders in deployment
//                FileObject fo = FileUtil.toFileObject(getFile());
//                if (fo == null || fo.isFolder())
//                    return false;
//            }
            return includedInDeployment;
        }
        
        public void setIncludedInDeployment(boolean includedInDeployment) {
            this.includedInDeployment = includedInDeployment;
        }
        
        public boolean isBroken() {
            return broken;
        }
        
        @Override
        public String toString() {
            return "artifactURI=" + artifactURI // NOI18N
                    + ", type=" + type // NOI18N
                    + ", property=" + property // NOI18N
                    + ", includedInDeployment=" + includedInDeployment // NOI18N
                    + ", raw=" + raw // NOI18N
                    + ", object=" + object // NOI18N
                    + ", broken=" + broken; // NOI18N
        }
        
        @Override
        public int hashCode() {
        
            int hash = getType();

            if (isBroken()) {
                return 42;
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
    
        @Override
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
            


    
    /**
     * Converts the ant reference to the name of the referenced property
     * @param ant reference
     * @param the name of the referenced property
     */ 
    public static String getAntPropertyName( String property ) {
        if ( property != null && 
             property.startsWith( "${" ) && // NOI18N
             property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 ); 
        }
        else {
            return property;
        }
    }
            
}
