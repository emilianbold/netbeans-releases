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

    public CndDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
	super(pf, loader);
	init();
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
}
