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
package org.netbeans.modules.collab.channel.filesharing.util;

import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;


/**
 * QueueItem
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class QueueItem extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* name */
    private String name = null;

    /* CCollab */
    private CCollab collabBean = null;

    /* size */
    private long size;

    /**
         *
         *
         */
    public QueueItem(String name, CCollab collabBean, long size) {
        super();
        this.name = name;
        this.collabBean = collabBean;
        this.size = size;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         * getName
         *
         * @return name
         */
    public String getName() {
        return this.name;
    }

    /**
         * getCollab
         *
         * @return collabBean
         */
    public CCollab getCollab() {
        return this.collabBean;
    }

    /**
         * getSize
         *
         * @return size
         */
    public long getSize() {
        return this.size;
    }
}
