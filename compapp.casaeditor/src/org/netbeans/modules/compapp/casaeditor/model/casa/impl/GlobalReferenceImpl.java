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

package org.netbeans.modules.compapp.casaeditor.model.casa.impl;


import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;


/**
 *
 * @author Nam Nguyen
 * @author rico
 */
public class GlobalReferenceImpl<T extends ReferenceableCasaComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of GlobalReferenceImpl */
    //for use by factory, create from scratch
    public GlobalReferenceImpl(
            T referenced, 
            Class<T> type, 
            CasaComponentImpl parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public GlobalReferenceImpl(
            Class<T> type, 
            CasaComponentImpl parent, 
            String refString){
        super(type, parent, refString);
    }
    
    public T get() {
        CasaComponentImpl wparent = CasaComponentImpl.class.cast(getParent());
        if (super.getReferenced() == null) {
            String localName = getLocalName();
            T target = null;
            CasaModel model = wparent.getModel();
            target = new FindReferencedVisitor<T>(model.getRootComponent()).
                    find(localName, getType());
                
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public String getEffectiveNamespace() {
        
       return Constants.EMPTY_STRING; //http://java.sun.com/xml/ns/casa"; // TMP
    }
}
