/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.xml.pof.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.namespace.QName;
import org.netbeans.modules.coherence.xml.pof.AllowInterfaces;
import org.netbeans.modules.coherence.xml.pof.AllowSubclasses;
import org.netbeans.modules.coherence.xml.pof.ClassName;
import org.netbeans.modules.coherence.xml.pof.DefaultSerializer;
import org.netbeans.modules.coherence.xml.pof.Include;
import org.netbeans.modules.coherence.xml.pof.InitParam;
import org.netbeans.modules.coherence.xml.pof.InitParams;
import org.netbeans.modules.coherence.xml.pof.ParamType;
import org.netbeans.modules.coherence.xml.pof.ParamValue;
import org.netbeans.modules.coherence.xml.pof.PofConfig;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponent;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.SerializerType;
import org.netbeans.modules.coherence.xml.pof.TypeId;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public enum PofConfigElements {
    ALLOWINTERFACES(AllowInterfaces.XML_TAG_NAME),
    ALLOWSUBCLASSES(AllowSubclasses.XML_TAG_NAME),
    CLASSNAME(ClassName.XML_TAG_NAME),
    DEFAULTSERIALIZER(DefaultSerializer.XML_TAG_NAME),
    INCLUDE(Include.XML_TAG_NAME),
    INITPARAM(InitParam.XML_TAG_NAME),
    INITPARAMS(InitParams.XML_TAG_NAME),
    PARAMTYPE(ParamType.XML_TAG_NAME),
    PARAMVALUE(ParamValue.XML_TAG_NAME),
    POFCONFIG(PofConfig.XML_TAG_NAME),
    SERIALIZER(Serializer.XML_TAG_NAME),
    SERIALIZERTYPE(SerializerType.XML_TAG_NAME),
    TYPEID(TypeId.XML_TAG_NAME),
    USERTYPE(UserType.XML_TAG_NAME),
    USERTYPELIST(UserTypeList.XML_TAG_NAME);
    
    PofConfigElements (String name) {
        this.name = name;
    }
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public QName getQName() {
        return new QName(PofConfigComponent.NAMESPACE, getName());
    }
    
    public static Set<QName> allQNames() {
        if ( myQNames.get() == null ) {
            Set<QName> set = new HashSet<QName>( values().length );
            for (PofConfigElements element : values() ) {
                set.add( element.getQName() );
            }
            myQNames.compareAndSet( null, set );
        }
        return myQNames.get();
    }
    
    private String myName;

    private static AtomicReference<Set<QName>> myQNames =
        new AtomicReference<Set<QName>>();
}
