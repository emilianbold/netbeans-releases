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
import java.util.*;
import javax.swing.Action;
import org.netbeans.api.looks.*;
import org.netbeans.spi.looks.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import threaddemo.model.*;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends DefaultLook implements PhadhailListener {
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public void attachTo(Object o) {
        Phadhail ph = (Phadhail)o;
        //System.err.println("attached to " + ph);
        ph.addPhadhailListener((PhadhailListener)WeakListener.create(PhadhailListener.class, this, ph));
    }
    
    /* Uncomment if present in Look; then also remove WeakListener usage from attachTo:
    public void unregister(Object o) {
        Phadhail ph = (Phadhail)o;
        ph.removePhadhailListener(this);
    }
     */
    
    public boolean isLeaf(Object o) {
        Phadhail ph = (Phadhail)o;
        return !ph.hasChildren();
    }
    
    public List getChildObjects(Object o) {
        Phadhail ph = (Phadhail)o;
        return ph.getChildren();
    }
    
    public String getName(Object o) {
        Phadhail ph = (Phadhail)o;
        return ph.getName();
    }

    public String getDisplayName(Object o) {
        Phadhail ph = (Phadhail)o;
        return ph.getPath();
    }
    
    public boolean canRename(Object o) {
        return true;
    }
    
    public void setName(Object o, String newName) {
        Phadhail ph = (Phadhail)o;
        try {
            ph.rename(newName);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    
    public boolean canDestroy(Object o) {
        return true;
    }
    
    public void destroy(Object o) throws IOException {
        Phadhail ph = (Phadhail)o;
        ph.delete();
    }
    
    public Action[] getActions(Object o) {
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
    
    public NewType[] getNewTypes(Object o) {
        Phadhail ph = (Phadhail)o;
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    /* XXX currently Look does not define these:
    public Lookup getLookup(Object o) {
        Phadhail ph = (Phadhail)o;
        return new PhadhailLookup(ph);
    }
    
    // XXX currently there is nothing lighter-weight than AbstractLookup: #32203
    private static final class PhadhailLookup extends AbstractLookup implements InstanceContent.Convertor, PhadhailEditorSupport.Saver {
        
        private static final Object ED_KEY = "editorSupport";
        
        private final InstanceContent ic;

        private final Phadhail ph;
        
        public PhadhailLookup(Phadhail ph) {
            this(ph, new InstanceContent());
        }
        
        private PhadhailLookup(Phadhail ph, InstanceContent ic) {
            super(ic);
            this.ic = ic;
            this.ph = ph;
            if (!ph.hasChildren()) {
                ic.add(ED_KEY, this);
            }
        }
        
        public Object convert(Object obj) {
            if (obj == ED_KEY) {
                return new PhadhailEditorSupport(ph, this);
            } else {
                throw new IllegalStateException();
            }
        }
        
        public String displayName(Object obj) {
            return null;
        }
        
        public String id(Object obj) {
            return null;
        }
        
        public Class type(Object obj) {
            if (obj == ED_KEY) {
                return PhadhailEditorSupport.class;
            } else {
                throw new IllegalStateException();
            }
        }
        
        public void addSaveCookie(SaveCookie s) {
            ic.add(s);
        }
        
        public void removeSaveCookie() {
            ic.remove(lookup(SaveCookie.class));
        }
        
        public String toString() {
            return "PhadhailLookup<" + ph + ">";
        }
        
    }
    
    public Lookup getLookupForChildren(Object o) {
        return Lookup.EMPTY;
    }
     */
    
    public void childrenChanged(PhadhailEvent ev) {
        if (!java.awt.EventQueue.isDispatchThread()) Thread.dumpStack();//XXX
        //System.err.println("firing childrenChanged on " + ev.getPhadhail());
        refreshChildren(ev.getPhadhail());
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        if (!java.awt.EventQueue.isDispatchThread()) Thread.dumpStack();//XXX
        fireNameChange(ev.getPhadhail(), ev.getOldName(), ev.getNewName());
        fireDisplayNameChange(ev.getPhadhail(), ev.getOldName(), ev.getNewName());
    }
    
}
