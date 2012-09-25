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
import java.io.StringWriter;
import java.io.Writer;
import org.codeviation.commons.patterns.Factory;
import org.codeviation.commons.patterns.Filter;
import org.codeviation.commons.utils.ArrayUtil;

/**
 *
 * @author Petr Hrebejk
 */
public final class PojsonSave<T> {

    
    private static int DEFAULT_STRING_BUFFER_SIZE = 2048;
    
    private Class<T> clazz;
    private Filter<String> fieldFilter;
    private String indentation = "    ";
    private int indentLevel = 0;
    
    private Factory<StoreInfo,Class> sif;
        
    public PojsonSave() {
        this.clazz = null;
        this.sif = new StoreInfoFactory(); // XXX make me a cache
    }
    
    
    private PojsonSave(Class<T> clazz) {    
        this.clazz = clazz;
        this.sif = new StoreInfoFactory();
    }
    
    public static PojsonSave<?> create() {
        return new PojsonSave();
    }
    
    public static <T> PojsonSave<T> create(Class<T> clazz) {
        return new PojsonSave<T>(clazz);
    }
             
    public void setFieldFilter(Filter<String> fieldFilter) {                
        this.fieldFilter = fieldFilter;
    }
    
    /** Sets the string which will be used as indetation
     * 
     * @param indentation The indentation string.
     */
    public void setIndentation(String indentation) {
        this.indentation = indentation;
    }
    
    /** Sets the initial level of indentation.
     * 
     * @param startIndentLevel Number of indentation string repeats
     */
    public void setIndentLevel(int startIndentLevel) {
        this.indentLevel = startIndentLevel;
    }
             
    /**
     * Saves the object as an JSON object.
     * 
     * @param object
     */
    public void save(Writer writer, T object) throws IOException {
        PojsonFormater pf = new PojsonFormater(sif, getIndentation(), writer);
        pf.write(object, getIndentLevel());
    }
    
    /**
     * Saves the object(s) as JSON Array.
     * 
     * @param array
     */
    public void save(Writer writer, T[] array) throws IOException {
        PojsonFormater pf = new PojsonFormater(sif, getIndentation(), writer);
        pf.write(array, getIndentLevel());
    }
    
    /**
     * Saves the object(s) as JSON Array.
     * @param array
     */
    public void save(Writer writer, T firstObject, T... array) throws IOException {
        PojsonFormater pf = new PojsonFormater(sif, indentation, writer);
        pf.write(ArrayUtil.union(new Object[] {firstObject}, array), getIndentLevel());
    }
    
    /**
     * Saves the object(s) in iterable as JSON Array.
     * 
     * @param array
     */
    public void save(Writer writer, Iterable<T> iterable) throws IOException {
        PojsonFormater pf = new PojsonFormater(sif, indentation, writer);
        pf.write(iterable, getIndentLevel());
    }
            
    /** Utility method for storing objects directlty to string
     * 
     * @param object Object to strore.
     * @return JSON String representing the object.
     */
    public String asString(T object) {
                
        StringWriter sw = new StringWriter(DEFAULT_STRING_BUFFER_SIZE);
        try {
            save(sw, object);
            return sw.toString();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        
    }
        
    /** Utility method for storing arrays directlty to string
     * 
     * @param object Object to strore.
     * @return JSON String representing the object.
     */
    public String asString(T firstObject, T... otherObjects) {

        StringWriter sw = new StringWriter(DEFAULT_STRING_BUFFER_SIZE);
        try {
            save(sw, firstObject, otherObjects);
            return sw.toString();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        
    }
    
    public String asString(T[] array) {

        StringWriter sw = new StringWriter(DEFAULT_STRING_BUFFER_SIZE);
        try {
            save(sw, array);
            return sw.toString();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        
    }
           
    /** Utility method for storing arrays directlty to string
     * 
     * @param object Object to strore.
     * @return JSON String representing the object.
     */
    public String asString(Iterable<T> iterable) {
        
        StringWriter sw = new StringWriter(DEFAULT_STRING_BUFFER_SIZE);
        try {
            save(sw, iterable);
            return sw.toString();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        
    }

              
    // Private methods ---------------------------------------------------------
    
    // Now for testing only
    // XXX performance
    String getFileName(T o) {
        Class c = clazz == null ? o.getClass() : clazz;
        StoreInfo si = sif.create(c);
        FileNameFactory<T> fnf = new FileNameFactory(c,si.getFields());
        return fnf.create(o);
    }
    
    
    private String getIndentation() {
        return indentation;
    }
    
    private int getIndentLevel() {
        return indentLevel;
    }
    
    private Filter<String> getFieldFilter() {
        return fieldFilter;
    }
    
    
    private class StoreInfoFactory implements Factory<StoreInfo,Class> {

        public StoreInfo create(Class clazz) {
            return new StoreInfo(clazz);
        }
        
    }
        
}
