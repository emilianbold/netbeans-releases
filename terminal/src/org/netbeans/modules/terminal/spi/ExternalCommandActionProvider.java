package org.netbeans.modules.terminal.spi;

import java.util.Collection;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.util.Lookup;

/**
 *
 * @author igromov
 */
public abstract class ExternalCommandActionProvider {

    protected abstract boolean canHandle(String command);
    public abstract void handle(String command, Lookup lookup);

    // helpers //
    
    private static final Collection<? extends ExternalCommandActionProvider> PROVIDERS
            = Lookup.getDefault().lookupAll(ExternalCommandActionProvider.class);

    public static ExternalCommandActionProvider getProvider(String command) {
        for (ExternalCommandActionProvider provider : PROVIDERS) {
            if (provider.canHandle(command)) {
                return provider;
            }
        }
        return new ExternalCommandActionProvider() {

            @Override
            public boolean canHandle(String command) {
                return true;
            }

            @Override
            public void handle(String command, Lookup lookup) {}
        };
    }
}

//enum Result {
//    FAILED,
//    PARTIAL_SUCCESS,
//    SUCCEED;
//}
