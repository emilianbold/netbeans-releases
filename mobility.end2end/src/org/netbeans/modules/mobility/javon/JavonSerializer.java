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

package org.netbeans.modules.mobility.javon;

import java.util.Map;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;

/**
 *
 */
public interface JavonSerializer {
    
    /**
     * Return name of the JavonSerializer
     * 
     * @return name of the serializer
     */
    public String getName();
    
    /**
     * Return true when the given type is supported
     * 
     * @param traversable 
     * @param type 
     * @return true when the type is supported
     */
    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache );
    
    /**
     * 
     * @param traversable 
     * @param type 
     * @return 
     */
    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache );
    
    /**
     * Return fully qualified or short name of the given type. The short name
     * could be used for java.lang package when no import is necesary
     * 
     * @param type
     * @return fully qualified name or short name of type
     */
    public String instanceOf( JavonMapping mapping, ClassData type  );
    
    /**
     * 
     * @param type 
     * @param variable 
     * @return 
     */
    public String toObject( JavonMapping mapping, ClassData type, String variable  );
    
    /**
     * 
     * @param type 
     * @param object 
     * @return 
     */
    public String fromObject( JavonMapping mapping, ClassData type, String object  );
    
    /**
     * 
     * @param type 
     * @param stream 
     * @param object 
     * @return 
     */
    public String toStream( JavonMapping mapping, ClassData type, String stream, String object );
    
    /**
     * Create serialization block for given type
     * 
     * <p>Sample implementation:
     * <code>
     * return object + " = new Integer(" + stream + ".readInt());";
     * </code>
     * 
     * @param type 
     * @param stream 
     * @param object 
     * @return 
     */
    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object );
    
    /**
     * Return Set of all types on which is the root ClassData object 
     * depending
     * 
     * @param rootClassData root class data
     * @return Set<ClassData> of all referenced types
     */
    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes );
    
    
}
