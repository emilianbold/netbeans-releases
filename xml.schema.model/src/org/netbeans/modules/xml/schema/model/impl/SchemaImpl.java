/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * SchemaImpl.java
 *
 * Created on October 4, 2005, 9:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.xam.locator.api.DepResolverException;
import org.netbeans.modules.xml.xam.locator.api.DependencyResolver;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Schema.Block;
import org.netbeans.modules.xml.schema.model.Schema.Final;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.openide.ErrorManager;
import org.w3c.dom.Element;

/**
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 */

public class SchemaImpl extends SchemaComponentImpl implements Schema {
    
    /** Creates a new instance of SchemaImpl */
    public SchemaImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SCHEMA, model));
    }
    
    public SchemaImpl(SchemaModelImpl model, Element e){
        super(model,e);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return Schema.class;
    }
    
    public Collection<SchemaComponent> getSchemaReferences() {
        List<Class<? extends SchemaComponent>> types = new ArrayList<Class<? extends SchemaComponent>>();
        types.add(Include.class);
        types.add(Redefine.class);
        types.add(Import.class);
        
        return getChildren(types);
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<GlobalElement> getElements() {
        return getChildren(GlobalElement.class);
    }
    
    public void removeElement(GlobalElement element) {
        removeChild(ELEMENTS_PROPERTY, element);
    }
    
    public void addElement(GlobalElement element) {
        appendChild(ELEMENTS_PROPERTY, element);
    }
    
    public Collection<GlobalAttributeGroup> getAttributeGroups() {
        return getChildren(GlobalAttributeGroup.class);
    }
    
    public void removeAttributeGroup(GlobalAttributeGroup group) {
        removeChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void addAttributeGroup(GlobalAttributeGroup group) {
        appendChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void removeExternalReference(SchemaComponent ref) {
        removeChild(SCHEMA_REFERENCES_PROPERTY, ref);
    }
    
    public void addExternalReference(SchemaComponent ref) {
	List<Class<? extends SchemaComponent>> afterChildren = 
		new ArrayList<Class<? extends SchemaComponent>>();
	afterChildren.add(Include.class);
	afterChildren.add(Import.class);
	afterChildren.add(Redefine.class);
	addAfter(SCHEMA_REFERENCES_PROPERTY, ref, afterChildren);
    }
    
    public Collection<GlobalComplexType> getComplexTypes() {
        return getChildren(GlobalComplexType.class);
    }
    
    public void removeComplexType(GlobalComplexType type) {
        removeChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public void addComplexType(GlobalComplexType type) {
        appendChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalAttribute> getAttributes() {
        return getChildren(GlobalAttribute.class);
    }
    
    public void addAttribute(GlobalAttribute attr) {
        appendChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void removeAttribute(GlobalAttribute attr) {
        removeChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void setVersion(String ver) {
        setAttribute(VERSION_PROPERTY, SchemaAttributes.VERSION, ver);
    }
    
    public String getVersion() {
        return getAttribute(SchemaAttributes.VERSION);
    }
    
    public void setLanguage(String language) {
        setAttribute(LANGUAGE_PROPERTY, SchemaAttributes.LANGUAGE, language);
    }
    
    public String getLanguage() {
        return getAttribute(SchemaAttributes.LANGUAGE);
    }
    
    public void setFinalDefault(Set<Final> finalDefault) {
        setAttribute(FINAL_DEFAULT_PROPERTY, SchemaAttributes.FINAL_DEFAULT, 
                finalDefault == null ? null : 
                    Util.convertEnumSet(Final.class, finalDefault));
    }
    
    public Set<Final> getFinalDefault() {
        String s = getAttribute(SchemaAttributes.FINAL_DEFAULT);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
    
    public Set<Final> getFinalDefaultEffective() {
        Set<Final> v = getFinalDefault();
        return v == null ? getFinalDefaultDefault() : v;
    }
    
    public Set<Final> getFinalDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Final>();
    }
    
    public void setTargetNamespace(String uri) {
        setAttribute(TARGET_NAMESPACE_PROPERTY, SchemaAttributes.TARGET_NS, uri);
    }
    
    public String getTargetNamespace() {
        return getAttribute(SchemaAttributes.TARGET_NS);
    }
    
    public void setElementFormDefault(Form form) {
        setAttribute(ELEMENT_FORM_DEFAULT_PROPERTY, SchemaAttributes.ELEM_FORM_DEFAULT, form);
    }
    
    public Form getElementFormDefault() {
        String s = getAttribute(SchemaAttributes.ELEM_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public void setAttributeFormDefault(Form form) {
        setAttribute(ATTRIBUTE_FORM_DEFAULT_PROPERTY, SchemaAttributes.ATTR_FORM_DEFAULT, form);
    }
    
    public Form getAttributeFormDefault() {
        String s = getAttribute(SchemaAttributes.ATTR_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public Collection<GlobalSimpleType> getSimpleTypes() {
        return getChildren(GlobalSimpleType.class);
    }
    
    public void removeSimpleType(GlobalSimpleType type) {
        removeChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public void addSimpleType(GlobalSimpleType type) {
        appendChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalGroup> getGroups() {
        return getChildren(GlobalGroup.class);
    }
    
    public void removeGroup(GlobalGroup group) {
        removeChild(GROUPS_PROPERTY, group);
    }
    
    public void addGroup(GlobalGroup group) {
        appendChild(GROUPS_PROPERTY, group);
    }
    
    public Collection<Notation> getNotations() {
        return getChildren(Notation.class);
    }
    
    public void removeNotation(Notation notation) {
        removeChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void addNotation(Notation notation) {
        appendChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void setBlockDefault(Set<Block> blockDefault) {
        setAttribute(BLOCK_DEFAULT_PROPERTY, SchemaAttributes.BLOCK_DEFAULT, 
                blockDefault == null ? null : 
                    Util.convertEnumSet(Block.class, blockDefault));
    }
    
    public Set<Block> getBlockDefault() {
        String s = getAttribute(SchemaAttributes.BLOCK_DEFAULT);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }
    
    public Set<Block> getBlockDefaultEffective() {
        Set<Block> v = getBlockDefault();
        return v == null ? getBlockDefaultDefault() : v;
    }
    
    public Set<Block> getBlockDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Block>();
    }
    
    public Form getElementFormDefaultEffective() {
        Form v = getElementFormDefault();
        return v == null ? getElementFormDefaultDefault() : v;
    }
    
    public Form getElementFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Form getAttributeFormDefaultEffective() {
        Form v = getAttributeFormDefault();
        return v == null ? getAttributeFormDefaultDefault() : v;
    }
    
    public Form getAttributeFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Collection<ModelSource> getRedefinedModelSources() {
        Collection<Redefine> redefinedSchemas = getChildren(Redefine.class);
        Collection<ModelSource> redefinedModelSources = new ArrayList<ModelSource>(redefinedSchemas.size());
        DependencyResolver resolver = getResolver();
        for(Redefine redefinedSchema: redefinedSchemas) {
            String schemaLoc = redefinedSchema.getSchemaLocation();
            try {
                ModelSource modelSource = resolver.getModelSource(new URI(schemaLoc));
                redefinedModelSources.add(modelSource);
            } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return redefinedModelSources;
    }
    
    public Collection<SchemaModel> getRedefinedSchemas() {
        Collection<ModelSource> redefinedModelSources = getRedefinedModelSources();
        Collection<SchemaModel> redefinedSchemaModels = new ArrayList<SchemaModel>(redefinedModelSources.size());
        for(ModelSource modelSource: redefinedModelSources) {
            try {
                SchemaModel model = SchemaModelFactory.getDefault().getModel(modelSource);
                redefinedSchemaModels.add(model);
            } catch(IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return redefinedSchemaModels;
    }
    
    public Collection<ModelSource> getIncludedModelSources() {
        Collection<Include> includedSchemas = getChildren(Include.class);
        DependencyResolver resolver = getResolver();
        Collection<ModelSource> includedModelSources = new ArrayList<ModelSource>(includedSchemas.size());
        for(Include includedSchema: includedSchemas) {
            String schemaLoc = includedSchema.getSchemaLocation();
            try {
                ModelSource modelSource = resolver.getModelSource(new URI(schemaLoc));
                includedModelSources.add(modelSource);
            } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return includedModelSources;
    }
    
    public Collection<SchemaModel> getIncludedSchemas() {
        Collection<ModelSource> includedModelSources = getIncludedModelSources();
        Collection<SchemaModel> includedSchemaModels = new ArrayList<SchemaModel>(includedModelSources.size());
        for(ModelSource modelSource: includedModelSources) {
            try {
                SchemaModel model = SchemaModelFactory.getDefault().getModel(modelSource);
                includedSchemaModels.add(model);
            } catch(IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return includedSchemaModels;
    }
    
    private DependencyResolver getResolver() {
        try {
            return getModel().getModelSource().getResolver();
        } catch(DepResolverException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Collection<ModelSource> getImportedModelSources() {
        Collection<Import> imports = getChildren(Import.class);
        DependencyResolver resolver = getResolver();
        Collection<ModelSource> importedModelSources = new ArrayList<ModelSource>(imports.size());
        for(Import i : imports) {
            String schemaLocation = i.getSchemaLocation();
            try {
                ModelSource modelSource = resolver.getModelSource(new URI(schemaLocation));
                importedModelSources.add(modelSource);
            } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return importedModelSources;
    }
    
    public Collection<SchemaModel> getImportedSchemas() {
        Collection<ModelSource> importedModelSources = getImportedModelSources();
        Collection<SchemaModel> importedSchemaModels = new ArrayList<SchemaModel>(importedModelSources.size());
        for(ModelSource modelSource: importedModelSources) {
            try {
                SchemaModel model = SchemaModelFactory.getDefault().getModel(modelSource);
                importedSchemaModels.add(model);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        return importedSchemaModels;
    }
    
    public Collection<GlobalElement> findAllGlobalElements() {
        Collection<GlobalElement> result = new ArrayList<GlobalElement>();
        Collection<GlobalElement> tempCollection = this.getElements();
        if(tempCollection != null) {
            result.addAll(tempCollection);
        }
        result.addAll(getExternalGlobalElements(this.getImportedSchemas()));
        result.addAll(getExternalGlobalElements(this.getIncludedSchemas()));
        //TODO consolidate redefinitions
        result.addAll(getExternalGlobalElements(this.getRedefinedSchemas()));
        return result;
    }
    
    private Collection<GlobalElement> getExternalGlobalElements(Collection<SchemaModel> externalRefs){
        Collection<GlobalElement> result = new ArrayList<GlobalElement>();
        for(SchemaModel sm : externalRefs){
            result.addAll( sm.getSchema().findAllGlobalElements());
        }
        return result;
    }
    
    public Collection<GlobalType> findAllGlobalTypes() {
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        //add all SimpleTypes
        Collection<? extends GlobalType> tempCollection = this.getSimpleTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add all complex types
        tempCollection = this.getComplexTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add from all the referenced docs
        result.addAll(getExternalGlobalTypes(this.getImportedSchemas()));
        result.addAll(getExternalGlobalTypes(this.getIncludedSchemas()));
        result.addAll(getExternalGlobalTypes(this.getRedefinedSchemas()));
        return result;
    }
    
    private Collection<GlobalType> getExternalGlobalTypes(Collection<SchemaModel> externalRefs){
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        for(SchemaModel sm : externalRefs){
            result.addAll( sm.getSchema().findAllGlobalTypes());
        }
        return result;
    }
}
