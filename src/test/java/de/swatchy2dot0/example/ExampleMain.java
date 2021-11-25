package de.swatchy2dot0.example;

import de.swatchy2dot0.base.ChromeDriverPool;
import de.swatchy2dot0.base.RuntimeContext;
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