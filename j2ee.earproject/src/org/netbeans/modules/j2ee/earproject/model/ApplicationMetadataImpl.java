/*
 * ApplicationMetadataImpl.java
 *
 * Created on 9. květen 2007, 16:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.earproject.model;

import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;

/**
 * Default implementation of {@link ApplicationMetadata}.
 * @author Tomas Mysik
 */
public class ApplicationMetadataImpl implements ApplicationMetadata {
    
    private final Application application;
    
    /**
     * Constructor with all properties.
     * @param application model of enterprise application.
     */
    public ApplicationMetadataImpl(Application application) {
        this.application = application;
    }

    public Application getRoot() {
        return application;
    }
}
