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

package org.netbeans.modules.javacard.ri.spi;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.javacard.ri.platform.MergeProperties;
import org.netbeans.modules.javacard.ri.platform.RIPlatform;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Tim Boudreau
 */
public class RIPlatformUtils {
    private static EditableProperties mergeDefaultRI (DataObject platformDef) throws IOException {
        //XXX need a way to do this that can wait until there really is a
        //default platform instance created
        DataObject pform = RIPlatform.findDefaultPlatform(platformDef);
        if (pform == null) {
            return null;
        }
        ObservableProperties riProps = pform.getLookup().lookup(PropertiesAdapter.class).asProperties();
        ObservableProperties pformProps = platformDef.getLookup().lookup(PropertiesAdapter.class).asProperties();
        MergeProperties mp = new MergeProperties(riProps, pformProps);
        //XXX horrible
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mp.store(out, null);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        EditableProperties props = new EditableProperties(true);
        props.load(in);
        in.close();
        props.setProperty("javacard.ri.properties.path", FileUtil.toFile(pform.getPrimaryFile()).getAbsolutePath()); //NOI18N
        RIPlatform defPform = pform.getNodeDelegate().getLookup().lookup(RIPlatform.class);
        if (defPform != null && defPform.getHome() != null) {
            props.setProperty("javacard.ri.home", defPform.getHome().getAbsolutePath()); //NOI18N
        }
        return props;
    }

    /**
     * Merge the default Java Card Reference Implementation's properties into
     * the properties in the passed "prototype" FileObject and write them to
     * the destination file
     * @param prototype An RI-wrapper file somewhere in the system FS
     * @param platformFo A fileobject to write to
     * @return True if a default platform was available and the merge completed
     * successfully.
     * @throws IOException
     */
    public static boolean mergeDefaultRI(FileObject prototype, FileObject platformFo) throws IOException {
        assert prototype != platformFo : "Same fo for both arguments";
        DataObject platformDef = DataObject.find(prototype);
        EditableProperties props = mergeDefaultRI(platformDef);
        if (props != null) {
            OutputStream out = new BufferedOutputStream(platformFo.getOutputStream());
            try {
                props.store(out);
            } finally {
                out.close();
            }
        }
        return props != null;
    }
}
