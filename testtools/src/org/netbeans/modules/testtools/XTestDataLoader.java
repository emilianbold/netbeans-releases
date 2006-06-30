/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.testtools;

/*
 * XTestDataLoader.java
 *
 * Created on May 2, 2002, 4:07 PM
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.openide.actions.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.compiler.Compiler;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.openide.util.NbBundle;

/** Data Loader class for XTest Workspace Script Data Object
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestDataLoader extends UniFileLoader {

    static final long serialVersionUID = 3860621574863539101L;

    /** creates new XTestDataLoader */    
    public XTestDataLoader () {
        super ("XTestDataObject");
    }

    /** returns default display name of XTestDataObject
     * @return String default display name of XTestDataObject */    
    protected String defaultDisplayName () {
        return NbBundle.getMessage(XTestDataLoader.class, "XTestBuildScriptName"); // NOI18N
    }

    /** performs initialization of Data Loader */    
    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType("text/x-ant+xml"); // NOI18N
    }

    
    /** returns default System Actions supported by XTestDataObject
     * @return array of SystemAction */    
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

    /** performs recognition of XTestDataObject
     * @param fo tested FileObject
     * @return given FileObject or null when XTestDataObject not recognized */    
    protected FileObject findPrimaryFile (FileObject fo) {
        fo = super.findPrimaryFile (fo);
        if (fo==null) return null;
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(fo.getInputStream()));
            String line;
            while ((line=br.readLine())!=null)
                if (line.indexOf("\"xtest.module\"")>=0) { // NOI18N
                    br.close();
                    return fo;
                }
        } catch (Exception e) {}
        return null;
    }

    /** creates instance of XTestDataObject for given FileObject
     * @param primaryFile FileObject
     * @throws DataObjectExistsException when Data Object already exists
     * @throws IOException when some IO problems
     * @return new XTestDataObject for given FileObject */    
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new XTestDataObject(primaryFile, this);
    }

    /** Action performing cleanup of XTest results */    
    public static class CleanResultsAction extends AbstractCompileAction {
        /** returns Depth of Compiler
         * @return Compiler.DEPTH_ONE */        
        protected Compiler.Depth depth () {
            return Compiler.DEPTH_ONE;
        }

        /** returns Cookie for this Action
         * @return XTestDataObject.CleanResults.class */        
        protected final Class cookie () {
            return XTestDataObject.CleanResults.class;
        }

        /** returns name of the Action
         * @return String Action name */        
        public String getName() {
            return NbBundle.getMessage(XTestDataLoader.class, "CleanResultsActionName"); // NOI18N
        }
        
        /** returns Help Context of the Action
         * @return HelpCtx */        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(CleanResultsAction.class);
        }
    }
}
