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
package org.netbeans.modules.bpel.model.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypes;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public abstract class ExtensibleElementsImpl extends BpelContainerImpl implements ExtensibleElements {

    public ExtensibleElementsImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    public ExtensibleElementsImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }

    public String getDocumentation() {
      Documentation [] documentations = getDocumentations();

      if (documentations == null || documentations.length == 0) {
        return null;
      }
      Documentation documentation = documentations[0];
      
      if (documentation == null) {
        return null;
      }
      return documentation.getContent();
    }

    public void setDocumentation(String value) throws VetoException {
      String content = getContent(value);
//System.out.println();
//System.out.println("SET DOCUMENTATION");
//System.out.println("  value: '" + value + "'");
//System.out.println("content: '" + content + "'");
//System.out.println();
      
      if (content == null) {
        if (sizeOfDocumentations() != 0) {
//System.out.println(" remove");
          removeDocumentation(0);
        }
      }
      else {
        Documentation documentation;

        if (sizeOfDocumentations() == 0) {
          documentation = getBpelModel().getBuilder().createDocumentation();
          documentation.setContent(content);
//System.out.println(" insert");
          insertDocumentation(documentation, 0);
        }
        else {
          documentation = getDocumentation(0);
          documentation.setContent(content);
        }
      }
    }

    private String getContent(String value) {
      if (value == null) {
        return null;
      }
      if (isEmpty(value)) {
        return null;
      }
      return value;
    }

    private boolean isEmpty(String value) {
      for (int i=0; i < value.length(); i++) {
        if (value.charAt(i) == ' ') {
          continue;
        }
        if (value.charAt(i) == '\r') {
          continue;
        }
        if (value.charAt(i) == '\n') {
          continue;
        }
        return false;
      }
      return true;
    }

    public void removeDocumentation() throws VetoException {
        setDocumentation(null);
    }

    public void addDocumentation(Documentation documentation) {
        addChildAfter(documentation, Documentation.class, BpelTypesEnum.DOCUMENTATION);
    }

    public Documentation getDocumentation(int i) {
        return getChild(Documentation.class, i);
    }

    public Documentation[] getDocumentations() {
        readLock();

        try {
            List<Documentation> list = getChildren( Documentation.class );
            return list.toArray( new Documentation[list.size()] );
        }
        finally {
            readUnlock();
        }
    }

    public void insertDocumentation(Documentation documentation, int i) {
        insertAtIndexAfter(documentation, Documentation.class, i, BpelTypesEnum.DOCUMENTATION);
    }

    public void removeDocumentation(int i) {
        removeChild(Documentation.class, i);
    }

    public void setDocumentation(Documentation documentation, int i) {
        setChildAtIndex(documentation, Documentation.class, i);
    }

    public void setDocumentations(Documentation[] documentations) {
        setArrayAfter(documentations, Documentation.class, BpelTypesEnum.DOCUMENTATION);
    }

    public int sizeOfDocumentations() {
        readLock();

        try {
            return getChildren( Documentation.class ).size();
        }
        finally {
            readUnlock();
        }
    }
    
    public <T extends ExtensionEntity> void addExtensionEntity(Class<T> clazz, T entity) {
        assert entity.canExtend(this);
        addChildAfter(entity, clazz, BpelTypesEnum.DOCUMENTATION);
    }

    public List<ExtensionEntity> getExtensionChildren() {
        return getChildren( ExtensionEntity.class );
    }
    
    @Override
    protected BpelEntity create( Element element )
    {
        if ( BpelElements.DOCUMENTATION.getName().equals( element.getLocalName()) ){
            return new DocumentationImpl( getModel() , element );
        }
        return null;
    }
    
    protected Attribute[] getDomainAttributes() {
        // TODO : common framework for accesing to extension attributes ? 
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[0];
            myAttributes.compareAndSet(null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();
}
