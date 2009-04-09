#ifndef _NEWFILE_H
#define	_NEWFILE_H

#ifdef	CPP
extern "C" {
#endif

struct AA {
#if defined(CPP)
        int cpp_class; /* C++ class of screen (monochrome, etc.) */
#else
        int class; /* class of screen (monochrome, etc.) */
#endif
};


#ifdef	CPP
}
#endif

#endif	/* _NEWFILE_H */