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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.sharability;

import java.beans.Customizer;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class ServerLibraryTypeProvider implements LibraryTypeProvider {

    public static final String LIBRARY_TYPE = "j2eeshared"; // NOI18N
    
    public static final String VOLUME_CLASSPATH = "classpath";
    
    public static final String VOLUME_WS_COMPILE_CLASSPATH = "wscompile";
    
    public static final String VOLUME_WS_GENERATE_CLASSPATH = "wsgenerate";
    
    public static final String VOLUME_WS_IMPORT_CLASSPATH = "wsimport";
    
    public static final String VOLUME_WS_INTEROP_CLASSPATH = "wsinterop";
    
    public static final String VOLUME_WS_JWSDP_CLASSPATH = "wsjwsdp";
    
    // This is runtime only
    //public static final String VOLUME_APP_CLIENT_CLASSPATH = "appclient";
    
    private static final String[] VOLUME_TYPES = new String[] {
            VOLUME_CLASSPATH,
            VOLUME_WS_COMPILE_CLASSPATH,
            VOLUME_WS_GENERATE_CLASSPATH,
            VOLUME_WS_IMPORT_CLASSPATH,
            VOLUME_WS_INTEROP_CLASSPATH,
            VOLUME_WS_JWSDP_CLASSPATH
    };
    
    private ServerLibraryTypeProvider() {
        super();
    }
    
    public static LibraryTypeProvider create() {
        return new ServerLibraryTypeProvider();
    }
    
    public LibraryImplementation createLibrary() {
        return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
    }

    public Customizer getCustomizer(String volumeType) {
        return new ServerVolumeCustomizer(volumeType);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ServerLibraryTypeProvider.class, "ServerLibraryTypeProvider.typeName");
    }

    public String getLibraryType() {
        return LIBRARY_TYPE;
    }

    public String[] getSupportedVolumeTypes() {
        return VOLUME_TYPES.clone();
    }

    public void libraryCreated(LibraryImplementation libraryImpl) {
        // TODO anything ?
    }

    public void libraryDeleted(LibraryImplementation libraryImpl) {
        // TODO anything ?
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

}
