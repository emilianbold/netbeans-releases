struct {
    int buffer_frames;
    int nbuffers;
    int isAtexit;
} conf = {
    .buffer_frames = 512,
    .nbuffers = 4,
    .isAtexit = 0
};