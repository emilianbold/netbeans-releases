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

package org.netbeans.modules.ruby.javaint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;

/**
 *
 * @author Petr Hrebejk
 */
public class ClassPathSupport {
                
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private RakeProjectHelper antProjectHelper;
    private Set<String> wellKnownPaths;
    private String libraryPrefix;
    private String librarySuffix;
    private String antArtifactPrefix;
        
    /** Creates a new instance of ClassPathSupport */
    public  ClassPathSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              RakeProjectHelper antProjectHelper,
                              String[] wellKnownPaths,
                              String libraryPrefix,
                              String librarySuffix,
                              String antArtifactPrefix ) {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.wellKnownPaths = wellKnownPaths == null ? null : new HashSet<String>(Arrays.asList(wellKnownPaths));
        this.libraryPrefix = libraryPrefix;
        this.librarySuffix = librarySuffix;
        this.antArtifactPrefix = antArtifactPrefix;
    }
    
//    /** Creates list of <CODE>Items</CODE> from given property.
//     */    
//    public Iterator<Item> itemsIterator( String propertyValue ) {
//        // XXX More performance frendly impl. would retrun a lazzy iterator.
//        return itemsList( propertyValue ).iterator();
//    }
//    
//    public List<Item> itemsList( String propertyValue ) {    
//        
//        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
//        List<Item> items = new ArrayList<Item>( pe.length );        
//        for( int i = 0; i < pe.length; i++ ) {
//            Item item;
//
//            // First try to find out whether the item is well known classpath
//            if ( isWellKnownPath( pe[i] ) ) {
//                // Some well know classpath
//                item = Item.create( pe[i] );
////            } 
////            else if ( isLibrary( pe[i] ) ) {
////                //Library from library manager
////                String libraryName = pe[i].substring( libraryPrefix.length(), pe[i].lastIndexOf('.') ); //NOI18N
////                Library library = LibraryManager.getDefault().getLibrary( libraryName );
////                if ( library == null ) {
////                    item = Item.createBroken( Item.TYPE_LIBRARY, pe[i] );
////                }
////                else {
////                    item = Item.create( library, pe[i] );
////                }
////            } 
////            else if ( isAntArtifact( pe[i] ) ) {
////                // Ant artifact from another project
////                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);                
////                if ( ret[0] == null || ret[1] == null ) {
////                    item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i] );
////                }
////                else {
////                    //fix of issue #55316
////                    AntArtifact artifact = (AntArtifact)ret[0];
////                    URI uri = (URI)ret[1];
////                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
////                    File artifactFile = new File (artifact.getScriptLocation().toURI().resolve(uri).normalize());
////                    if (usedFile.equals(artifactFile)) {
////                        item = Item.create( artifact, uri, pe[i] );
////                    }
////                    else {
////                        item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i] );
////                    }
////                }
//            } else {
//                // Standalone jar or property
//                String eval = evaluator.evaluate( pe[i] );
//                File f = null;
//                if (eval != null) {
//                    f = antProjectHelper.resolveFile( eval );
//                }                    
//                
//                if ( f == null || !f.exists() ) {
//                    item = Item.createBroken( Item.TYPE_JAR, pe[i] );
//                }
//                else {
//                    item = Item.create( f, pe[i] );
//                }
//            }
//            
//            items.add( item );
//           
//        }
//
//        return items;
//        
//    }
    
//    /** Converts list of classpath items into array of Strings.
//     * !! This method creates references in the project !!
//     */
//    public String[] encodeToStrings(Iterator<Item> classpath) {
//        
//        ArrayList result = new ArrayList();
//        
//        while( classpath.hasNext() ) {
//
//            Item item = classpath.next();
//            String reference = null;
//            
//            switch( item.getType() ) {
//
//                case Item.TYPE_JAR:
//                    reference = item.getReference();
//                    if ( item.isBroken() ) {
//                        break;
//                    }
//                    if (reference == null) {
//                        // New file
//                        File file = item.getFile();
//                        // pass null as expected artifact type to always get file reference
//                        reference = referenceHelper.createForeignFileReference(file, null);
//                    }
//                    break;
////                case Item.TYPE_LIBRARY:
////                    reference = item.getReference();
////                    if ( item.isBroken() ) {
////                        break;
////                    }                    
////                    Library library = item.getLibrary();                                       
////                    if (reference == null) {
////                        if ( library == null ) {
////                            break;
////                        }
////                        reference = getLibraryReference( item );
////                    }
////                    break;    
////                case Item.TYPE_ARTIFACT:
////                    reference = item.getReference();
////                    if ( item.isBroken() ) {
////                        break;
////                    }
////                    AntArtifact artifact = (AntArtifact)item.getArtifact();                                       
////                    if ( reference == null) {
////                        if ( artifact == null ) {
////                            break;
////                        }
////                        reference = referenceHelper.addReference( item.getArtifact(), item.getArtifactURI());
////                    }
////                    break;
//                case Item.TYPE_CLASSPATH:
//                    reference = item.getReference();
//                    break;
//            }
//            
//            if ( reference != null ) {
//                result.add( reference );
//            }
//
//        }
//
//        String[] items = new String[ result.size() ];
//        for( int i = 0; i < result.size(); i++) {
//            if ( i < result.size() - 1 ) {
//                items[i] = result.get( i ) + ":";
//            }
//            else  {       
//                items[i] = (String)result.get( i );    //NOI18N
//            }
//        }
//        
//        return items;
//    }
//    
////    public String getLibraryReference( Item item ) {
////        if ( item.getType() != Item.TYPE_LIBRARY ) {
////            throw new IllegalArgumentException( "Item must be of type LIBRARY" );
////        }
////        return libraryPrefix + item.getLibrary().getName() + librarySuffix;        
////    }
//    
//    // Private methods ---------------------------------------------------------
//
//    private boolean isWellKnownPath( String property ) {
//        return wellKnownPaths == null ? false : wellKnownPaths.contains( property );
//    }
//    
//    private boolean isAntArtifact( String property ) {        
//        return antArtifactPrefix == null ? false : property.startsWith( antArtifactPrefix );
//    }
//    
//    private boolean isLibrary( String property ) {
//        if ( libraryPrefix != null && property.startsWith( libraryPrefix ) ) {
//            return librarySuffix == null ? true : property.endsWith( librarySuffix );
//        }
//        else {
//            return false;
//        }
//        
//    }
        
//    // Innerclasses ------------------------------------------------------------
//    
//    /** Item of the classpath.
//     */    
//    public static class Item {
//        
//        // Types of the classpath elements
//        public static final int TYPE_JAR = 0;
//        public static final int TYPE_LIBRARY = 1;
//        public static final int TYPE_ARTIFACT = 2;
//        public static final int TYPE_CLASSPATH = 3;
//
//        // Reference to a broken object
//        private static final String BROKEN = "BrokenReference"; // NOI18N
//        
//        private Object object;
//        private URI artifactURI;
//        private int type;
//        private String property;
//        
//        private Item( int type, Object object, String property ) {
//            this.type = type;
//            this.object = object;
//            this.property = property;
//        }
//        
//        private Item( int type, Object object, URI artifactURI, String property ) {
//            this( type, object, property );
//            this.artifactURI = artifactURI;
//        }
//              
//        // Factory methods -----------------------------------------------------
//        
//        
////        public static Item create( Library library, String property ) {
////            if ( library == null ) {
////                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
////            }
////            return new Item( TYPE_LIBRARY, library, property );
////        }
////        
////        public static Item create( AntArtifact artifact, URI artifactURI, String property ) {
////            if ( artifactURI == null ) {
////                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
////            }
////            if ( artifact == null ) {
////                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
////            }
////            return new Item( TYPE_ARTIFACT, artifact, artifactURI, property );
////        }
//        
//        public static Item create( File file, String property ) {
//            if ( file == null ) {
//                throw new IllegalArgumentException( "file must not be null" ); // NOI18N
//            }
//            return new Item( TYPE_JAR, file, property );
//        }
//        
//        public static Item create( String property ) {
//            if ( property == null ) {
//                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
//            }
//            return new Item ( TYPE_CLASSPATH, null, property );
//        }
//        
//        public static Item createBroken( int type, String property ) {
//            if ( property == null ) {
//                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
//            }
//            return new Item( type, BROKEN, property );
//        }                
//        
//        
//        // Instance methods ----------------------------------------------------
//        
//        public int getType() {
//            return type;
//        }
////        
////        public Library getLibrary() {
////            if ( getType() != TYPE_LIBRARY ) {
////                throw new IllegalArgumentException( "Item is not of required type - LIBRARY" ); // NOI18N
////            }
////            assert object instanceof Library :
////                "Invalid object type: "+object.getClass().getName()+" instance: "+object.toString()+" expected type: Library";   //NOI18N
////            return (Library)object;
////        }
////        
//        public File getFile() {
//            if ( getType() != TYPE_JAR ) {
//                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
//            }
//            return (File)object;
//        }
////        
////        public AntArtifact getArtifact() {
////            if ( getType() != TYPE_ARTIFACT ) {
////                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
////            }
////            return (AntArtifact)object;
////        }
//        
//        public URI getArtifactURI() {
//            if ( getType() != TYPE_ARTIFACT ) {
//                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
//            }
//            return artifactURI;
//        }
//        
//        
//        public String getReference() {
//            return property;
//        }
//        
//        public boolean isBroken() {
//            return object == BROKEN;
//        }
//                        
//        public int hashCode() {
//        
//            int hash = getType();
//
//            if ( object == BROKEN ) {
//                return BROKEN.hashCode();
//            }
//            
//            switch ( getType() ) {
////                case TYPE_ARTIFACT:
////                    hash += getArtifact().getType().hashCode();                
////                    hash += getArtifact().getScriptLocation().hashCode();
////                    hash += getArtifactURI().hashCode();
////                    break;
//                case TYPE_CLASSPATH:
//                    hash += property.hashCode();
//                    break;
//                default:
//                    hash += object.hashCode();
//            }
//
//            return hash;
//        }
//    
//        public boolean equals( Object itemObject ) {
//
//            if ( !( itemObject instanceof Item ) ) {
//                return false;
//            }
//            
//            Item item = (Item)itemObject;
//
//            if ( getType() != item.getType() ) {
//                return false;
//            }
//            
//            if ( isBroken() != item.isBroken() ) {
//                return false;
//            }
//            
//            if ( isBroken() ) {
//                return getReference().equals( item.getReference() );
//            }
//
//            switch ( getType() ) {
////                case TYPE_ARTIFACT:
////                    if ( getArtifact().getType() != item.getArtifact().getType() ) {
////                        return false;
////                    }
////
////                    if ( !getArtifact().getScriptLocation().equals( item.getArtifact().getScriptLocation() ) ) {
////                        return false;
////                    }
////
////                    if ( !getArtifactURI().equals( item.getArtifactURI() ) ) {
////                        return false;
////                    }
////                    return true;
//                case TYPE_CLASSPATH:
//                    return property.equals( item.property );
//                default:
//                    return object.equals( item.object );
//            }
//
//        }
//                
//    }
//    
//    /**
//     * Tokenize library classpath and try to relativize all the jars.
//     * @param ep the editable properties in which the result should be stored
//     * @param aph AntProjectHelper used to resolve files
//     * @param libCpProperty the library classpath property
//     */
//    public static boolean relativizeLibraryClassPath (final EditableProperties ep, final AntProjectHelper aph, final String libCpProperty) {
//        String value = PropertyUtils.getGlobalProperties().getProperty(libCpProperty);
//        // bugfix #42852, check if the classpath property is set, otherwise return null
//        if (value == null) {
//            return false;
//        }
//        String[] paths = PropertyUtils.tokenizePath(value);
//        StringBuffer sb = new StringBuffer();
//        File projectDir = FileUtil.toFile(aph.getProjectDirectory());
//        for (int i=0; i<paths.length; i++) {
//            File f = aph.resolveFile(paths[i]);
//            if (CollocationQuery.areCollocated(f, projectDir)) {
//                sb.append(PropertyUtils.relativizeFile(projectDir, f));
//            } else {
//                return false;
//            }
//            if (i+1<paths.length) {
//                sb.append(File.pathSeparatorChar);
//            }
//        }
//        if (sb.length() == 0) {
//            return false;
//        }            
//        ep.setProperty(libCpProperty, sb.toString());
//        ep.setComment(libCpProperty, new String[]{
//            // XXX this should be I18N! Not least because the English is wrong...
//            "# Property "+libCpProperty+" is set here just to make sharing of project simpler.",
//            "# The library definition has always preference over this property."}, false);
//        return true;
//    }
//    
//    /**
//     * Converts the ant reference to the name of the referenced property
//     * @param ant reference
//     * @param the name of the referenced property
//     */ 
//    public static String getAntPropertyName( String property ) {
//        if ( property != null && 
//             property.startsWith( "${" ) && // NOI18N
//             property.endsWith( "}" ) ) { // NOI18N
//            return property.substring( 2, property.length() - 1 ); 
//        }
//        else {
//            return property;
//        }
//    }
            
}
