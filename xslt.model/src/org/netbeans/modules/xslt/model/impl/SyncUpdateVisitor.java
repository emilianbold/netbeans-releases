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
package org.netbeans.modules.xslt.model.impl;

import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xslt.model.ApplyImports;
import org.netbeans.modules.xslt.model.ApplyTemplates;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.Choose;
import org.netbeans.modules.xslt.model.Copy;
import org.netbeans.modules.xslt.model.CopyOf;
import org.netbeans.modules.xslt.model.Document;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.Import;
import org.netbeans.modules.xslt.model.Include;
import org.netbeans.modules.xslt.model.Key;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.Namespace;
import org.netbeans.modules.xslt.model.Number;
import org.netbeans.modules.xslt.model.Otherwise;
import org.netbeans.modules.xslt.model.Output;
import org.netbeans.modules.xslt.model.Param;
import org.netbeans.modules.xslt.model.ParamContainer;
import org.netbeans.modules.xslt.model.Sequence;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.StylesheetChild;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.Text;
import org.netbeans.modules.xslt.model.ValueOf;
import org.netbeans.modules.xslt.model.Variable;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.TypeSpec;
import org.netbeans.modules.xslt.model.UseAttributesSetsSpec;
import org.netbeans.modules.xslt.model.UseCharacterMapsSpec;
import org.netbeans.modules.xslt.model.WithParam;


/**
 * @author ads
 *
 */
class SyncUpdateVisitor implements ComponentUpdater<XslComponent>, XslVisitor 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( XslComponent target, XslComponent child, 
            Operation operation ) 
    {
        update(target, child, -1, operation);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.ComponentUpdater#update(org.netbeans.modules.xml.xam.Component, org.netbeans.modules.xml.xam.Component, int, org.netbeans.modules.xml.xam.ComponentUpdater.Operation)
     */
    public void update( XslComponent target, XslComponent child, int index, 
            Operation operation ) 
    {
        assert target != null;
        assert child != null;
        assert operation == null || operation == Operation.ADD || 
            operation == Operation.REMOVE;

        myParent = target;
        myIndex = index;
        myOperation = operation;
        child.accept(this);
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Stylesheet)
     */
    public void visit( Stylesheet stylesheet )
    {
        assert false : "Should never add or remove stylesheet root";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Template)
     */
    public void visit( Template template )
    {
        updateStylesheetChild(template);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.ApplyTemplates)
     */
    public void visit( ApplyTemplates applyTemplates ) {
        updateChildInSequenceConstructor( applyTemplates );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Attribute)
     */
    public void visit( Attribute attribute ) {
        if ( getParent() instanceof AttributeSet ) {
            AttributeSet set = (AttributeSet) getParent();
            if ( isAdd() ) {
                set.addAttribute(attribute, getIndex() );
            }
            else if( isRemove() )
                set.removeAttribute( attribute );
        }
        else {
            updateChildInSequenceConstructor( attribute );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.AttributeSet)
     */
    public void visit( AttributeSet attributeSet ) {
        updateStylesheetChild( attributeSet );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.CallTemplate)
     */
    public void visit( CallTemplate callTemplate ) {
        updateChildInSequenceConstructor( callTemplate );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Choose)
     */
    public void visit( Choose choose ) {
        updateChildInSequenceConstructor( choose );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Copy)
     */
    public void visit( Copy copy ) {
        updateChildInSequenceConstructor( copy );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.CopyOf)
     */
    public void visit( CopyOf copyOf ) {
        updateChildInSequenceConstructor( copyOf );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Document)
     */
    public void visit( Document document ) {
        updateChildInSequenceConstructor( document );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Element)
     */
    public void visit( Element element ) {
        updateChildInSequenceConstructor( element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.ForEach)
     */
    public void visit( ForEach forEach ) {
        updateChildInSequenceConstructor( forEach );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.If)
     */
    public void visit( If iff ) {
        updateChildInSequenceConstructor( iff );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Import)
     */
    public void visit( Import impt ) {
        assert getParent() instanceof Stylesheet;
        Stylesheet stylesheet = (Stylesheet)getParent();
        if ( isAdd() ) {
            stylesheet.addImport(impt, getIndex() );
        }
        else if ( isRemove() ) {
            stylesheet.removeImport(impt);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Include)
     */
    public void visit( Include include ) {
        updateStylesheetChild( include );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Key)
     */
    public void visit( Key key ) {
        updateStylesheetChild( key );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.LiteralResultElement)
     */
    public void visit( LiteralResultElement element ) {
        updateChildInSequenceConstructor( element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Namespace)
     */
    public void visit( Namespace namespace ) {
        updateChildInSequenceConstructor( namespace );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Number)
     */
    public void visit( Number number ) {
        updateChildInSequenceConstructor( number );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Otherwise)
     */
    public void visit( Otherwise otherwise ) {
        assert getParent() instanceof Choose;
        Choose choose = (Choose) getParent();
        if ( isAdd() ) {
            if ( getIndex() > -1 ) {
                addChild( Choose.OTHERWISE_PROPERTY , otherwise );
            }
            else {
                choose.setOtherwise(otherwise);
            }
        }
        else if ( isRemove() ) {
            removeChild( Choose.OTHERWISE_PROPERTY , otherwise );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Output)
     */
    public void visit( Output output ) {
        updateStylesheetChild( output );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Param)
     */
    public void visit( Param param ) {
        if ( getParent() instanceof ParamContainer ) {
            ParamContainer container = (ParamContainer) getParent();
            if ( isAdd() ) {
                container.addParam(param, getIndex() );
            }
            else if ( isRemove() ) {
                container.removeParam(param);
            }
        }
        else {
            updateStylesheetChild( param );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Sequence)
     */
    public void visit( Sequence sequence ) {
        updateChildInSequenceConstructor( sequence );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Sort)
     */
    public void visit( Sort sort ) {
        if ( getParent() instanceof ApplyTemplates ) {
            ApplyTemplates applyTemplates = (ApplyTemplates) getParent();
            if ( isAdd() ) {
                applyTemplates.addChildElement(sort, getIndex());
            }
            else if ( isRemove() ) {
                applyTemplates.removeChildElement(sort);
            }
        }
        else {
            assert getParent() instanceof ForEach;
            ForEach forEach = (ForEach) getParent();
            if (isAdd()) {
                forEach.addSort(sort, getIndex());
            }
            else if (isRemove()) {
                forEach.removeSort(sort);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Text)
     */
    public void visit( Text text ) {
        updateChildInSequenceConstructor( text );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.ValueOf)
     */
    public void visit( ValueOf valueOf ) {
        updateChildInSequenceConstructor( valueOf );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.Variable)
     */
    public void visit( Variable variable ) {
        if ( getParent() instanceof Stylesheet ) {
            updateStylesheetChild( variable );
        }
        else {
            updateChildInSequenceConstructor( variable );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.When)
     */
    public void visit( When when ) {
        assert getParent() instanceof Choose;
        Choose choose = (Choose) getParent();
        if ( isAdd() ) {
            choose.addWhen(when, getIndex() );
        }
        else if ( isRemove() ) {
            choose.removeWhen(when);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitor#visit(org.netbeans.modules.xslt.model.ApplyImports)
     */
    public void visit( ApplyImports impt ) {
        updateChildInSequenceConstructor(impt);
    }
    
    public void visit(TypeSpec typeSpec) {}
    public void visit(UseAttributesSetsSpec useAttributesSetsSpec) {}
    public void visit(UseCharacterMapsSpec useCharacterMapsSpec) {}
    public void visit(WithParam withParam) {}

    private void updateStylesheetChild( StylesheetChild child ) {
        assert getParent() instanceof Stylesheet;
        if ( isAdd() ) {
            ((Stylesheet)getParent()).addStylesheetChild( child, getIndex() );
        }
        else if ( isRemove() ) {
            ((Stylesheet)getParent()).removeStylesheetChild( child );
        }
    }
    
    private void updateChildInSequenceConstructor( SequenceElement element  ) {
        assert getParent() instanceof SequenceConstructor;
        SequenceConstructor constructor = (SequenceConstructor) getParent();
        if ( isAdd() ) {
            constructor.addSequenceChild(element, getIndex());
        }
        else if ( isRemove() ){
            constructor.removeSequenceChild(element);
        }
    }
    
    private void removeChild( String propertyName, Otherwise otherwise ) {
        assert getParent() instanceof AbstractComponent;
        ((AbstractComponent<XslComponent>) getParent()).removeChild(
                propertyName, otherwise );
    }

    private void addChild( String propertyName, XslComponent otherwise ) {
        assert getParent() instanceof AbstractComponent;
        ((AbstractComponent<XslComponent>) getParent()).insertAtIndex(
                propertyName, otherwise, getIndex());
    }
    
    private boolean isAdd() {
        return getOperation() == Operation.ADD;
    }
    
    private boolean isRemove() {
        return getOperation() == Operation.REMOVE;
    }

    private XslComponent getParent() {
        return myParent;
    }
    
    private int getIndex() {
        return myIndex;
    }
    
    private Operation getOperation() {
        return myOperation;
    }
    
    private XslComponent myParent;
    
    private int myIndex;
    
    private Operation myOperation;

}
