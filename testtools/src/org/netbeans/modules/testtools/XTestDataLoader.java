/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools;

/*
 * XTestDataLoader.java
 *
 * Created on May 2, 2002, 4:07 PM
 */

import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import java.io.IOException;
import org.apache.tools.ant.module.loader.AntCompilerSupport;
import org.openide.compiler.Compiler;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestDataLoader extends UniFileLoader {

    public XTestDataLoader () {
        super (org.netbeans.modules.testtools.XTestDataObject.class);
    }

    protected String defaultDisplayName () {
        return "XTest Build Script";
    }

    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType("text/x-ant+xml");
    }

    
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(OpenLocalExplorerAction.class),
            null,
            SystemAction.get(CleanAction.class),
            null,
            SystemAction.get(CompileAction.class),
            null,
            SystemAction.get(ExecuteAction.class),
            null,
            SystemAction.get(CleanResultsAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(ReorderAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(SaveAsTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }

    protected FileObject findPrimaryFile (FileObject fo) {
        fo = super.findPrimaryFile (fo);
        if (fo==null) return null;
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(fo.getInputStream()));
            String line;
            while ((line=br.readLine())!=null)
                if (line.indexOf("\"xtest.module\"")>=0) {
                    br.close();
                    return fo;
                }
        } catch (Exception e) {}
        return null;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new XTestDataObject(primaryFile, this);
    }

    public static class CleanResultsAction extends AbstractCompileAction {
        protected Compiler.Depth depth () {
            return Compiler.DEPTH_ONE;
        }

        protected final Class cookie () {
            return XTestDataObject.CleanResults.class;
        }

        public String getName() {
            return "Clean Results";
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(CleanResultsAction.class);
        }
    }
}
