package de.swatchy2dot0.base;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractCrawler {

    protected ChromeDriverPool driverPool;

    protected SecureRandom random = new SecureRandom();

    public AbstractCrawler(ChromeDriverPool driverPool) {
        super();
        this.driverPool = driverPool;
    }

    public static boolean isPageCrash(WebDriverException ex) {
        return ex.getMessage().contains("session deleted because of page crash");
    }

    /**
     * Returns a list with all links contained in the input
     */
    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public static String getBase64FromImageURL(URL imageUrl) {

        try {
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            System.err.printf("Failed while reading bytes %s", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    protected void goToUrl(String url) throws IOException {
        goToUrl(url, false);
    }

    protected void goToUrl(String url, boolean force) throws IOException {
        if (StringUtils.isBlank(url)) {
            throw new WebsiteWorkflowException("cannot open URL, because it is empty or null");
        }
        String currentUrl = "";
        WebDriver driver = driverPool.driver();
        for (int i = 0; i < 10; i++) {
            try {
                currentUrl = driver.getCurrentUrl();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("closing driver because current URL cannot be determined");
                driverPool.closeDriver();
                driver = driverPool.driver();
            }
            if ((i == 0 && force) || !url.equals(currentUrl)) {
                try {
                    driver.get(url);
                } catch (WebDriverException ex) {
                    if (isPageCrash(ex)) {
                        // retry once
                        System.out.println("closing driver because of page crash -> retry");
                        driverPool.closeDriver();
                        driver = driverPool.driver();
                        driver.get(url);
                    } else {
                        throw ex;
                    }
                }
            } else {
                break;
            }
        }
    }

    protected List<WebElement> findByNumberOfElementsToBe(final By locator, final Integer number) {
        try {
            // wait for the system to acknowledge the new photo, and use the WebDriverWait to verify
            // that the new photo is there
            WebDriverWait wait = new WebDriverWait(driverPool.driver(), 10);
            ExpectedCondition condition = ExpectedConditions.numberOfElementsToBe(locator, 2);
            wait.until(condition);

            return findWebElements(locator);
        } catch (Exception ex) {
            // element not found
        }
        return new ArrayList<>();
    }

    protected List<WebElement> findWebElements(By... bys) {
        List<WebElement> result = new ArrayList<>();
        for (By by : bys) {
            try {
                result.addAll(driverPool.driver().findElements(by));
            } catch (Exception ex) {
                // element not found
            }
        }
        return result;
    }

    protected List<WebElement> findWebElements(WebElement parentElement, By... bys) {
        List<WebElement> result = new ArrayList<>();
        for (By by : bys) {
            try {
                result.addAll(parentElement.findElements(by));
            } catch (Exception ex) {
                // element not found
            }
        }
        return result;
    }

    protected Optional<WebElement> findWebElement(WebElement parentElement, By by) {
        try {
            return Optional.ofNullable(parentElement.findElement(by));
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElement(By by) {
        return findWebElement(by, 10L);
    }

    protected Optional<WebElement> findEitherWebElement(int timeOutInSeconds, By... bys) {
        for (int i = 1; i <= timeOutInSeconds; i++) {
            for (By by : bys) {
                Optional<WebElement> element = findWebElement(by, 1);
                if (element.isPresent()) {
                    return element;
                }
            }
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElement(By by, long timeOutInSeconds) {
        try {
            return Optional.ofNullable(new WebDriverWait(driverPool.driver(), timeOutInSeconds)
                    .until(ExpectedConditions.presenceOfElementLocated(by)));
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElementByTextContains(String searchText, By by) {
        try {
            for (WebElement element : findWebElements(by)) {
                String text = element.getText();
                if (text.toLowerCase().contains(searchText.toLowerCase())) {
                    return Optional.of(element);
                }
            }
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElementByTextContains(WebElement parentElement, String searchText, By by) {
        try {
            for (WebElement element : findWebElements(parentElement, by)) {
                String text = element.getText();
                if (text.toLowerCase().contains(searchText.toLowerCase())) {
                    return Optional.of(element);
                }
            }
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElementByInnerHTMLContains(String searchText, By by) {
        try {
            for (WebElement element : findWebElements(by)) {
                String html = element.getAttribute("innerHTML");
                if (html.toLowerCase().contains(searchText.toLowerCase())) {
                    return Optional.of(element);
                }
            }
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected Optional<WebElement> findWebElementByInnerHTMLContains(WebElement parentElement, String searchText,
                                                                     By by) {
        try {
            for (WebElement element : findWebElements(parentElement, by)) {
                String html = element.getAttribute("innerHTML");
                if (html.toLowerCase().contains(searchText.toLowerCase())) {
                    return Optional.of(element);
                }
            }
        } catch (Exception ex) {
            // element not found
        }
        return Optional.empty();
    }

    protected void click(WebElement elementToClick, long timeOutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driverPool.driver(), timeOutInSeconds);
            WebElement elementClickable = wait.until(ExpectedConditions.elementToBeClickable(elementToClick));

            new Actions(driverPool.driver()).moveToElement(elementClickable).click().build().perform();
        } catch (Exception ex) {
            // element not found
        }
    }

    protected void takeScreenshot(String fileName) throws IOException {

        String theadName = Thread.currentThread().getName();

        File scrFile = ((TakesScreenshot) ChromeDriverPool.getInstance().driver()).getScreenshotAs(OutputType.FILE);
        // Now you can do whatever you need to do with it, for example copy
        // somewhere
        try {
            String pathname = fileName + ".png";
            FileUtils.copyFile(scrFile, new File(pathname));
            System.out.println(theadName + " screenshot           : " + pathname);
        } catch (IOException e) {
        }
    }

    protected void takeScreenshot(String fileName, WebElement element) throws IOException {

        String theadName = Thread.currentThread().getName();

        // Get entire page screenshot
        File scrFile = ((TakesScreenshot) ChromeDriverPool.getInstance().driver()).getScreenshotAs(OutputType.FILE);
        try {
            BufferedImage fullImg = ImageIO.read(scrFile);

            // Get the location of element on the page
            Point point = element.getLocation();

            // Get width and height of the element
            int eleWidth = element.getSize().getWidth();
            int eleHeight = element.getSize().getHeight();

            // Crop the entire page screenshot to get only element screenshot
            BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
                    eleWidth, eleHeight);

            // Now you can do whatever you need to do with it, for example copy
            // somewhere

            String pathname = fileName + ".png";
            ImageIO.write(eleScreenshot, "png", new File(pathname));
            System.out.println(theadName + " screenshot           : " + pathname);
        } catch (IOException e) {
        }
    }

    protected void randomSleep(long minMillis, long maxMillis) {
        long boundedLong =
                new SecureRandom()
                        .longs(minMillis, maxMillis + 1)
                        .findFirst()
                        .getAsLong();
        sleep(boundedLong);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }
    }

    protected void sendPostRequest(Map<String, String> formData, String destinationUrl) throws IOException {
        String htmlContent = "<!DOCTYPE html><html><head><title>Form</title></head><body><form id=\"injectedForm\" action=\"" + destinationUrl + "\" method=\"post\">";
        for (Map.Entry<String, String> keyValue : formData.entrySet()) {
            htmlContent += "<input id=\"" + keyValue.getKey() + "\" name=\"" + keyValue.getKey() + "\" value=\"" + keyValue.getValue() + "\">";
        }
        htmlContent += "<input id=\"mySubmit\" type=\"submit\" value=\"Submit\">";
        htmlContent += "</form></body></html>";

        String encodedHtml = Base64.getEncoder().encodeToString(htmlContent.getBytes());

        driverPool.driver().get("data:text/html;base64," + encodedHtml);

        Optional<WebElement> injectedSubmitOpt = findWebElement(By.id("mySubmit"));
        if (injectedSubmitOpt.isPresent()) {
            injectedSubmitOpt.get().click();
        }
    }

    protected void waitForPageLoad(WebElement elementThatShallDisappearWithPageLoad) {
        long waitStartMillis = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() - waitStartMillis < 20000L) {
                elementThatShallDisappearWithPageLoad.findElement(By.xpath("./.."));
            }
        } catch (Exception ignore) {
            // ignore
        }
    }

    protected void scrollToPageEndJS() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driverPool.driver();
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        } catch (Exception ex) {
        }
    }
}