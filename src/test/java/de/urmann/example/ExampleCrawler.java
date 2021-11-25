package de.urmann.example;

import de.urmann.base.AbstractCrawler;
import de.urmann.base.ChromeDriverPool;

import java.io.IOException;

public class ExampleCrawler extends AbstractCrawler {

    public ExampleCrawler(ChromeDriverPool driverPool) {
        super(driverPool);
    }

    public void crawl() {
        try {
            goToUrl("https://radar.wo-cloud.com/desktop/embedded?wrx=45.0,9.0&wrm=11&wry=45.0,9.0", true);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}