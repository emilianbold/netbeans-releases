package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;

/**
 * @author Rastislav Komara
 */
public class VMOptionTreeAdaptor extends CommonTreeAdaptor {

    @Override
    public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
        return nil();
//        return new UnrecognizedOption(input, start, stop, e);
    }
}
