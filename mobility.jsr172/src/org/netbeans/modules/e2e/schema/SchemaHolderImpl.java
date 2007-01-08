/*
 * SchemaHolderImpl.java
 *
 * Created on October 9, 2006, 5:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.schema.Element;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct;
import org.netbeans.modules.e2e.api.schema.SchemaHolder;
import org.netbeans.modules.e2e.api.schema.Type;

/**
 *
 * @author Michal Skvor
 */
public class SchemaHolderImpl implements SchemaHolder {
    
    private Map<QName, Element> elements;
    private Map<QName, Type> types;
    
    private Map<QName, SchemaConstruct> schemaConstructs;

    public SchemaHolderImpl() {
        elements = new HashMap();
        types = new HashMap();
    }

    public void addSchemaType( Type type ) {
        types.put( type.getName(), type );
    }

    public Type getSchemaType( QName name ) {
        return types.get( name );
    }

    public Set<Type> getSchemaTypes() {
        return new HashSet( types.values());
    }

    public void addSchemaElement( Element element ) {
        elements.put( element.getName(), element );
    }

    public Element getSchemaElement( QName name ) {
        return elements.get( name );
    }

    public Set<Element> getSchemaElements() {
        return new HashSet( elements.values());
    }

    public void importSchema( SchemaHolder schemaHolder ) {
        for( Element element : schemaHolder.getSchemaElements()) {
            addSchemaElement( element );
        }
        
        for( Type type : schemaHolder.getSchemaTypes()) {
            addSchemaType( type );
        }
    }
    
}
