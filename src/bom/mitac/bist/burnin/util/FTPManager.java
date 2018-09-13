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
 * Time: 
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
        //PassiveMode
        ftpClient.enterLocalPassiveMode();
        //
        try {
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
        String remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
        String remoteDirectory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (remoteDirectory.startsWith("/")) {
            try {
                if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
                    //
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
            Log.d("feong", "CREATE_DIRECTORY_FAIL");
            status = Status.CREATE_DIRECTORY_FAIL;
            return false;
        }

        // 

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
        //PassiveMode
        ftpClient.enterLocalPassiveMode();
        //
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
        String remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
        String remoteDirectory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (remoteDirectory.startsWith("/")) {
            try {
                if (!ftpClient.changeWorkingDirectory(remoteDirectory)) {
                    //
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
            Log.d("feong", "CREATE_DIRECTORY_FAIL");
            status = Status.CREATE_DIRECTORY_FAIL;
            return false;
        }

        //
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
                //
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
            //

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

        //
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
     * 
     *
     * @param remote
     * @param local
     * @param continues
     * @return
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
                Log.d("feong", "FILE_LARGER_THAN_LOCAL\r\n");
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
                    //countPercent(f, remoteSize);
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
