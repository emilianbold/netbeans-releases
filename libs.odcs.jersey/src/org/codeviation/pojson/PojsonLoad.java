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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Iterator;

/** Good for reconstructing objects from the JSON format.
 *
 * @author Petr Hrebejk
 */
public class PojsonLoad {

    public static PojsonLoad create() {
        return new PojsonLoad();
    }
            
    public Object toCollections(InputStream is) throws IOException {
        Handlers.CollectionsInfo i = new Handlers.CollectionsInfo(Handlers.Info.ROOT, null);
        Handlers.Generic h = new Handlers.Generic(i);
        Parser.parse(is, h);        
        return i.getValue();
    }
    
    public Object toCollections(String s) throws IOException {        
        return toCollections( new ByteArrayInputStream(s.getBytes())) ;        
    }
    
    /** XXX for now does not handle arrays correctly
     * 
     * @param is
     * @param clazz
     * @return
     * @throws java.io.IOException
     */
    public <T1> T1 load(InputStream is, Class<T1> clazz) throws IOException {
        try {
            T1 o =  clazz.isArray() ? (T1)Array.newInstance(clazz.getComponentType(), 0) : clazz.newInstance();
            return update(is,o);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException( ex );
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException( ex );
        }        
    }
    
    /** Should become incremental load later */
    public <T1> Iterator<T1> loadArray(InputStream is, Class<T1> clazz) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    public Iterator loadArray(InputStream is) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    
    public <T1> T1 update(InputStream is, T1 object) throws IOException {
        Handlers.PojoInfo i = new Handlers.PojoInfo(object);
        Handlers.Generic h = new Handlers.Generic(i);
        Parser.parse(is, h);
        return (T1)i.getValue();       
    }
        
    public <T1> T1 load(String s, Class<T1> clazz) {
        try {
            return load( new ByteArrayInputStream(s.getBytes()), clazz) ;
        }
        catch( IOException ex ) {
            throw new IllegalStateException();
        }
    }
    
    public <T1> Iterator<T1> loadArray(String s, Class<T1> clazz) {
        try {
            return loadArray( new ByteArrayInputStream(s.getBytes()), clazz) ;
        }
        catch( IOException ex ) {
            throw new IllegalStateException();
        }
    }
    
    public Iterator loadArray(String s) {
        try {
            return loadArray( new ByteArrayInputStream(s.getBytes())) ;
        }
        catch( IOException ex ) {
            throw new IllegalStateException();
        }
    }
    
    public <T1> T1 update(String s, T1 object) {
    
        try {
            return update( new ByteArrayInputStream(s.getBytes()), object) ;
        }
        catch( IOException ex ) {
            throw new IllegalStateException();
        }
        
    }
        
    
}
