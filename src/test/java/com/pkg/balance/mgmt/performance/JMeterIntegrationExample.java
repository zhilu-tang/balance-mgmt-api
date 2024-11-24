//package com.pkg.balance.mgmt.performance;
//
//import com.github.javafaker.Faker;
//import org.apache.jmeter.config.Arguments;
//import org.apache.jmeter.engine.StandardJMeterEngine;
//import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
//import org.apache.jmeter.samplers.SampleResult;
//import org.apache.jmeter.save.SaveService;
//import org.apache.jmeter.threads.JMeterContext;
//import org.apache.jmeter.threads.JMeterContextService;
//import org.apache.jmeter.util.JMeterUtils;
//import org.apache.jorphan.collections.HashTree;
//
//import java.io.File;
//
//public class JMeterIntegrationExample {
//
//    public static void main(String[] args) throws Exception {
//        // 初始化 JMeter 属性
//        JMeterUtils.loadJMeterProperties("path/to/your/jmeter.properties");
//        JMeterUtils.setJMeterHome("path/to/your/jmeter/home");
//        SaveService.loadProperties();
//
//        // 创建 JMeter 引擎
//        StandardJMeterEngine jmeter = new StandardJMeterEngine();
//
//        // 加载 JMeter 测试计划
//        HashTree testPlanTree = SaveService.loadTree(new File("path/to/your/testplan.jmx"));
//
//        // 配置 JMeter 引擎
//        jmeter.configure(testPlanTree);
//
//        // 生成动态数据
//        Faker faker = new Faker();
//        String accountNumber = faker.number().digits(6);
//        double balance = faker.number().randomDouble(2, 1000, 10000);
//
//        // 创建 Arguments 对象并设置参数
//        Arguments arguments = new Arguments();
//        arguments.addArgument("accountNumber", accountNumber);
//        arguments.addArgument("balance", String.valueOf(balance));
//
//        // 创建 JavaSamplerContext 并传递 Arguments 对象
//        JavaSamplerContext context = new JavaSamplerContext(arguments);
//
//        // 创建 JMeterVariables 对象
//        JMeterContext jmeterContext = JMeterContextService.getContext();
//        jmeterContext.setVariables(new org.apache.jmeter.threads.JMeterVariables());
//
//        // 设置 JMeterVariables 对象中的变量
//        org.apache.jmeter.threads.JMeterVariables variables = jmeterContext.getVariables();
//        variables.put("accountNumber", accountNumber);
//        variables.put("balance", String.valueOf(balance));
//
//        // 执行采样器
//        jmeter.runTest(testPlanTree, jmeterContext);
//
//        // 输出结果
//        SampleResult sampleResult = jmeterContext.getPreviousResult();
//        if (sampleResult != null) {
//            System.out.println("Response Code: " + sampleResult.getResponseCode());
//            System.out.println("Response Message: " + sampleResult.getResponseMessage());
//            System.out.println("Response Data: " + sampleResult.getResponseDataAsString());
//        } else {
//            System.out.println("No result available.");
//        }
//    }
//}
