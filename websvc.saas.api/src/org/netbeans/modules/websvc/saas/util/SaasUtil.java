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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;

/**
 *
 * @author nam
 */
public class SaasUtil {

    public static SaasGroup loadSaasGroup(File input) throws IOException, JAXBException {
        InputStream in = new FileInputStream(input);
        try {
            return loadSaasGroup(new FileInputStream(input));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static SaasGroup loadSaasGroup(InputStream input) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Group.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement<Group> groupElement = (JAXBElement)unmarshaller.unmarshal(input);
        if (groupElement != null && groupElement.getValue() != null) {
            return new SaasGroup(null, groupElement.getValue());
        }
        return null;
    }

    public static void saveSaasGroup(SaasGroup saasGroup, File outFile) throws IOException, JAXBException {
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            saveSaasGroup(saasGroup, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public static final QName QNAME_GROUP = new QName(Saas.NS_SAAS, "group");
    
    public static void saveSaasGroup(SaasGroup saasGroup, OutputStream output) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Group.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<Group> jbe = new JAXBElement<Group>(QNAME_GROUP, Group.class, saasGroup.getDelegate());
        marshaller.marshal(jbe, output);
    }
}
