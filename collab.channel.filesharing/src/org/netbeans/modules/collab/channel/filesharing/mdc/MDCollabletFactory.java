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
package org.netbeans.modules.collab.channel.filesharing.mdc;

import com.sun.collablet.Collablet;
import com.sun.collablet.CollabletFactory;
import com.sun.collablet.Conversation;

import org.openide.options.*;
import org.openide.util.*;

import java.util.*;

import javax.swing.*;


/**
 * FilesharingProvider - creates MDCollablet instance
 * @author  Todd Fast, todd.fast@sun.com
 */
public class MDCollabletFactory extends Object implements CollabletFactory {
    /**
     *
     *
     */
    public MDCollabletFactory() {
        super();
    }

    /**
     *
     * @return identifier
     */
    public String getIdentifier() {
        return "filesharing"; // NOI18N
    }

    /**
     *
     * @return displayName
     */
    public String displayName() {
        return getDisplayName();
    }

    /**
     *
     * @return channel provide display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(MDCollabletFactory.class, "LBL_MDCollabletFactory_DisplayName");
    }

    /**
     *
     * @return channel
     * @param conversation
     */
    public Collablet createInstance(Conversation conversation) {
        return new MDCollablet(conversation);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property methods
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected boolean clearSharedData()
    //	{
    //		super.clearSharedData();
    //		return false;
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public Integer getLockTimoutInterval()
    //	{
    ////		return lockTimoutInterval;
    ////		return (Integer)getProperty("lockTimoutInterval");
    //	}
    //
    //	
    //	/**
    //	 *
    //	 *
    //	 */
    //	public void setLockTimoutInterval(Integer value)
    //	{
    ////		lockTimoutInterval=value;
    ////		putProperty("lockTimoutInterval",value,true);
    //	}
    //
    //
    //
    //	////////////////////////////////////////////////////////////////////////////
    //	// Lookup methods
    //	////////////////////////////////////////////////////////////////////////////
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public static MDCollabletFactory getDefault()
    //	{
    ////		MDCollabletFactory result=(MDCollabletFactory)
    ////			Lookup.getDefault().lookup(MDCollabletFactory.class);
    //
    //		MDCollabletFactory result=(MDCollabletFactory)
    //			findObject(MDCollabletFactory.class,true);
    //		assert result!=null:
    //			"Default MDCollabletFactory object was null";
    //		return result;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    //	private int lockTimoutInterval=2;
}
