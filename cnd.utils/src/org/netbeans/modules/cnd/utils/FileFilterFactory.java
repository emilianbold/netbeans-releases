/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils;

import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.utils.filters.AllFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CCSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.ConfigureFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.FortranSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.HeaderSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.MakefileFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.QtFileFilter;
import org.netbeans.modules.cnd.utils.filters.ResourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.ShellFileFilter;
import org.netbeans.modules.cnd.utils.filters.WorkshopProjectFilter;

/**
 *
 * @author Alexander Simon
 */
public final class FileFilterFactory {
    private FileFilterFactory() {
    }
    public static FileFilter getAllFileFilter(){
        return AllFileFilter.getInstance();
    }
    public static FileFilter getAllSourceFileFilter(){
        return AllSourceFileFilter.getInstance();
    }
    public static FileFilter getCCSourceFileFilter(){
        return CCSourceFileFilter.getInstance();
    }
    public static FileFilter getCSourceFileFilter(){
        return CSourceFileFilter.getInstance();
    }
    public static FileFilter getConfigureFileFilter(){
        return ConfigureFileFilter.getInstance();
    }
    public static FileFilter getElfDynamicLibraryFileFilter(){
        return ElfDynamicLibraryFileFilter.getInstance();
    }
    public static FileFilter getElfExecutableFileFilter(){
        return ElfExecutableFileFilter.getInstance();
    }
    public static FileFilter getElfStaticLibraryFileFilter(){
        return ElfStaticLibraryFileFilter.getInstance();
    }
    public static FileFilter getFortranSourceFileFilter(){
        return FortranSourceFileFilter.getInstance();
    }
    public static FileFilter getHeaderSourceFileFilter(){
        return HeaderSourceFileFilter.getInstance();
    }
    public static FileFilter getMacOSXDynamicLibraryFileFilter(){
        return MacOSXDynamicLibraryFileFilter.getInstance();
    }
    public static FileFilter getMacOSXExecutableFileFilter(){
        return MacOSXExecutableFileFilter.getInstance();
    }
    public static FileFilter getMakefileFileFilter(){
        return MakefileFileFilter.getInstance();
    }
    public static FileFilter getPeDynamicLibraryFileFilter(){
        return PeDynamicLibraryFileFilter.getInstance();
    }
    public static FileFilter getPeExecutableFileFilter(){
        return PeExecutableFileFilter.getInstance();
    }
    public static FileFilter getPeStaticLibraryFileFilter(){
        return PeStaticLibraryFileFilter.getInstance();
    }
    public static FileFilter getQtFileFilter(){
        return QtFileFilter.getInstance();
    }
    public static FileFilter getResourceFileFilter(){
        return ResourceFileFilter.getInstance();
    }
    public static FileFilter getShellFileFilter(){
        return ShellFileFilter.getInstance();
    }
    public static FileFilter getWorkshopProjectFilter(){
        return WorkshopProjectFilter.getInstance();
    }
}
