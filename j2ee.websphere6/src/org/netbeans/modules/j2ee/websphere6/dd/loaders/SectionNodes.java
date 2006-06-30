/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 *
 * @author dlm198383
 */
public class SectionNodes {

    /** Creates a new instance of SectionNodes */
    public SectionNodes() {
    }
    public static class ResRefNode extends org.openide.nodes.AbstractNode {
        public ResRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");
            
        }
    }
    
    public static class EjbRefNode extends org.openide.nodes.AbstractNode {
        public  EjbRefNode() {            
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");                       
        }
        
    }
    
    public static class ResEnvRefNode extends org.openide.nodes.AbstractNode {
        public ResEnvRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #"); 
            //setIconBaseWithExtension("org/netbeans/modules/webbndmultiview/ws.gif"); //NOI18N
        }
    }   
}
