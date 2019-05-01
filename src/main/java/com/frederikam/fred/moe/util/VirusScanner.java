package com.frederikam.fred.moe.util;

import com.frederikam.fred.moe.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class VirusScanner extends Thread {

    private static final Logger log = LoggerFactory.getLogger(VirusScanner.class);

    private static final int CODE_CLEAN = 0;
    private static final int CODE_BAD = 1;

    private static final int CRON_INTERVAL = 24 * 60 * 60 * 1000;
    private final LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<>();

    public VirusScanner() {
        setName("VirusScanner");
        setDaemon(true);
    }

    @Override
    public void run() {
        new CronVirusScanner().start();

        while (true) {
            try {
                handleFile(queue.take());
            } catch (InterruptedException e) {
                log.error("Got interrupted", e);
                break;
            } catch (IOException e) {
                log.error("Caught IOException", e);
            }
        }
    }

    public void scanAsync(File file) {
        queue.add(file);
    }

    private static void handleFile(File file) throws IOException, InterruptedException {
        if(!isFileBad(file))
            return;

        //noinspection ResultOfMethodCallIgnored
        file.delete(); // TODO: 5/9/2017 Create a custom page telling how the file was removed

        PrintWriter pw = new PrintWriter(file);
        pw.write("This file has been automatically removed after it has been found to be malicious on " + Date.from(Instant.now()));
        pw.close();
    }

    private static boolean isFileBad(File file) throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec("clamscan " + file.getAbsolutePath());
        new SLF4JInputStreamLogger(log, proc.getInputStream()).start();

        int exitCode = proc.waitFor();

        switch (exitCode) {
            case CODE_CLEAN:
                return false;
            case CODE_BAD:
                return true;
            default:
                throw new IOException("Unexpected exit code");
        }
    }

    public static boolean isAvInstalled() {
        try {
            Runtime.getRuntime().exec("clamscan");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private class CronVirusScanner extends Thread {

        CronVirusScanner() {
            setName("CronVirusScanner");
            setDaemon(true);
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    tick();
                    sleep(CRON_INTERVAL);
                } catch (InterruptedException e) {
                    log.error("Got interrupted", e);
                    break;
                }
            }
        }

        private void tick() throws InterruptedException {
            try {
                log.info("Now doing cron job");

                Runtime rt = Runtime.getRuntime();

                Process proc1 = rt.exec("freshclam");
                new SLF4JInputStreamLogger(log, proc1.getInputStream()).start();
                log.info("freshclam exited with " + proc1.waitFor());

                //noinspection ConstantConditions
                for (File file : ResourceManager.getDataDir().listFiles()) {
                    VirusScanner.handleFile(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
