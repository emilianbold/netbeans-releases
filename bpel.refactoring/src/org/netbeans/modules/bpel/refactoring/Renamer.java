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
package org.netbeans.modules.bpel.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.FaultNameReference;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeReference;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.PropertyReference;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.refactoring.api.RenameRefactoring;

import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import static org.netbeans.modules.print.api.PrintUI.*;


/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
class Renamer extends Plugin implements XMLRefactoringPlugin{
    
    public Renamer(RenameRefactoring refactoring) {
        this.request = refactoring;
    }

   
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        ErrorItem error = null;
        if(obj instanceof Model) {
           error = RefactoringUtil.precheck((Model)obj, request.getNewName());
        } else if(obj instanceof Nameable) {
           error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        }
                
        if (error != null) {
            Problem p = new Problem(true, error.getMessage());
            return p;
        }
        
        return null;
    }
    
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
       Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
       if( obj == null)
           return null;
       if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
         
        Model model = SharedUtils.getModel(obj);
        ErrorItem error = RefactoringUtil.precheckTarget(model, true);
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
       
        if(obj instanceof Model)
            error  = RefactoringUtil.precheck((Model)model, request.getNewName());
        else if(obj instanceof Nameable)
            error = RefactoringUtil.precheck((Nameable)obj, request.getNewName());
        if(error != null)
            return new Problem(isFatal(error), error.getMessage());
        
              
        return null;
       
        
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        if(obj == null)
            return null;
        if( !((obj instanceof Model) ||  (obj instanceof Nameable)) )
            return null;
             
        fireProgressListenerStart(ProgressEvent.START, -1);
        Set<Component> searchRoots = getSearchRoots(obj);
       
        List<Element> elements = new ArrayList<Element>();
        for (Component root : searchRoots) {
            List<Element> founds = find(obj, root);
            if (founds != null) {
                   elements.addAll(founds);
            }
        }
       
         
        if(elements!=null && elements.size() > 0) {
            List<Model> models = getModels(elements);
            List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
            if(errors !=null && errors.size() > 0 ){
                return processErrors(errors);
              } 
        } 
        
        //get the gloabl object responsible for refactoring
        XMLRefactoringTransaction transaction = request.getContext().lookup(XMLRefactoringTransaction.class);
        transaction.register((XMLRefactoringPlugin)this, elements);
        refactoringElements.registerTransaction(transaction);
        if (elements != null && elements.size() >0 )   {
            for (RefactoringElementImplementation ug : elements) {
                //System.out.println("BPELRenameRefactoring::adding element");
                refactoringElements.add(request, ug);
                fireProgressListenerStep();
             }
        }      
              
        fireProgressListenerStop();
        return null;
    }
      
    public Problem processErrors(List<ErrorItem> errorItems){
        
        if (errorItems == null || errorItems.size()== 0){
            return null;
        }
        Problem parent = null;
        Problem child = null;
        Problem head = null;
        Iterator<ErrorItem> iterator = errorItems.iterator();
                
        while(iterator.hasNext()) {
            ErrorItem error = iterator.next();
            if(parent == null ){
                parent = new Problem(isFatal(error), error.getMessage());
                child = parent;
                head = parent;
                continue;
            }
            child = new Problem(isFatal(error), error.getMessage());
            parent.setNext(child);
            parent = child;
            
        }
        
       
        return head;
    }
    
    public boolean isFatal(ErrorItem error){
        if(error.getLevel() == ErrorItem.Level.FATAL)
            return true;
        else
            return false;
   }  
    
     
    public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
        Map<Model, Set<RefactoringElementImplementation>> modelsInRefactoring = getModelMap(elements);
        Set<Model> models = modelsInRefactoring.keySet();
        Referenceable obj = request.getRefactoringSource().lookup(Referenceable.class);
        String oldName = request.getContext().lookup(String.class);
        
        for(Model model: models){
            if(model instanceof BpelModel){
                 if(obj instanceof Nameable){
                     /*new Renamer().*/rename(getComponents(modelsInRefactoring.get(model)), model, (Named)obj, oldName);
                 }else if(obj instanceof Model){
                     /*new Renamer().*/rename(getComponents(modelsInRefactoring.get(model)), request);
                 }
            }
        }       
}
    
    private List<Component> getComponents(Set<RefactoringElementImplementation> bpelElements) {
        List<Component> comp = new ArrayList<Component>(bpelElements.size());
        for(RefactoringElementImplementation elem: bpelElements){
            comp.add(elem.getLookup().lookup(Component.class));
        }
        return comp;
    }
       
    /**
     * @param component the component to check for model reference.
     * @return the reference string if this component is a reference to an 
     * external model, for example, the schema <import> component, 
     * otherwise returns null.
     */
     public String getModelReference(Component component) {
         if (component instanceof Import) {
              return ((Import) component).getLocation();
         }
         return null;
     }




/// --------------------------------------

  void rename(
    List<Component> components,
    Model model,
    Named target,
    String oldName) throws IOException
  {
    if (target == null || components == null || model == null) {
      return;
    }
    myXPath = new XPath(null, target, oldName);
    myOldName = oldName;
    boolean doTransaction = !model.isIntransaction(); 
    
    try {
      if (doTransaction) {
        model.startTransaction();
      }
      for (Component component : components) {
        renameComponent(component, target);
      }
    } 
    finally {
      if (doTransaction && model.isIntransaction()) {
        model.endTransaction();
      }  
    }
  }

  private void rename(
    List<Component> components,
    RenameRefactoring request) throws IOException
  {
    if (components == null) {
      return;
    }
    for (Component component : components) {
      renameFile(component, request);
    }
  }

  private void renameFile(
    Component component,
    RenameRefactoring request) throws IOException
  {
//out();
//out("FILE RENAME: " + Util.getName(component));
    if ( !(component instanceof Import)) {
      return;
    }
    try {
      Import _import = (Import) component;
      _import.setLocation(SharedUtils.calculateNewLocationString(_import.getLocation(), request));
    }
    catch(VetoException e) {
      throw new IOException(e.getMessage());
    }
  }

  private void renameComponent(Component component,Named target) throws IOException{
//out();
//out("RENAME: " + Util.getName(target));
//out("    in: " + Util.getName(component));
//out();
    if (component instanceof PartReference &&
      ((PartReference) component).getPart() != null &&
      component instanceof ReferenceCollection &&
      target instanceof Part)
    {
      rename((PartReference) component, (Part) target);
    }
    else if (component instanceof PropertyReference &&
      ((PropertyReference) component).getProperty() != null &&
      component instanceof ReferenceCollection &&
      target instanceof CorrelationProperty)
    {
      rename((PropertyReference) component, (CorrelationProperty) target);
    }
    else if (component instanceof Variable) {
      rename((Variable) component, target);
    }
    else {
      renameCamponent(component, target);
    }
  }

  private void renameCamponent(Component component,Named target) throws IOException{
    if (component instanceof OperationReference &&
      component instanceof ReferenceCollection &&
      target instanceof Operation)
    {
      rename((OperationReference) component, (Operation) target);
    }
    else if (component instanceof PortTypeReference &&
      component instanceof ReferenceCollection &&
      target instanceof PortType)
    {
      rename((PortTypeReference) component, (PortType) target);
    }
    else if (component instanceof MessageExchangeReference &&
      component instanceof ReferenceCollection &&
      target instanceof MessageExchange)
    {
      rename((MessageExchangeReference) component, (MessageExchange) target);
    }
    else {
      renameCampanent(component, target);
    }
  }

  private void renameCampanent(Component component,Named target) throws IOException{
    if (component instanceof Role &&
      target instanceof PortType)
    {
      rename((Role) component, (PortType) target);
    }
    else if (component instanceof Catch) {
      rename((Catch) component, target);
    }
    else if (component instanceof Reply) {
      rename((Reply) component, target);
    }
    else if (component instanceof Throw) {
      rename((Throw) component, target);
    }
    else if (component instanceof OnEvent) {
      rename((OnEvent) component, target);
    }
    else if (component instanceof CorrelationProperty) {
      rename((CorrelationProperty) component, target);
    }
    else if (component instanceof PropertyAlias) {
      rename((PropertyAlias) component, target);
    }
    else if (component instanceof PartnerLink) {
      rename((PartnerLink) component, target);
    }
    else {
      renameKomponent(component, target);
    }
  }

  private void renameKomponent(Component component,Named target) throws IOException{
    if (component instanceof CorrelationSet) {
      rename((CorrelationSet) component, target);
    }
    else if (component instanceof ContentElement &&
      component instanceof BpelEntity)
    {
      rename((ContentElement) component);
    }
    else {
//out();
//out("!!! RENAME IN !!! : " + component.getClass().getName());
//out();
      return;
    }
  }

  private void rename(PartReference reference, Part part) {
    reference.setPart(((ReferenceCollection) reference).
      createWSDLReference(part, Part.class));
  }

  private void rename(PropertyReference reference, CorrelationProperty property) {
    reference.setProperty(((ReferenceCollection) reference).
      createWSDLReference(property, CorrelationProperty.class));
  }

  private void rename(PortTypeReference reference, PortType portType) {
    reference.setPortType(((ReferenceCollection) reference).
      createWSDLReference(portType, PortType.class));
  }

  private void rename(OperationReference reference, Operation operation) {
    reference.setOperation(((ReferenceCollection) reference).
      createWSDLReference(operation, Operation.class));
  }

  private void rename(MessageExchangeReference reference, MessageExchange exchange) {
    reference.setMessageExchange(((ReferenceCollection) reference).
      createReference(exchange, MessageExchange.class));
  }

  private void rename(Role role, PortType target) throws IOException {
      role.setPortType(target.createReferenceTo(target, PortType.class));
  }

  private void rename(ContentElement element) throws IOException {
    String content = myXPath.rename(element.getContent(), (BpelEntity) element);
//out();
//out("New content: " + content);
//out();
    try {
      element.setContent(content);
    }
    catch(VetoException e) {
      throw new IOException(e.getMessage());
    }
  }

  private void rename(
    CorrelationProperty property,
    Named target) throws IOException
  {
    NamedComponentReference<GlobalElement> element = property.getElement();
    if (element != null && target instanceof GlobalElement) {
      property.setElement(((GlobalElement) target).createReferenceTo(
        (GlobalElement) target, GlobalElement.class));
    }
    NamedComponentReference<GlobalType> type = property.getType();

    if (type != null && target instanceof GlobalType) {
      property.setType(((GlobalType) target).createReferenceTo(
        (GlobalType) target, GlobalType.class));
    }
  }

  private void rename(PropertyAlias alias, Named target) throws IOException {
    NamedComponentReference<GlobalElement> element = alias.getElement();

    if (element != null && target instanceof GlobalElement) {
      alias.setElement(((GlobalElement) target).createReferenceTo(
        (GlobalElement) target, GlobalElement.class));
    }
    NamedComponentReference<GlobalType> type = alias.getType();

    if (type != null && target instanceof GlobalType) {
      alias.setType(((GlobalType) target).createReferenceTo(
        (GlobalType) target, GlobalType.class));
    }
    if (target instanceof Message) {
      alias.setMessageType(((Message) target).createReferenceTo(
        (Message) target, Message.class));
    }
    else if (target instanceof Part) {
      alias.setPart(((Part) target).getName());
    }
    else if (target instanceof CorrelationProperty) {
      alias.setPropertyName(((CorrelationProperty) target).createReferenceTo(
        (CorrelationProperty) target, CorrelationProperty.class));
    }
  }

  private void rename(OnEvent event, Named target) throws IOException {
    if (target instanceof Message) {
      event.setMessageType( 
        event.createWSDLReference((Message) target, Message.class));
    }
  }

  private void rename(PartnerLink partnerLink, Named target) throws IOException {
    if (target instanceof PartnerLinkType) {
      partnerLink.setPartnerLinkType(partnerLink.createWSDLReference(
        (PartnerLinkType) target, PartnerLinkType.class));
    }
    else if (target instanceof Role) {
      WSDLReference<Role> reference =
        partnerLink.createWSDLReference((Role) target, Role.class);

      if (isRenamedRole(partnerLink, PartnerLink.MY_ROLE)) {
        partnerLink.setMyRole(reference);
      }
      else if (isRenamedRole(partnerLink, PartnerLink.PARTNER_ROLE)) {
        partnerLink.setPartnerRole(reference);
      }
    }
  }

  private void rename(
    CorrelationSet correlationSet,
    Named target) throws IOException
  {
    if (target instanceof CorrelationProperty) {
//out();
//out("RENAME");
      List<WSDLReference<CorrelationProperty>> references =
        correlationSet.getProperties();

      List<WSDLReference<CorrelationProperty>> list =
        new ArrayList<WSDLReference<CorrelationProperty>>();

      if (references == null) {
        return;
      }
      for (WSDLReference<CorrelationProperty> reference : references) {
        if (reference.get() != null) {
          list.add(reference);
//out("  see: " + reference.get());
        }
        else {
//out("  add");
          list.add(correlationSet.createWSDLReference(
            (CorrelationProperty) target, CorrelationProperty.class));
        }
      }
      correlationSet.setProperties(list);
    }
  }

  private boolean isRenamedRole(PartnerLink partnerLink, String attribute) {
    String roleName = partnerLink.getAttribute(new StringAttribute(attribute));
    return myOldName.equals(roleName);
  }

  private void rename(Variable variable, Named target) {
//out();
//out("RENAME: " + Util.getName(target));
//out("    in: " + Util.getName(variable));
//out();
    if (target instanceof GlobalElement) {
      variable.setElement(
        variable.createSchemaReference((GlobalElement) target, GlobalElement.class));
    }
    else if (target instanceof GlobalType) {
      variable.setType(
        variable.createSchemaReference((GlobalType) target, GlobalType.class));
    }
    else if (target instanceof Message) {
      variable.setMessageType( 
        variable.createWSDLReference((Message) target, Message.class));
    }
  }

  private void rename(Catch _catch, Named target) throws IOException {
    if (target instanceof Fault) {
      renameFaultNameReference(_catch, target);
    }
    else if (target instanceof GlobalElement) {
      _catch.setFaultElement(
        _catch.createSchemaReference((GlobalElement) target, GlobalElement.class));
    }
    else if (target instanceof Message) {
      _catch.setFaultMessageType( 
        _catch.createWSDLReference((Message) target, Message.class));
    }
  }

  private void rename(Reply reply, Named target) throws IOException {
    if (target instanceof Fault) {
      renameFaultNameReference(reply, target);
    }
  }

  private void rename(Throw _throw, Named target) throws IOException {
    if (target instanceof Fault) {
      renameFaultNameReference(_throw, target);
    }
  }

  private void renameFaultNameReference(
    FaultNameReference reference,
    Named target) throws IOException
  {
    try {
      reference.setFaultName(getQName(reference.getFaultName(), target));
    }
    catch(VetoException e) {
      throw new IOException(e.getMessage());
    }
  }

  private QName getQName(QName qName, Named target) {
    if (qName == null) {
      return null;
    }
    return new QName(qName.getNamespaceURI(), target.getName(), qName.getPrefix());
  }

  // --------------------------------------------------------------
  private static final class StringAttribute implements Attribute {
    
    public StringAttribute(String name) {
      myName = name;
    }
    
    public Class getType() {
      return String.class;
    }
   
    public String getName() {
      return myName;
    }

    public Class getMemberType() {
      return null;
    }

    private String myName;
  }

  private XPath myXPath;
  private String myOldName;
  private RenameRefactoring request;
}
