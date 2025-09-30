<template>
  <PageWrapper contentClass="p-4">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" preIcon="ant-design:reload-outlined" @click="handleReload">刷新</a-button>
      </template>
    </BasicTable>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { h } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable } from '/@/components/Table';
  import type { BasicColumn, FormSchema } from '/@/components/Table';
  import { Tag } from 'ant-design-vue';
  import { formatToDateTime } from '/@/utils/dateUtil';
  import { fetchCommandList } from '/@/api/iot/acc';

  const statusLabels: Record<string, string> = {
    PENDING: '待下发',
    SENT: '已下发',
    ACKED: '执行成功',
    FAILED: '执行失败',
  };

  const statusColors: Record<string, string> = {
    PENDING: 'orange',
    SENT: 'blue',
    ACKED: 'green',
    FAILED: 'red',
  };

  const columns: BasicColumn[] = [
    { title: '设备SN', dataIndex: 'sn', width: 180 },
    { title: '命令编号', dataIndex: 'commandCode', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      customRender: ({ text }) =>
        text
          ? h(Tag, { color: statusColors[text] || 'default' }, () => statusLabels[text] || text)
          : '',
    },
    {
      title: '排队时间',
      dataIndex: 'enqueueTime',
      width: 170,
      customRender: ({ text }) => (text ? formatToDateTime(text) : ''),
    },
    {
      title: '下发时间',
      dataIndex: 'sentTime',
      width: 170,
      customRender: ({ text }) => (text ? formatToDateTime(text) : ''),
    },
    {
      title: '回执时间',
      dataIndex: 'ackTime',
      width: 170,
      customRender: ({ text }) => (text ? formatToDateTime(text) : ''),
    },
    { title: '结果码', dataIndex: 'resultCode', width: 120 },
    { title: '结果信息', dataIndex: 'resultMessage', ellipsis: true, width: 220 },
    { title: '命令内容', dataIndex: 'commandContent', ellipsis: true, width: 280 },
    { title: '最后上报IP', dataIndex: 'lastReportIp', width: 150 },
  ];

  const searchSchemas: FormSchema[] = [
    {
      field: 'sn',
      label: '设备SN',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'commandCode',
      label: '命令编号',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'status',
      label: '状态',
      component: 'Select',
      colProps: { span: 6 },
      componentProps: {
        options: Object.keys(statusLabels).map((key) => ({
          label: statusLabels[key],
          value: key,
        })),
        allowClear: true,
      },
    },
    {
      field: 'resultCode',
      label: '结果码',
      component: 'Input',
      colProps: { span: 6 },
    },
  ];

  const [registerTable, { reload }] = useTable({
    title: '命令队列',
    api: fetchCommandList,
    columns,
    useSearchForm: true,
    bordered: true,
    showTableSetting: true,
    canResize: true,
    formConfig: {
      labelWidth: 90,
      schemas: searchSchemas,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
  });

  function handleReload() {
    reload();
  }
</script>
