package com.didiglobal.turbo.demo;

import com.didiglobal.turbo.demo.service.LeaveServiceImpl;
import com.didiglobal.turbo.engine.engine.ProcessEngine;
import com.didiglobal.turbo.engine.model.InstanceData;
import com.didiglobal.turbo.engine.param.CommitTaskParam;
import com.didiglobal.turbo.engine.param.RollbackTaskParam;
import com.didiglobal.turbo.engine.param.StartProcessParam;
import com.didiglobal.turbo.engine.result.CommitTaskResult;
import com.didiglobal.turbo.engine.result.RollbackTaskResult;
import com.didiglobal.turbo.engine.result.StartProcessResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <B>主类名称：</B>HoboDemoTest<BR>
 * <B>概要说明：</B><BR>
 *
 * @author hobo
 * @since 2024/3/26  00:30
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class HoboDemoTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(LeaveServiceImpl.class);


    /*hobo demo start*/

    @Resource
    private ProcessEngine processEngine;

    /**
     * 查询流程列表接口
     */
    @Test
    public void doAllProcessTest() {
        // 1. 创建流程

        // 2. 更新流程

        // 3.部署流程

        // 4.开启流程

        startProcessToEnd();
        // 5.提交流程

    }


    private void startProcessToEnd() {
        StartProcessResult startProcess = startProcess();
        CommitTaskResult commitTaskResult = inputTime(startProcess);
        RollbackTaskResult rollbackTaskResult = rollbackToInputTime(commitTaskResult);
        CommitTaskResult result = inputTimeAgain(rollbackTaskResult);
        commitCompleteProcess(result);
    }

    // 用户拉起请假sop
    private StartProcessResult startProcess() {
        StartProcessParam startProcessParam = new StartProcessParam();
        startProcessParam.setFlowModuleId("255f6ac5-eabe-11ee-9225-525b926859ce");
        List<InstanceData> variables = new ArrayList<>();
        variables.add(new InstanceData("user_name", "请假人名字"));
        startProcessParam.setVariables(variables);
        StartProcessResult startProcessResult = processEngine.startProcess(startProcessParam);

        LOGGER.info("startProcess.||startProcessResult={}", startProcessResult);
        return startProcessResult;
    }

    // 输入请假天数
    private CommitTaskResult inputTime(StartProcessResult startProcessResult) {
        CommitTaskParam commitTaskParam = new CommitTaskParam();
        commitTaskParam.setFlowInstanceId(startProcessResult.getFlowInstanceId());
        commitTaskParam.setTaskInstanceId(startProcessResult.getActiveTaskInstance().getNodeInstanceId());
        List<InstanceData> variables = new ArrayList<>();
        variables.add(new InstanceData("user_name", "请假人名字"));
        variables.add(new InstanceData("day", 5));
        commitTaskParam.setVariables(variables);

        CommitTaskResult commitTaskResult = processEngine.commitTask(commitTaskParam);
        LOGGER.info("inputTime.||commitTaskResult={}", commitTaskResult);
        return commitTaskResult;
    }

    // 请假撤回
    private RollbackTaskResult rollbackToInputTime(CommitTaskResult commitTaskResult) {
        RollbackTaskParam rollbackTaskParam = new RollbackTaskParam();
        rollbackTaskParam.setFlowInstanceId(commitTaskResult.getFlowInstanceId());
        rollbackTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
        RollbackTaskResult rollbackTaskResult = processEngine.rollbackTask(rollbackTaskParam);

        LOGGER.info("rollbackToInputTime.||rollbackTaskResult={}", rollbackTaskResult);
        return rollbackTaskResult;
    }

    // 填写请假天数
    private CommitTaskResult inputTimeAgain(RollbackTaskResult rollbackTaskResult) {
        CommitTaskParam commitTaskParam = new CommitTaskParam();
        commitTaskParam.setFlowInstanceId(rollbackTaskResult.getFlowInstanceId());
        commitTaskParam.setTaskInstanceId(rollbackTaskResult.getActiveTaskInstance().getNodeInstanceId());
        List<InstanceData> variables = new ArrayList<>();
        variables.add(new InstanceData("user_name", "请假人名字"));
        variables.add(new InstanceData("day", 2));
        commitTaskParam.setVariables(variables);

        CommitTaskResult commitTaskResult = processEngine.commitTask(commitTaskParam);
        LOGGER.info("inputTimeAgain.||commitTaskResult={}", commitTaskResult);
        return commitTaskResult;
    }

    // BadCase：已完成流程，继续执行流程会失败。
    private CommitTaskResult commitCompleteProcess(CommitTaskResult commitTaskResult) {
        CommitTaskParam commitTaskParam = new CommitTaskParam();
        commitTaskParam.setFlowInstanceId(commitTaskResult.getFlowInstanceId());
        commitTaskParam.setTaskInstanceId(commitTaskResult.getActiveTaskInstance().getNodeInstanceId());
        List<InstanceData> variables = new ArrayList<>();
        variables.add(new InstanceData("user_name", "请假人名字"));
        variables.add(new InstanceData("n", 4));
        commitTaskParam.setVariables(variables);

        CommitTaskResult result = processEngine.commitTask(commitTaskParam);
        LOGGER.info("inputTimeBadCase.||CommitTaskResult={}", result);
        return result;
    }

    /**
     * 够轻量级，实现了基本的流程定义、流程部署、流程发布提交回滚等操作，支持排他网关， 其他特殊操作需要自定义实现，例如委派、跳转、抄送等
     * 前端有简易等流程设计器，可支持定制化开发，后端每个节点也可以支持动态表单。 总来来说，骨架都在，上手较为容易，但是需要功能需要自己实现
     */
}
