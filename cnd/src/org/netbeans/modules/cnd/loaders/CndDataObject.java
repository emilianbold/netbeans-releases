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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.util.Set;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;
import org.openide.nodes.Node.Cookie;

/**
 *  Abstract superclass of a C/C++/Fortran DataObject.
 */
public abstract class CndDataObject extends MultiDataObject {

    /** Serial version number */
    static final long serialVersionUID = -6788084224129713370L;

    /** the object file extension */
    public final static String OBJ_EXTENSION = "o";			// NOI18N

    /** Store some information for ElfTaster metrics */
    private static CircularQueue cq;


    public CndDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
	super(pf, loader);
	init();

	if (cq == null) {
	    cq = new CircularQueue(10, 0L);
	}
	cq.add(System.currentTimeMillis());
    }

    /**
     *  Initialize cookies for this DataObject. This method may get overridden
     *  by derived classes who need to use a different set of cookies.
     */
    protected void init() {
	CookieSet cookies = getCookieSet();
	Entry primary = getPrimaryEntry();

	cookies.add(new CppEditorSupport(primary.getDataObject()));
	cookies.add(new BinaryExecSupport(primary));
    }


    /**
     *  The DeleteList is the list of suffixes which should be deleted during
     *  a clean action.
     */
    public final Set getDeleteList() {
	return secondaryEntries();
    }


    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }  
    
    void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }
    
    void removeSaveCookie(SaveCookie save) {
        getCookieSet().remove(save);
    }

    public void addCookie(Cookie nc) {
        getCookieSet().add(nc);
    }
    
    public void removeCookie(Cookie nc) {
        getCookieSet().remove(nc);
    }

    @Override
    protected abstract Node createNodeDelegate();

    /**
     *  Remove a secondary entry from the list. Access method
     *
     *  @param fe the entry to remove
     */
    public final void removeSecondaryEntryAccess(Entry fe) {
        removeSecondaryEntry(fe);
    }

    /**
     *  Creates new object from template. Check to make sure the user
     *  has entered a valid string. 
     *
     *  @param df Folder to create the template in
     *  @param name New template name
     *  @exception IOException
     */
    @Override
    protected DataObject handleCreateFromTemplate (DataFolder df, String name)
	throws IOException {

        if ((name != null) && (!isValidName(name))) {
            throw new IOException(NbBundle.getMessage(CndDataObject.class,
			"FMT_Not_Valid_FileName", name)); // NOI18N
	}
        return super.handleCreateFromTemplate(df, name);
    }


    /**
     * Is the given name a valid template name for our module?
     * In other words, is it a valid basename for a source/data file
     * created by our templates, or is it even a valid filename we will
     * allow you to rename source files to?
     * <p>
     * Note that Unix allows you to name files anything (except for null
     * characters and the slash character) but we're making a stricter
     * restriction here. We Want To Help You (tm). No blank file names.
     * No control characters in the filename. No meta characters in the
     * filename.   (Possibly controversial: no whitespace in filename)
     *
     * @param name Name to check
     */
    static boolean isValidName(String name) {
	int len = name.length();
        
	if (len == 0) {
	    return false;
	}
	for (int i = 0; i < len; i++) {
	    char c = name.charAt(i);
	    if (Character.isISOControl(c)) {
		return false;
	    }
	}
	return true;
    }


    public long sum(int num) {
	return cq.sum(num);
    }

    public long mostRecent() {
	return cq.mostRecent();
    }


    /**
     *  This CircularQueue is used to store historical information used in
     *  computing some usage metrics. Currently, the only user is the Elf
     *  recognition code in ElfTaster. The most recent times a CndDataObject
     *  was created are stored in the queue. This information is used by the
     *  ElfTaster in computing its Dynamic tasting policy.
     */
    public static class CircularQueue {

	/** Store times last n DataObjects were created */
	private Long[] queue;

	/** Index of the add spot */
	private int head;

	/** Count of items which have been added to the queue */
	private int count;

	/** The size of the queue */
	private int size;

	/** The time (in millis) the queue was created */
	private long created;

	/**
	 *  Create and initialize the queue.
	 *
	 *  @param size	The size of the queue
	 */
	public CircularQueue(int size) {
	    this.size = size;
	    queue = new Long[size];
	    created = System.currentTimeMillis();
	    head = 0;
	    count = 0;
	}

	public CircularQueue(int size, long created) {
	    this(size);
	    this.created = created;
	}

	/**
	 *  Add another time value to the queue, possibly removing the oldest.
	 *
	 *  @param val	The value to store
	 */
	public void add(long val) {
	    count++;
	    queue[head++] = new Long(val - created); 
	    if (head >= size) {
		head = 0;
	    }
	}

	/**
	 *  Calculate the sum of the last <I>num</I> items added.
	 *
	 *  @param num	The number of items to sum
	 *  @return	The summation of the last num items
	 */
	public long sum(int num) {
	    long sum = 0;

	    if (num > count) {
		return -1;
	    }

	    int i = head - 1;
	    while (num-- > 0) {
		if (i < 0) {
		    i = size - 1;
		}
		sum += queue[i--].longValue();
	    }

	    return sum;
	}

	/** The time of the most recent item added */
	public long mostRecent() {
	    return sum(1);
	}
    }
}
