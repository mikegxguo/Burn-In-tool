package bom.mitac.bist.burnin.util;

import android.util.Log;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-1-11
 * Time: 涓嬪�?:44
 */
public class FTPManager {
    public static enum Status {
        CREATE_DIRECTORY_FAIL,
        FILE_EXITS,
        FILE_MORE_THAN_ONE,
        FILE_LARGER_THAN_LOCAL,
        REMOTE_BIGGER_LOCAL,
        UPLOAD_FROM_BREAK_SUCCESS,
        UPLOAD_NEW_FILE_SUCCESS,
        UPLOAD_NEW_FILE_FAILED,
        DOWNLOAD_FROM_BREAK_SUCCESS,
        DOWNLOAD_NEW_FILE_SUCCESS,
        DOWNLOAD_NEW_FILE_FAILED,
        DELETE_REMOTE_FAILD,
        EXCEPTION,
        NOTHING_DONE
    }

    private FTPClient ftpClient;
    private Status status;
    private float speed;
    private long percent;
    private Timer percentTimer;
    private static FTPManager instance;
    private InputStream is;
    private OutputStream out;

    private FTPManager(int defaultTimeout, int connectTimeout, int dataTimeout) {
        ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(defaultTimeout);
        ftpClient.setConnectTimeout(connectTimeout);
        ftpClient.setDataTimeout(dataTimeout);
        status = Status.NOTHING_DONE;
        speed = 0;
        percent = 0;
    }

    public static FTPManager getInstance(int defaultTimeout, int connectTimeout, int dataTimeout) {
        if (instance == null)
            instance = new FTPManager(defaultTimeout, connectTimeout, dataTimeout);
        return instance;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Get the upload or download speed, the unit is kbps.
     */
    public float getSpeed() {
        return speed * 8 / 1024;
    }

    public long getPercent() {
        return percent;
    }

    public boolean connect(String hostname, int port, String username, String password) throws IOException {
        ftpClient.connect(hostname, port);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(username, password)) {
                return true;
            } else {
                disconnect();
                return false;
            }
        } else {
            disconnect();
            return false;
        }
    }

    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public boolean upload(String local, String remote) {
        long startTime;
        long stopTime;
        File f = new File(local);
        long localSize = f.length();
        //璁剧疆PassiveMode浼犺�?
        ftpClient.enterLocalPassiveMode();
        //璁剧疆浠ヤ簩杩涘埗娴佺殑鏂瑰紡浼犺緭
        try {
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //瀵硅繙绋嬬洰褰曠殑澶勭悊
        String remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
        String remoteDirectory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (remoteDirectory.startsWith("/")) {
            try {
                if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
                    //濡傛灉杩滅▼鐩綍涓嶅瓨鍦紝鍒欏垱寤鸿繙绋嬫湇鍔�?櫒鐩綍
                    if (ftpClient.makeDirectory(remoteDirectory)) {
                        ftpClient.changeWorkingDirectory(remoteDirectory);
                    } else {
                        status = Status.CREATE_DIRECTORY_FAIL;
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("feong", "鍒涘缓鐩綍澶辫�?");
            status = Status.CREATE_DIRECTORY_FAIL;
            return false;
        }

        // 涓婁紶鏂版枃浠�

        boolean isUploaded = false;
        try {
            is = new FileInputStream(local);
            startTime = System.nanoTime();
            isUploaded = ftpClient.storeFile(remoteFileName, is);
            if (isUploaded) {
                stopTime = System.nanoTime();
                speed = (float) (localSize * 1000000000 / (stopTime - startTime));
                status = Status.UPLOAD_NEW_FILE_SUCCESS;
                return true;
            } else {
                status = Status.UPLOAD_NEW_FILE_FAILED;
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isUploaded;

    }

    public boolean uploadContinues(String local, String remote) {
        long startTime;
        long stopTime;
        File f = new File(local);
        long localSize = f.length();
        //璁剧疆PassiveMode浼犺�?
        ftpClient.enterLocalPassiveMode();
        //璁剧疆浠ヤ簩杩涘埗娴佺殑鏂瑰紡浼犺緭
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //瀵硅繙绋嬬洰褰曠殑澶勭悊
        String remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
        String remoteDirectory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (remoteDirectory.startsWith("/")) {
            try {
                if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
                    //濡傛灉杩滅▼鐩綍涓嶅瓨鍦紝鍒欏垱寤鸿繙绋嬫湇鍔�?櫒鐩綍
                    if (ftpClient.makeDirectory(remoteDirectory)) {
                        ftpClient.changeWorkingDirectory(remoteDirectory);
                    } else {
                        status = Status.CREATE_DIRECTORY_FAIL;
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("feong", "鍒涘缓鐩綍澶辫�?");
            status = Status.CREATE_DIRECTORY_FAIL;
            return false;
        }

        //妫�煡杩滅▼鏄惁�?樺湪鏂囦�?
        FTPFile[] files = new FTPFile[0];
        try {
            files = ftpClient.listFiles(remoteFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files.length == 1) {
            long remoteSize = files[0].getSize();
            if (remoteSize == localSize) {
                status = Status.FILE_EXITS;
                return false;
            } else if (remoteSize > localSize) {
                status = Status.REMOTE_BIGGER_LOCAL;
                return false;
            } else {
                //灏濊瘯绉诲姩鏂囦欢鍐呰鍙栨寚閽��?炵幇鏂偣缁紶
                boolean isUploaded = false;
                try {
                    is = new FileInputStream(f);
                    if (is.skip(remoteSize) == remoteSize) {
                        ftpClient.setRestartOffset(remoteSize);
                        startTime = System.nanoTime();
                        isUploaded = ftpClient.storeFile(remote, is);
                        if (isUploaded) {
                            stopTime = System.nanoTime();
                            speed = (float) ((localSize - remoteSize) * 1000000000 / (stopTime - startTime));
                            status = Status.UPLOAD_FROM_BREAK_SUCCESS;
                            return true;
                        } else {
                            status = Status.UPLOAD_FROM_BREAK_SUCCESS;
                            return true;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return isUploaded;
            }
        } else {
            // 涓婁紶鏂版枃浠�

            boolean isUploaded = false;
            try {
                is = new FileInputStream(local);
                startTime = System.nanoTime();
                isUploaded = ftpClient.storeFile(remoteFileName, is);
                if (isUploaded) {
                    stopTime = System.nanoTime();
                    speed = (float) (localSize * 1000000000 / (stopTime - startTime));
                    status = Status.UPLOAD_NEW_FILE_SUCCESS;
                    return true;
                } else {
                    status = Status.UPLOAD_NEW_FILE_FAILED;
                    return false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return isUploaded;
        }
    }

    public boolean download(String remote, String local) {

        long startTime;
        long stopTime;
        File f = new File(local);
        long localSize = f.length();

        ftpClient.enterLocalPassiveMode();
        try {
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FTPFile[] files = new FTPFile[0];
        try {
            files = ftpClient.listFiles(remote);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (FTPFile fp : files) {
//            Log.d("feong", "F: " + fp.toString());
//        }
        if (files.length != 1) {
            status = Status.FILE_MORE_THAN_ONE;
            return false;
        }

        //瀵规湰鍦扮洰褰曠殑澶勭悊
        String localDirectory = local.substring(0, local.lastIndexOf("/"));
        if (localDirectory.startsWith("/")) {
            File folder = new File(localDirectory);
            if (folder.exists()||folder.mkdirs()) {
            } else {
                status = Status.CREATE_DIRECTORY_FAIL;
                return false;
            }

        } else {
            status = Status.CREATE_DIRECTORY_FAIL;
            return false;
        }

        long remoteSize = files[0].getSize();
        boolean isDownloaded = false;
        try {
            out = new FileOutputStream(f);
//            countPercent(f, remoteSize);
            startTime = System.nanoTime();
            isDownloaded = ftpClient.retrieveFile(remote, out);
            if (isDownloaded) {
                stopTime = System.nanoTime();
                speed = (float) (remoteSize * 1000000000 / (stopTime - startTime));
                status = Status.DOWNLOAD_NEW_FILE_SUCCESS;
            } else {
                status = Status.DOWNLOAD_NEW_FILE_FAILED;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isDownloaded;
    }

    // TODO

    /**
     * 浠嶧TP鏈嶅姟鍣ㄤ笂涓嬭浇鏂囦欢
     *
     * @param remote    杩滅▼鏂囦欢璺�?
     * @param local     鏈湴鏂囦欢璺�?
     * @param continues 鏂偣缁紶
     * @return 鏄惁鎴愬姛
     */
    public boolean download(String remote, String local, boolean continues) throws IOException {
        long startTime;
        long stopTime;
        File f = new File(local);
        long localSize = f.length();

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        FTPFile[] files = ftpClient.listFiles(remote);
        if (files.length != 1) {
            status = Status.FILE_MORE_THAN_ONE;
            return false;
        }
        long remoteSize = files[0].getSize();
        if (f.exists() && continues) {
            if (localSize >= remoteSize) {
                Log.d("feong", "鏈湴鏂囦欢澶у皬澶т簬杩滅▼鏂囦欢澶у皬锛屼笅杞戒腑姝�");
                status = Status.FILE_LARGER_THAN_LOCAL;
                return false;
            } else {
                OutputStream out = new FileOutputStream(f, true);
                ftpClient.setRestartOffset(localSize);
//                countPercent(f, remoteSize);
                startTime = System.nanoTime();
                if (ftpClient.retrieveFile(remote, out)) {
                    stopTime = System.nanoTime();
                    speed = (float) (localSize * 1000000000 / (stopTime - startTime));
                    status = Status.DOWNLOAD_FROM_BREAK_SUCCESS;
                    out.close();
                    return true;
                } else {
                    // 鏂偣缁紶澶辫触锛岄噸鏂颁笅杞�?//                    countPercent(f, remoteSize);
                    startTime = System.nanoTime();
                    if (ftpClient.retrieveFile(remote, out)) {
                        stopTime = System.nanoTime();
                        speed = (float) (localSize * 1000000000 / (stopTime - startTime));
                        status = Status.DOWNLOAD_NEW_FILE_SUCCESS;
                        out.close();
                        return true;
                    } else {
                        status = Status.DOWNLOAD_NEW_FILE_FAILED;
                        out.close();
                        return false;
                    }
                }
            }
        } else {
            OutputStream out = new FileOutputStream(f);
//            countPercent(f, remoteSize);
            startTime = System.nanoTime();
            if (ftpClient.retrieveFile(remote, out)) {
                stopTime = System.nanoTime();
                speed = (float) (remoteSize * 1000000000 / (stopTime - startTime));
                status = Status.DOWNLOAD_NEW_FILE_SUCCESS;
                out.close();
                return true;
            } else {
                status = Status.DOWNLOAD_NEW_FILE_FAILED;
                out.close();
                return false;
            }
        }
    }

    public void abort() {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public boolean abort() {
//        try {
//            return ftpClient.abort();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private void countPercent(final File file, final long total) {
        if (total == 0) {
            return;
        }
        if (percentTimer != null) {
            percentTimer.cancel();
            percentTimer = null;
        }
        percent = 0;
        percentTimer = new Timer();
        percentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long current = file.length();
                percent = current * 100 / total;
                if (percent == 100) {
                    percentTimer.cancel();
                    percentTimer = null;
                }
            }
        }, 0, 1000);
    }

}
