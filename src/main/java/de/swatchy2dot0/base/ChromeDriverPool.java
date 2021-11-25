package de.swatchy2dot0.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class ChromeDriverPool {

    private final static Logger logger = LoggerFactory.getLogger(ChromeDriverPool.class);

    private static ChromeDriverPool instance;

    private final Map<String, WebDriver> drivers = new HashMap<String, WebDriver>();

    private final ReentrantLock lock = new ReentrantLock();

    private final boolean headless = true;

    public static ChromeDriverPool getInstance() {
        if (instance == null) {
            instance = new ChromeDriverPool();
            logger.debug("ChromeDriverPool created");
        }
        return instance;
    }

    public static String execCmd(String cmd) throws IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream())
                .useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public WebDriver driver() throws IOException {
        return driver(true);
    }

    public WebDriver driver(boolean withImages) {
        return driver(withImages, "");
    }

    public WebDriver driver(boolean withImages, String downloadPath) {

        RuntimeContext runtimeContext = RuntimeContext.getCurrent();
        String driverContext = runtimeContext.peekDriverContext();

        lock.lock();
        try {
            WebDriver driver = drivers.get(driverContext);
            if (driver == null) {

                WebDriverManager.chromedriver().setup();

                System.setProperty("user.country", "DE");
                System.setProperty("user.language", "de");
                ChromeOptions options = new ChromeOptions();
                options.addArguments("window-size=1200x1000");
                options.addArguments("--lang=de-DE");
                System.setProperty("LANG", "de_DE");
                System.setProperty("LANGUAGE", "de_DE");
                Map<String, Object> prefs = new HashMap<String, Object>();
                if (StringUtils.isNotBlank(downloadPath)) {
                    prefs.put("profile.default_content_settings.popups", 0);
                    prefs.put("download.default_directory", downloadPath);
                }
                prefs.put("intl.accept_languages", "de-DE");
                options.setExperimentalOption("prefs", prefs);
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                options.addArguments("--whitelisted-ips");
                System.setProperty("webdriver.chrome.whitelistedIps", "");

                if (!withImages) {
                    options.addArguments("--blink-settings=imagesEnabled=false");
                }

                // if blocked, pinterest login will no longer work options.addArguments("--host-resolver-rules=MAP accounts.google.com 127.0.0.1");
                if (isHeadless()) {
                    options.addArguments("--headless"); // only if you are ACTUALLY running headless
                    options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--log-level=3");
                    // ChromeDriver is just AWFUL because every version or two it breaks unless you pass cryptic arguments
                    //AGRESSIVE: options.setPageLoadStrategy(PageLoadStrategy.NONE); // https://www.skptricks.com/2018/08/timed-out-receiving-message-from-renderer-selenium.html
                    options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
                    options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
                    options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
                    options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
                    options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
                    //System.setProperty("webdriver.chrome.logfile", "./chromedriver.log");
                    //System.setProperty("webdriver.chrome.verboseLogging", "true");
                    logger.debug("headless");
                }

                driver = new ChromeDriver(options);

                drivers.put(driverContext, driver);

                logger.debug("new ChromeDriver created for driver context '{}'", driverContext);

                sleep(3000L);

                loadCookies();
            }
            return driver;
        } finally {
            lock.unlock();
        }
    }

    private void installChromeDriver() throws IOException {

        String chromdriverHome = execCmd("which chromedriver");

//        version=$(curl -s https://chromedriver.storage.googleapis.com/LATEST_RELEASE)
//        wget -qP "/tmp/" "https://chromedriver.storage.googleapis.com/${version}/chromedriver_linux64.zip"
//        sudo unzip -o /tmp/chromedriver_linux64.zip -d /usr/local/bin
//        sudo chmod 755 /usr/local/bin/chromedriver

        throw new RuntimeException(chromdriverHome + " is not a valid chromdriver Home -> install chromedriver (https://sites.google.com/a/chromium.org/chromedriver/downloads)");
    }

    private void checkOperatingSystem() {
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.toLowerCase().startsWith("windows")) {
            throw new RuntimeException(operatingSystem + " is not a supported operating system");
        }
    }

    public void closeDriver() {

        RuntimeContext runtimeContext = RuntimeContext.getCurrent();
        String driverContext = runtimeContext.peekDriverContext();

        lock.lock();
        try {
            try {
                WebDriver driver = drivers.get(driverContext);
                if (driver != null) {
                    driver.quit();
                    drivers.remove(driverContext);
                    logger.info("ChromeDriver closed");
                }
            } catch (Exception ex) {
            }
        } finally {
            lock.unlock();
        }
    }

    protected boolean isHeadless() {
        Optional<String> envVar = Optional.ofNullable(System.getenv().get("headless"));
        if (envVar.isPresent()) {
            return Boolean.valueOf(envVar.get());
        }
        return headless;
    }

    public void storeCookies() {

        RuntimeContext runtimeContext = RuntimeContext.getCurrent();
        String driverContext = runtimeContext.peekDriverContext();

        // create file named Cookies to store Login Information
        File file = new File(driverContext + "_Cookies.data");

        FileOutputStream f = null;
        ObjectOutputStream o = null;

        try {
            f = new FileOutputStream(file);
            o = new ObjectOutputStream(f);

            for (Cookie ck : driver().manage().getCookies()) {
                o.writeObject(ck);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (o != null) {
                    o.close();
                }
                if (f != null) {
                    f.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteCookies() {

        RuntimeContext runtimeContext = RuntimeContext.getCurrent();
        String driverContext = runtimeContext.peekDriverContext();

        File file = new File(driverContext + "_Cookies.data");
        if (file.exists()) {
            file.delete();
        }
    }

    public void loadCookies() {

        boolean silent = true;
        boolean toBeDeleted = false;

        RuntimeContext runtimeContext = RuntimeContext.getCurrent();
        String driverContext = runtimeContext.peekDriverContext();

        FileInputStream fi = null;
        ObjectInputStream oi = null;
        try {
            File file = new File(driverContext + "_Cookies.data");

            if (file.exists()) {

                fi = new FileInputStream(file);

                oi = new ObjectInputStream(fi);

                // open dummy page otherwise cookies cannot be set
                driver().get("https://example.com/");

                sleep(2000L);

                // Read objects
                int i = 1;
                while (true) {
                    Cookie ck = (Cookie) oi.readObject();
                    String domain = ck.getDomain();
                    String name = ck.getName();
                    String path = ck.getPath();
                    String value = ck.getValue();
                    try {
                        driver().manage().addCookie(ck); // This will add the stored cookie to your current session
                        if (!silent) {
                            System.out.println(i + ": added Cookie           : domain=" + domain + ", name=" + name + ", path=" + path + ", value=" + value);
                        }
                    } catch (Exception exw) {
                        System.err.println(exw.getMessage() + " for Cookie: domain=" + domain + ", name=" + name + ", path=" + path + ", value=" + value);
                        exw.printStackTrace();
                        toBeDeleted = true;
                    }
                    i++;
                }
            } else {
                System.out.println("no Cookies to load");
            }
        } catch (EOFException ex) {
            // end of file
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (oi != null) {
                try {
                    oi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (toBeDeleted) {
                File file = new File(driverContext + "_Cookies.data");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }
    }
}
