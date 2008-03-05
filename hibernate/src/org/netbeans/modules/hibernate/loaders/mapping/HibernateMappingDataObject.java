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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.hibernate.loaders.mapping;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Represents the Hibernate mapping file
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingDataObject extends MultiDataObject {

    private HibernateMapping mapping;

    public HibernateMappingDataObject(FileObject pf, HibernateMappingDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        cookies.add(validateCookie);
    }

    /**
     * Adds MyClass object to Mapping 
     * 
     */
    public void addMyClass(MyClass myClass) {
        OutputStream out = null;
        try {
            getHibernateMapping().addMyClass(myClass);
            out = getPrimaryFile().getOutputStream();
            getHibernateMapping().write(out);
        } catch (FileAlreadyLockedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Gets the object graph representing the contents of the 
     * Hibernate mapping file with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     */
    public HibernateMapping getHibernateMapping() {
        if (mapping == null) {
            try {
                mapping = HibernateMappingMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert mapping != null;
        return mapping;
    }

    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save() {
        EditorCookie edit = (EditorCookie) getCookie(EditorCookie.class);
        if (edit != null) {
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    protected Node createNodeDelegate() {
        return new HibernateMappingDataNode(this);
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
