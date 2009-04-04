#ifndef _NEWFILE_H
#define	_NEWFILE_H

#ifdef	__cplusplus
extern "C" {
#endif

struct AA {
#if defined(__cplusplus)
        int cpp_class; /* C++ class of screen (monochrome, etc.) */
#else
        int class; /* class of screen (monochrome, etc.) */
#endif
};


#ifdef	__cplusplus
}
#endif

#endif	/* _NEWFILE_H */