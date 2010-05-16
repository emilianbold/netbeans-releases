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

package org.netbeans.modules.bpel.mapper.model;

import javax.swing.Icon;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralDataObject;
import org.netbeans.modules.bpel.mapper.palette.BpelPalette;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.xpath.mapper.model.builder.VertexFactory;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.time.Deadline;
import org.netbeans.modules.xml.time.Duration;
import org.netbeans.modules.xml.time.TimeUtil;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;

/**
 * Constructs Vertex objects for operatins, functions and constants.
 * This class is stateless, so it is used as a singleton.
 * 
 * @author nk160297
 */
public final class BpelVertexFactory extends VertexFactory  {
        
    private static BpelVertexFactory singleton = new BpelVertexFactory();
    
    public static BpelVertexFactory getInstance() {
        return singleton;
    }
    
    
    @Override
    protected Vertex createVertex(Object object) {
      Vertex newVertex = super.createVertex(object);
      if (newVertex != null) {
          return newVertex;
      }
      //
      if (object instanceof XmlLiteralDataObject) {
          return createXmlLiteral((XmlLiteralDataObject) object);
      }
      return null;
    }


    @Override
    public Constant createStringLiteral(String value) {
        try {
            if (value != null && value.length() > 0) {
            
            }
            Duration duration = TimeUtil.parseDuration(value, true, false);
            
            if (duration != null) {
                Constant newVertex = new Constant(XPathStringLiteral.class, DURATION_ICON);
                VertexItem contentItem = new VertexItem(newVertex, duration, value, Duration.class);
                newVertex.addItem(contentItem);
                return newVertex;
            }
        } catch (Exception ex) {
            // Ignore exception. Just continue.
        }
        try {
            Deadline deadline = TimeUtil.parseDeadline(value, false);

            if (deadline != null) {
                Constant newVertex = new Constant(XPathStringLiteral.class, DEADLINE_ICON);
                VertexItem contentItem = new VertexItem(newVertex, deadline, value, Deadline.class);
                newVertex.addItem(contentItem);
                return newVertex;
            }
        } catch (Exception ex) {
            // Ignore exception. Just continue.
        }
        //
        return super.createStringLiteral(value);
    }

    public Constant createXmlLiteral(XmlLiteralDataObject value) {
        Constant newVertex = new Constant(Literal.class, XML_LITERAL_ICON);
        VertexItem contentItem = new VertexItem(newVertex, value, value.getTextContent(), XmlLiteralDataObject.class);
        newVertex.addItem(contentItem);
        return newVertex;
    }
   
    //==========================================================================
    
   private static final Icon DURATION_ICON = 
           XPathMapperUtils.icon(BpelPalette.class, "duration"); // NOI18N

   private static final Icon DEADLINE_ICON = 
           XPathMapperUtils.icon(BpelPalette.class, "deadline"); // NOI18N
   
   private static final Icon XML_LITERAL_ICON = 
           XPathMapperUtils.icon(BpelPalette.class, "literal"); // NOI18N
}
    