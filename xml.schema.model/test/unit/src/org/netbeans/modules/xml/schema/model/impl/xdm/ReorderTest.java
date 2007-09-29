/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalComplexTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SequenceImpl;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ayub Khan
 */
public class ReorderTest extends TestCase {
    
    public ReorderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }

    /**
     * Test of cut/paste operation
     */
    public void testReorderLocalElementUsingSchemaModel() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/ReorderTest.xsd");
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
		
		/* before
  <xsd:element name="OrgChart">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="CompanyLogo1"/>
        <xsd:element name="Office1" type="OfficeType" maxOccurs="unbounded"/>
        <xsd:element name="CompanyLogo2"/>
        <xsd:element name="Office2" type="OfficeType" maxOccurs="unbounded"/>	
        <xsd:element name="Office3" type="OfficeType" maxOccurs="unbounded"/>		
      </xsd:sequence>
    </xsd:complexType>
  </xsd:element>
		 **/
		java.util.List<LocalElement> letis = new ArrayList<LocalElement>();
		for(SchemaComponent sc:seq.getContent())
			letis.add((LocalElement)sc);
		
        assertEquals("testReorderLocalElement.schema.node", 5, seq.getChildren().size());
		assertEquals("testReorderLocalElement.schema.node", 11, seq.getPeer().getChildNodes().getLength());
		checkChildren(seq.getChildren(), "CompanyLogo1, Office1, CompanyLogo2, Office2, Office3");
		
		model.startTransaction();
		java.util.List<LocalElement> clonedLetis = new ArrayList<LocalElement>();
		for(LocalElement leti:letis) {
			clonedLetis.add((LocalElementImpl) leti.copy(seq));
			seq.removeContent(leti);
		}
		model.endTransaction();

        assertEquals("testReorderLocalElement.schema.node", 0, seq.getChildren().size());
		assertEquals("testReorderLocalElement.schema.node", 0, seq.getPeer().getChildNodes().getLength());		
		checkChildren(seq.getChildren(), "");
		
		model.startTransaction();
		int index = 0;
		seq.addContent(clonedLetis.get(0), index++);		
		seq.addContent(clonedLetis.get(2), index++);
		seq.addContent(clonedLetis.get(3), index++);
		seq.addContent(clonedLetis.get(1), index++);
		seq.addContent(clonedLetis.get(4), index++);				
		model.endTransaction();	
		
		/* after
  <xsd:element name="OrgChart">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="CompanyLogo1"/>
        <xsd:element name="CompanyLogo2"/>
        <xsd:element name="Office2" type="OfficeType" maxOccurs="unbounded"/>	
		<xsd:element name="Office1" type="OfficeType" maxOccurs="unbounded"/>
        <xsd:element name="Office3" type="OfficeType" maxOccurs="unbounded"/>		
      </xsd:sequence>
    </xsd:complexType>
  </xsd:element>
		 **/				
		
        assertEquals("testReorderLocalElement.schema.node", 5, seq.getChildren().size());
        assertEquals("testReorderLocalElement.schema.node", 11, seq.getPeer().getChildNodes().getLength());
        checkChildren(seq.getChildren(), "CompanyLogo1, CompanyLogo2, Office2, Office1, Office3");
    }
	
    /**
     * Test of cut/paste operation
     */
    public void testReorderLocalElementUsingXAM() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/ReorderTest.xsd");
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
		
		/* before
  <xsd:element name="OrgChart">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="CompanyLogo1"/>
        <xsd:element name="Office1" type="OfficeType" maxOccurs="unbounded"/>
        <xsd:element name="CompanyLogo2"/>
        <xsd:element name="Office2" type="OfficeType" maxOccurs="unbounded"/>	
        <xsd:element name="Office3" type="OfficeType" maxOccurs="unbounded"/>		
      </xsd:sequence>
  </xsd:element>                         
		 **/
		Component parent = seq;		
		int[] newPositions = {0, 3, 1, 2, 4}; //CompanyLogo1, CompanyLogo2, Office2, Office1, Office3
                java.util.List<Component> children = parent.getChildren();
                checkChildren(children, "CompanyLogo1, Office1, CompanyLogo2, Office2, Office3");
		// parent is the Component whose children are being reordered
                model.startTransaction();
                List<Component> copies = new ArrayList<Component>(children);
                for (int currentPos = 0; currentPos < newPositions.length; currentPos++) {
                    copies.set(newPositions[currentPos], children.get(currentPos).copy(parent));
                }
        checkChildren(copies, "CompanyLogo1, CompanyLogo2, Office2, Office1, Office3");
			// Cannot remove children until after they are copied.
			for (Component child : children.toArray(new Component[children.size()])) {
				model.removeChildComponent(child);
			}
			assertEquals("number of childs",0, parent.getChildren().size());
			// Now add the copies back to the parent.
			for (Component copy : copies) {
				model.addChildComponent(parent, copy, -1);
			}
                        model.endTransaction();
		/* after
  <xsd:element name="OrgChart">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="CompanyLogo1"/>
        <xsd:element name="CompanyLogo2"/>
        <xsd:element name="Office2" type="OfficeType" maxOccurs="unbounded"/>	
		<xsd:element name="Office1" type="OfficeType" maxOccurs="unbounded"/>
        <xsd:element name="Office3" type="OfficeType" maxOccurs="unbounded"/>		
      </xsd:sequence>
    </xsd:complexType>
  </xsd:element>
		 **/		
        checkChildren(parent.getChildren(), "CompanyLogo1, CompanyLogo2, Office2, Office1, Office3");
        
        um.undo();
        checkChildren(parent.getChildren(), "CompanyLogo1, Office1, CompanyLogo2, Office2, Office3");
    }
	
	private <T extends Component> void checkChildren(final List<T> children, final String order) {
		int index = 0;
                String result = "";
		for (int i=0; i<children.size(); i++) {
                    T sc = children.get(i);
                    result += ((Named)sc).getName();
                    if (i != children.size()-1) {
                        result += ", ";
                    }
		}
                if (order != null) {
                    assertEquals("check sequence", order, result);
                } else {
                    System.out.println(result);
                }
	}	

        private void checkChildrenNodes(final Component c) {
		NodeList childs = ((SchemaComponent)c).getPeer().getChildNodes();
		System.out.println("\nxdm nodes: ");
		for(int i=0;i<childs.getLength();i++) {
			Node child = childs.item(i);
			if(child instanceof Element)
				System.out.print(((Element)child).getAttribute("name")+", ");
			else if(child instanceof Text)
				System.out.print("["+((Text)child).getTokens().get(0).getValue().replaceAll("\n", "~")+"]");
		}
		System.out.println();
        }
}
