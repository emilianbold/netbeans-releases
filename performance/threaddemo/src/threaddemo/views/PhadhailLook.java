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
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import threaddemo.model.*;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends DefaultLook {
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public Look.NodeSubstitute attachTo(Object o) {
        Phadhail ph = (Phadhail)o;
        return new PhadhailWrapper(ph, this);
    }
    
    public boolean isLeaf(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return !ph.hasChildren();
    }
    
    public List getChildObjects(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return ph.getChildren();
    }
    
    public String getName(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return ph.getName();
    }

    public String getDisplayName(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return ph.getPath();
    }
    
    public boolean canRename(Look.NodeSubstitute substitute) {
        return true;
    }
    
    public void setName(Look.NodeSubstitute substitute, String newName) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        try {
            ph.rename(newName);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    
    public boolean canDestroy(Look.NodeSubstitute substitute) {
        return true;
    }
    
    public void destroy(Look.NodeSubstitute substitute) throws IOException {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        ph.delete();
    }
    
    public Action[] getActions(Look.NodeSubstitute substitute) {
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
    
    public NewType[] getNewTypes(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    private static final class PhadhailWrapper extends Look.NodeSubstitute implements InstanceContent.Convertor, PhadhailEditorSupport.Saver, PhadhailListener {
        
        private static final Object ED_KEY = "editorSupport";
        
        private final InstanceContent c;
        
        public PhadhailWrapper(Phadhail ph, Look l) {
            this(ph, l, new InstanceContent());
        }
        
        private PhadhailWrapper(Phadhail ph, Look l, InstanceContent c) {
            // XXX currently there is nothing lighter-weight than AbstractLookup: #32203
            super(ph, l, new AbstractLookup(c));
            this.c = c;
            if (!ph.hasChildren()) {
                c.add(ED_KEY, this);
            }
            ph.addPhadhailListener(this);
            //System.err.println("Created " + this);
            //Thread.dumpStack();
        }
        
        protected void unregister() {
            ((Phadhail)getRepresentedObject()).removePhadhailListener(this);
            //System.err.println("Disposed of " + this);
        }
        
        public Object convert(Object obj) {
            if (obj == ED_KEY) {
                return new PhadhailEditorSupport((Phadhail)getRepresentedObject(), this);
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
            return PhadhailEditorSupport.class;
        }
        
        public void addSaveCookie(SaveCookie s) {
            c.add(s);
        }
        
        public void removeSaveCookie() {
            c.remove(getLookup().lookup(SaveCookie.class));
        }
        
        public void childrenChanged(PhadhailEvent ev) {
            refreshChildren();
        }
        
        public void nameChanged(PhadhailNameEvent ev) {
            fireNameChange(ev.getOldName(), ev.getNewName());
            fireDisplayNameChange(ev.getOldName(), ev.getNewName());
        }
        
        public String toString() {
            return "PhadhailWrapper<" + getRepresentedObject() + ">";
        }
        
    }
    
}
