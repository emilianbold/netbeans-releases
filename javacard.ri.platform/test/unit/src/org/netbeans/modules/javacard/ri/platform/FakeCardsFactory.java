package org.netbeans.modules.javacard.ri.platform;

import org.netbeans.modules.javacard.ri.spi.CardsFactory;
import org.netbeans.modules.javacard.spi.Cards;
import org.openide.util.Lookup.Provider;

public final class FakeCardsFactory extends CardsFactory {

    @Override
    protected Cards createCards(Provider source) {
        return new PlatformDataObjectTest.FakeCards(source);
    }
}
