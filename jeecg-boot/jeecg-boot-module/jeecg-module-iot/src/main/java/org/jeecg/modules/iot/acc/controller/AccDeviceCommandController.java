package org.jeecg.modules.iot.acc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommand;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for inspecting device command queues.
 */
@Tag(name = "IOT-ACC:命令管理")
@RestController
@RequestMapping("/iot/acc/command")
@RequiredArgsConstructor
public class AccDeviceCommandController extends JeecgController<AccDeviceCommand, AccDeviceCommandService> {

    private final AccDeviceCommandService accDeviceCommandService;

    @GetMapping("/list")
    @Operation(summary = "分页查询设备命令")
    public Result<IPage<AccDeviceCommand>> list(AccDeviceCommand command,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                HttpServletRequest req) {
        QueryWrapper<AccDeviceCommand> queryWrapper = QueryGenerator.initQueryWrapper(command, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<AccDeviceCommand> page = new Page<>(pageNo, pageSize);
        IPage<AccDeviceCommand> pageList = accDeviceCommandService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}
