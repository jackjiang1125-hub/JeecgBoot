<template>
  <PageWrapper contentClass="p-4">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleReload" preIcon="ant-design:reload-outlined">刷新</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="createActions(record)" />
      </template>
    </BasicTable>

    <a-modal
      v-model:open="authorizeVisible"
      title="授权设备"
      :confirm-loading="authorizeLoading"
      @ok="handleAuthorizeSubmit"
    >
      <a-form layout="vertical">
        <a-form-item label="设备SN">
          <a-input v-model:value="authorizeForm.sn" disabled />
        </a-form-item>
        <a-form-item label="RegistryCode">
          <a-input v-model:value="authorizeForm.registryCode" placeholder="留空将自动生成" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="authorizeForm.remark" :rows="3" placeholder="可选" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="commandVisible"
      title="下发设备命令"
      :confirm-loading="commandLoading"
      width="600px"
      @ok="handleCommandSubmit"
    >
      <a-form layout="vertical">
        <a-form-item label="设备SN">
          <a-input v-model:value="commandForm.sn" disabled />
        </a-form-item>
        <a-form-item label="命令内容">
          <a-textarea
            v-model:value="commandForm.commandsText"
            :rows="8"
            placeholder="每行一条命令，例如：C:2001:DATA UPDATE ..."
          />
        </a-form-item>
      </a-form>
      <div class="text-xs text-gray-500">
        最多可一次输入多条命令，系统会按照设备心跳一次下发不超过50条指令。
      </div>
    </a-modal>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { h, reactive, ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import type { BasicColumn, FormSchema } from '/@/components/Table';
  import { Tag } from 'ant-design-vue';
  import { formatToDateTime } from '/@/utils/dateUtil';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    authorizeDevice,
    enqueueDeviceCommands,
    fetchDeviceList,
    type AuthorizeDeviceParams,
  } from '/@/api/iot/acc';

  interface DeviceRecord {
    id: string;
    sn: string;
    deviceName?: string;
    deviceType?: string;
    firmwareVersion?: string;
    lockCount?: number;
    readerCount?: number;
    lastHeartbeatTime?: string;
    lastKnownIp?: string;
    status?: string;
    authorized?: boolean;
    registryCode?: string;
    remark?: string;
  }

  const { createMessage } = useMessage();

  const statusLabels: Record<string, string> = {
    PENDING: '待授权',
    AUTHORIZED: '已授权',
    REVOKED: '已禁用',
  };

  const statusColors: Record<string, string> = {
    PENDING: 'orange',
    AUTHORIZED: 'green',
    REVOKED: 'red',
  };

  const columns: BasicColumn[] = [
    { title: '设备SN', dataIndex: 'sn', width: 180, align: 'left' },
    { title: '设备名称', dataIndex: 'deviceName', width: 160 },
    { title: '设备类型', dataIndex: 'deviceType', width: 100 },
    {
      title: '固件版本',
      dataIndex: 'firmwareVersion',
      width: 180,
      ellipsis: true,
    },
    {
      title: '授权',
      dataIndex: 'authorized',
      width: 90,
      customRender: ({ record }) =>
        h(
          Tag,
          { color: record.authorized ? 'green' : 'red' },
          () => (record.authorized ? '已授权' : '未授权'),
        ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      customRender: ({ text }) =>
        text
          ? h(Tag, { color: statusColors[text] || 'blue' }, () => statusLabels[text] || text)
          : '',
    },
    {
      title: '最后心跳',
      dataIndex: 'lastHeartbeatTime',
      width: 180,
      customRender: ({ text }) => (text ? formatToDateTime(text) : ''),
    },
    { title: '当前IP', dataIndex: 'lastKnownIp', width: 140 },
    { title: 'RegistryCode', dataIndex: 'registryCode', width: 200, ellipsis: true },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
  ];

  const searchSchemas: FormSchema[] = [
    {
      field: 'sn',
      label: '设备SN',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'deviceName',
      label: '设备名称',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'status',
      label: '状态',
      component: 'Select',
      colProps: { span: 6 },
      componentProps: {
        options: Object.keys(statusLabels).map((value) => ({
          label: statusLabels[value],
          value,
        })),
        allowClear: true,
      },
    },
    {
      field: 'authorized',
      label: '是否授权',
      component: 'Select',
      colProps: { span: 6 },
      componentProps: {
        options: [
          { label: '已授权', value: '1' },
          { label: '未授权', value: '0' },
        ],
        allowClear: true,
      },
    },
  ];

  const [registerTable, { reload }] = useTable({
    title: '门禁设备列表',
    api: fetchDeviceList,
    columns,
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    canResize: true,
    formConfig: {
      labelWidth: 90,
      schemas: searchSchemas,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    actionColumn: {
      width: 170,
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const authorizeVisible = ref(false);
  const authorizeLoading = ref(false);
  const authorizeForm = reactive<AuthorizeDeviceParams>({ sn: '', registryCode: '', remark: '' });

  const commandVisible = ref(false);
  const commandLoading = ref(false);
  const commandForm = reactive<{ sn: string; commandsText: string }>({ sn: '', commandsText: '' });

  function handleReload() {
    reload();
  }

  function openAuthorize(record: DeviceRecord) {
    authorizeForm.sn = record.sn;
    authorizeForm.registryCode = record.registryCode || '';
    authorizeForm.remark = record.remark || '';
    authorizeVisible.value = true;
  }

  async function handleAuthorizeSubmit() {
    if (!authorizeForm.sn) {
      createMessage.error('缺少设备SN');
      return;
    }
    try {
      authorizeLoading.value = true;
      await authorizeDevice({ ...authorizeForm });
      createMessage.success('授权指令已提交');
      authorizeVisible.value = false;
      reload();
    } finally {
      authorizeLoading.value = false;
    }
  }

  function openCommandModal(record: DeviceRecord) {
    commandForm.sn = record.sn;
    commandForm.commandsText = '';
    commandVisible.value = true;
  }

  async function handleCommandSubmit() {
    if (!commandForm.sn) {
      createMessage.error('缺少设备SN');
      return;
    }
    const lines = commandForm.commandsText
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0);
    if (!lines.length) {
      createMessage.warning('请至少输入一条命令');
      return;
    }
    try {
      commandLoading.value = true;
      await enqueueDeviceCommands({ sn: commandForm.sn, commands: lines });
      createMessage.success(`已加入${lines.length}条命令到下发队列`);
      commandVisible.value = false;
    } finally {
      commandLoading.value = false;
    }
  }

  function createActions(record: DeviceRecord) {
    const actions = [
      {
        label: '下发命令',
        onClick: () => openCommandModal(record),
      },
    ];
    actions.unshift({
      label: record.authorized ? '重新授权' : '授权',
      onClick: () => openAuthorize(record),
    });
    return actions;
  }

  defineExpose({ reload });
</script>

<style scoped>
.text-gray-500 {
  color: #6b7280;
}
</style>
