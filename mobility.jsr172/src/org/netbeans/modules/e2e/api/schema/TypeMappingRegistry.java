/*
 * TypeMappingRegistry.java
 *
 * Created on October 25, 2006, 3:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class TypeMappingRegistry {
    
    private SchemaHolder schemaHolder;
    private Map<QName, TypeMapping> mappings;
    
    /** Creates a new instance of TypeMappingRegistry */
    public TypeMappingRegistry( SchemaHolder schemaHolder ) {
        this.schemaHolder = schemaHolder;
        mappings = new HashMap();
        
        for( Element e : schemaHolder.getSchemaElements()) {
            mapType( e );
        }
        
    }
    
    public TypeMapping getType( QName name ) {
        return mappings.get( name );
    }
    
    private void mapType( Element e ) {
        Type type = e.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            mappings.put( e.getName(), new TypeMappingImpl( type, e.getMaxOccurs() > 1, e.isNillable()));
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if( type.getSubconstructs().size() == 0 ) {
                System.err.print( "void" );
            } else if( type.getSubconstructs().size() == 1 ) {
                SchemaConstruct sc = type.getSubconstructs().get( 0 );
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element)sc;
                    if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                        System.err.print( sce.getType().getJavaTypeName());
                    } else {
                        System.err.print( sce.getName().getLocalPart());
                    }
                }
            }
        }            
    }
    
    private static final class TypeMappingImpl implements TypeMapping {
        
        private String name;
        private boolean array;
        private boolean primitive;
        private boolean nillable;
        
        private Type type;
        
        public TypeMappingImpl( Type type, boolean array, boolean nillable ) {
            this.type = type;
            this.array = array;
            this.nillable = nillable;
        }        

//    if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
//        System.err.print( type.getJavaTypeName());
//    } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
//        if( type.getSubconstructs().size() == 0 ) {
//            System.err.print( "void" );
//        } else if( type.getSubconstructs().size() == 1 ) {
//            SchemaConstruct sc = type.getSubconstructs().get( 0 );
//            if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
//                Element sce = (Element)sc;
//                if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
//                    System.err.print( sce.getType().getJavaTypeName());
//                } else {
//                    System.err.print( sce.getName().getLocalPart());
//                }
//            }
//        }
//    }
        
    }
}
