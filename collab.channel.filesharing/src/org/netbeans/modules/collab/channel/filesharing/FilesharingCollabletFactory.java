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
package org.netbeans.modules.collab.channel.filesharing;

import com.sun.collablet.*;

import org.openide.options.*;
import org.openide.util.*;

import java.util.*;

import javax.swing.*;


/**
 * FilesharingProvider - creates FilesharingCollablet instance
 * @author  Todd Fast, todd.fast@sun.com
 */
public class FilesharingCollabletFactory extends Object implements CollabletFactory, FilesharingConstants {
    /**
     *
     *
     */
    public FilesharingCollabletFactory() {
        super();
    }

    /**
     *
     * @return identifier
     */
    public String getIdentifier() {
        //return "filesharing"; // NOI18N
        return FILESHARING_NAMESPACE;
    }

    /**
     * get channel provider displayName
     *
     * @return channel provider display name
     */
    public String displayName() {
        return getDisplayName();
    }

    /**
     *
     * @return display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(FilesharingCollabletFactory.class, "LBL_FilesharingCollabletFactory_DisplayName");
    }

    /**
     *
     * @param conversation
     * @return channel
     */
    public Collablet createInstance(Conversation conversation) {
        return new FilesharingCollablet(conversation);
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
    //	public static FilesharingCollabletFactory getDefault()
    //	{
    ////		FilesharingCollabletFactory result=(FilesharingCollabletFactory)
    ////			Lookup.getDefault().lookup(FilesharingCollabletFactory.class);
    //
    //		FilesharingCollabletFactory result=(FilesharingCollabletFactory)
    //			findObject(FilesharingCollabletFactory.class,true);
    //		assert result!=null:
    //			"Default FilesharingCollabletFactory object was null";
    //		return result;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    //	private int lockTimoutInterval=2;
}
