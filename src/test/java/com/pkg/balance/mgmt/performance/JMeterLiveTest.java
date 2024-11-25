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
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This class is a live test that runs JMeter scripts using Java code.
 * It requires the JMETER_HOME environment variable to be set to run the test.
 */
public class JMeterLiveTest {

    /**
     * The Spring Boot application context.
     */
    private static ConfigurableApplicationContext context;

    /**
     * Sets up the Spring Boot application before all tests.
     */
    @BeforeAll
    public static void setUp() {
        context = SpringApplication.run(BalanceMgmtApplication.class);
    }

    /**
     * Tears down the Spring Boot application after all tests.
     */
    @AfterAll
    public static void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    /**
     * Executes a JMeter script using Java code.
     *
     * @throws IOException If an I/O error occurs while saving the JMeter script or configuring the JMeter engine.
     */
    @Test
    void givenJMeterScript_whenUsingCode_thenExecuteViaJavaProgram() throws IOException {
        String jmeterHome = System.getenv("JMETER_HOME");
        if (jmeterHome == null) {
            jmeterHome = "~/dev/code/bin/apache-jmeter-5.6.3/bin";
//            throw new RuntimeException("JMETER_HOME environment variable is not set.");
        }

        String file = Objects.requireNonNull(JMeterLiveTest.class.getClassLoader().getResource("loadtesting/jmeter.properties")).getFile();
        JMeterUtils.setJMeterHome(jmeterHome);

        JMeterUtils.loadJMeterProperties(file);
        JMeterUtils.initLocale();

        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        HTTPSamplerProxy httpSampler = getDynamicHttpSamplerProxy();

        LoopController loopController = getLoopController();

        ThreadGroup threadGroup = getThreadGroup(loopController);

        TestPlan testPlan = getTestPlan(threadGroup);

        // Create a HashTree and add the test plan
        ListedHashTree testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);

        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);

        SaveService.saveTree(testPlanTree, Files.newOutputStream(Paths.get("target", "script.jmx")));
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "target/output-logs.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        jmeter.configure(testPlanTree);
        jmeter.run();

        System.out.println("Test completed. See output-logs.jtl file for results");
        System.out.println("JMeter .jmx script is available at script.jmx");
    }

    /**
     * Creates and returns a TestPlan object with the specified ThreadGroup.
     *
     * @param threadGroup The ThreadGroup to add to the TestPlan.
     * @return A configured TestPlan object.
     */
    private static TestPlan getTestPlan(ThreadGroup threadGroup) {
        TestPlan testPlan = new TestPlan("Sample Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.addThreadGroup(threadGroup);
        return testPlan;
    }

    /**
     * Creates and returns a ThreadGroup object with the specified LoopController.
     *
     * @param loopController The LoopController to use in the ThreadGroup.
     * @return A configured ThreadGroup object.
     */
    private static ThreadGroup getThreadGroup(LoopController loopController) {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Sample Thread Group");
        threadGroup.setNumThreads(10);
        threadGroup.setRampUp(5);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        return threadGroup;
    }

    /**
     * Creates and returns a LoopController object.
     *
     * @return A configured LoopController object.
     */
    private static LoopController getLoopController() {
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }

    /**
     * Creates and returns an HTTPSamplerProxy object with random account number and balance.
     *
     * @return A configured HTTPSamplerProxy object.
     */
    private static HTTPSamplerProxy getHttpSamplerProxy() {
        Faker faker = new Faker();
        String accountNumber = faker.number().digits(6);
        double balance = faker.number().randomDouble(2, 1000, 10000);

        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost"); // Assuming the application is running locally
        httpSampler.setPort(8080); // Assuming the application is running on port 8080
        httpSampler.setPath("/api/account/create");
        httpSampler.setMethod("POST");
        httpSampler.addArgument("accountNumber", accountNumber);
        httpSampler.addArgument("balance", String.valueOf(balance));
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        return httpSampler;
    }

    private static HTTPSamplerProxy getDynamicHttpSamplerProxy() {
        // 创建 HTTPSamplerProxy 实例
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setDomain("localhost");
        sampler.setPort(8080);
        sampler.setPath("/api/account/createTransaction");
        sampler.setMethod("POST");

        // 创建 JSR223PreProcessor 实例
        JSR223PreProcessor preProcessor = new JSR223PreProcessor();
        preProcessor.setProperty("cacheKey", "");
        preProcessor.setProperty("filename", "");
        preProcessor.setProperty("parameters", "");
        preProcessor.setProperty("script", "import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;\n" +
                "import java.util.Random;\n" +
                "\n" +
                "Random random = new Random();\n" +
                "String sourceAccountNumber = String.format('%010d', random.nextInt(1000000000));\n" +
                "String destinationAccountNumber = String.format('%010d', random.nextInt(1000000000));\n" +
                "double amount = 100 + (1000 - 100) * random.nextDouble();\n" +
                "\n" +
                "vars.put('sourceAccountNumber', sourceAccountNumber);\n" +
                "vars.put('destinationAccountNumber', destinationAccountNumber);\n" +
                "vars.put('amount', String.valueOf(amount));");
        preProcessor.setProperty("scriptLanguage", "groovy");

        // 设置 HTTP 请求体
        Arguments arguments = new Arguments();
        HTTPArgument bodyArg = new HTTPArgument("", "{\n" +
                "  \"sourceAccountNumber\": ${sourceAccountNumber},\n" +
                "  \"destinationAccountNumber\": ${destinationAccountNumber},\n" +
                "  \"amount\": ${amount}\n" +
                "}", "", false);
        arguments.addArgument(bodyArg);

        // 将 Arguments 对象设置为请求体
        sampler.setPostBodyRaw(true);
        sampler.setArguments(arguments);

        // 创建 HTTPHeaderManager 实例并设置请求头
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("content-type", "application/json"));
        headerManager.add(new Header("accept", "application/json"));

        System.out.println("Request Body: " + sampler.getArguments().getArgument(0).getValue());

        // 打印 HeaderManager 中的头部信息
        for (JMeterProperty header : headerManager.getHeaders()) {
            System.out.println("Header: " + header.getName() + ": " + header.getStringValue());
        }
        // 将 JSR223PreProcessor 和 HTTPHeaderManager 添加到 HTTPSamplerProxy
        sampler.addTestElement(preProcessor);
        sampler.addTestElement(headerManager);

        return sampler;
    }


}
