/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater.Operation;
/**
 *
 * @author Nam Nguyen
 */
public class SyncUpdateVisitor<T extends SchemaComponent> implements SchemaVisitor, ComponentUpdater<T> {
    
    private Operation operation;
    private SchemaComponent parent;
    private int index;
    
    public SyncUpdateVisitor(){}
    
    public void update(SchemaComponent target, SchemaComponent child, 
            Operation operation) {
        update(target, child, -1, operation);
    }
    public void update(SchemaComponent target, SchemaComponent child, int index,
            Operation operation) {
        assert target != null;
        assert child != null;
        assert operation == Operation.ADD || operation == Operation.REMOVE;

        this.parent = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

    private Schema getSchema() {
        assert (parent instanceof Schema);
        return (Schema) parent;
    }
    
    public void visit(Schema schema) {
        assert false; //should never happen
    }
    
    public void visit(GlobalAttribute child) {
        if(operation == Operation.ADD) {
            getSchema().addAttribute(child);
        } else {
            getSchema().removeAttribute(child);
        }
    }
    
    public void visit(GlobalAttributeGroup child) {
        if(operation == Operation.ADD) {
            getSchema().addAttributeGroup(child);
        } else {
            getSchema().removeAttributeGroup(child);
        }
    }
    
    public void visit(GlobalElement child) {
        if(operation == Operation.ADD) {
            getSchema().addElement(child);
        } else {
            getSchema().removeElement(child);
        }
    }
    
    public void visit(GlobalGroup child) {
        if(operation == Operation.ADD) {
            getSchema().addGroup(child);
        } else {
            getSchema().removeGroup(child);
        }
    }
    
    public void visit(GlobalSimpleType child) {
        if(operation == Operation.ADD) {
            getSchema().addSimpleType(child);
        } else {
            getSchema().removeSimpleType(child);
        }
    }
    
    public void visit(GlobalComplexType child) {
        if(operation == Operation.ADD) {
            getSchema().addComplexType(child);
        } else {
            getSchema().removeComplexType(child);
        }
    }
    
    public void visit(Notation child) {
        if(operation == Operation.ADD) {
            getSchema().addNotation(child);
        } else {
            getSchema().removeNotation(child);
        }
    }
    
    public void visit(Import child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else {
            getSchema().removeExternalReference(child);
        }
    }
    
    public void visit(Include child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else {
            getSchema().removeExternalReference(child);
        }
    }
    
    public void visit(Redefine child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else {
            getSchema().removeExternalReference(child);
        }
    }

    public void visit(LocalSimpleType child) {
        if (parent instanceof List) {
            List list = (List) parent;
            if (operation == Operation.ADD) {
                list.setInlineType(child);
            } else {
                list.setInlineType(null);
            }
        } else if (parent instanceof Union) {
            Union union = (Union) parent;
            if (operation == Operation.ADD) {
                union.addInlineType(child);
            } else {
                union.removeInlineType(child);
            }
        } else if (parent instanceof LocalAttribute || 
		   parent instanceof GlobalAttribute) {
	    if (parent instanceof LocalAttribute) {
		LocalAttribute a = (LocalAttribute) parent;
		if (operation == Operation.ADD) {
		    a.setInlineType(child);
		} else {
		    a.setInlineType(null);
		}
	    } else {
		GlobalAttribute a = (GlobalAttribute) parent;
		if (operation == Operation.ADD) {
		    a.setInlineType(child);
		} else {
		    a.setInlineType(null);
		}
	    }
        } else if (parent instanceof SimpleRestriction) {
            SimpleRestriction sr = (SimpleRestriction) parent;
            if (operation == Operation.ADD) {
                sr.setInlineType(child);
            } else {
                sr.setInlineType(null);
            }
        } else if (parent instanceof TypeContainer) {
            TypeContainer target = (TypeContainer) parent;
            if (operation == Operation.ADD) {
                target.setInlineType(child);
            } else {
                target.setInlineType(null);
            }
        } else {
            System.err.println(parent.getClass().getName());
            assert false;
        }
    }

    public void visit(All child) {
        assert (parent instanceof ComplexContentRestriction ||
                parent instanceof ComplexType ||
                parent instanceof GlobalGroup);
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else if (parent instanceof GlobalGroup) {
            GlobalGroup target = (GlobalGroup) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        }
    }

    public void visit(ComplexContentRestriction child) {
        assert (parent instanceof ComplexContent);
        ComplexContent target = (ComplexContent) parent;
        if (operation == Operation.ADD) {
            target.setLocalDefinition(child);
        } else {
            // required, can't set null
        }
    }

    public void visit(AnyElement child) {
        if (parent instanceof CommonChoice) {
            CommonChoice target = (CommonChoice) parent;
            if (operation == Operation.ADD)
                target.addAny(child);
            else 
                target.removeAny(child);
        } else if (parent instanceof CommonSequence) {
            CommonSequence target = (CommonSequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else 
                target.removeContent(child);
        }
    }

    public void visit(AllElement child) {
        assert (parent instanceof GroupAll);
        GroupAll target = (GroupAll) parent;
        if (operation == Operation.ADD)
            target.addElement(child);
        else 
            target.removeElement(child);
    }

    public void visit(GroupReference child) {
        assert (parent instanceof CommonChoice);
        CommonChoice target = (CommonChoice) parent;
        if (operation == Operation.ADD)
            target.addGroupReference(child);
        else 
            target.removeGroupReference(child);
    }

    public void visit(Enumeration child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addEnumeration(child);
        else 
            target.removeEnumeration(child);
    }

    private void updateConstraintOnCommonElement(Constraint child) {
        assert (parent instanceof CommonElement);
        CommonElement target = (CommonElement) parent;
        if (operation == Operation.ADD)
            target.addConstraint(child);
        else 
            target.removeConstraint(child);
    }
    
    public void visit(KeyRef child) {
        updateConstraintOnCommonElement(child);
    }

    public void visit(Key child) {
        updateConstraintOnCommonElement(child);
    }

    public void visit(Unique child) {
        updateConstraintOnCommonElement(child);
    }

    public void visit(AttributeGroupReference child) {
        assert (parent instanceof LocalAttributeContainer);
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            target.addAttributeGroupReference(child);
        else 
            target.removeAttributeGroupReference(child);
    }

    private void updateGlobalGroup(LocalGroupDefinition child) {
        assert (parent instanceof GlobalGroup);
        GlobalGroup target = (GlobalGroup) parent;
        if (operation == Operation.ADD) 
            target.setDefinition(child);
        else
            target.setDefinition(null);
    }

    public void visit(GroupSequence child) {
        updateGlobalGroup(child);
    }

    public void visit(GroupAll child) {
        updateGlobalGroup(child);
    }

    public void visit(GroupChoice child) {
        updateGlobalGroup(child);
    }

    public void visit(Documentation child) {
        assert (parent instanceof Annotation);
        Annotation target = (Annotation) parent;
        if (operation == Operation.ADD)
            target.addDocumentation(child);
        else 
            target.removeDocumentation(child);
    }

    public void visit(AppInfo child) {
        assert (parent instanceof Annotation);
        Annotation target = (Annotation) parent;
        if (operation == Operation.ADD)
            target.addAppInfo(child);
        else 
            target.removeAppInfo(child);
    }
    
    public void visit(Choice child) {
        if (parent instanceof CommonChoice) {
            CommonChoice target = (CommonChoice) parent;
            if (operation == Operation.ADD)
                target.addChoice(child);
            else 
                target.removeChoice(child);
        } else if (parent instanceof ComplexExtension) {
            ComplexExtension target = (ComplexExtension) parent;
            if (operation.equals(Operation.ADD)) {
                target.setLocalDefinition(child);
            } else  {
                target.setLocalDefinition(null);
            }
        } else if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation.equals(Operation.ADD)) {
                target.setDefinition(child);
            } else  {
                target.setDefinition(null);
            }
        } else if (parent instanceof CommonSequence) {
            CommonSequence target = (CommonSequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else 
                target.removeContent(child);
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD)
                target.setDefinition(child);
            else 
                target.setDefinition(null);
        } else {
            System.err.println(parent.getClass().getName());
            assert false;
        } 
        
    }

    public void visit(SimpleContentRestriction child) {
        assert (parent instanceof SimpleContent);
        SimpleContent target = (SimpleContent) parent;
        if (operation == Operation.ADD)
            target.setLocalDefinition(child);
        else
            target.setLocalDefinition(null);
    }

    public void visit(Selector child) {
        assert (parent instanceof Constraint);
        Constraint target = (Constraint) parent;
        if (operation == Operation.ADD) 
            target.setSelector(child);
        else 
            target.setSelector(null);
    }

    public void visit(LocalElement child) {
        if (parent instanceof CommonChoice) {
            CommonChoice target = (CommonChoice) parent;
            if (operation == Operation.ADD)
                target.addLocalElement(child);
            else 
                target.removeLocalElement(child);
        } else if (parent instanceof CommonSequence) {
            CommonSequence target = (CommonSequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else 
                target.removeContent(child);
        } else {
            assert false;
        } 
    }
    
    public void visit(ElementReference child) {
	assert parent instanceof CommonChoice ||
	       parent instanceof CommonSequence;
	
	if (parent instanceof CommonChoice) {
            CommonChoice target = (CommonChoice) parent;
            if (operation == Operation.ADD)
                target.addElementReference(child);
            else 
                target.removeElementReference(child);
        } else if (parent instanceof CommonSequence) {
            CommonSequence target = (CommonSequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else 
                target.removeContent(child);
        } 
	
    }

     public void visit(AllElementReference child) {
	assert parent instanceof All ||
	       parent instanceof GroupAll;
	
	if (parent instanceof All) {
            All target = (All) parent;
            if (operation == Operation.ADD)
                target.addElementReference(child);
            else 
                target.removeElementReference(child);
        } else if (parent instanceof GroupAll) {
            GroupAll target = (GroupAll) parent;
            if (operation == Operation.ADD)
                target.addElementReference(child);
            else 
                target.removeElementReference(child);
        } 
	
    }
    
    public void visit(Annotation child) {
        if (operation == Operation.ADD) {
            parent.setAnnotation(child);
        } else {
            parent.setAnnotation(null);
        }
    }

    public void visit(ComplexExtension child) {
        assert (parent instanceof ComplexContent);
        ComplexContent target = (ComplexContent) parent;
        if (operation == Operation.ADD) {
            target.setLocalDefinition(child);
        } else {
            // can't set null
        }
    }

    public void visit(SimpleExtension child) {
        assert (parent instanceof SimpleContent);
        SimpleContent target = (SimpleContent) parent;
        if (operation == Operation.ADD) {
            target.setLocalDefinition(child);
        } else {
            target.setLocalDefinition(null);
        }
    }

    public void visit(Sequence child) {
        if (parent instanceof CommonChoice) {
            CommonChoice target = (CommonChoice) parent;
            if (operation == Operation.ADD)
                target.addSequence(child);
            else 
                target.removeSequence(child);
        } else if (parent instanceof CommonSequence) {
            CommonSequence target = (CommonSequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else 
                target.removeContent(child);
        } else if (parent instanceof ComplexExtension) {
            ComplexExtension target = (ComplexExtension) parent;
            if (operation == Operation.ADD) {
                target.setLocalDefinition(child);
            } else {
                target.setLocalDefinition(null);
            }
        } else if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation.equals(Operation.ADD)) {
                target.setDefinition(child);
            } else  {
                target.setDefinition(null);
            }
        } else {
            assert false;
        } 
    }

    public void visit(MinExclusive child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMinExclusive(child);
        else 
            target.removeMinExclusive(child);
    }

    public void visit(MinInclusive child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMinInclusive(child);
        else 
            target.removeMinInclusive(child);
    }

    public void visit(Pattern child) {
        assert (parent instanceof SimpleRestriction);
            
        SimpleRestriction target = (SimpleRestriction) parent;
        
        if (operation == Operation.ADD)
        
            target.addPattern(child);
            
        else 
        
            target.removePattern(child);
    }

    public void visit(MinLength child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMinLength(child);
        else 
            target.removeMinLength(child);
    }

    public void visit(MaxLength child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMaxLength(child);
        else 
            target.removeMaxLength(child);
    }

    public void visit(Whitespace child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addWhitespace(child);
        else 
            target.removeWhitespace(child);
    }

    public void visit(MaxInclusive child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMaxInclusive(child);
        else 
            target.removeMaxInclusive(child);
    }

    public void visit(LocalComplexType child) {
	assert parent instanceof TypeContainer;
        TypeContainer target = (TypeContainer) parent;
        if (operation == Operation.ADD) {
            target.setInlineType(child);
        } else {
            target.setInlineType(null);
        }
    }

    public void visit(FractionDigits child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addFractionDigits(child);
        else 
            target.removeFractionDigits(child);
    }

    public void visit(TotalDigits child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addTotalDigit(child);
        else 
            target.removeTotalDigit(child);
    }

    private void updateSimpleType(SimpleTypeDefinition child) {
        assert (parent instanceof SimpleType);
        SimpleType target = (SimpleType) parent;
        if (operation == Operation.ADD) {
            target.setDefinition(child);
        } else {
            // can't set null
        }
    }
    
    public void visit(List child) {
        updateSimpleType(child);
    }

    public void visit(SimpleTypeRestriction child) {
        updateSimpleType(child);
    }

    public void visit(Union child) {
        updateSimpleType(child);
    }

    public void visit(MaxExclusive child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addMaxExclusive(child);
        else 
            target.removeMaxExclusive(child);
    }

    public void visit(AttributeReference child) {
        assert (parent instanceof LocalAttributeContainer);
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            target.addAttributeReference(child);
        else 
            target.removeAttributeReference(child);
    }
    
    public void visit(LocalAttribute child) {
        assert (parent instanceof LocalAttributeContainer);
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            target.addLocalAttribute(child);
        else 
            target.removeLocalAttribute(child);
    }

    public void visit(SimpleContent child) {
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else {
            assert false;
        } 
    }

    public void visit(ComplexContent child) {
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                target.setDefinition(child);
            } else {
                target.setDefinition(null);
            }
        } else {
            assert false;
        } 
        
    }

    public void visit(AnyAttribute child) {
        assert (parent instanceof LocalAttributeContainer);
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD) {
            target.setAnyAttribute(child);
        } else {
            target.setAnyAttribute(null);
        }
    }

    public void visit(Length child) {
        assert (parent instanceof SimpleRestriction);
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            target.addLength(child);
        else 
            target.removeLength(child);
    }

    public void visit(Field child) {
        assert (parent instanceof Constraint);
        Constraint target = (Constraint) parent;
        if (operation == Operation.ADD)
            target.addField(child);
        else 
            target.deleteField(child);
    }
}
