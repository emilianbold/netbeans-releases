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


package org.netbeans.modules.iep.editor.designer;

import org.netbeans.modules.iep.editor.ps.PropertyPanel;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.openide.util.NbBundle;

import com.nwoods.jgo.DomDoc;
import com.nwoods.jgo.DomElement;
import com.nwoods.jgo.DomNode;
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;

/**
 * SimpleNodeLabel is just a JGoText that treats a single click as a command
 * to start editing the text of the label.
 */
public class SimpleNodeLabel extends JGoText implements SharedConstants {
    JGoArea mParent = null;
    
    /** Create an empty label for a SimpleNode.  Call initialize() before using. */
    public SimpleNodeLabel() {
        super();
    }
    
    /** Create a label containing the given text for a SimpleNode */
    public SimpleNodeLabel(String text, JGoArea parent) {
        super(text);
        mParent = parent;
        initialize(text, parent);
    }
    
    public void initialize(String text, JGoArea parent) {
        setSelectable(true);
        setDraggable(false);
        setResizable(false);
        setVisible(true);
        setEditable(true);
        setEditOnSingleClick(false);
        setTransparent(true);
        setAlignment(JGoText.ALIGN_CENTER);
        setTopLeft(parent.getLeft(), parent.getTop());
        parent.addObjectAtTail(this);
        // Same font as org.netbeans.modules.iep.editor.tcg.palette.PaletteLabel
        setFaceName("Tahoma");
        setFontSize(10);
        setBold(false);
        setItalic(false);
    }
    
    public JGoArea getParent() {
	return mParent;
    }
    
    /* (non-Javadoc)
     * @see com.nwoods.jgo.JGoText#setText(java.lang.String)
     */
    @Override
    public void setText(String str) {
	String oldVal = getText();
	if (mParent != null && mParent instanceof EntityNode) {
	    EntityNode node = (EntityNode) mParent;
	    OperatorComponent comp =  node.getModelComponent();
	    IEPModel model = comp.getModel();
	    Property mProperty = comp.getProperty(PROP_NAME);
	    String modelVal = mProperty.getValue();
            OperatorComponentContainer ocContainer = model.getPlanComponent().getOperatorComponentContainer();
            if (oldVal.equals(str)) {
        	return;
            }
            if (modelVal.equals(str)) { 
        	// accounts for condition that ueser changed the name value 
        	// in the property dialog of operator.
        	super.setText(str);
        	return;
            }
	    if(ocContainer.findOperator(str) != null) {
		// popup info dialog that tells the user that the name is already in use.
		String msg = NbBundle.getMessage(SimpleNodeLabel.class,
			"SimpleNodeLabel.NAME_IS_ALREADY_TAKEN_BY_ANOTHER_OPERATOR",
			str);
		PlanCanvas.showInformationDialog(msg);
		return;
	    } else {
		super.setText(str);
	    }
	} else {
	    super.setText(str);
	}
    }

    public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup) {
        // Add <SimpleNodeLabel> element
        if (svgDoc.JGoXMLOutputEnabled()) {
            svgDoc.createJGoClassElement("org.netbeans.modules.iep.editor.designer.SimpleNodeLabel", jGoElementGroup);
        }
        
        // Have superclass add to the JGoObject group
        super.SVGWriteObject(svgDoc, jGoElementGroup);
    }
    
    public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, 
        DomElement svgElement, DomElement jGoChildElement)
    {
        if (jGoChildElement != null) {
            // This is a <SimpleNodeLabel> element
            super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingElement());
        }
        return svgElement.getNextSibling();
    }
    

}
