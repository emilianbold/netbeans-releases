/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.cnd.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public class CCFSrcFileIterator implements TemplateWizard.Iterator {
    /** Holds list of event listeners */
    private static Vector listenerList = null;

    private WizardDescriptor.Panel targetChooserDescriptorPanel;

    public WizardDescriptor.Panel current() {
	return targetChooserDescriptorPanel;
    }
    
    public boolean hasNext() {
	return false;
    }

    public boolean hasPrevious() {
	return false;
    }
    
    public synchronized void nextPanel() {
    }
    
    public synchronized void previousPanel() {
    }

    public void initialize (TemplateWizard wiz) {
	targetChooserDescriptorPanel = wiz.targetChooser();
    }
    
    public void uninitialize (TemplateWizard wiz) {
    }
    
    public Set instantiate (TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder ();
        DataObject template = wiz.getTemplate ();
	String ext = template.getPrimaryFile().getExt();

	String filename = wiz.getTargetName();
	if (filename != null && ext != null) {
	    if (filename.endsWith("." + ext)) { // NOI18N
		// strip extension, it will be added later ...
		filename = filename.substring(0, filename.length()-(ext.length()+1));
	    }
	}

	DataObject result = template.createFromTemplate(targetFolder, filename);


	if (result != null) {
	    fireWizardEvent(new EventObject(result));
	    OpenCookie open = (OpenCookie) result.getCookie (OpenCookie.class);
	    if (open != null) {
		open.open ();
	    }
	}

	return Collections.singleton(result);
    }

    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
       
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public String name() {
	return ""; // NOI18N ?????
    }

    /* ------------------------------------------*/

    protected static void fireWizardEvent(EventObject e)
    {
	Vector listeners = getListenerList();

	for (int i = listeners.size()-1; i >= 0; i--) {
	    ((SrcFileWizardListener)listeners.elementAt(i)).srcFileCreated(e);
	}
    }

    private static Vector getListenerList() {
	if (listenerList == null) {
	    listenerList = new Vector(0);
	}
	return listenerList;
    }

    public static void addSrcFileWizardListener(SrcFileWizardListener l) {
	getListenerList().add(l);
    }

    public static void removeSrcFileWizardListener(SrcFileWizardListener l) {
	getListenerList().remove(l);
    }

}

