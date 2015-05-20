package bom.mitac.bist.burnin.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CommandManager {

    public static synchronized String run_command(String[] cmd, String workdirectory){
        StringBuffer result = new StringBuffer();
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);

            InputStream in = null;
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
                builder.redirectErrorStream(true);
                Process process = builder.start();

                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

}
