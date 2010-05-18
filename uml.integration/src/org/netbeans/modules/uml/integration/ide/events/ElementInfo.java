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

/*
 * File         : ElementInfo.java
 * Version      : 1.4
 * Description  : Base class for the source info classes.
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 * ElementInfo is the base class of all Source Element information class.
 * ElementInfo provides the functionality to store the common data needed
 * by all source code elements.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-06  Darshan     Added constructor to take an INamedElement
 *                              for model-source work.
 */
public abstract class ElementInfo implements Cloneable {
    /** Specifes that the element is a to be created. */
    public final static int CREATE = 0;

    /** Specifies that the element is modifing an existing element. */
    public final static int MODIFY = 1;

    /** Specifies that the element is deleting an existing element. */
    public final static int DELETE = 2;

    private String mName = null;
    private String mNewName = null;
    protected Integer mModifiers = null;
    private int mChangeType = CREATE;
    private String mComment = null;
    
    private boolean isCommentSet = false;

    /**
     * Contructs a new ElementInfo and specifies the type of change that is to
     * occur.
     * @param type The change that that occured (CREATE, MODIFY, DELETE).
     */
    public ElementInfo(int type) {
        setChangeType(type);
    }

    public IProject getProject() {
        return null;
    }

    public String getFilename() {
        return null;
    }

    private INamedElement element;

    public ElementInfo(INamedElement el) {
        setChangeType(MODIFY);
        if (el != null)
            setComment(el.getDocumentation());
	element = el;
    }

    /**
     * Updates Describe of the model change.  Each decendent will have to implement
     * <b>update</b> to send the correct messages to Describe.  The currently active
     * Describe system will be used.  <b>Use the EventManager to send messages to
     * Describe.</b>
     * @see GDProSupport#getCurrentSystem()
     */
    abstract public void update();

    /**
     * Returns the Describe IProject that owns the element described by this
     * ElementInfo. The IProject is only returned if this ElementInfo has an
     * associated Describe IElement.
     * 
     * @return The owning IProject, or null if this information is unavailable.
     */
    public IProject getOwningProject() {
        return null;
    }

    /**
     * Sets the name of the model element.  This is the original name of the element.
     * When used with setNewName before and after information is provided.
     * @param value The name of the element.
     * @see #setNewName(String value)
     */
    public void setName(String value) {
        mName = value;
    }

    /**
     * Retrieves the name of the model element.  This is the original name of the element.
     * When used with getNewName before and after information is provided.
     * @return The name of the element.
     * @see #getNewName()
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the new name of the model element.  This is the name of the element after
     * a name changed has occured. When used with setName before and after
     * information is provided.
     * @param value The name of the element.
     * @see #setName ( String value )
     */
    public void setNewName(String value) {
        mNewName = value;
    }

    /**
     * Retrieves the new name of the model element.  This is the name of the element after
     * a name changed has occured. When used with getName before and after
     * information is provided.
     * @return The name of the element.
     * @see #getName()
     */
    public String getNewName() {
        return mNewName;
    }

    /**
     * Sets the elements modifiers.
     * @param mod - disjunction of constants from
     * <A HREF="http://java.sun.com/products/jdk/1.2/docs/api/java/lang/reflect/Modifier.html"><CODE>Modifier</CODE></A>.
     */
    public void setModifiers(Integer mod) {
        mModifiers = mod;
    }

    /**
     * Gets the elements modifiers.
     * @return disjunction of constants from
     *         <A HREF="http://java.sun.com/products/jdk/1.2/docs/api/java/lang/reflect/Modifier.html"><CODE>Modifier</CODE></A>.
     */
    public Integer getModifiers() {
        return mModifiers;
    }

    /**
     * Sets the type of change that has occured.
     * @param value CREATE, MODIFY, or DELETE
     * @sse #CREATE
     * @see #MODIFY
     * @see #DELETE
     */
    public void setChangeType(int value) {
        mChangeType = value;
    }

    /**
     * Retrieves the type of change that has occured.
     * @see #CREATE
     * @see #MODIFY
     * @see #DELETE
     *
     * @return <code>CREATE, MODIFY,</code> or <code>DELETE</code>
     */
    public int getChangeType() {
        return mChangeType;
    }

    /**
     * Retrieve a textual representation of the type of change that occured.
     * <B>Use for Debuging.</B>
     * @return The name of the change.
     */
    public String getChangeName() {
        String retVal = "";

        switch (getChangeType()) {
            case CREATE :
                retVal = "Create";
                break;
            case MODIFY :
                retVal = "Modify";
                break;
            case DELETE :
                retVal = "Delete";
                break;
        }

        return retVal;
    }

    /**
     * Sets the Comment that appears before the element.
     * @param value The comment.
     */
    public void setComment(String value) {
        
        // I am having to trim the comment to remove white space from the 
        // beginning and the end of the string.  I am having to do this because
        // When calling getJavadocText() on a Java model element is returing 
        // a couple of additional newlines.  I think the java models javadoc 
        // logic just removes the '*' characters, but keeps the '\n' characters.
        // which is fine if the there are actual comments on the line, but if
        // it is white space the it is not what you want.
        if (value == null)
            return;
        
        mComment = value.trim();
        isCommentSet = true;
    }

	/**
	 * Returns <code>true</code> if a valid comment is set on this object.
	 * @return <code>true</code> if a valid comment is set.
	 */
	public boolean isCommentSet() {
		return /*mComment != null*/isCommentSet;		
	}

    /**
     *  produces marker id 
     */
    public String getMarkerID() {
	String markerID = null;
	ITaggedValue tag = element.getTaggedValueByName("MarkerID");
	if (tag != null)
	{
	    markerID = tag.getDataValue();
	}
	if (markerID == null || markerID.trim().equals("")) {
	    markerID = element.getXMIID();
	}
	return markerID;
    }


    /**
     * Retrieves the Comment that appears before the element.
     * @return The comment.
     */
    public String getComment() {
	
		if (mComment != null && mComment.trim().length() == 0)
			mComment = null;		
        return mComment;
    }


    /**
     *  Determines if the given name is a valid Java identifier.
     *
     * @param name  The name to be tested for identifier-hood :-)
     * @return <code>true</code> if the name is an identifier.
     */
    public static boolean isIdentifier(String name) {
        if (name == null || (name = name.trim()).length() == 0)
            return false;

        if (!Character.isJavaIdentifierStart(name.charAt(0)))
            return false;
        for (int i = 1; i < name.length(); ++i) {
            if (!Character.isJavaIdentifierPart(name.charAt(i)))
                return false;
        }
        return true;
    }

    protected void assertIsIdentifier(String name)
        throws InvalidIdentifierException {
        if (!isIdentifier(name)) {
            throw new InvalidIdentifierException(
                "'" + name + "' is not a valid Java identifier");
        }
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException ignored) {
        }
        return null;
    }

    /**
     *  Returns a code unique to each subclass of ElementInfo.
     *
     * @return A short <code>String</code> unique among the subclasses of
     *         <code>ElementInfo</code>.
     */
    abstract public String getCode();

    /**
     *  Transfers the contents of relevant 'newXyz' fields to the corresponding
     * 'xyz' fields, and nulls out the newXyz fields. Only those fields that are
     * needed for the element to be located in the Describe model need to be
     * synced; although subclasses may choose to sync additional fields, this is
     * not mandatory.
     */
    public void syncFields() {
        if (getNewName() != null) {
            setName(getNewName());
            setNewName(null);
        }
    }

    /**
     *  Determines if the element supplied matches selected non-null fields in
     * this element. The fields examined should concern only this element and
     * qualifying parent elements, not any children. A minimal implementation will
     * check just element names and call match() for the parent elements. This
     * method will be used to filter out events from model-source and source-model
     * and should be as specific as necessary.
     *
     * @param el An <code>ElementInfo</code> of the same class as this.
     * @return <code>true</code> If el is of the same class as this and all non-
     *         null fields of this element are equal to the corresponding fields
     *         of <code>el</code>.
     */
    public boolean matches(ElementInfo el) {
        return (this == el);
    }
}
