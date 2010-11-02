package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

/**
 * @author Rastislav Komara
 */
public abstract class AbstractJavaVMOptionParser extends Parser {
    AbstractJavaVMOptionParser(TokenStream input) {
        super(input);
    }

    AbstractJavaVMOptionParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

}
