package com.pkg.balance.mgmt.performance;

import com.github.javafaker.Faker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import com.pkg.balance.mgmt.BalanceMgmtApplication;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JMeterLiveTest {

    private static ConfigurableApplicationContext context;

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

        // Test Plan
        TestPlan testPlan = getTestPlan(setupThreadGroup, mainThreadGroup);

        // Create a HashTree and add the test plan
        ListedHashTree testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);

        // Add Setup Thread Group
        HashTree setupThreadGroupHashTree = testPlanTree.add(testPlan, setupThreadGroup);
        setupThreadGroupHashTree.add(createAccount1);
        setupThreadGroupHashTree.add(createAccount2);

        // Add Main Thread Group
        HashTree mainThreadGroupHashTree = testPlanTree.add(testPlan, mainThreadGroup);
        mainThreadGroupHashTree.add(createTransaction);

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

    private static HTTPSamplerProxy getCreateAccountSampler(long accountNumber, long balance) {
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/account/create");
        httpSampler.setMethod("POST");
        httpSampler.setPostBodyRaw(true);
        Arguments arguments = new Arguments();
        HTTPArgument bodyArg = new HTTPArgument("", "{\n" +
                "  \"accountNumber\": " + accountNumber + ",\n" +
                "  \"balance\": " + balance + "\n" +
                "}", "", false);
        arguments.addArgument(bodyArg);
        httpSampler.setArguments(arguments);

        // Add Header Manager
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("content-type", "application/json"));
        headerManager.add(new Header("accept", "application/json"));
        httpSampler.addTestElement(headerManager);

        return httpSampler;
    }

    private static HTTPSamplerProxy getCreateTransactionSampler(long sourceAccountNumber, long destinationAccountNumber, double amount) {
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/account/createTransaction");
        httpSampler.setMethod("POST");
        httpSampler.setPostBodyRaw(true);
        Arguments arguments = new Arguments();
        HTTPArgument bodyArg = new HTTPArgument("", "{\n" +
                "  \"sourceAccountNumber\": " + sourceAccountNumber + ",\n" +
                "  \"destinationAccountNumber\": " + destinationAccountNumber + ",\n" +
                "  \"amount\": " + amount + "\n" +
                "}", "", false);
        arguments.addArgument(bodyArg);
        httpSampler.setArguments(arguments);

        // Add Header Manager
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("content-type", "application/json"));
        headerManager.add(new Header("accept", "application/json"));
        httpSampler.addTestElement(headerManager);

        return httpSampler;
    }
}
