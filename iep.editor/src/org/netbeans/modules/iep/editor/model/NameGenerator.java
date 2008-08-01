package org.netbeans.modules.iep.editor.model;

import java.util.List;
import java.util.logging.Level;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentType;

public class NameGenerator {

    private static final int MAX_COUNT = 1000000000;
    
    public static String generateNewName(Component parent, TcgComponentType ct) {
        String tn = ct.getName();
        List<Component> list = parent.getChildComponents();
        
            for (int i = 0; i < MAX_COUNT; i++) {
                String name = tn + i;
                boolean exist = false;
                for (int j = 0, J = list.size(); j < J; j++) {
                    Component c = list.get(j);
                    if (c.getName().equalsIgnoreCase(name)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    return name;
                }
            }
        
        return null;
    }
    
    public static String generateNewName(OperatorComponentContainer parent, TcgComponentType ct) {
        String tn = ct.getName();
        List<OperatorComponent> list = parent.getAllOperatorComponent();
        
            for (int i = 0; i < MAX_COUNT; i++) {
                String name = tn + i;
                boolean exist = false;
                for (int j = 0, J = list.size(); j < J; j++) {
                    OperatorComponent c = list.get(j);
                    if (c.getDisplayName().equalsIgnoreCase(name)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    return name;
                }
            }
        
        return null;
    }
    public static String generateId(OperatorComponentContainer parent, String prefix) {
        
            for (int i = 0; i < MAX_COUNT; i++) {
                String id = prefix + i;
                
                if (parent.findChildComponent(id) == null) {
                    return id;
                }
            }
         
        return null;
    }
    
    public static String generateSchemaName(SchemaComponentContainer parent) {
        String prefix = "schema"; //NOTI18N
        String schemaName = generateSchemaName(parent, prefix);
        
        return schemaName;
    }
    
        public static String generateSchemaName(SchemaComponentContainer parent, List<String> skipTheseSchemaNames) {
        String prefix = "schema"; //NOTI18N
        
        for (int i = 0; i < MAX_COUNT; i++) {
                    String name = prefix + i;
                    if ((parent.findSchema(name) == null) && !skipTheseSchemaNames.contains(name)) {
                        return name;
                    }
                }
            
            return null;
    }
        
    public static String generateSchemaName(SchemaComponentContainer parent, String prefix) {
        for (int i = 0; i < MAX_COUNT; i++) {
            String name = prefix + i;
            if (parent.findSchema(name) == null) {
                return name;
            }
        }
        return null;
    }
    
    public static String generateLinkName(LinkComponentContainer parent) {
        String prefix = "link"; //NOTI18N
        String schemaName = generateLinkName(parent, prefix);
        
        return schemaName;
    }
    
    public static String generateLinkName(LinkComponentContainer parent, String prefix) {
        for (int i = 0; i < MAX_COUNT; i++) {
            String name = prefix + i;
            if (parent.findLink(name) == null) {
                return name;
            }
        }
        return null;
    }
}
