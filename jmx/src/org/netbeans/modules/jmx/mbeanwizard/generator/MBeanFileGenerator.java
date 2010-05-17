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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.generator;
import org.netbeans.modules.jmx.MBeanDO;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Generic MBean File generator.
 * @author thomas
 */
public abstract class MBeanFileGenerator {
    /**
     * Generates all the files for the new MBean.
     * @param mbean <CODE>MBeanDO</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated file which is MBean class.
     */
    public abstract FileObject generateMBean(MBeanDO mbean)
            throws java.io.IOException, Exception;
    
    // returns the Dynamic MBean file template
    protected static DataObject getTemplate(String templatePath) throws Exception {
        /* get the template DataObject... */
        FileObject template = FileUtil.getConfigFile(templatePath);
        return DataObject.find(template);
    }
    
    /**
     * Add the block close code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    public static void closeBloc(StringBuffer sb) {
        sb.append("}\n");// NOI18N
    }
    
    /**
     * Add a new line code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    public static void newLine(StringBuffer sb) {
        sb.append("\n");// NOI18N
    }
    
  
    /*
    public static Type getType(JavaModelPackage pkg, String typeName) {
        if (typeName.endsWith("[]")) { // NOI18N
            org.netbeans.jmi.javamodel.Array array = pkg.getArray().resolveArray(
                    getType(pkg,
                    WizardHelpers.getFullTypeName(
                    typeName.substring(0,typeName.length() - 2)))); // NOI18N
            return array;
        } else
            return pkg.getType().resolve(typeName);
    }
    
    public static MultipartId getTypeRef(JavaModelPackage pkg, String typeName) {
        return pkg.getMultipartId().createMultipartId(
                    typeName,
                    null,
                    Collections.EMPTY_LIST);
    }
    */
}
