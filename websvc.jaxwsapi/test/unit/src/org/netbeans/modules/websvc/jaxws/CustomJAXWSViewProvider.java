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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxws;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewFactory;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomJAXWSViewProvider implements JAXWSViewProvider {
    
    /** Creates a new instance of CustomJAXWSViewProvider */
    public CustomJAXWSViewProvider() {
    }
    
    public JAXWSView findJAXWSView() {
        return JAXWSViewFactory.createJAXWSView(new CustomJAXWSViewImpl());
    }
    
    private static final class CustomJAXWSViewImpl implements JAXWSViewImpl {
    
    /** Creates a new instance of CustomJAXWSViewImpl */
    public CustomJAXWSViewImpl() {
    }
    
    public Node createJAXWSView(Project project) {
        return null;
    }
    
}
}
