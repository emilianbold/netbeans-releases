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

package org.netbeans.modules.encoder.custom.aip;

import com.sun.encoder.custom.appinfo.Delimiter;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.ImageUtilities;

/**
 * The node that represents the delimiter in the delimiter tree.
 *
 * @author Jun Xu
 */
public class DelimTreeDelimiterNode extends AbstractNode
        implements PropertyChangeListener, DelimiterSetChangeNotificationSupport {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private final DelimiterSetChangeNotifier mChangeNotifier = 
            new DelimiterSetChangeNotifier();
    private final DelimiterOption mDelimOption;
    
    /** Creates a new instance of DelimTreeDelimiterNode */
    public DelimTreeDelimiterNode(Delimiter delim, Lookup lookup) {
        super(new Children.Array(), lookup);
        mDelimOption = DelimiterOption.create(delim);
        mDelimOption.addPropertyChangeListener(this);
    }
    
    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public String getName() {
        return "Delimiter"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return _bundle.getString("delim_tree_delim_node.lbl.delimiter");
    }
    
    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage("org/netbeans/modules/encoder/custom/aip/delimIcon.PNG");  //NOI18N
    }

    @Override
    public Image getOpenedIcon(int i) {
        return ImageUtilities.loadImage("org/netbeans/modules/encoder/custom/aip/delimOpenIcon.PNG");  //NOI18N
    }

    @Override
    protected Sheet createSheet() {
        Sheet.Set propSet = Sheet.createPropertiesSet();
        try {
            //The Node Type Property
            PropertySupport.Reflection<String> kindProp =
                    new PropertySupport.Reflection<String>(mDelimOption,
                    String.class, "kind");  //NOI18N
            kindProp.setName("kind");  //NOI18N
            kindProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.type"));
            kindProp.setPropertyEditorClass(DelimKindPropertyEditor.class);
            propSet.put(kindProp);
            
            PropertySupport.Reflection<Short> precedenceProp =
                    new PropertySupport.Reflection<Short>(mDelimOption,
                            short.class, "precedence");  //NOI18N
            precedenceProp.setName("precedence");  //NOI18N
            precedenceProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.precedence"));
            precedenceProp.setPropertyEditorClass(DelimPrecedencePropertyEditor.class);
            propSet.put(precedenceProp);
            
            PropertySupport.Reflection<String> optionModeProp =
                    new PropertySupport.Reflection<String>(mDelimOption,
                            String.class, "optionMode");  //NOI18N
            optionModeProp.setName("optionMode");  //NOI18N
            optionModeProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.opt_mode"));
            optionModeProp.setPropertyEditorClass(DelimOptionModePropertyEditor.class);
            propSet.put(optionModeProp);
            
            PropertySupport.Reflection<String> termModeProp =
                    new PropertySupport.Reflection<String>(mDelimOption,
                            String.class, "termMode");  //NOI18N
            termModeProp.setName("termMode");  //NOI18N
            termModeProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.term_mode"));
            termModeProp.setPropertyEditorClass(DelimTermModePropertyEditor.class);
            propSet.put(termModeProp);

            PropertySupport.Reflection<String> bytesProp =
                    new PropertySupport.Reflection<String>(mDelimOption,
                    String.class, "bytes");  //NOI18N
            bytesProp.setName("bytes");  //NOI18N
            bytesProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.delim_bytes"));
            bytesProp.setPropertyEditorClass(DelimiterPropertyEditor.class);
            propSet.put(bytesProp);
            
            PropertySupport.Reflection<Integer> offsetProp =
                    new PropertySupport.Reflection<Integer>(mDelimOption,
                            int.class, "offset");  //NOI18N
            offsetProp.setName("offset");  //NOI18N
            offsetProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.offset"));
            offsetProp.setPropertyEditorClass(DelimOffsetPropertyEditor.class);
            propSet.put(offsetProp);
            
            PropertySupport.Reflection<Short> lengthProp =
                    new PropertySupport.Reflection<Short>(mDelimOption,
                            short.class, "length");  //NOI18N
            lengthProp.setName("length");  //NOI18N
            lengthProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.length"));
            lengthProp.setPropertyEditorClass(DelimLengthPropertyEditor.class);
            propSet.put(lengthProp);
            
            PropertySupport.Reflection<Boolean> detachedProp =
                    new PropertySupport.Reflection<Boolean>(mDelimOption,
                            boolean.class, "detached");  //NOI18N
            detachedProp.setName("detached");  //NOI18N
            detachedProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.detached"));
            propSet.put(detachedProp);
            
            PropertySupport.Reflection<String> beginBytesProp =
                    new PropertySupport.Reflection<String>(mDelimOption,
                    String.class, "beginBytes");  //NOI18N
            beginBytesProp.setName("beginBytes");  //NOI18N
            beginBytesProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.begin_delim_bytes"));
            beginBytesProp.setPropertyEditorClass(DelimiterPropertyEditor.class);
            propSet.put(beginBytesProp);
            
            PropertySupport.Reflection<Integer> beginOffsetProp =
                    new PropertySupport.Reflection<Integer>(mDelimOption,
                            int.class, "beginOffset");  //NOI18N
            beginOffsetProp.setName("beginOffset");  //NOI18N
            beginOffsetProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.begin_offset"));
            beginOffsetProp.setPropertyEditorClass(DelimOffsetPropertyEditor.class);
            propSet.put(beginOffsetProp);
            
            PropertySupport.Reflection<Short> beginLengthProp =
                    new PropertySupport.Reflection<Short>(mDelimOption,
                            short.class, "beginLength");  //NOI18N
            beginLengthProp.setName("beginLength");  //NOI18N
            beginLengthProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.begin_length"));
            beginLengthProp.setPropertyEditorClass(DelimLengthPropertyEditor.class);
            propSet.put(beginLengthProp);
            
            PropertySupport.Reflection<Boolean> beginDetachedProp =
                    new PropertySupport.Reflection<Boolean>(mDelimOption,
                            boolean.class, "beginDetached");  //NOI18N
            beginDetachedProp.setName("beginDetached");  //NOI18N
            beginDetachedProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.begin_detached"));
            propSet.put(beginDetachedProp);
            
            PropertySupport.Reflection<Boolean> skipLeadingProp =
                    new PropertySupport.Reflection<Boolean>(mDelimOption,
                            boolean.class, "skipLeading");  //NOI18N
            skipLeadingProp.setName("skipLeading");  //NOI18N
            skipLeadingProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.skip_leading"));
            propSet.put(skipLeadingProp);
            
            PropertySupport.Reflection<Boolean> collapseProp =
                    new PropertySupport.Reflection<Boolean>(mDelimOption,
                            boolean.class, "collapse");  //NOI18N
            collapseProp.setName("collapse");  //NOI18N
            collapseProp.setDisplayName(_bundle.getString("delim_tree_delim_node.lbl.collapse"));
            propSet.put(collapseProp);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(_bundle.getString("delim_tree_delim_node.exp.no_such_mthd"), e);
        }
        Sheet sheet = Sheet.createDefault();
        sheet.put(propSet);
        return sheet;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        mChangeNotifier.fireDelimiterSetChangeEvent(
                evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
    
    public void addDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        mChangeNotifier.addDelimiterSetChangeListener(listener);
    }

    public DelimiterSetChangeListener[] getDelimiterSetChangeListeners() {
        return mChangeNotifier.getDelimiterSetChangeListeners();
    }

    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        mChangeNotifier.removeDelimiterSetChangeListener(listener);
    }

    public void addDelimiterSetChangeListener(DelimiterSetChangeListener[] listeners) {
        mChangeNotifier.addDelimiterSetChangeListener(listeners);
    }

    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener[] listeners) {
        mChangeNotifier.removeDelimiterSetChangeListener(listeners);
    }
}
