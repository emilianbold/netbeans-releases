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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.type;

import javax.swing.Icon;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;

/**
 * A wrapper used for tracin
 * @author Vladimir Kvashin 
 */
/* package-local */ 
class TracingTypeDescriptor extends TypeDescriptor {
    
    private TypeDescriptor delegate;
    private String name;

    TracingTypeDescriptor(TypeDescriptor delegate) {
	this.delegate = delegate;
	name = delegate.getSimpleName();
    }
	    
    public String getContextName() {
	System.err.printf("TypeDescriptor.getContextName(%s)\n", name);
	return delegate.getContextName();
    }

    public FileObject getFileObject() {
	System.err.printf("TypeDescriptor.getFileObject(%s)\n", name);
	return delegate.getFileObject();
    }

    public Icon getIcon() {
	System.err.printf("TypeDescriptor.getIcon(%s)\n", name);
	return delegate.getIcon();
    }

    public int getOffset() {
	System.err.printf("TypeDescriptor.getOffset(%s)\n", name);
	return delegate.getOffset();
    }

    public String getOuterName() {
	System.err.printf("TypeDescriptor.(getOuterName)\n", name);
	return delegate.getOuterName();
    }

    public Icon getProjectIcon() {
	System.err.printf("TypeDescriptor.getProjectIcon(%s)\n", name);
	return delegate.getProjectIcon();
    }

    public String getProjectName() {
	System.err.printf("TypeDescriptor.getProjectName(%s)\n", name);
	return delegate.getProjectName();
    }

    public String getSimpleName() {
	System.err.printf("TypeDescriptor.getSimpleName(%s)\n", name);
	return delegate.getSimpleName();
    }

    public String getTypeName() {
	System.err.printf("TypeDescriptor.getTypeName(%s)\n", name);
	return delegate.getTypeName();
    }

    public void open() {
	System.err.printf("TypeDescriptor.open(%s)\n", name);
	delegate.open();
    }    
}
