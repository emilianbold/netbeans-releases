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

package org.netbeans.modules.soa.jca.base.spi;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * Provider interface for Global Rar modules.  Each Global Rar eway modules have
 * provide implementation of class, and register it using META-INF/services
 * method.
 *
 * @author echou
 */
public abstract class GlobalRarProvider {


    /**
     * ======================= START GENERAL ===============================
     */

    /**
     * Unique name that identifies a Global Rar provider.
     * Note: this name has to be the same as Global Rar runtime module's name.
     * For example, if File Eway Global Rar module is deployed to Glassfish
     * AppServer under module name "fileeway", then this name will also be
     * "fileeway".
     */
    public abstract String getName();


    /**
     * Human-readable display name for this provider.
     */
    public abstract String getDisplayName();

    /**
     * a short name that is also a valid Java identifier
     */
    public String getShortName() {
        return getName();
    }


    /**
     * Deprecated, use getLibraryNames() instead.
     * Unique name that is used to register a Library inside NetBeans IDE.
     * @deprecated
     */
    public String getLibraryName() {
        return null;
    }

    /**
     * Returns a list of library names that this provider depends on.
     * Subclass needs to override this method default implementation.
     */
    public List<String> getLibraryNames() {
        return Collections.unmodifiableList(Collections.singletonList(getLibraryName()));
    }

    /**
     * This method returns a List of supported static OTD types for this
     * Global Rar.  If list is empty, means it uses dynamic OTDs.
     */
    public abstract List<String> getOTDTypes();

    /**
     * This method returns a List of supported dynamic OTD types for this
     * Global Rar, for example Oracle Global Rar supports
     * otd.OracleDatabaseObjectTypeDefinition
     */
    public List<String> getSupportedDynamicOTDTypes() {
        return Collections.unmodifiableList(Collections.EMPTY_LIST);
    }

    /**
     * This method returns the specific HelpCtx for this Global Rar.
     * Override default implementation to return your HelpCtx.
     *
     * @return
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * ======================= END GENERAL ===============================
     */



    /**
     * ======================= START INBOUND ==============================
     */

    /**
     * By default, this method returns true.  If your provider does not
     * support inbound, override this method to return false, and don't
     * need to implement rest of methods in the inbound section
     *
     * @return if this provider supports inbound direction
     */
    public boolean supportsInbound() {
        return true;
    }

    /**
     * Optional static OTD types for inbound MDB, if return list is null or empty,
     * inbound wizard will skip the step to ask user for OTD types
     *
     * @return
     */
    public List<String> getInboundStaticOTDTypes() {
        return Collections.unmodifiableList(Collections.EMPTY_LIST);
    }

    /**
     * Optional OTD types for inbound MDB, if return list is null or empty,
     * inbound wizard will skip the step to ask user for OTD types
     *
     * @return
     */
    public List<String> getInboundOTDTypes() {
        return Collections.unmodifiableList(Collections.EMPTY_LIST);
    }


    /**
     * By default, this method returns false.  If your provider does support
     * inbound tx, override this method to return true.
     *
     * @return if this provider's inbound supports tx
     */
    public boolean supportsInboundTx() {
        return false;
    }

    /**
     * Return an Icon representing this Global Rar provider for inbound
     * portion of wizard.
     */
    public ImageIcon getIcon() {
        return null;
    }

    /**
     * If this provider want to have custom GUI for the inbound activation
     * configuration, then return an implementation of InboundConfigCustomPanel
     * interface, return null if provider choose to use the default NetBeans
     * propertysheet as Inbound Configuration instead.
     *
     * @param project reference to current Project handle from wizard
     * @param contextName optional String value for custom panel's use, by default
     * it is in this format "<project name>:<MDB name>"
     *
     * @return InboundConfigCustomPanel instance, or null if doesn't support
     */
    public InboundConfigCustomPanel getInboundConfigCustomPanel(Project project, String contextName) {
        return null;
    }

    /**
     * This method returns a InputStream of Inbound default Activation Configuration
     * for this type of Global Rar.  Caller of this method is responsible for
     * closing the InputStream.
     */
    public InputStream getInboundConfig() {
        return null;
    }

    /**
     * This method returns a FileObject instance of Inbound Message-Driven-Bean
     * code template for this Global Rar.
     */
    public FileObject getInboundMDBTemplate() {
        return null;
    }

    /**
     * Returns a list of supported MDB listener interfaces by this Global Rar.
     */
    public List<String> getListenerInterfaces() {
        return null;
    }


    /**
     * ======================= end inbound ==============================
     */




    /**
     * ======================= start outbound ==============================
     */

    /**
     * This method returns a InputStream of Outbound Java code template.
     * Caller of this method is responsible for closing the InputStream.
     */
    public abstract InputStream getTemplate();


    /**
     * Returns a Properties instance for any additional configuration this
     * Global Rar might require.
     */
    public abstract Properties getAdditionalConfig();


    /**
     * ======================= end outbound ==============================
     */

}
