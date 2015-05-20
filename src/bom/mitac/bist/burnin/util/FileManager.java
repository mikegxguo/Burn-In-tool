package bom.mitac.bist.burnin.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 13-12-5
 * Time: 2013-12-5 11:24:54
 * Version: V1.0
 */
public class FileManager {

    /**
     * Create Random file using character in abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
     * The buffer is 1KB.
     * If the file is already existed, new file will cover the old one.
     *
     * @param file
     * @param size The unit is KB
     * @return
     * @throws Exception
     */
    public static boolean create(File file, int size) {
        if (Validator.isFile(file))
            file.delete();

        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int unit = 1 * 1024;
        char[] buff = new char[unit];

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            Random random = new Random();

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < unit; j++) {
                    int number = random.nextInt(62);// [0,62)
                    buff[j] = str.charAt(number);
                }
                fileWriter.write(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                    fileWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (file.length() != size * unit) {
            file.delete();
            return false;
        } else
            return true;
    }

    /**
     * Copy File f1 to File f2 by FileChannel way.
     * The buffer is 2MB.
     * If the file f2 is already existed, new file will cover the old one.
     *
     * @param f1
     * @param f2
     * @return The transfer time by microsecond.
     * @throws Exception
     */
//    public static long copy(File f1, File f2) {
//        if (Validator.isFile(f2))
//            f2.delete();
//
//        long time = System.nanoTime();
//        int length;
//        FileInputStream in = null;
//        FileOutputStream out = null;
//        try {
//            in = new FileInputStream(f1);
//            out = new FileOutputStream(f2);
//            FileChannel inC = in.getChannel();
//            FileChannel outC = out.getChannel();
//            while (true) {
//                if (inC.position() == inC.size()) {
//                    return (System.nanoTime() - time) / 1000000;
//                }
//                if ((inC.size() - inC.position()) < 2097152)
//                    length = (int) (inC.size() - inC.position());
//                else
//                    length = 2097152;
//                inC.transferTo(inC.position(), length, outC);
//                inC.position(inC.position() + length);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                    in = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (out != null) {
//                try {
//                    out.close();
//                    out = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }/**/
//        }
//        return -1;
//    }

    // Method from Kitty
    public static long copy(File src, File dst) {
        try {
            long time = System.nanoTime();
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
            return (System.nanoTime() - time) / 1000000;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * Compare the two files whether they are the same
     *
     * @param f1
     * @param f2
     * @return
     * @throws Exception
     */
    public static boolean compare(File f1, File f2) {
        if (!Validator.isFile(f1) || !Validator.isFile(f2)) {
            return false;
        }
        if (f1.length() == f2.length()) {
            FileInputStream fis1 = null;
            FileInputStream fis2 = null;
            try {
                fis1 = new FileInputStream(f1);
                fis2 = new FileInputStream(f2);
                String a = new String(Hex.encodeHex(DigestUtils.md5(fis1)));
                String b = new String(Hex.encodeHex(DigestUtils.md5(fis2)));
                if (a.equals(b)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis1 != null) {
                    try {
                        fis1.close();
                        fis1 = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fis2 != null) {
                    try {
                        fis2.close();
                        fis2 = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
}
