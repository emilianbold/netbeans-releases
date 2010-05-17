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
 * SessionBeanNodeChildren.java
 *
 * Created on May 3, 2004, 6:33 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The child nodes of the session bean node, which are MethodNodes
 *
 * @author  cao
 */
public class SessionBeanNodeChildren extends Children.Keys 
{
    private EjbGroup ejbGroup;
    private EjbInfo ejbInfo;
    
    /** Creates a new instance of SessionBeanNodeChildren */
    public SessionBeanNodeChildren( EjbGroup ejbGroup, EjbInfo ejbInfo ) 
    {
        this.ejbGroup = ejbGroup;
        this.ejbInfo = ejbInfo;
    }
    
    // Populate the children node with the business methods of
    // the session bean
    protected org.openide.nodes.Node[] createNodes( Object key ) 
    {
        if( key instanceof MethodInfo )
        {
            MethodInfo mInfo = (MethodInfo)key;
            
            // Only display business methods
            if( mInfo.isBusinessMethod() )
                return new Node[] { new MethodNode( ejbGroup, mInfo, ejbInfo ) };
        }
        
        return null;
    }
    
    protected void addNotify() 
    {
        // Set the keys for the children
        // Note: can not just use the method name as key because the method can be overloaded
        
        super.addNotify();
        super.setKeys( ejbInfo.getMethods() );
    }
    
    protected void removeNotify() 
    {
        setKeys( Collections.EMPTY_SET );
        super.removeNotify();
    }
    
}
