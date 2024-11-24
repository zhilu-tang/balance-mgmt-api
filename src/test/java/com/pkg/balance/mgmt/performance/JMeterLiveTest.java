package com.pkg.balance.mgmt.performance;

import com.github.javafaker.Faker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
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
import org.junit.jupiter.api.Test;

/**
 * This is a Live Test so that JMETER_HOME Environment variable will be available to run this test.
 */
public class JMeterLiveTest {

    @Test
    void givenJMeterScript_whenUsingCode_thenExecuteViaJavaProgram() throws IOException {
        String jmeterHome = System.getenv("JMETER_HOME");
        if (jmeterHome == null) {
            jmeterHome = "~/dev/code/bin/apache-jmeter-5.6.3/bin";
//            throw new RuntimeException("JMETER_HOME environment variable is not set.");
        }

        String file = Objects.requireNonNull(JMeterLiveTest.class.getClassLoader().getResource("jmeter.properties")).getFile();
        JMeterUtils.setJMeterHome(jmeterHome);

        JMeterUtils.loadJMeterProperties(file);
        JMeterUtils.initLocale();

        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        HTTPSamplerProxy httpSampler = getHttpSamplerProxy();

        LoopController loopController = getLoopController();

        ThreadGroup threadGroup = getThreadGroup(loopController);

        TestPlan testPlan = getTestPlan(threadGroup);

       // 创建 HashTree 并添加测试计划
        ListedHashTree testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);

        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(httpSampler);

        SaveService.saveTree(testPlanTree, Files.newOutputStream(Paths.get("script.jmx")));
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "output-logs.jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        jmeter.configure(testPlanTree);
        jmeter.run();

        System.out.println("Test completed. See output-logs.jtl file for results");
        System.out.println("JMeter .jmx script is available at script.jmx");
    }

    private static TestPlan getTestPlan(ThreadGroup threadGroup) {
        TestPlan testPlan = new TestPlan("Sample Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.addThreadGroup(threadGroup);
        return testPlan;
    }

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

    private static LoopController getLoopController() {
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }

    private static HTTPSamplerProxy getHttpSamplerProxy() {
        Faker faker = new Faker();
        String accountNumber = faker.number().digits(6);
        double balance = faker.number().randomDouble(2, 1000, 10000);

        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost"); // 假设你的应用运行在本地
        httpSampler.setPort(8080); // 假设你的应用运行在 8080 端口
        httpSampler.setPath("/api/account/create");
        httpSampler.setMethod("POST");
        httpSampler.addArgument("accountNumber", accountNumber);
        httpSampler.addArgument("balance", String.valueOf(balance));
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        return httpSampler;
    }

}
