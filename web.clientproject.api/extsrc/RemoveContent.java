
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RemoveContent {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("missing single argument - ZIP file to process");
        }
        if (!(new File(args[0])).exists()) {
            throw new RuntimeException("given file does not exist: "+args[0]);
        }
        ZipInputStream str = new ZipInputStream(new BufferedInputStream(new FileInputStream(args[0])));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(args[0]+".empty"));
        out.setLevel(9);
        ZipEntry entry;
        try {
            while ((entry = str.getNextEntry()) != null) {
                ZipEntry ze = new ZipEntry(entry.getName());
                out.putNextEntry(ze);
                if (!entry.isDirectory() &&
                        (entry.getName().endsWith("package.json") ||
                        entry.getName().endsWith("MIT-LICENSE"))) {
                    copy(str, out);
                }
            }
        } finally {
            str.close();
            out.close();
        }
    }

    public static void copy(InputStream is, OutputStream os)
    throws IOException {
        final byte[] BUFFER = new byte[65536];
        int len;

        for (;;) {
            len = is.read(BUFFER);

            if (len == -1) {
                return;
            }

            os.write(BUFFER, 0, len);
        }
    }

}
