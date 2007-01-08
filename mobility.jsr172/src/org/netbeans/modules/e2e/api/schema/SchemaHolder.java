/*
 * Schema.java
 *
 * Created on October 2, 2006, 3:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface SchemaHolder {
    
    public void addSchemaType( Type type );
    
    public Type getSchemaType( QName name );
    
    public Set<Type> getSchemaTypes();
    
    public void addSchemaElement( Element element );
    
    public Element getSchemaElement( QName name );
    
    public Set<Element> getSchemaElements();
    
    public void importSchema( SchemaHolder schemaHolder );
}
