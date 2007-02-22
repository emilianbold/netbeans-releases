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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.io.IOException;
import org.netbeans.modules.xml.xam.Model;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;

/**
 * Represents a collection of external references, or a single file.
 *
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class ExternalReferenceDataNode extends FilterNode
        implements ExternalReferenceNode {
    /** Name of the 'selected' property. */
    public static final String PROP_SELECTED = "selected";
    /** Name of the 'prefix' property. */
    public static final String PROP_PREFIX = "prefix";
    /** Controls the appearance of this node. */
    private ExternalReferenceDecorator decorator;
    /** Set of PropertySets. */
    private Sheet sheet;
    /** True if selected, false otherwise. */
    private boolean selected;
    /** The namespace prefix, if specified. */
    private String prefix;
    /** True if the prefix is generated, false if user edited. */
    private boolean prefixGenerated;

    /**
     * Creates a new instance of ExternalReferenceDataNode.
     *
     * @param  original   the delegate Node.
     * @param  decorator  the external reference decorator.
     */
    public ExternalReferenceDataNode(Node original,
            ExternalReferenceDecorator decorator) {
        super(original, new Children(original, decorator));
        this.decorator = decorator;
    }

    public boolean canRename() {
        // Disable rename as it serves no purpose here and makes the
        // single-click-select-toggle difficult to use.
        return false;
    }

    /**
     * Indicates if this node allows setting it selected.
     *
     * @return  true if this node can be selected, false otherwise.
     */
    public boolean canSelect() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        return dobj != null && !dobj.getPrimaryFile().isFolder() &&
                decorator.validate(this) == null;
    }

    /**
     * Creates a node property of the given key (same as the column keys)
     * and specific getter/setter methods on the given object.
     *
     * @param  key     property name (same as matching column).
     * @param  type    Class of the property (e.g. String.class).
     * @param  inst    object on which to reflect.
     * @param  getter  name of getter method for property value.
     * @param  setter  name of setter method for property value (may be null).
     * @return  new property.
     */
    private Node.Property createProperty(String key, Class type, Object inst,
            String getter, String setter) {
        Property prop = null;
        try {
            prop = new Reflection(inst, type, getter, setter);
            prop.setName(key);
            prop.setDisplayName(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_ExternalReferenceCreator_Column_Name_" + key));
            prop.setShortDescription(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_ExternalReferenceCreator_Column_Desc_" + key));
        }  catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
        }
        return prop;
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, String.class, this,
                "getHtmlDisplayName", null));
        if (canSelect()) {
            set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
                    "isSelected", "setSelected"));
            Node.Property prop = createProperty(PROP_PREFIX, String.class,
                    this, "getPrefix", "setPrefix");
            // Suppress the [...] button because it is not needed.
            prop.setValue("suppressCustomEditor", Boolean.TRUE);
            set.put(prop);
        } else {
            // Do not include this property so the checkbox is not shown.
            //set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
            //        "isSelected", null));
            Node.Property prop = createProperty(PROP_PREFIX, String.class,
                    this, "getPrefix", null);
            // Suppress the [...] button because it is not needed.
            prop.setValue("suppressCustomEditor", Boolean.TRUE);
            set.put(prop);
        }
        return sheet;
    }

    protected final synchronized Sheet getSheet() {
        if (sheet != null) {
            return sheet;
        }
        sheet = createSheet();
        firePropertySetsChange(null, null);
        return sheet;
    }

    public PropertySet[] getPropertySets() {
        Sheet s = getSheet();
        return s.toArray();
    }

    public String getHtmlDisplayName() {
        String name = getOriginal().getHtmlDisplayName();
        if (decorator != null) {
            if (name == null) {
                name = getDisplayName();
            }
            name = decorator.getHtmlDisplayName(name, this);
        }
        return name;
    }

    public String getNamespace() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    Model model = cookie.getModel();
                    return decorator.getNamespace(model);
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public Model getModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    return cookie.getModel();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public String getPrefix() {
        if (prefix == null) {
            prefix = decorator.generatePrefix(this);
            prefixGenerated = true;
        }
        return prefix;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean hasModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            // Don't check for a model, as it may not be well-formed, and
            // this method is not checking for that, just that we should
            // have a model in the normal case.
            return cookie != null;
        }
        return false;
    }

    /**
     * Indicates if the prefix value was changed in the interface.
     *
     * @return  true if prefix was changed.
     */
    public boolean isPrefixChanged() {
        return !prefixGenerated;
    }

    public void setDisplayName(String s) {
        super.disableDelegation(DELEGATE_GET_DISPLAY_NAME|DELEGATE_SET_DISPLAY_NAME);
        super.setDisplayName(s);
    }

    /**
     * Set the namespace prefix for this node.
     *
     * @param  prefix  new namespace prefix.
     */
    public void setPrefix(String prefix) {
        String old = this.prefix;
        this.prefix = prefix;
        prefixGenerated = false;
        firePropertyChange(PROP_PREFIX, old, prefix);
    }

    /**
     * Mark this node as selected.
     *
     * @param  selected  true to select, false to unselect.
     */
    public void setSelected(boolean selected) {
        if (!canSelect()) {
            throw new IllegalStateException("node cannot be selected");
        }
        boolean old = this.selected;
        this.selected = selected;
        firePropertyChange(PROP_SELECTED, old, selected);
    }

    private static class Children extends FilterNode.Children {
        /** Controls the appearance of child nodes. */
        private ExternalReferenceDecorator decorator;

        public Children(Node original, ExternalReferenceDecorator decorator) {
            super(original);
            this.decorator = decorator;
        }

        protected Node[] createNodes(Node n) {
            DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
            if(dobj!=null) {
                FileObject fobj = dobj.getPrimaryFile();
                if (fobj.isFolder() && fobj.getNameExt().equals("nbproject")) {
                    // May be the NetBeans project folder, see if it contains a
                    // project.xml file, in which case we can be fairly certain.
                    FileObject[] files = fobj.getChildren();
                    for (FileObject f : files) {
                        if (f.getNameExt().equals("project.xml")) {
                            // Ignore the nbproject folder.
                            return new Node[0];
                        }
                    }
                }
                ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
                String fname = fobj.getNameExt();
                String ext = decorator.getDocumentType().toString();
                if (fobj.isFolder() || cookie != null && fname.endsWith(ext)) {
                    return super.createNodes(n);
                }
            }
            return new Node[0];
        }

        protected Node copyNode(Node node) {
            return decorator.createExternalReferenceNode(node);
        }
    }
}
