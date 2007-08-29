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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema;

import java.io.IOException;
import javax.swing.Action;
import javax.xml.transform.Source;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.ModelProvider;
import org.netbeans.modules.xml.schema.actions.SchemaViewOpenAction;
import org.netbeans.modules.xml.schema.multiview.SchemaMultiViewSupport;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.xml.sax.InputSource;

/**
 * XML Schema owner. It provides text editing and validation cookies support.
 *
 * @author  Petr Kuzel
 * @author  Jeri Lockhart
 */
public final class SchemaDataObject extends MultiDataObject {
    
    /**
     * Creates a new instance of SchemaDataObject.
     *
     * @param  obj     file object containing data.
     * @param  loader  the file loader.
     */
    public SchemaDataObject(FileObject obj, UniFileLoader loader) throws
            DataObjectExistsException {
        super(obj, loader);
        CookieSet set = getCookieSet();
        // editor support defines MIME type understood by EditorKits registry
        schemaEditorSupport = new SchemaEditorSupport(this);
        set.add(schemaEditorSupport);
        // Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
        // Add TransformableCookie
        Source source = DataObjectAdapters.source(this);
        set.add(new TransformableSupport(source));
        set.add(new SchemaValidateXMLCookie(this));
        // ViewComponentCookie implementation
        set.add(new SchemaMultiViewSupport(this));
    }

    public final Lookup getLookup() {
        return Lookups.fixed(new Object[]{
            super.getLookup(),
            this,
            XmlFileEncodingQueryImpl.singleton()});
    }
    
    /**
     * Return the editor support for this data object.
     *
     * @return  schema editor support.
     */
    public SchemaEditorSupport getSchemaEditorSupport() {
	return schemaEditorSupport;
    }

    @Override
    public void handleDelete() throws IOException {
	if (isModified()) {
	    setModified(false);
	}
	getSchemaEditorSupport().getEnv().unmarkModified();
	super.handleDelete();
    }
    
    protected Node createNodeDelegate() {
	SchemaNode n = new SchemaNode(this);
	n.setIconBaseWithExtension(SCHEMA_ICON_BASE_WITH_EXT);
	n.setShortDescription(NbBundle.getMessage(SchemaDataObject.class,
	    "LBL_SchemaNode_desc"));
	
	return n;
    }

    static class SchemaNode extends DataNode implements ModelProvider {
        public SchemaNode(SchemaDataObject dobj) {
            super(dobj, Children.LEAF);
            getCookieSet().add(this);
        }

        public Action getPreferredAction() {
            return SystemAction.get(SchemaViewOpenAction.class);
        }

        public void setName(String name, boolean rename) {
            if (! rename || name != null && name.equals(this.getDataObject().getName())) {
                return;
            }
            //System.out.println("SchemaDataObject:: setName :: ");
            SchemaModel model = getModel();
            
            try {
            //    RefactoringManager.getInstance().execute(request, true);
                SharedUtils.silentFileRefactor(model, name, true);
            } catch(CannotRefactorException ex) {
                SharedUtils.showFileRenameRefactoringUI(model, name);
            } catch(IOException ex) {
                String msg = NbBundle.getMessage(SchemaDataObject.class, "MSG_UnableToRename", ex.getMessage());
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }

        public SchemaModel getModel() {
            try {
                SchemaDataObject dobj = (SchemaDataObject) getDataObject();
                return dobj.getSchemaEditorSupport().getModel();
            } catch(IOException ex) {
                String msg = NbBundle.getMessage(SchemaDataObject.class, "MSG_UnableToLoadSchema", ex.getMessage());
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            return null;
        }
    }
    
    @Override
    public void setModified(boolean modified) {
	super.setModified(modified);
	if (modified) {
	    getCookieSet().add(getSaveCookie());
	} else {
	    getCookieSet().remove(getSaveCookie());
	}
    }
    
    private SaveCookie getSaveCookie() {
	return new SaveCookie() {
	    public void save() throws IOException {
		getSchemaEditorSupport().saveDocument();
	    }
	    
	    @Override
	    public int hashCode() {
		return getClass().hashCode();
	    }
	    
	    @Override
	    public boolean equals(Object other) {
		return other != null && getClass().equals(other.getClass());
	    }
	};
    }

    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    protected FileObject handleMove(DataFolder df) throws IOException {
        // TODO: Launch refactor file dialog
        if(isModified()) {
            SaveCookie sCookie = this.getCookie(SaveCookie.class);
            if(sCookie != null) {
                sCookie.save();
            }
        }
        return super.handleMove(df);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    
    public static final String SCHEMA_ICON_BASE_WITH_EXT =
	"org/netbeans/modules/xml/schema/resources/Schema_File.png"; // NOI18N
    private static final long serialVersionUID = -8229569186860053169L;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private transient SchemaEditorSupport schemaEditorSupport;
}
