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
