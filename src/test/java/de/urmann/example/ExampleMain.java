package de.urmann.example;

import de.urmann.base.ChromeDriverPool;
import de.urmann.base.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMain {

    private final static Logger logger = LoggerFactory.getLogger(ExampleMain.class);

    public static void main(String[] args) throws Exception {

        Thread.currentThread().setName("EX");

        logger.info("ExampleMain start");

        RuntimeContext.getCurrent().pushDriverContext("EX");

        ChromeDriverPool driverPool = ChromeDriverPool.getInstance();

        try {
            new ExampleCrawler(driverPool).crawl();
        } finally {
            driverPool.closeDriver();
        }

        System.exit(0);
    }
}