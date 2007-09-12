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
package org.netbeans.modules.xslt.project.anttasks.jbi;

import javax.xml.namespace.QName;
import org.w3c.dom.Node;

public class TMapServiceEntry {
    //Member variable representing partner link name
    private QName mPartnerLinkNameQname = null;
    private String mRoleName = null;
    private String mOperation = null;
    private String mFile = null;
    private String mTransformJBI = null;
    
    private Node mNode;

        /**
     * Constructor
     * @param partnerLinkName Partner link name
     * @param portName    Port name
     * @param partnerLinkNS Namespace URI of the Partner Link
     * @param portNameNS Namespace URI of the portname
     * @param rolename  role name
     */
    public TMapServiceEntry(QName partnerLinkNameQname, 
            String roleName, 
            String operation, 
            String file, 
            String transformJBI, 
            Node node
            ) 
    {
        assert partnerLinkNameQname != null;
        
        mPartnerLinkNameQname = partnerLinkNameQname;
        mRoleName = roleName;
        mOperation = operation;
        mFile = file;
        mTransformJBI = transformJBI;
        
        mNode = node;
    }
    

    public QName getPartnerLinkNameQname() {
        return mPartnerLinkNameQname;
    }
    
    
    /**
     * Return Role name
     * @return String role name
     */   
    public String getRoleName(){
        return mRoleName;
    }
    
    /**
     * Return operation
     * @return String operation
     */   
    public String getOperation(){
        return mOperation;
    }

    
    /**
     * Return file
     * @return String file
     */   
    public String getFile(){
        return mFile;
    }

    /**
     * Return TransformJBI
     * @return String transformJBI
     */   
    public String getTransformJBI(){
        return mTransformJBI;
    }

    public Node  getNode() {
        return mNode;
    }
    
    public boolean equals(Object obj) {
        if (! (obj instanceof TMapServiceEntry)) {
            return false;
        }
        TMapServiceEntry serviceEntry = (TMapServiceEntry)obj;
        String pltLocalName = this.mPartnerLinkNameQname.getLocalPart();
        String servicePltLocalName = serviceEntry.getPartnerLinkNameQname().getLocalPart();
        
        
        if (pltLocalName != null && pltLocalName.equals(servicePltLocalName)  && 
            this.mRoleName.equals(serviceEntry.getRoleName()) &&
            this.mOperation.equals(serviceEntry.getOperation()))
        {
                return true;
        }
        return false;
    }
    
    public int hashCode() {
       return (this.mPartnerLinkNameQname.getLocalPart()+this.mRoleName+this.mOperation).hashCode();
    }
        
}
