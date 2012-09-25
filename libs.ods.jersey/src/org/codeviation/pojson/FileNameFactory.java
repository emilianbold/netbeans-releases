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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.commons.patterns.Factory;

class FileNameFactory<T> implements Factory<String, T> {
        
    private String fileNameFormat;
    private List<Field> idParts;
    
    public FileNameFactory(Class<T> clazz, Collection<Field> fields) {
        this.idParts = getIdParts(fields); 
        Pojson.FileNameFormat fnfA = clazz.getAnnotation(Pojson.FileNameFormat.class);
        this.fileNameFormat = fnfA == null ? null : fnfA.value();
    }
    
    public String create(T object) {
        
        Object[] ids = new Object[idParts.size()];
        for (int i = 0; i < idParts.size(); i++) {
            try {
                Field field = idParts.get(i);
                field.setAccessible(true);
                ids[i] = field.get(object);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StoreInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StoreInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        if ( fileNameFormat == null ) {
            for (Object id : ids) {
                sb.append(id == null ?  "null" : id.toString());
            }
            sb.append(Pojson.DEFAULT_EXTENSION);
        }
        else {
            Formatter f = new Formatter();
            f.format(fileNameFormat, ids);
            sb.append(f.toString());    
        }
        return sb.toString();
    }
    
    public List<Field> getIdParts(Collection<Field> fields) {
        
        SortedMap<Integer,Field> numberedIds= new TreeMap<Integer, Field>();
        List<Field> anonIds = new ArrayList<Field>();

        Field first = null;

        for (Field f : fields) {

            if (first == null) {
                first = f;
            }

            Pojson.IdPart idAn = f.getAnnotation(Pojson.IdPart.class);
            if (idAn != null ) {
                int value = idAn.value();
                if ( value == -1 ) {
                    anonIds.add(f);                        
                }
                else {
                    numberedIds.put(value, f);
                }
            }

        }

        int idsSize = numberedIds.size() + anonIds.size();
        List<Field> idFields = new ArrayList(idsSize == 0 ? 1 : idsSize);

        if ( idsSize == 0 ) {
            if ( first != null ) {                    
                idFields.add(first);
            }
        }
        else {
            idFields.addAll(numberedIds.values());
            idFields.addAll(anonIds);
        }
         
        return idFields;
    }

}
