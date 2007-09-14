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
package org.netbeans.core.spi.multiview;

/**
 * 
 * A marker interface for <code>MultiViewDescription</code> instances that allows to identify them
 * as containing source code. The associated <code>MultiViewElement</code>'s visual representation
 * is assumed to implement <code>CloneableEditorSupport.Pane</code> interface.
 * Fixes issue <a href="http://www.netbeans.org/issues/show_bug.cgi?id=68912">#68912</a>.
 * @author Milos Kleint
 * @since org.netbeans.core.multiview 1.10
 */
public interface SourceViewMarker {

}
