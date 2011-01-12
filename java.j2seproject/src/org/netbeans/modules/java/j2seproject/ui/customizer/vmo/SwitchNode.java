package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Token;

/**
 * @author Rastislav Komara
 */
public class SwitchNode extends JavaVMOption<OptionValue.SwitchOnly> {

    public SwitchNode(Token t) {
        super(t);
        if (t != null) {
            setName(t.getText());
            setValue((OptionValue.SwitchOnly) OptionValue.createSwitch());
        }
    }
        
    public SwitchNode(int ttype, Token t) {
        this(t);
    }

    public SwitchNode(String name) {
        super(name);
        setValue(new OptionValue.SwitchOnly(false));
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        return getValue().isPresent() ? sb.append(SPACE).append(HYPHEN).append(getName()) : sb;
    }
    
}
