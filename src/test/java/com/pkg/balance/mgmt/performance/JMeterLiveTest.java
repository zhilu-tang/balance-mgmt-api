package com.pkg.balance.mgmt.performance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.pkg.balance.mgmt.BalanceMgmtApplication;
import com.pkg.balance.mgmt.entity.Account;
import com.pkg.balance.mgmt.entity.Transaction;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.HEADER_MANAGER;
import static org.apache.jmeter.protocol.http.util.HTTPConstantsInterface.HEADER_CONTENT_TYPE;

public class JMeterLiveTest {

    private static ConfigurableApplicationContext context;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() {
        context = SpringApplication.run(BalanceMgmtApplication.class);
    }

    @AfterAll
    public static void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void givenJMeterScript_whenUsingCode_thenExecuteViaJavaProgram() throws IOException {
        String jmeterHome = System.getenv("JMETER_HOME");
        if (jmeterHome == null) {
            jmeterHome = "~/dev/code/bin/apache-jmeter-5.6.3/bin";
        }

        String file = Objects.requireNonNull(JMeterLiveTest.class.getClassLoader().getResource("jmeter.properties")).getFile();
        JMeterUtils.setJMeterHome(jmeterHome);

        JMeterUtils.loadJMeterProperties(file);
        JMeterUtils.initLocale();

        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        // Setup Thread Group
        Faker faker = new Faker();
        Long sourceAccountNumber = Long.parseLong(faker.number().digits(6));
        Long destinationAccountNumber = Long.parseLong(faker.number().digits(6));

        HTTPSamplerProxy createAccount1 = getCreateAccountSampler(sourceAccountNumber, 100000000);
        HTTPSamplerProxy createAccount2 = getCreateAccountSampler(destinationAccountNumber, 100000000);
        LoopController setupLoopController = getLoopController(1);
        ThreadGroup setupThreadGroup = getThreadGroup(setupLoopController, "setUp Thread Group", 1, 0);

        // Main Thread Group
        HTTPSamplerProxy createTransaction = getCreateTransactionSampler(sourceAccountNumber, destinationAccountNumber, 1);
        LoopController mainLoopController = getLoopController(1);
        ThreadGroup mainThreadGroup = getThreadGroup(mainLoopController, "Thread Group", 10, 5);

        // Create Header Manager
        HeaderManager manager = getHeaderManager();

        // Test Plan
        TestPlan testPlan = getTestPlan(setupThreadGroup, mainThreadGroup);

        // Create a HashTree and add the test plan
        ListedHashTree testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);

        // HTTP Request Sampler and Header Manager
        HashTree createAccountTree1 = new HashTree();
        createAccountTree1.add(createAccount1, manager);
        HashTree createAccountTree2 = new HashTree();
        createAccountTree2.add(createAccount2, manager);

        // Add Setup Thread Group
        HashTree setupThreadGroupHashTree = testPlanTree.add(testPlan, setupThreadGroup);
        setupThreadGroupHashTree.add(createAccountTree1);
        setupThreadGroupHashTree.add(createAccountTree2);

        // HTTP Request Sampler and Header Manager
        HashTree createTransactionTree = new HashTree();
        createTransactionTree.add(createTransaction, manager);

        // Add Main Thread Group
        HashTree mainThreadGroupHashTree = testPlanTree.add(testPlan, mainThreadGroup);
        mainThreadGroupHashTree.add(createTransactionTree);

        // Save the test plan to a .jmx file
        SaveService.saveTree(testPlanTree, Files.newOutputStream(Paths.get("target", "script.jmx")));

        // Configure result collectors
        Summariser summer = new Summariser("summary");
        String logFile = "target/output-logs.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Run the test
        jmeter.configure(testPlanTree);
        jmeter.run();

        System.out.println("Test completed. See output-logs.jtl file for results");
        System.out.println("JMeter .jmx script is available at script.jmx");
    }

    private static @NotNull HeaderManager getHeaderManager() {
        // https://stackoverflow.com/questions/41605836/jmeter-add-custom-http-headers-programatically-in-httpsampler
        HeaderManager manager = new HeaderManager();
        manager.add(new Header(HEADER_CONTENT_TYPE, "application/json;"));
        manager.add(new Header("Accept", "application/json"));
        manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
        manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return manager;
    }

    private static TestPlan getTestPlan(ThreadGroup... threadGroups) {
        TestPlan testPlan = new TestPlan("high_competing_transaction");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        for (ThreadGroup threadGroup : threadGroups) {
            testPlan.addThreadGroup(threadGroup);
        }
        return testPlan;
    }

    private static ThreadGroup getThreadGroup(LoopController loopController, String name, int numThreads, int rampTime) {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName(name);
        threadGroup.setNumThreads(numThreads);
        threadGroup.setRampUp(rampTime);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        return threadGroup;
    }

    private static LoopController getLoopController(int loops) {
        LoopController loopController = new LoopController();
        loopController.setLoops(loops);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }

    private static HTTPSamplerProxy getCreateAccountSampler(long accountNumber, long balance) throws JsonProcessingException {
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/account/create");
        httpSampler.setMethod("POST");
        httpSampler.setPostBodyRaw(true);

        Account account = new Account();
        account.setAccountNumber(String.valueOf(accountNumber));
        account.setBalance(balance);

        Arguments arguments = new Arguments();
        HTTPArgument bodyArg = new HTTPArgument("", objectMapper.writeValueAsString(account), null, true);
        bodyArg.setAlwaysEncoded(false);
        arguments.addArgument(bodyArg);
        httpSampler.setArguments(arguments);

        // 打印请求体
        System.out.println("Request Body: " + bodyArg.getValue());

        return httpSampler;
    }

    private static HTTPSamplerProxy getCreateTransactionSampler(long sourceAccountNumber, long destinationAccountNumber, double amount) throws JsonProcessingException {
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/account/createTransaction");
        httpSampler.setMethod("POST");
        httpSampler.setPostBodyRaw(true);

        Transaction transaction = new Transaction();
        transaction.setSourceAccountNumber(String.valueOf(sourceAccountNumber));
        transaction.setDestinationAccountNumber(String.valueOf(destinationAccountNumber));
        transaction.setAmount(amount);

        Arguments arguments = new Arguments();
        HTTPArgument bodyArg = new HTTPArgument("", objectMapper.writeValueAsString(transaction), null, true);
        bodyArg.setAlwaysEncoded(false);
        arguments.addArgument(bodyArg);
        httpSampler.setArguments(arguments);

        // 打印请求体
        System.out.println("Request Body: " + bodyArg.getValue());

        return httpSampler;
    }
}
