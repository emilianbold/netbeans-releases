/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.codeviation.pojson;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.commons.patterns.Factory;

/**
 *
 * @author Petr Hrebejk
 */
class PojsonFormater {

    private Factory<StoreInfo,Class> sif;
    private String indent;
    private Writer w;

    public PojsonFormater(Factory<StoreInfo, Class> sif, String indent, Writer w) {
        this.sif = sif;
        this.indent = indent;
        this.w = w;
    }
           
    void write( Object object, int indentLevel ) throws IOException {
        indent(w, indent, indentLevel);
        writeAny(object, indentLevel);
    }
        
    private void writeAny( Object object, int indentLevel ) throws IOException {
        
        if ( object == null ) {
            w.write("null");
            return;
        }
        
        StoreInfo si = sif.create(object.getClass());
        
        switch(si.getKind()) {
            case ARRAY:
                writeArray(si, JsonUtils.getIterator(object), indentLevel);
                break;
            case OBJECT:
                writeObject(si, object, indentLevel);
                break;
            case VALUE:
                writeValue(object);
                break;
            default:
                throw new IllegalStateException("Unknown store kind");            
            }                
    }
        
    private void writeObject(StoreInfo si, Object object, int indentLevel ) throws IOException {
        if ( object == null ) {
            w.write("null");
            return;
        }
        w.write('{');

        if ( object instanceof Map ) {
            writeMap(si, (Map)object, indentLevel);
        }
        else {
            writePojo(si, object, indentLevel);
        }
        indent(w, indent, indentLevel);
        w.write('}');            
    }
    
    private void writeMap(StoreInfo si, Map<?,?> map, int indentLevel ) throws IOException {
        
        boolean first = true;

        for( Map.Entry entry : map.entrySet() ) {

            Object value = entry.getValue();

            if( !first ) {
                w.write(',');
                if ( indent != null ) {
                    w.write('\n');
                }
            } 
            else {
                if ( indent != null) {
                    w.write('\n');
                }
                first = false;
            }

            key(w, entry.getKey().toString(), indent, indentLevel + 1);
            writeAny( value, indentLevel + 1);
        }

        if( !first && indent != null ) { // There were some fields we need new line
            w.write('\n');
        }
        
    }
    
    
    private void writePojo(StoreInfo si, Object object, int indentLevel ) throws IOException {
        
        try {
            boolean first = true;

            for (Field f : si.getFields()) {

                f.setAccessible(true);
                Object value = f.get(object);

                if (value == null && si.isSkipNullValues(f)) {
                    continue;
                }                
                if( !first ) {
                    w.write(',');
                    if ( indent != null ) {
                        w.write('\n');
                    }
                } 
                else {
                    if ( indent != null) {
                        w.write('\n');
                    }
                    first = false;
                }

                key(w, si.getPojsonFieldName(f), indent, indentLevel + 1);
                writeAny( value, indentLevel + 1);
            }

            if( !first && indent != null ) { // There were some fields we need new line
                w.write('\n');
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StoreInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StoreInfo.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void writeValue(Object value) throws IOException {
        w.write(JsonUtils.toJsonString(value));
    }
    
    private <T> void writeArray(StoreInfo si, Iterator it, int indentLevel) throws IOException {
        
        if (it == null ) {
            w.write("null");
            return;
        }
        
        w.write('[');
                
        boolean wasEmpty = true;
        
        while( it.hasNext() ) {
            if( indent != null) {
                w.write('\n');
            }
            wasEmpty = false;
            indent(w, indent, indentLevel + 1);            
            writeAny(it.next(), indentLevel + 1);
            if ( it.hasNext() ) {
                w.write(',');                
            }            
        }
             
        if( !wasEmpty && indent != null) {
            w.write('\n');
            indent(w, indent, indentLevel);
        }
        
        w.write(']');
    }      
           
    private static void key(Writer w, String name, String indent, int indentLevel ) throws IOException {
        indent(w, indent, indentLevel);
        w.write(JsonUtils.quote(name));
        w.write(':');
        if (indent != null) {
            w.write(' ');
        }
    }
    
    private static void indent(Writer w, String indent, int indentLevel ) throws IOException {
     
         if ( indent == null || indentLevel == 0 ) {
             return;
         }

         for( int i = 0; i < indentLevel; i++) {
             w.write(indent);
         }
    }
   
}
