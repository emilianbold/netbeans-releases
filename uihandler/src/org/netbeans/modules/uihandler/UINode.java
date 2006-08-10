/*
 * UIHandler.java
 *
 * Created on 10. srpen 2006, 14:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uihandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author Jaroslav Tulach
 */
public class UINode extends AbstractNode {
    private LogRecord log;

    public UINode(LogRecord r) {
        super(Children.LEAF);
        log = r;
        setName(r.getMessage());
        try {
            Sheet.Set s = new Sheet.Set();
            s.setName(Sheet.PROPERTIES);
            s.put(new PropertySupport.Reflection(log, long.class, "millis")); // NOI18N
            
            getSheet().put(s);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }
    
}
