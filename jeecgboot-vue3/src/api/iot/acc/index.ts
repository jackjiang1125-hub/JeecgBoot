import { defHttp } from '/@/utils/http/axios';
import type { BasicPageParams } from '/@/api/model/baseModel';

export enum AccApi {
  DeviceList = '/iot/acc/device/list',
  AuthorizeDevice = '/iot/acc/device/authorize',
  EnqueueCommands = '/iot/acc/device/commands',
  CommandList = '/iot/acc/command/list',
}

export interface DeviceListParams extends BasicPageParams {
  sn?: string;
  deviceType?: string;
  deviceName?: string;
  status?: string;
  authorized?: string | boolean;
}

export interface AuthorizeDeviceParams {
  sn: string;
  registryCode?: string;
  remark?: string;
}

export interface CommandBatchParams {
  sn: string;
  commandsText?: string;
  commands?: string[];
}

export interface CommandListParams extends BasicPageParams {
  sn?: string;
  status?: string;
  commandCode?: string;
  resultCode?: string;
}

export const fetchDeviceList = (params: DeviceListParams) =>
  defHttp.get({ url: AccApi.DeviceList, params });

export const authorizeDevice = (params: AuthorizeDeviceParams) =>
  defHttp.post({ url: AccApi.AuthorizeDevice, params });

export const enqueueDeviceCommands = (params: CommandBatchParams) =>
  defHttp.post({ url: AccApi.EnqueueCommands, params });

export const fetchCommandList = (params: CommandListParams) =>
  defHttp.get({ url: AccApi.CommandList, params });
