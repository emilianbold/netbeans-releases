/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.masterindex.schemagenerator.datamodel;

import java.util.ArrayList;

/**
 * 
 * this represent the ObjectDefinition or metaData for a given DataObject
 * 
 * 
 * @author Sujit Biswas
 * 
 */
public class ObjectDefinition {

	private String name;

	/**
	 * list of fields
	 */
	ArrayList<Field> fields = new ArrayList<Field>();

	/**
	 * list of children
	 */
	ArrayList<ObjectDefinition> children = new ArrayList<ObjectDefinition>();

	/**
	 * @param name
	 */
	public ObjectDefinition(String name) {
		super();
		this.name = name;
	}

	/**
	 * @param name
	 * @param fields
	 * @param children
	 */
	public ObjectDefinition(String name, ArrayList<Field> fields,
			ArrayList<ObjectDefinition> children) {
		super();
		this.name = name;
		this.fields = fields;
		this.children = children;
	}

	/**
	 * add field
	 * 
	 * @param o
	 * @return
	 */
	public boolean addField(Field o) {
		return fields.add(o);
	}

	/**
	 * add field at a specified location, shift the elements from the current
	 * position to the right
	 * 
	 * @param index
	 * @param element
	 */
	public void addField(int index, Field element) {
		fields.add(index, element);
	}

	private void ensureFieldCapacity(int minCapacity) {
		int size = minCapacity + 1;

		if (fields.size() < size) {
			for (int i = fields.size(); i < size; i++) {
				fields.add(i, null);
			}
		}

	}

	/**
	 * get field for the given index
	 * 
	 * @param index
	 * @return
	 */
	public Field getField(int index) {
		return fields.get(index);
	}

	/**
	 * set the field at the given index
	 * 
	 * @param index
	 * @param element
	 * @return
	 */
	public Field setField(int index, Field element) {
		ensureFieldCapacity(index);
		return fields.set(index, element);
	}

	/**
	 * add child at the given index
	 * 
	 * @param index
	 * @param element
	 */
	public void addchild(int index, ObjectDefinition element) {
		children.add(index, element);
	}

	/**
	 * add child
	 * 
	 * @param o
	 * @return
	 */
	public boolean addchild(ObjectDefinition o) {
		return children.add(o);
	}

	/**
	 * get child at the given index
	 * 
	 * @param index
	 * @return
	 */
	public ObjectDefinition getchild(int index) {
		return children.get(index);
	}

	/**
	 * set child at the given index
	 * 
	 * @param index
	 * @param element
	 * @return
	 */
	public ObjectDefinition setchild(int index, ObjectDefinition element) {
		ensureChildCapacity(index);
		return children.set(index, element);
	}

	private void ensureChildCapacity(int minCapacity) {
		int size = minCapacity + 1;

		if (children.size() < size) {
			for (int i = children.size(); i < size; i++) {
				children.add(i, null);
			}
		}

	}

	/**
	 * @return the children
	 */
	public ArrayList<ObjectDefinition> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(ArrayList<ObjectDefinition> children) {
		this.children = children;
	}

	/**
	 * @return the fields
	 */
	public ArrayList<Field> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("name: " + name + "\n");

		sb.append(" Fields: \n");
		sb.append(fields.toString());

		if (!children.isEmpty())
			sb.append("\nChildren: ");

		for (ObjectDefinition c : children) {

			sb.append("\n child ");
			if (c != null)
				sb.append(c.toString());
		}

		return sb.toString();
	}

}
