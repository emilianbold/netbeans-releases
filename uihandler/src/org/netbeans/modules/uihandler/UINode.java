/*
 * UIHandler.java
 *
 * Created on 10. srpen 2006, 14:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uihandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

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
    }
}
