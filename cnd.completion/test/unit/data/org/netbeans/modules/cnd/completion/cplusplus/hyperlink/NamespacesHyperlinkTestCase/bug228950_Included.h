#ifndef BUG228950_INCLUDED_H
#define	BUG228950_INCLUDED_H

namespace bug228950_included {
    struct list {
        int size() {
            return 0;
        }
    };
    class Field {
    public:
        typedef list typeOther;
    };
}

#endif	/* BUG228950_INCLUDED_H */
