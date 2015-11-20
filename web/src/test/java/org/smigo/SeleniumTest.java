package org.smigo;

/*
 * #%L
 * Smigo
 * %%
 * Copyright (C) 2015 Christian Nilsson
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smigo.config.DevelopmentConfiguration;
import org.smigo.user.RegisterFormBean;
import org.smigo.user.UserBean;
import org.smigo.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Test(groups = "selenium")
@ContextConfiguration(classes = {TestConfiguration.class})
public class SeleniumTest extends AbstractTestNGSpringContextTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String FACEBOOK_LOGIN = "ubgcvsa_dinglesky_1423300619@tfbnw.net";
    private static final String NON_LATIN_LETTERS = "Şehirde güzel köpek леп пас у граду 在城里漂亮的狗 سگ خوب در شهر";
    private static final String EMAIL_PROVIDER = "@mailinator.com";
    private static final String DISPLAY_NAME = "Tomte Nisse";
    private static final String PASSWORD = "qwerty";
    private static final String HASHPW = BCrypt.hashpw(PASSWORD, BCrypt.gensalt(4));
    private static final String NEW_PASSWORD = "password1";
    private static final String SPECIES_NAME = "Frango Salada";
    private static final String SCIENTIFIC_NAME = "Frangus Saladus";
    private static final String ITEM_NAME = "Sand";
    private static final String HOST_URL = "http://lh.smigo.org:8080";
    private static final int NUMBER_OF_SPECIES = 83;

    private static final By SQUARE = By.className("square-content");

    @Autowired
    private UserDao userDao;
    @Autowired
    private TestDao testDao;

    private WebDriver d;
    private WebDriverWait w;


    @BeforeClass
    public void init() {
        d = new FirefoxDriver();
        d.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        w = new WebDriverWait(d, 10);
    }

    @BeforeMethod
    public void goHome() {
        d.manage().deleteCookieNamed("JSESSIONID");
        d.manage().deleteCookieNamed("remember-me");
        d.get(HOST_URL + "/garden");
    }

    @AfterClass
    public void destruct() {
        d.quit();
    }

    private String addUser() {
        return addUser(false).getUsername();
    }

    private UserBean addUser(boolean addEmail) {
        final RegisterFormBean registerFormBean = new RegisterFormBean();
        final String username = "selenium" + System.currentTimeMillis();
        registerFormBean.setUsername(username);
        registerFormBean.setTermsOfService(true);
        registerFormBean.setPassword(HASHPW);
        int id = userDao.addUser(registerFormBean, HASHPW, 0);
        UserBean user = new UserBean(username, "", username + EMAIL_PROVIDER, "", Locale.ENGLISH);
        user.setTermsOfService(true);
        if (addEmail) {
            userDao.updateUser(id, user);
        }
        return user;
    }

    private void login(String username, String password) throws InterruptedException {
        Thread.sleep(1000);
        d.findElement(By.id("login-link")).click();
        d.findElement(By.name("username")).clear();
        d.findElement(By.name("username")).sendKeys(username);
        d.findElement(By.name("password")).clear();
        d.findElement(By.name("password")).sendKeys(password);
        d.findElement(By.id("submit-login-register-form")).click();
        Thread.sleep(2000);
    }

    @Test(enabled = true)
    public void registerWithFacebook() throws Exception {
        //add default species
        testDao.deleteUserConnection(1375958496052051l);
        testDao.removeEmail(FACEBOOK_LOGIN);
        d.findElement(By.className("square")).click();

        d.findElement(By.id("register-link")).click();
        d.findElement(By.partialLinkText("Facebook")).click();
        //login at facebook
        d.findElement(By.name("email")).sendKeys(FACEBOOK_LOGIN);
        d.findElement(By.name("pass")).sendKeys(PASSWORD);
        d.findElement(By.name("login")).click();
        //accept tos
        d.findElement(By.name("termsOfService")).click();
        d.findElement(By.tagName("button")).click();

        final WebElement plant = w.until(ExpectedConditions.presenceOfElementLocated(By.className("plant")));
        Assert.assertEquals(plant.getTagName(), "img");
        Assert.assertEquals(d.findElements(By.id("logout-link")).size(), 1);

        d.findElement(By.id("logout-link")).click();
        Thread.sleep(3000);

        d.findElement(By.id("login-link")).click();
        d.findElement(By.partialLinkText("Facebook")).click();

        final WebElement plant2 = w.until(ExpectedConditions.presenceOfElementLocated(By.className("plant")));
        Assert.assertEquals(plant2.getTagName(), "img");
        Assert.assertEquals(d.findElements(By.id("logout-link")).size(), 1);
    }

    @Test(enabled = true)
    public void register() throws Exception {
        final String username = "selenium" + System.currentTimeMillis();

        //add default species
        d.findElement(By.className("square")).click();

        //sign up
        d.findElement(By.id("register-link")).click();
        d.findElement(By.name("username")).clear();
        d.findElement(By.name("username")).sendKeys(username);
        d.findElement(By.name("password")).clear();
        d.findElement(By.name("password")).sendKeys(PASSWORD);
        d.findElement(By.name("passwordagain")).clear();
        d.findElement(By.name("passwordagain")).sendKeys(PASSWORD);
        d.findElement(By.name("termsOfService")).click();
        Thread.sleep(2000);
        d.findElement(By.id("submit-login-register-form")).click();

        w.until(ExpectedConditions.presenceOfElementLocated(By.className("plant")));
        List<WebElement> src = d.findElements(By.className("plant"));
        Assert.assertEquals(src.size(), 1);


        UserBean user = userDao.getUser(username);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getUsername(), username);
        log.info("Register finished successfully. Username:" + username);
    }

    @Test(enabled = true)
    public void addSpecies() throws InterruptedException {
        final String username = addUser();
        final String speciesName = SPECIES_NAME + System.currentTimeMillis();
        login(username, PASSWORD);
        //add species
        Thread.sleep(5000);
        d.findElement(By.id("species-frame")).findElement(By.tagName("input")).sendKeys(speciesName);
        Thread.sleep(5000);
        d.findElement(By.id("add-species-link")).click();
        Thread.sleep(5000);
        d.findElement(By.className("square")).click();
        Thread.sleep(5000);

        d.findElement(By.id("logout-link")).click();

        d.get(HOST_URL + "/wall/" + username);

        final WebElement plant = d.findElement(By.className("plant"));
        Thread.sleep(500);
        Assert.assertEquals(plant.getAttribute("alt"), speciesName);
        log.info("Add species finished successfully. Username:" + username);

    }

    @Test(enabled = true)
    public void addYear() {
        //add default species
        d.findElement(SQUARE).click();

        //add year
        d.findElement(By.id("add-year-link")).click();
        d.findElement(By.id("add-forward-year-button")).click();

        Assert.assertEquals(d.findElements(By.className("visible-remainder")).size(), 1);
        Assert.assertEquals(d.findElements(By.className("select-year")).size(), 2);


        d.findElement(By.id("species-frame")).findElement(By.tagName("input")).sendKeys("vit");
        d.findElement(By.partialLinkText("Grapes")).click();
        d.findElement(By.className("visible-remainder")).click();

        //add year
        d.findElement(By.id("add-year-link")).click();
        d.findElement(By.id("add-forward-year-button")).click();

        Assert.assertEquals(d.findElements(By.className("visible-remainder")).size(), 1);
        Assert.assertEquals(d.findElements(By.className("select-year")).size(), 3);
        Assert.assertEquals(d.findElements(By.className("plant")).size(), 1);
        log.info("Add year finished successfully.");

    }

    @Test(enabled = true)
    public void changePassword() throws InterruptedException {
        final String username = addUser();
        login(username, PASSWORD);

        //go to account details
        d.findElement(By.id("account-link")).click();

        //change password
        d.findElement(By.name("oldPassword")).sendKeys(PASSWORD);
        d.findElement(By.name("newPassword")).sendKeys(NEW_PASSWORD);
        d.findElement(By.name("verifyPassword")).sendKeys(NEW_PASSWORD);
        d.findElement(By.id("submit-password-button")).click();
        w.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-success")));
        d.findElement(By.id("logout-link")).click();

        //login again
        login(username, NEW_PASSWORD);
        Assert.assertEquals(d.findElements(By.id("logout-link")).size(), 1);
        log.info("Change password finished successfully. Username:" + username);
    }

    @Test(enabled = true)
    public void loginWrongPassword() throws InterruptedException {
        final String username = addUser();
        login(username, "wrong password");
        WebElement element = d.findElement(By.id("bad-credentials"));
        Assert.assertTrue(element.isDisplayed());
        Assert.assertFalse(element.getText().isEmpty());
        log.info("LoginWrongPassword finished successfully. Username:" + username);
    }

    @Test(enabled = true)
    public void addMessage() throws InterruptedException {
        final String username = addUser();
        login(username, PASSWORD);
        d.findElement(By.id("forum-link")).click();
        d.findElement(By.tagName("textarea")).sendKeys(NON_LATIN_LETTERS);
        Thread.sleep(2000);
        d.findElement(By.id("submit-account-button")).click();
        Thread.sleep(2000);
        Assert.assertTrue(d.getPageSource().contains(NON_LATIN_LETTERS));
        Assert.assertEquals(d.findElements(By.partialLinkText(username)).size(), 1);
    }

    @Test(enabled = true)
    public void resetPassword() throws Exception {
        final UserBean user = addUser(true);

        d.findElement(By.id("login-link")).click();

        //Reset
        d.findElement(By.id("request-password-link")).click();
        d.findElement(By.name("email")).clear();
        d.findElement(By.name("email")).sendKeys(user.getEmail());
        d.findElement(By.id("submit-request-button")).submit();
        w.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-info")));

        //Check email
        final String mail = FileUtils.readFileToString(DevelopmentConfiguration.MAIL_FILE);
        final String resetUrl = mail.replaceAll(".+(?=http)", "");
        d.get(resetUrl);

        //Set new password
        d.findElement(By.name("password")).sendKeys(NEW_PASSWORD);
        d.findElement(By.tagName("button")).click();
        Assert.assertEquals(d.getCurrentUrl(), HOST_URL + "/login");

        //login
        login(user.getUsername(), NEW_PASSWORD);
        d.findElement(By.id("logout-link")).click();

        //reset password again with same key
        d.get(resetUrl);
        d.findElement(By.name("password")).sendKeys(PASSWORD);
        d.findElement(By.tagName("button")).click();
        Assert.assertEquals(d.getCurrentUrl(), HOST_URL + "/reset-password");
        Assert.assertTrue(d.getPageSource().contains("Key already used"));

        log.info("Reset password finished successfully. Username:" + user.getUsername());
    }

    @Test(enabled = true)
    public void resetPasswordNonexistentEmail() throws Exception {
        final String emailAddress = "asdf@mailinator.com";
        d.findElement(By.id("login-link")).click();

        //Reset
        d.findElement(By.id("request-password-link")).click();
        d.findElement(By.name("email")).clear();
        d.findElement(By.name("email")).sendKeys(emailAddress);
        d.findElement(By.id("submit-request-button")).submit();
        w.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-info")));

        //Check email
        final String mail = FileUtils.readFileToString(DevelopmentConfiguration.MAIL_FILE);
        Assert.assertEquals(mail, "Can not reset password. No user with email " + emailAddress);
    }

    @Test(enabled = true)
    public void updateAccount() throws InterruptedException {
        final String username = addUser();
        login(username, PASSWORD);

        d.findElement(By.id("account-link")).click();

        Assert.assertEquals(d.findElements(By.tagName("input")).size(), 5);

        d.findElement(By.name("email")).sendKeys(username + EMAIL_PROVIDER);
        d.findElement(By.name("displayName")).sendKeys(DISPLAY_NAME);
        new Select(d.findElement(By.name("locale"))).selectByValue("sv");
        d.findElement(By.name("about")).sendKeys(NON_LATIN_LETTERS);
        d.findElement(By.id("submit-account-button")).click();

        w.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-success")));

        d.findElement(By.id("logout-link")).click();

        Thread.sleep(1000);
        login(username, PASSWORD);

        d.findElement(By.id("account-link")).click();

        Thread.sleep(1000);

        Assert.assertEquals(d.findElement(By.name("email")).getAttribute("value"), username + EMAIL_PROVIDER);
        Assert.assertEquals(d.findElement(By.name("displayName")).getAttribute("value"), DISPLAY_NAME);
        Assert.assertEquals(new Select(d.findElement(By.name("locale"))).getFirstSelectedOption().getAttribute("value"), "sv");
        Assert.assertEquals(d.findElement(By.name("about")).getAttribute("value"), NON_LATIN_LETTERS);
        log.info("Update account finished successfully. Username:" + username);
    }
}
