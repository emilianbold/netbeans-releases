package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Token;

/**
 * @author Rastislav Komara
 */
public class UnknownOption extends SwitchNode {

    public UnknownOption(Token t) {
        super(t);
        if (t != null) {
            setName(t.getText());
            setValue(new OptionValue.SwitchOnly(true));
        }
    }

    public UnknownOption(String name) {
        super(name);
        setValue(new OptionValue.SwitchOnly(true));
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        final StringBuilder sb = ensureBuilder(builder);
        sb.append(getName());
        return sb;
    }

    public String toString() {
        return "UnknownOption{" +
                "name='" + getName() + '\'' +
                ", value=" + getValue() +
                ", valid=" + isValid() +
                '}';
    }

}
