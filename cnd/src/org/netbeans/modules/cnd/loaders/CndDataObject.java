/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.execution.BinaryExecSupport;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;

/**
 *  Abstract superclass of a C/C++/Fortran DataObject.
 */
public abstract class CndDataObject extends MultiDataObject {

    /** Serial version number */
    static final long serialVersionUID = -6788084224129713370L;
    private Reference<CppEditorSupport> cppEditorSupport;
    private BinaryExecSupport binaryExecSupport;

    public CndDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
	super(pf, loader);
	init();
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    /**
     *  Initialize cookies for this DataObject. This method may get overridden
     *  by derived classes who need to use a different set of cookies.
     */
    protected void init() {
	CookieSet cookies = getCookieSet();
	//cookies.add(new CppEditorSupport(primary.getDataObject()));
        cookies.add(CppEditorSupport.class, new CookieSet.Factory() {
            public <T extends Cookie> T createCookie(Class<T> klass) {
                return klass.cast(createCppEditorSupport());
            }
        });
	//cookies.add(new BinaryExecSupport(primary));
            cookies.add(BinaryExecSupport.class, new CookieSet.Factory() {
            public <T extends Cookie> T createCookie(Class<T> klass) {
                return klass.cast(createBinaryExecSupport());
            }
        });
    }

    private synchronized CppEditorSupport createCppEditorSupport() {
        CppEditorSupport support = (cppEditorSupport == null) ? null : cppEditorSupport.get();
        if (support == null) {
            support = new CppEditorSupport(getPrimaryEntry().getDataObject());
            cppEditorSupport = new SoftReference<CppEditorSupport>(support);
        }
        return support;
    }

    private synchronized BinaryExecSupport createBinaryExecSupport() {
        if (binaryExecSupport == null) {
            binaryExecSupport = new BinaryExecSupport(getPrimaryEntry());
        }
        return binaryExecSupport;
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
