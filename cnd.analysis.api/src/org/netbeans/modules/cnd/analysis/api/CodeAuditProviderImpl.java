/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.analysis.api;

import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class CodeAuditProviderImpl {

    private static final Default DEFAULT = new Default();

    public static Collection<CodeAuditProvider> getDefault() {
        return DEFAULT.getAuditProviders();
    }

    private static final class Default {

        private final Lookup.Result<CodeAuditProvider> res;

        Default() {
            res = Lookup.getDefault().lookupResult(CodeAuditProvider.class);
        }

        public Collection<CodeAuditProvider> getAuditProviders() {
            List<CodeAuditProvider> audits = new ArrayList<CodeAuditProvider>();
            for (CodeAuditProvider selector : res.allInstances()) {
                audits.add(selector);
            }
            return audits;
        }
    }
}
