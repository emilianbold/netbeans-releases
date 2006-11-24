package org.netbeans.installer.utils.helper;

import java.util.Map;
import java.util.Set;

public interface MutualMap<F, S> extends Map<F, S> {

  F reversedGet(S object);

  F reversedRemove(Object value);

  Set<Entry<S, F>> reversedEntrySet();

  //todo: not all reversed method added!
}
