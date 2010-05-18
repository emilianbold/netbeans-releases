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
/*
 * EjbRootNodeChildren.java
 *
 * Created on May 3, 2004, 11:04 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelListener;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The container for the EjbRootNode children
 *
 * @author cao
 */
public class EjbRootNodeChildren extends Children.Keys implements EjbDataModelListener
{
    public EjbRootNodeChildren() {
    }
    
    protected org.openide.nodes.Node[] createNodes( Object key ) 
    {
        if( key instanceof String ) 
        {
            EjbGroup ejbGrp = EjbDataModel.getInstance().getEjbGroup( (String)key );
            
            Node node = new EjbGroupNode( ejbGrp );
            return new Node[] {node};
        } 
        else
            return null;
    }
    
    protected void addNotify() 
    {
        // Set the keys for the children
        
        super.addNotify();
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
        
        // Listen on the changes in the EjbDataModel
        EjbDataModel.getInstance().addListener( this );
    }
    
    protected void removeNotify() 
    {
        setKeys( Collections.EMPTY_SET );
        super.removeNotify();
        
        // No need to listen on the data model any more
        EjbDataModel.getInstance().removeListener( this );
    }
    
    public void groupAdded(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupChanged(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupDeleted(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
    public void groupsDeleted() 
    {
        setKeys( EjbDataModel.getInstance().getEjbGroupNames() );
    }
    
}
