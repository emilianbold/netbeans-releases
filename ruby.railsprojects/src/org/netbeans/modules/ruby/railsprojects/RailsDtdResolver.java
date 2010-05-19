/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.railsprojects;

import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rhtml.spi.DtdResolver;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.ruby.rhtml.spi.DtdResolver.class)
public final class RailsDtdResolver implements DtdResolver {

    // default to xhtml 1.0
    private static final String DEFAULT_FALLBACK_DTD = "-//W3C//DTD XHTML 1.0 Strict//EN"; //NOI18N
    private static final String RHTML_DTD_PROPERTY = "rhtml.doctype"; //NOI18N

    // todo: this could try resolving the doctype from the layout templates in view/layouts
    public String getIdentifier(Document doc) {
        FileObject fo = getFileObject(doc);
        if (fo == null) {
            return null;
        }
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return null;
        }
        RubyBaseProject project = owner.getLookup().lookup(RubyBaseProject.class);
        if (project == null) {
            return null;
        }
        String result = project.evaluator().getProperty(RHTML_DTD_PROPERTY);
        return result != null ? result : DEFAULT_FALLBACK_DTD;
    }

    private static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        }
        return null;
    }

}
