/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views;

import java.io.IOException;
import java.util.Collections;
import javax.swing.Action;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import threaddemo.data.*;
import threaddemo.model.*;

// XXX view of DOM tree too

/**
 * A plain node view of a phadhail tree.
 * @author Jesse Glick
 */
final class PhadhailNode extends AbstractNode implements PhadhailListener {
    
    private final Phadhail ph;
    
    public PhadhailNode(Phadhail ph) {
        super(ph.hasChildren() ? new PhadhailChildren(ph) : Children.LEAF, PhadhailLookups.getLookup(ph));
        this.ph = ph;
        ph.addPhadhailListener((PhadhailListener)WeakListeners.create(PhadhailListener.class, this, ph));
    }
    
    public String getName() {
        return ph.getName();
    }
    
    public String getDisplayName() {
        return ph.getPath();
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        ((PhadhailChildren)getChildren()).update();
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        fireNameChange(ev.getOldName(), ev.getNewName());
        fireDisplayNameChange(null, ev.getPhadhail().getPath());
    }
    
    public boolean canRename() {
        return true;
    }
    
    public void setName(String s) {
        try {
            ph.rename(s);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException {
        ph.delete();
    }
    
    public NewType[] getNewTypes() {
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(SaveAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            SystemAction.get(ToolsAction.class),
        };
    }
    
    private static final class PhadhailChildren extends Children.Keys {
        
        private final Phadhail ph;
        
        public PhadhailChildren(Phadhail ph) {
            this.ph = ph;
        }
        
        protected void addNotify() {
            update();
        }
        
        public void update() {
            setKeys(ph.getChildren());
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {new PhadhailNode((Phadhail)key)};
        }
        
    }
    
}
