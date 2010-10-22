package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Token;

import java.util.Map;

/**
 * @author Rastislav Komara
 */
public class UserPropertyNode extends JavaVMOption<OptionValue<Map.Entry<String, String>>> {
    private int start = -1;

    public UserPropertyNode(Token name, Token value, int start) {
        super("D");
        this.start = start;        
        setValue(new OptionValue.StringPair(name.getText(), (value != null ? value.getText() : null)));
    }

    public UserPropertyNode() {
        super("D");
        setValue(new OptionValue.StringPair());        
    }

    public UserPropertyNode(int cprop, Token name, Token value, int start) {
        this(name, value, start);
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        OptionValue<Map.Entry<String, String>> val = getValue();
        if (val.isPresent()) {
            sb.append(SPACE).append(HYPHEN).append(getName());
            Map.Entry<String, String> entry = val.getValue();
            sb.append(entry.getKey());
            if (entry.getValue() != null) {
                sb.append("=").append(entry.getValue());
            }
        }
        return sb;
    }

    /*@Override
    public int compareTo(JavaVMOption<?> o) {
        if (o instanceof UserPropertyNode) {
            return super.compareTo(o);
        }
        return 1;
    }*/
}
