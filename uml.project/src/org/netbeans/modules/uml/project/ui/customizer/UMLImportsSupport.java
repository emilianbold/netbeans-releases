/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.project.ui.customizer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mike Frisino
 */
public class UMLImportsSupport {
	
	// TODO - MCF - test whether the broken reference behavior automatically 
	// works. Much of this code is copied from similar in J2SE project.
	// Hopefully, it will yeild "free" broken reference support.
                
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private Set /*<String>*/ wellKnownPaths;
    private String libraryPrefix;
    private String librarySuffix;
    private String umlImportPrefix;
        
    /**
     * Creates a new instance of UMLImportsSupport 
     */
    public  UMLImportsSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              AntProjectHelper antProjectHelper,
                              String umlImportPrefix ) {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.umlImportPrefix = umlImportPrefix;
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

			// Ant artifact from another project
			Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);                
			if ( ret[0] == null || ret[1] == null ) {
				item = Item.createBroken( pe[i] );
			}
			else {
				//fix of issue #55316
				AntArtifact artifact = (AntArtifact)ret[0];
				URI uri = (URI)ret[1];
				File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
				File artifactFile = new File (artifact.getScriptLocation().toURI().resolve(uri).normalize());
				if (usedFile.equals(artifactFile)) {
					item = Item.create( artifact, uri, pe[i] );
				}
				else {
					item = Item.createBroken(pe[i] );
				}
			}  
            items.add( item );         
        }

        return items;
        
    }
    
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     */
    public String[] encodeToStrings( Iterator /*<Item>*/ imports ) {
        
        ArrayList result = new ArrayList();
        
        while( imports.hasNext() ) {

            Item item = (Item)imports.next();
            String reference = null;

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
    
    // Private methods ---------------------------------------------------------


    
    private boolean isUMLImport( String property ) {        
        return umlImportPrefix == null ? false : property.startsWith( umlImportPrefix );
    }
    

        
    // Innerclasses ------------------------------------------------------------
    
    /** Item of the uml project ref
     */    
    public static class Item {
        
        // Reference to a broken object
        private static final String BROKEN = "BrokenReference"; // NOI18N
        
        private Object object;
        private URI artifactURI;
        private String property;
        
        private Item(Object object, String property ) {
            this.object = object;
            this.property = property;
        }
        
        private Item( Object object, URI artifactURI, String property ) {
            this( object, property );
            this.artifactURI = artifactURI;
        }
              
        // Factory methods -----------------------------------------------------

        
        public static Item create( AntArtifact artifact, URI artifactURI, String property ) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item(artifact, artifactURI, property );
        }
        
        
        public static Item createBroken( String property ) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            return new Item(BROKEN, property );
        }
        
        // Instance methods ----------------------------------------------------
        
       
        
        public AntArtifact getArtifact() {
            return (AntArtifact)object;
        }
        
        public URI getArtifactURI() {
            return artifactURI;
        }
        
        
        public String getReference() {
            return property;
        }
        
        public boolean isBroken() {
            return object == BROKEN;
        }
                        
        public int hashCode() {
        
            int hash = 0;

            if ( object == BROKEN ) {
                return BROKEN.hashCode();
            }
            
			hash += getArtifact().getType().hashCode();                
			hash += getArtifact().getScriptLocation().hashCode();
			hash += getArtifactURI().hashCode();

            return hash;
        }
    
        public boolean equals( Object itemObject ) {

            if ( !( itemObject instanceof Item ) ) {
                return false;
            }
            
            Item item = (Item)itemObject;
            
            if ( isBroken() != item.isBroken() ) {
                return false;
            }
            
            if ( isBroken() ) {
                return getReference().equals( item.getReference() );
            }


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
 
        }
        
        public String toString()
        {
            return artifactURI.toString();
        }
        
        public String getDirectoryLocation()
        {
            String retVal = "";
            if(object instanceof AntArtifact)
            {
                AntArtifact ant = (AntArtifact)object;
                Project project = ant.getProject();
                FileObject fo = project.getProjectDirectory();
                retVal = fo.getPath();
            }
            
            return retVal;
        }
                
    }
            
}
