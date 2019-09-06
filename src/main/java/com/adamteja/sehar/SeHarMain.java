package com.adamteja.sehar;

import net.lightbody.bmp.core.har.HarEntry;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.List;


public class SeHarMain{

    public static void main(String[] args){
        String projectRoot = "/Users/ateja/IdeaProjects/SeHar";
        PropertyConfigurator.configure("log4j.properties");
        final Logger logger = LoggerFactory.getLogger(SeHarMain.class);
        logger.debug("Se HAR");

//start BrowserMobProxy
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);

// Create Selenium Proxy
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        final String proxyStr = "localhost:" + proxy.getPort();
        seleniumProxy.setHttpProxy(proxyStr);

// Create a WebDriver
//        System.setProperty("webdriver.chrome.driver",projectRoot+"/chromedriver");
//        System.setProperty("webdriver.chrome.logfile",projectRoot+"/out/chrome.log");
//        System.setProperty("webdriver.chrome.verboseLogging", "true");
//        ChromeOptions options = new ChromeOptions();
//        options.setCapability(CapabilityType.PROXY, seleniumProxy);
//        WebDriver driver = new ChromeDriver(options);

        System.setProperty("webdriver.gecko.driver",projectRoot+"/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.setProxy(seleniumProxy);
        WebDriver driver = new FirefoxDriver(options);


        driver.manage().deleteAllCookies();

        // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
//        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.enableHarCaptureTypes(
                CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES,
                CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_COOKIES,
                CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_BINARY_CONTENT, CaptureType.REQUEST_BINARY_CONTENT);
        proxy.newHar("test");



        driver.get("http://www.google.com/");


        int wait = 20000;
        WebDriverWait WAIT = new WebDriverWait(driver,wait);
        try {
            System.out.println("waiting for document to finish");

            WAIT.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        }catch(Exception e){
            System.out.println("wait failed");
        }


        // And now use this to visit Google
        System.out.println("Page title is: " + driver.getTitle());





        //write har
        Har har = proxy.getHar();
        List<HarEntry> results = har.getLog().getEntries();
        for(int i=0; i<results.size(); i++){

            String url = results.get(i).getRequest().getUrl().toString();
            System.out.println("["+i+"] "+url);
        }
        String sFilename = projectRoot+"/out/test.har";
        File harFile = new File(sFilename);

        try {
            har.writeTo(harFile);
            System.out.println("write to file " + sFilename);

        } catch (IOException ex) {
            System.out.println (ex.toString());
            System.out.println("Could not find file " + sFilename);
        }

//
//        //Close the browser
        proxy.stop();
        driver.quit();
    }

}