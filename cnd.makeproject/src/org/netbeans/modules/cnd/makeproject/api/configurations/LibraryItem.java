/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.platform.Platform;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class LibraryItem {
    public static final int PROJECT_ITEM = 0;
    public static final int STD_LIB_ITEM = 1;
    public static final int LIB_ITEM = 2;
    public static final int LIB_FILE_ITEM = 3;
    public static final int OPTION_ITEM = 4;

    private int type;

    protected LibraryItem() {
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

    // Should be overridden
    public String getToolTip() {
	return "Should be overridden"; // NOI18N
    }

    // Should be overridden
    public String getIconName() {
	return "org/netbeans/modules/cnd/resources/blank.gif"; // NOI18N
    }

    // Should be overridden
    public void setValue(String value) {
    }

    // Should be overridden
    public String getPath() {
        return null;
    }

    // Should be overridden
    @Override
    public String toString() {
	return "Should be overridden"; // NOI18N
    }

    // Should be overridden
//    public String getOption() {
//	return ""; // NOI18N
//    }
    
    // Should be overridden
    public String getOption(MakeConfiguration conf) {
	return "" + getOption(conf); // NOI18N
    }

    // Should be overridden
    public boolean canEdit() {
	return false;
    }

    // Should be overridden
    @Override
    public LibraryItem clone() {
	return this;
    }

    public static class ProjectItem extends LibraryItem {
	private MakeArtifact makeArtifact;
	private Project project; // Just for caching

	public ProjectItem(MakeArtifact makeArtifact) {
	    this.makeArtifact = makeArtifact;
	    setType(PROJECT_ITEM);
	}

	public MakeArtifact getMakeArtifact() {
	    return makeArtifact;
	}

	public void setMakeArtifact(MakeArtifact makeArtifact) {
	    this.makeArtifact = makeArtifact;
	}

	public Project getProject(String baseDir) {
	    if (project == null) {
		String location = CndPathUtilitities.toAbsolutePath(baseDir, getMakeArtifact().getProjectLocation());
		try {
		    FileObject fo = FileUtil.toFileObject(new File(location).getCanonicalFile());
                    project = ProjectManager.getDefault().findProject(fo);
		}
		catch (Exception e) {
		    System.err.println("Cannot find subproject in '"+location+"' "+e); // FIXUP // NOI18N
		}
	    }
	    return project;
	}

        @Override
	public String getToolTip() {
            String ret = getString("ProjectTxt") + " " + getMakeArtifact().getProjectLocation(); // NOI18N
            if (getMakeArtifact().getOutput() != null && getMakeArtifact().getOutput().length() > 0) {
                ret = ret + " (" + getMakeArtifact().getOutput() + ")"; // NOI18N
            }
            return ret;
	}

        @Override
	public String getIconName() {
	    return "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif"; // NOI18N
	}

        @Override
        public String toString() {
            String ret = CndPathUtilitities.getBaseName(getMakeArtifact().getProjectLocation());
            if (getMakeArtifact().getOutput() != null && getMakeArtifact().getOutput().length() > 0) {
                ret = ret + " (" + getMakeArtifact().getOutput() + ")"; // NOI18N
            }
            return ret;
        }

        @Override
        public void setValue(String value) {
            // Can't do
        }

        @Override
        public String getPath() {
            String libPath = getMakeArtifact().getOutput();
            if (!CndPathUtilitities.isPathAbsolute(libPath)) {
                libPath = getMakeArtifact().getProjectLocation() + '/' + libPath; // UNIX path
            }
            return libPath;
        }

        @Override
        public String getOption(MakeConfiguration conf) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            Platform platform = Platforms.getPlatform(conf.getDevelopmentHost().getBuildPlatform());
            String libPath = getPath();
            String libDir = CndPathUtilitities.getDirName(libPath);
            String libName = CndPathUtilitities.getBaseName(libPath);
            return platform.getLibraryLinkOption(libName, libDir, libPath, compilerSet);
        }

        @Override
	public boolean canEdit() {
	    return false;
	}

        @Override
	public ProjectItem clone() {
	    ProjectItem clone = new ProjectItem(getMakeArtifact());
	    return clone;
	}
    }

    public static class StdLibItem extends LibraryItem {
	private final String name;
	private final String displayName;
	private final String[] libs;

	public StdLibItem(String name, String displayName, String[] libs) {
	    this.name = name;
	    this.displayName = displayName;
	    this.libs = libs;
	    setType(STD_LIB_ITEM);
	}

	public String getName() {
	    return name;
	}

	public String getDisplayName() {
	    return displayName;
	}

	public String[] getLibs() {
	    return libs;
	}

        @Override
	public String getToolTip() {
	    return getString("StandardLibraryTxt") + " " + getDisplayName() + " (" + getOption(null) + ")"; // NOI18N
	}

        @Override
	public String getIconName() {
	    return "org/netbeans/modules/cnd/resources/stdLibrary.gif"; // NOI18N
	}

        @Override
	public String toString() {
	    return getDisplayName();
	}

        @Override
	public void setValue(String value) {
	    // Can't do
	}

        @Override
        public String getOption(MakeConfiguration conf) {
            StringBuilder options = new StringBuilder();
            for (int i = 0; i < libs.length; i++) {
                if (libs[i].charAt(0) != '-') {
                    options.append("-l").append(libs[i]).append(" "); // NOI18N
                } else {
                    options.append(libs[i]).append(" "); // NOI18N
                }
            }
            return options.toString();
        }

        @Override
	public boolean canEdit() {
	    return false;
	}

        @Override
	public StdLibItem clone() {
	    StdLibItem clone = new StdLibItem(getName(), getDisplayName(), getLibs());
	    return clone;
	}
    }

    public static class LibItem extends LibraryItem {
	private String libName;

	public LibItem(String libName) {
	    this.libName = libName;
	    setType(LIB_ITEM);
	}

	public String getLibName() {
	    return libName;
	}

	public void setLibName(String libName) {
	    this.libName = libName;
	}

        @Override
	public String getToolTip() {
	    return getString("LibraryTxt") + "  " + getLibName() + " (" + getOption(null) + ")"; // NOI18N
	}

        @Override
	public String getIconName() {
	    return "org/netbeans/modules/cnd/loaders/LibraryIcon.gif"; // NOI18N
	}

        @Override
	public String toString() {
	    return getLibName();
	}

        @Override
	public void setValue(String value) {
	    setLibName(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
	    return "-l" + getLibName(); // NOI18N
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public LibItem clone() {
	    return new LibItem(getLibName());
	}
    }

    public static class LibFileItem extends LibraryItem {
	private String path;

	public LibFileItem(String path) {
	    this.path = path;
	    setType(LIB_FILE_ITEM);
	}

        @Override
	public String getPath() {
	    return path;
	}

	public void setPath(String path) {
	    this.path = path;
	}

        @Override
	public String getToolTip() {
	    return getString("LibraryFileTxt") + " "  + getPath() + " (" + getOption(null) + ")"; // NOI18N
	}

        @Override
        public String getIconName() {
            if (getPath().endsWith(".so") || getPath().endsWith(".dll") || getPath().endsWith(".dylib")) { // NOI18N
                return "org/netbeans/modules/cnd/loaders/DllIcon.gif"; // NOI18N
            } else if (getPath().endsWith(".a")) { // NOI18N
                return "org/netbeans/modules/cnd/loaders/static_library.gif"; // NOI18N
            } else {
                return "org/netbeans/modules/cnd/loaders/unknown.gif"; // NOI18N
            }
        }

        @Override
	public String toString() {
	    return getPath();
	}

        @Override
	public void setValue(String value) {
	    setPath(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
            String lpath = getPath();
            if (conf != null) {
                CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                lpath = CppUtils.normalizeDriveLetter(cs, lpath);
            }
	    return lpath;
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public LibFileItem clone() {
	    return new LibFileItem(getPath());
	}
    }

    public static class OptionItem extends LibraryItem {
	private String libraryOption;

	public OptionItem(String libraryOption) {
	    this.libraryOption = libraryOption;
	    setType(OPTION_ITEM);
	}

	public String getLibraryOption() {
	    return libraryOption;
	}

	public void setLibraryOption(String libraryOption) {
	    this.libraryOption = libraryOption;
	}

        @Override
	public String getToolTip() {
	    return getString("LibraryOptionTxt") + " "  + getLibraryOption() + " (" + getOption(null) + ")"; // NOI18N
	}

        @Override
	public String getIconName() {
	    return "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif"; // NOI18N
	}

        @Override
	public String toString() {
	    return getLibraryOption();
	}

        @Override
	public void setValue(String value) {
	    setLibraryOption(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
	    return getLibraryOption();
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public OptionItem clone() {
	    return new OptionItem(getLibraryOption());
	}
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(LibraryItem.class, s);
    }
}
