/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPQNames;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Import;
import org.w3c.dom.Element;


/**
 *
 * @author radval
 */
public class ImportImpl extends IEPComponentBase implements Import {

	
	
    public ImportImpl(IEPModel model) {
        this(model, createNewElement(IEPQNames.IMPORT.getQName(), model));
    }
    
    public ImportImpl(IEPModel model, Element e) {
        super(model, e);
    }

    public void accept(IEPVisitor visitor) {
        visitor.visitImport(this);
    }

    public IEPComponent createChild(Element childEl) {
        return null;
    }

    public String getLocation() {
        return getAttribute(ATTR_LOCATION);
    }

    public void setLocation(String location) {
        setAttribute(LOCATIION_PROPERTY, ATTR_LOCATION, location);
    }

    public String getNamespace() {
        return getAttribute(ATTR_NAMESPACE);
    }

    public void setNamespace(String namespace) {
        setAttribute(NAMESPACE_PROPERTY, ATTR_NAMESPACE, namespace);
    }

    public String getImportType() {
        return getAttribute(ATTR_IMPORT_TYPE);
    }

    public void setImportType(String value) {
        setAttribute(IMPORT_TYPE_PROPERTY, ATTR_IMPORT_TYPE, value);
    }
    
    public Component getParentComponent() {
        return (Component) getParent();
    }

    
}
