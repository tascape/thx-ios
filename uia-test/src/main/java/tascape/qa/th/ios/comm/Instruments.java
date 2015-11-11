/*
 * Copyright 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tascape.qa.th.ios.comm;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.comm.EntityCommunication;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class Instruments extends EntityCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(Instruments.class);

    public static final String SYSPROP_INSTRMENTS_EXECUTABLE = "qa.th.comm.INSTRMENTS_EXECUTABLE";

    static {
        LOG.debug("Please specify where adb executable is by setting system property {}={}",
            SYSPROP_INSTRMENTS_EXECUTABLE, "/path/to/your/xcode/instruments");
    }

    private final static String ADB = SystemConfiguration.getInstance().getProperty(SYSPROP_INSTRMENTS_EXECUTABLE,
        "instruments");

    private String serial = "";


    public Instruments()  {
        this("");
    }

    public Instruments(String serial)  {
    }

    @Override
    public void connect() throws Exception {
    }

    @Override
    public void disconnect() throws Exception {
    }


    private static class AdbStreamHandler implements ExecuteStreamHandler {
        private final List<String> output;

        AdbStreamHandler(List<String> output) {
            this.output = output;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.trace("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.debug(line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.debug(line);
                output.add(line);
            } while (true);
        }

        @Override
        public void start() throws IOException {
            LOG.trace("start");
        }

        @Override
        public void stop() {
            LOG.trace("stop");
        }

    }

    private class AdbStreamToFileHandler implements ExecuteStreamHandler {
        File output;

        AdbStreamToFileHandler(File output) {
            this.output = output;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.trace("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.error(line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            if (this.output == null) {
                String line = "";
                while (line != null) {
                    LOG.trace(line);
                    line = bis.readLine();
                }

            } else {
                PrintWriter pw = new PrintWriter(this.output);
                LOG.debug("Log stdout to {}", this.output);
                String line = "";
                try {
                    while (line != null) {
                        pw.println(line);
                        pw.flush();
                        line = bis.readLine();
                    }
                } finally {
                    pw.flush();
                }
            }
        }

        @Override
        public void start() throws IOException {
            LOG.trace("start");
        }

        @Override
        public void stop() {
            LOG.trace("stop");
        }
    }

    public static void main(String[] args) throws Exception {
    }
}
