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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy.properties;

import java.io.IOException;

import java.util.Hashtable;

/**
 * PropertyFactory class
 * <p>
 * 
 *
 */

public class PropertyFactory {
    
    // Debug info reader/writer
    private PropertyReaderWriter debugrw = null;
    
    // Element name
    private String elementName;
    
    // Property readers array
    private PropertyReader preaders[] = null;
    
    // Property writers array
    private PropertyReaderWriter pwriters[] = null;
    
    // Property readers hashtable
    private Hashtable prhash = new Hashtable();
    
    // Property writers hashtable
    private Hashtable prwhash = new Hashtable();

    /**
     * Constructs PropertyFactory object
     *
     * @param elementName name of component
     */
    public PropertyFactory(String elementName) {
        this.elementName = elementName;
        initialize();
    }
    
    protected void initialize() {
        // Creates arrays of Readers and ReadersWriters
        preaders = new PropertyReader[1];
        pwriters = new PropertyReaderWriter[1];
        
        preaders[0] = pwriters[0] = new PropertySheetReaderWriter();


        // Creates hashtables using arrays of Readers and ReadersWriters
        for (int i = 0; i < getReadersNum(); i++) {
            prhash.put(preaders[i].getName(), preaders[i]);
        }
        for (int i = 0; i < getWritersNum(); i++) {
            prwhash.put(pwriters[i].getName(), pwriters[i]);
        }
        
        // Insert debugging enable/disable code
        if (false) {
            try {
                debugrw = new DebugReaderWriter(elementName, "c:/temp/testdebug.out");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Return number of available PropertyReaders
     *
     * @return number of readers
     */
    public int getReadersNum() {
        return (preaders == null)?0:preaders.length;
    }

    /**
     * Return number of available PropertyReaderWriters
     *
     * @return number of writers
     */
    public int getWritersNum() {
        return (pwriters == null)?0:pwriters.length;
    }
    
    /**
     * Set property value by specified writer
     *
     * @param writer writer index
     * @param name property name
     * @param value property value to set
     */
    public void setValue(int writer, String name, Object value) {
        if (writer < 0 || writer >= getWritersNum())
            throw new ArrayIndexOutOfBoundsException(writer);
        pwriters[writer].setPropertyValue(name, value);
        if (debugrw != null)
            debugrw.setPropertyValue(name, value);
    }

    /**
     * Get property value by specified reader
     *
     * @param reader reader index
     * @param name property name
     * @return property value
     */
    public Object getValue(int reader, String name) {
        if (reader < 0 || reader >= getReadersNum())
            throw new ArrayIndexOutOfBoundsException(reader);
        if (debugrw != null)
            debugrw.getPropertyValue(name);          
        return preaders[reader].getPropertyValue(name);
    }
    /**
     * Set property value by specified writer
     *
     * @param writer writer name
     * @param name property name
     * @param value property value to set
     */
    public void setValue(String writer, String name, Object value) {
        if (!prwhash.containsKey(writer))
            throw new IllegalArgumentException("There is no PropertyReaderWriter with name "+writer);
        ((PropertyReaderWriter)prwhash.get(writer)).setPropertyValue(name, value);
        if (debugrw != null)
            debugrw.setPropertyValue(name, value);
    }

    /**
     * Get property value by specified reader
     *
     * @param reader reader name
     * @param name property name
     * @return property value
     */
    public Object getValue(String reader, String name) {
        if (!prhash.containsKey(reader))
            throw new IllegalArgumentException("There is no PropertyReader with name "+reader);
        if (debugrw != null)
            debugrw.getPropertyValue(name);          
        return ((PropertyReader)prhash.get(reader)).getPropertyValue(name);
    }
}
