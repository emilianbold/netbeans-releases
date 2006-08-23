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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
	return "Should be overridden";
    }

    // Should be overridden
    public String getIconName() {
	return "org/netbeans/modules/cnd/resources/blank.gif";
    }

    // Should be overridden
    public void setValue(String value) {
    }

    // Should be overridden
    public String toString() {
	return "Should be overridden";
    }

    // Should be overridden
    public String getOption() {
	return "";
    }
    
    // Should be overridden
    public String getOption(MakeConfiguration conf) {
	return "" + getOption();
    }

    // Should be overridden
    public boolean canEdit() {
	return false;
    }

    // Should be overridden
    public Object clone() {
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
		String location = IpeUtils.toAbsolutePath(baseDir, getMakeArtifact().getProjectLocation());
		location = FilePathAdaptor.mapToLocal(location); // PC path
		try {
		    FileObject fo = FileUtil.toFileObject(new File(location).getCanonicalFile()); 
		    project = ProjectManager.getDefault().findProject(fo);
		}
		catch (Exception e) {
		    System.err.println(e); // FIXUP
		}
	    }
	    return project;
	}

	public String getToolTip() {
	    return "Project: " + getMakeArtifact().getProjectLocation() + " (" + getMakeArtifact().getOutput() + ")";
	}

	public String getIconName() {
	    return "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif";
	}

	public String toString() {
	    return IpeUtils.getBaseName(getMakeArtifact().getProjectLocation()) + " (" + getMakeArtifact().getOutput() + ")";
	}

	public void setValue(String value) {
	    // Can't do
	}

	public String getOption(MakeConfiguration conf) {
            CompilerSet compilerSet = CompilerSets.getCompilerSet(conf.getCompilerSet().getValue());
            Platform platform = Platforms.getPlatform(conf.getPlatform().getValue());
        
	    String libPath = getMakeArtifact().getOutput();
	    if (!IpeUtils.isPathAbsolute(libPath))
		libPath = getMakeArtifact().getProjectLocation() + '/' + libPath; // UNIX path
	    String libDir = IpeUtils.getDirName(libPath);
	    String libName = IpeUtils.getBaseName(libPath);
            
            return platform.getLibraryLinkOption(libName, libDir, libPath, compilerSet);
	}

	public boolean canEdit() {
	    return false;
	}

	public Object clone() {
	    ProjectItem clone = new ProjectItem(getMakeArtifact());
	    return clone;
	}
    }

    public static class StdLibItem extends LibraryItem {
	private String name;
	private String displayName;
	private String[] libs;

	public StdLibItem(String name, String displayName, String[] libs) {
	    this.name = name;
	    this.displayName = displayName;
	    this.libs = libs;
	    setType(STD_LIB_ITEM);
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getDisplayName() {
	    return displayName;
	}

	public void setDisplayName(String displayName) {
	    this.displayName = displayName;
	}

	public String[] getLibs() {
	    return libs;
	}

	public void setLibs(String[] libs) {
	    this.libs = libs;
	}

	public String getToolTip() {
	    return "Standard Library: " + getDisplayName() + " (" + getOption() + ")";
	}

	public String getIconName() {
	    return "org/netbeans/modules/cnd/resources/stdLibrary.gif";
	}

	public String toString() {
	    return getDisplayName();
	}

	public void setValue(String value) {
	    // Can't do
	}

	public String getOption() {
	    String options = "";
	    for (int i = 0; i < libs.length; i++) {
		if (libs[i].charAt(0) != '-')
		    options += "-l" + libs[i] + " ";
		else
		    options += libs[i] + " ";
	    }
	    return options;
	}

	public boolean canEdit() {
	    return false;
	}

	public Object clone() {
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

	public String getToolTip() {
	    return "Library: " + getLibName() + " (" + getOption() + ")";
	}

	public String getIconName() {
	    return "org/netbeans/modules/cnd/loaders/LibraryIcon.gif";
	}

	public String toString() {
	    return getLibName();
	}

	public void setValue(String value) {
	    setLibName(value);
	}

	public String getOption() {
	    return "-l" + getLibName();
	}

	public boolean canEdit() {
	    return true;
	}

	public Object clone() {
	    return new LibItem(getLibName());
	}
    }

    public static class LibFileItem extends LibraryItem {
	private String path;

	public LibFileItem(String path) {
	    this.path = path;
	    setType(LIB_FILE_ITEM);
	}

	public String getPath() {
	    return path;
	}

	public void setPath(String path) {
	    this.path = path;
	}

	public String getToolTip() {
	    return "Library File: " + getPath() + " (" + getOption() + ")";
	}

	public String getIconName() {
	    if (getPath().endsWith(".so"))
		return "org/netbeans/modules/cnd/loaders/DllIcon.gif";
	    else if (getPath().endsWith(".a"))
		return "org/netbeans/modules/cnd/loaders/static_library.gif";
	    else
		return "org/netbeans/modules/cnd/loaders/unknown.gif";
	}

	public String toString() {
	    return getPath();
	}

	public void setValue(String value) {
	    setPath(value);
	}

	public String getOption() {
	    return getPath();
	}

	public boolean canEdit() {
	    return true;
	}

	public Object clone() {
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

	public String getToolTip() {
	    return "Library Option: " + getLibraryOption() + " (" + getOption() + ")";
	}

	public String getIconName() {
	    return "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif";
	}

	public String toString() {
	    return getLibraryOption();
	}

	public void setValue(String value) {
	    setLibraryOption(value);
	}

	public String getOption() {
	    return getLibraryOption();
	}

	public boolean canEdit() {
	    return true;
	}

	public Object clone() {
	    return new OptionItem(getLibraryOption());
	}
    }
}
