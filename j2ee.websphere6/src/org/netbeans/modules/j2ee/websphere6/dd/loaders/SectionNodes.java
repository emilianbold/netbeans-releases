/*
 * SectionNodes.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
