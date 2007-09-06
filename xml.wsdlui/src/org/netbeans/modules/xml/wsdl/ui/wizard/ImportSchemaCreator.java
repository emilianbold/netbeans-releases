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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.ImportSchemaCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * An import customizer for schema in a WSDL document from the new WSDL
 * file wizard.
 *
 * @author  Nathan Fiedler
 */
public class ImportSchemaCreator extends ImportSchemaCustomizer {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** URIs of previously selected files. */
    private List<String> selectedFiles;
    /** If true, ignore the property change event. */
    private boolean ignorePropertyChange;

    /**
     * Creates a new instance of ImportCustomizer
     *
     * @param  schema  component to contain the import(s).
     * @param  model   the WSDL model.
     * @param  files   comma-separated file URIs.
     */
    public ImportSchemaCreator(Schema schema, WSDLModel model, String files) {
        super(schema, model);
        StringTokenizer tokenizer = new StringTokenizer(files, ",");
        selectedFiles = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            selectedFiles.add(tokenizer.nextToken());
        }
    }

    protected boolean allowEmptySelection() {
        // Need to permit user to deselect files that were previously
        // selected to the point of having no files selected at all.
        return true;
    }

    @Override
    public void applyChanges() throws IOException {
        // Do NOT call the superclass, as we are operating from within
        // the new WSDL file wizard, and there is nothing to which we
        // can commit our changes.
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        ExternalReferenceDataNode erdn = super.createExternalReferenceNode(original);
        // This method gets called from the superclass constructor, and we
        // have nothing to add during that early phase, so just skip it.
        if (selectedFiles != null) {
            // Mark the node selected if it had been selected earlier.
            DataObject dobj = erdn.getLookup().lookup(DataObject.class);
            String location = dataObjectToURI(dobj);
            if (selectedFiles.contains(location)) {
                ignorePropertyChange = true;
                erdn.setSelected(true);
                ignorePropertyChange = false;
            }
        }
        return erdn;
    }

    /**
     * Convert a DataObject to a file URI string.
     *
     * @param  dobj  the DataObject to convert.
     * @return  the URI string for the file.
     */
    private static String dataObjectToURI(DataObject dobj) {
        FileObject fobj = dobj.getPrimaryFile();
        File file = FileUtil.toFile(fobj);
        return file.toURI().normalize().toString();
    }

    /**
     * Return a String of comma-separated URIs for the selected files.
     *
     * @return  selected files as URIs.
     */
    public String getSelectedFiles() {
        StringBuilder sb = new StringBuilder();
        for (String uri : selectedFiles) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append(uri);
        }
        return sb.toString();
    }

    public void propertyChange(PropertyChangeEvent event) {
        // Let superclass do its thing.
        super.propertyChange(event);
        // Manage our set of selected file URIs. This is done here since
        // the user may never expand the nodes, and thus they may never
        // become "selected", and yet we want any previously selected 
        // files (in the form of our URI list) to be maintained.
        if (!ignorePropertyChange && event.getPropertyName().equals(
                ExternalReferenceDataNode.PROP_SELECTED)) {
            ExternalReferenceDataNode erdn =
                    (ExternalReferenceDataNode) event.getSource();
            boolean selected = ((Boolean) event.getNewValue()).booleanValue();
            DataObject dobj = erdn.getLookup().lookup(DataObject.class);
            String uri = dataObjectToURI(dobj);
            if (selected) {
                selectedFiles.add(uri);
            } else {
                selectedFiles.remove(uri);
            }
        }
    }
}
