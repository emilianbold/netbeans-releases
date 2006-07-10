/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
