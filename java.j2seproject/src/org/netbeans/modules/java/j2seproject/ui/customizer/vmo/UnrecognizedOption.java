package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

/**
 * @author Rastislav Komara
 */
public class UnrecognizedOption extends SwitchNode {
    private final TokenStream input;
    private final Token start;
    private final Token stop;
    private final RecognitionException e;

    public UnrecognizedOption(TokenStream input, Token start, Token stop, RecognitionException e) {
        super(start);        
        this.input = input;
        this.start = start;
        this.stop = stop;
        this.e = e;
        if (start != null) {
            setName(start.getText());
        }
        setValue(new OptionValue.SwitchOnly(true));
    }

    public UnrecognizedOption(Token start) {
        this(null, start, null, null);
    }

    public UnrecognizedOption(String name) {
        this(null, null, null, null);
        setName(name);
        setValue(new OptionValue.SwitchOnly(true));
    }

    @Override
    public StringBuilder print(StringBuilder sb) {
        sb = ensureBuilder(sb);
        if (input != null) {
            for (int i = start.getTokenIndex(); i <= stop.getTokenIndex(); i++) {
                sb.append(input.get(i).getText());
            }
        } else {
            sb.append(HYPHEN).append(getName());
        }
        return sb;
    }

    @Override
    public String toString() {
        return "UnrecognizedOption{" +
                "input=" + input +
                ", start=" + start +
                ", stop=" + stop +
                ", e=" + e +
                " as " + super.toString() + 
                '}';
    }
}
