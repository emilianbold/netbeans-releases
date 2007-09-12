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

package org.netbeans.modules.wsdlextensions.file.model.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

import org.netbeans.modules.wsdlextensions.file.model.FileAddress;
import org.netbeans.modules.wsdlextensions.file.model.FileComponent;
import org.netbeans.modules.wsdlextensions.file.model.FileQName;

/**
 * @author sweng
 */
public class FileAddressImpl extends FileComponentImpl implements FileAddress {
    public FileAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FileAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(FileQName.ADDRESS.getQName(), model));
    }
    
    public void accept(FileComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setRelativePath(boolean val) {
        setAttribute(ATTR_FILE_RELATIVE_PATH, FileAttribute.FILE_ADDRESS_RELATIVEPATH_PROPERTY, val? "true" : "false");
    }
    
    public boolean getRelativePath() {
        String s = getAttribute(FileAttribute.FILE_ADDRESS_RELATIVEPATH_PROPERTY);
        return s != null && s.equals("true");
    }

    public void setFileDirectory(String val) {
        setAttribute(ATTR_FILE_ADDRESS, FileAttribute.FILE_ADDRESS_FILEDIRECTORY_PROPERTY, val);
    }
    
    public String getFileDirectory() {
        return getAttribute(FileAttribute.FILE_ADDRESS_FILEDIRECTORY_PROPERTY);
    }
    
    public void setPathRelativeTo(String val) {
        setAttribute(ATTR_FILE_PATH_RELATIVE_TO, FileAttribute.FILE_ADDRESS_PATHRELATIVETO_PROPERTY, val);
    }
    
    public String getPathRelativeTo() {
        return getAttribute(FileAttribute.FILE_ADDRESS_PATHRELATIVETO_PROPERTY);
    }

    public void setLockName(String val) {
        setAttribute(ATTR_FILE_LOCK_NAME, FileAttribute.FILE_ADDRESS_LOCK_NAME, val);
    }

    public String getLockName() {
        return getAttribute(FileAttribute.FILE_ADDRESS_LOCK_NAME);
    }

    public void setWorkArea(String val) {
        setAttribute(ATTR_FILE_WORK_AREA, FileAttribute.FILE_ADDRESS_WORK_AREA, val);
    }

    public String getWorkArea() {
        return getAttribute(FileAttribute.FILE_ADDRESS_WORK_AREA);
    }

    public void setSeqName(String val) {
        setAttribute(ATTR_FILE_SEQ_NAME, FileAttribute.FILE_ADDRESS_SEQ_NAME, val);
    }

    public String getSeqName() {
        return getAttribute(FileAttribute.FILE_ADDRESS_SEQ_NAME);
    }
    
}
