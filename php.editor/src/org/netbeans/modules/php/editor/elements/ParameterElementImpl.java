/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.PhpElementImpl.SEPARATOR;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public final class ParameterElementImpl implements ParameterElement {

    private final String name;
    private final String defaultValue;
    private final Set<TypeResolver> types;
    private final int offset;
    private boolean isRawType;
    private boolean isMandatory;

    public ParameterElementImpl(
            final String name,
            final String defaultValue,
            final int offset,
            final Set<TypeResolver> types,
            final boolean isRawType) {
        this.name = name;
        this.isMandatory = defaultValue == null;
        this.defaultValue = defaultValue != null ? decode(defaultValue) : null;//NOI18N
        this.offset = offset;
        this.types = types;
        this.isRawType = isRawType;

    }

    static List<ParameterElement> parseParameters(String signature) {
        List<ParameterElement> retval = new ArrayList<ParameterElement>();
        if (signature != null && signature.length() > 0) {
            final String regexp = String.format("\\%s", SEPARATOR.COMMA.toString());//NOI18N
            for (String sign : signature.split(regexp)) {
                final ParameterElement param = parseParameter(sign);
                if (param != null) {
                    retval.add(param);
                }
            }
        }
        return retval;
    }

    private static ParameterElement parseParameter(String sig) throws NumberFormatException {
        ParameterElement retval = null;
        final String regexp = String.format("\\%s", SEPARATOR.COLON.toString());//NOI18N
        String[] parts = sig.split(regexp);
        if (parts.length > 0) {
            String paramName = parts[0];
            Set<TypeResolver> types = TypeResolverImpl.parseTypes(parts[1]);
            boolean isRawType = Integer.parseInt(parts[2]) > 0;
            String defValue = parts.length > 3 ? parts[3] : null;
            retval = new ParameterElementImpl(
                    paramName, defValue, -1, types, isRawType);
        }
        return retval;
    }

    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(SEPARATOR.COLON);
        StringBuilder typeBuilder = new StringBuilder();
        for (TypeResolver typeResolver : getTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            typeBuilder.append(resolverImpl.getSignature());
        }
        sb.append(typeBuilder);
        sb.append(SEPARATOR.COLON);//NOI18N
        sb.append(isRawType ? 1 : 0);
        sb.append(SEPARATOR.COLON);//NOI18N
        if (!isMandatory()) {
            final String defVal = getDefaultValue();
            sb.append(encode(defVal));
        }
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Set<TypeResolver> getTypes() {
        return types;
    }

    @Override
    public final String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final boolean hasDeclaredType() {
        return isRawType;
    }

    @Override
    public final boolean isMandatory() {
        return isMandatory;
    }

    private static String encode(String inStr) {
        StringBuffer outStr = new StringBuffer(6 * inStr.length());

        for (int i = 0; i < inStr.length(); i++) {
            final char charAt = inStr.charAt(i);
            boolean encode = isEncodedChar(i, inStr);
            if (!encode) {
                SEPARATOR[] values = SEPARATOR.values();
                for (SEPARATOR separator : values) {
                    char separatorChar = separator.toString().charAt(0);
                    if (charAt == separatorChar) {
                        encode = true;
                        break;
                    }
                }
            }
            if (encode) {
                outStr.append(encodeChar(inStr.charAt(i)));
                continue;
            }

            outStr.append(inStr.charAt(i));
        }

        return outStr.toString();
    }

    private static String encodeChar(char ch) {
        String encChar = Integer.toString((int) ch, 16);

        return "\\u" + "0000".substring(0, "0000".length() - encChar.length()).concat(encChar); // NOI18N
    }

    private static String decode(final String inStr) {
        StringBuffer outStr = new StringBuffer(inStr.length());

        try {
            for (int i = 0; i < inStr.length(); i++) {
                if (isEncodedChar(i, inStr)) {
                    String decChar = inStr.substring(i + 2, i + 6);
                    outStr.append((char) Integer.parseInt(decChar, 16));
                    i += 5;
                } else {
                    outStr.append(inStr.charAt(i));
                }
            }
        } catch (NumberFormatException e) {
            Exceptions.printStackTrace(e);

            return inStr;
        }

        return outStr.toString();
    }

    private static boolean isEncodedChar(final int currentPosition, final String inStr) {
        boolean isEncodedChar = (currentPosition + 5) < inStr.length();

        if (isEncodedChar) {
            isEncodedChar &= ((inStr.charAt(currentPosition) == '\\')
                    && (inStr.charAt(currentPosition + 1) == 'u'));

            for (int i = currentPosition + 2; isEncodedChar && (i < (currentPosition + 6)); i++) {
                char c = inStr.charAt(i);
                isEncodedChar &= (Character.digit(c, 16) != -1);
            }
        }

        return isEncodedChar;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ParameterElement parsedParameter = parseParameter(retval);
            assert getName().equals(parsedParameter.getName());
            assert hasDeclaredType() == parsedParameter.hasDeclaredType();
//            assert (getDefaultValue() == null ?
//                parsedParameter.getDefaultValue() == null :
//                getDefaultValue().equals(parsedParameter.getDefaultValue()));
//            assert isMandatory() == parsedParameter.isMandatory();
        }
    }

    @Override
    public final OffsetRange getOffsetRange() {
        int endOffset = getOffset() + getName().length();
        return new OffsetRange(offset, endOffset);
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        Set<TypeResolver> typesResolvers = getTypes();
        if (hasDeclaredType()) {
            if (typesResolvers.size() > 1) {
                sb.append("mixed").append(' '); //NOI18N
            } else {
                for (TypeResolver typeResolver : typesResolvers) {
                    if (typeResolver.isResolved()) {
                        sb.append(typeResolver.getTypeName(false)).append(' '); //NOI18N
                        break;
                    }
                }
            }
        }
        sb.append(getName());
        String defVal = getDefaultValue();
        if (!isMandatory()) {
            sb.append(" = ");//NOI18N
            if (defVal != null) {
                sb.append(defVal.length() > 10 ?
                            "..." : defVal); //NOI18N
            }
        }
        return sb.toString();
    }
}
