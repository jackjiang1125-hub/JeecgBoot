package org.jeecg.modules.iot.acc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.iot.acc.entity.AccDevice;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommand;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandService;
import org.jeecg.modules.iot.acc.service.AccDeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST endpoints for managing access control devices.
 */
@Tag(name = "IOT-ACC:设备管理")
@RestController
@RequestMapping("/iot/acc/device")
@RequiredArgsConstructor
public class AccDeviceController extends JeecgController<AccDevice, AccDeviceService> {

    private final AccDeviceService accDeviceService;
    private final AccDeviceCommandService accDeviceCommandService;

    @GetMapping("/list")
    @Operation(summary = "分页查询门禁设备")
    public Result<IPage<AccDevice>> list(AccDevice accDevice,
                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                         HttpServletRequest req) {
        QueryWrapper<AccDevice> queryWrapper = QueryGenerator.initQueryWrapper(accDevice, req.getParameterMap());
        String authorizedParam = req.getParameter("authorized");
        if (StringUtils.isNotBlank(authorizedParam)) {
            boolean authorized = "1".equalsIgnoreCase(authorizedParam)
                    || Boolean.parseBoolean(authorizedParam);
            queryWrapper.eq("authorized", authorized);
        }
        Page<AccDevice> page = new Page<>(pageNo, pageSize);
        IPage<AccDevice> pageList = accDeviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping("/authorize")
    @Operation(summary = "手动授权设备")
    public Result<AccDevice> authorize(@RequestBody AuthorizeRequest request) {
        if (request == null || StringUtils.isBlank(request.getSn())) {
            return Result.error("设备SN不能为空");
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        return accDeviceService.authorizeDevice(request.getSn(), request.getRegistryCode(), request.getRemark(), operator)
                .map(Result::OK)
                .orElseGet(() -> Result.error("未找到设备:" + request.getSn()));
    }

    @PostMapping("/commands")
    @Operation(summary = "为设备新增下发命令")
    public Result<List<AccDeviceCommand>> enqueueCommands(@RequestBody CommandBatchRequest request) {
        if (request == null || StringUtils.isBlank(request.getSn())) {
            return Result.error("设备SN不能为空");
        }
        List<String> commands = new ArrayList<>();
        if (request.getCommands() != null && !request.getCommands().isEmpty()) {
            request.getCommands().stream()
                    .map(line -> StringUtils.trimToEmpty(line))
                    .filter(StringUtils::isNotBlank)
                    .forEach(commands::add);
        }
        if (StringUtils.isNotBlank(request.getCommandsText())) {
            Arrays.stream(request.getCommandsText().split("\\r?\\n"))
                    .map(StringUtils::trimToEmpty)
                    .filter(StringUtils::isNotBlank)
                    .forEach(commands::add);
        }
        if (commands.isEmpty()) {
            return Result.error("命令内容不能为空");
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        List<AccDeviceCommand> saved = accDeviceCommandService.enqueueCommands(request.getSn(), commands, operator);
        return Result.OK(saved);
    }

    public record AuthorizeRequest(String sn, String registryCode, String remark) {
    }

    public static class CommandBatchRequest {
        private String sn;
        private String commandsText;
        private List<String> commands;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getCommandsText() {
            return commandsText;
        }

        public void setCommandsText(String commandsText) {
            this.commandsText = commandsText;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }
    }
}
