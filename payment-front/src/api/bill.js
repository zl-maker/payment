import request from '@/utils/request'

export default {

  downloadBillWxPay(billDate, type) {
    return request({
      url: '/api/wx-pay/downloadbill/' + billDate + '/' + type,
      method: 'get'
    })
  },

  downloadBillAliPay(billDate, type) {
    return request({
      url: '/api/ali-pay/bill/downloadurl/' + billDate + '/' + type,
      method: 'get'
    })
  }
}
