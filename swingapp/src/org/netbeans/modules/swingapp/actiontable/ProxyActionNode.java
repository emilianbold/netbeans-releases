package org.netbeans.modules.swingapp.actiontable;

import org.netbeans.modules.swingapp.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

public class ProxyActionNode extends AbstractNode {
    
    public ProxyActionNode() {
        super(Children.LEAF);//new ProxyActionChildren());
        setDisplayName("Root");
    }
    
    public ProxyActionNode(Lookup lk) {
        super(Children.LEAF,lk);
    }
    
    public ProxyActionNode(ProxyActionChildren children, Lookup lk) {
        super(children,lk);
    }
    
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.createPropertiesSet();
        ProxyAction act = getLookup().lookup(ProxyAction.class);
        try {
            Property prop;
            prop = new PropertySupport.Reflection(act, String.class, "getMethodName",null) {
                public boolean canWrite() {
                    return false;
                }
            };
            prop.setName("methodName");
            
            prop = new PropertySupport.ReadOnly("methodName",String.class,"Method Name","A Method Name") {
                public Object getValue() {
                    return "asdf";
                }
            };
           
            
            set.put(prop);
            prop = new PropertySupport.Reflection(act, String.class, "getClassname",null);
            prop.setName("classname");
            set.put(prop);
            
            prop = new PropertySupport.Reflection(act, String.class, "getId",null);
            prop.setName("id");
            set.put(prop);
            
            prop = new PropertySupport.Reflection(act, Boolean.class, "isTaskEnabled",null);
            prop.setName("task");
            set.put(prop);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sheet.put(set);
        return sheet;
    }
    
}

