import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const iotAcc: AppRouteModule = {
  path: '/iot/acc',
  name: 'IotAcc',
  component: LAYOUT,
  redirect: '/iot/acc/device',
  meta: {
    orderNo: 120,
    icon: 'ant-design:interaction-outlined',
    title: t('routes.iot.accModule'),
  },
  children: [
    {
      path: 'device',
      name: 'IotAccDevice',
      component: () => import('/@/views/iot/acc/DeviceList.vue'),
      meta: {
        title: t('routes.iot.accDeviceList'),
      },
    },
    {
      path: 'command',
      name: 'IotAccCommand',
      component: () => import('/@/views/iot/acc/CommandList.vue'),
      meta: {
        title: t('routes.iot.accCommandList'),
      },
    },
  ],
};

export default iotAcc;
