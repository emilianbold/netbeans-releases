namespace bug243083 { 
    template <typename EntryType> 
    struct SpecEntryTraits243083 {
      typedef EntryType DeclType;
    };

    template <typename EntryType,
              typename _SETraits = SpecEntryTraits243083<EntryType>,
              typename _DeclType = typename _SETraits::DeclType>
    struct SpecIterator243083 {
        typedef _DeclType DeclType;

        DeclType foo() {
            return DeclType();
        }

        DeclType *operator*() const {
            return 0;
        }
        DeclType *operator->() const { 
            return 0; 
        }

    }; 

    struct AAA243083 {
        int boo() {
            return 1;
        }
    };

    typedef SpecIterator243083<AAA243083> spec_iterator243083;

    int main243083() {
        spec_iterator243083 I;
        I->boo();  // boo is unresolved with warning
        (*I)->boo(); // boo is unresolved with warning
        I.foo().boo(); // boo is unresolved with warning
        return 0; 
    }  
}