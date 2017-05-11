package com.frederikam.fred.moe;

import com.frederikam.fred.moe.util.SLF4JInputStreamLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Caddy extends Thread {

    private static final Logger log = LoggerFactory.getLogger(Caddy.class);

    public Caddy() {
        setName("Caddy");
    }

    @Override
    public void run() {
        try {
            log.info("Starting Caddy");

            ProcessBuilder pb = new ProcessBuilder()
                    .command("caddy", "-conf Caddyfile", "-agree", "-email $CADDY_EMAIL");
            Process proc = pb.start();
            new SLF4JInputStreamLogger(log, proc.getInputStream()).start();

            try {
                int code = proc.waitFor();
                log.warn("Exited with code " + code);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
}
