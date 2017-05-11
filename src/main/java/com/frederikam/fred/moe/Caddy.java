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

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("caddy -conf Caddyfile -agree -email " + System.getenv("CADDY_EMAIL"));
            new SLF4JInputStreamLogger(log, proc.getInputStream());

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
