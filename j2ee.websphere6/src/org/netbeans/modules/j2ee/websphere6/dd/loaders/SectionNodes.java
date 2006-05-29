/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
