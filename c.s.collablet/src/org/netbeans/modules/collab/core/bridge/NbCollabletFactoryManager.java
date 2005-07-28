/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.core.bridge;

import com.sun.collablet.*;

import org.openide.util.*;

import java.beans.*;

import java.util.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NbCollabletFactoryManager extends CollabletFactoryManager implements LookupListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Lookup.Result results;

    /**
     *
     *
     */
    public NbCollabletFactoryManager() {
        super();

        // Lookup all the CollabletFactories in the system.  By default, these
        // should be registered in the /Services/Collaboration/Channels
        // folder.  The results will be updated as they change.
        results = Lookup.getDefault().lookup(new Lookup.Template(CollabletFactory.class));
        results.addLookupListener(this);

        // Workaround for not finding objects registered through lookup
        //		FileObject fileObject=Repository.getDefault().getDefaultFileSystem()
        //			.findResource("Services/Collaboration/Channels"); // NOI18N
        //		FileObject[] children=fileObject.getChildren();
        //
        //Debug.out.println("Number of children = "+children.length);
        //		assert children!=null && children.length>0:
        //			"Standard channels not found in system filesystem";
        //for (int i=0; i<children.length; i++)
        //{
        //	Debug.out.println("- Child "+i+": "+children[i]);
        //	try
        //	{
        //		DataObject.find(children[i]);
        //	}
        //	catch (Exception e)
        //	{
        //		Debug.debugNotify(e);
        //	}
        //}
    }

    /**
     *
     *
     */
    public synchronized CollabletFactory[] getCollabletFactories() {
        //		Lookup.Result results=Lookup.getDefault().lookup(
        //			new Lookup.Template(ChannelProvider.class));
        Collection providers = results.allInstances();

        return (CollabletFactory[]) providers.toArray(new CollabletFactory[providers.size()]);
    }

    /**
     *
     *
     */
    public void resultChanged(LookupEvent lookupEvent) {
        getChangeSupport().firePropertyChange(PROP_COLLABLET_FACTORIES, null, null);
    }
}
