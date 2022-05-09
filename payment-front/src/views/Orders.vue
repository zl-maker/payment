<template>
  <div class="bg-fa of">
    <section id="index" class="container">
      <header class="comm-title">
        <h2 class="fl tac">
          <span class="c-333">订单列表</span>
        </h2>
      </header>
      <el-table :data="list" border style="width: 100%">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column
          prop="orderNo"
          label="订单编号"
          width="230"
        ></el-table-column>
        <el-table-column prop="title" label="订单标题"></el-table-column>
        <el-table-column prop="totalFee" label="订单金额">
          <template slot-scope="scope"> {{ scope.row.totalFee }} 元 </template>
        </el-table-column>
        <el-table-column prop="paymentType" label="支付方式"></el-table-column>
        <el-table-column label="订单状态">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.orderStatus === '未支付'">
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag v-if="scope.row.orderStatus === '支付成功'" type="success">
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag
              v-if="scope.row.orderStatus === '超时已关闭'"
              type="warning"
            >
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag v-if="scope.row.orderStatus === '用户已取消'" type="info">
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag v-if="scope.row.orderStatus === '退款中'" type="danger">
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag v-if="scope.row.orderStatus === '已退款'" type="info">
              {{ scope.row.orderStatus }}
            </el-tag>
            <el-tag v-if="scope.row.orderStatus === '退款异常'" type="danger">
              {{ scope.row.orderStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="createTime" label="创建时间"></el-table-column> -->
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.orderStatus === '未支付'"
              type="text"
              @click="cancel(scope.row.orderNo, scope.row.paymentType)"
              >取消</el-button
            >
            <el-button
              v-if="scope.row.orderStatus === '支付成功'"
              type="text"
              @click="refund(scope.row.orderNo, scope.row.paymentType)"
              >退款</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 退款对话框 -->
    <el-dialog
      :visible.sync="refundDialogVisible"
      @close="closeDialog"
      width="350px"
      center
    >
      <el-form>
        <el-form-item label="退款原因">
          <el-select v-model="reason" placeholder="请选择退款原因">
            <el-option label="不喜欢" value="不喜欢"></el-option>
            <el-option label="买错了" value="买错了"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="toRefunds()"
          :disabled="refundSubmitBtnDisabled"
          >确 定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import aliPay from "../api/aliPay";
import orderInfoApi from "../api/orderInfo";
import wxPayApi from "../api/wxPay";

export default {
  data() {
    return {
      list: [], //订单列表
      refundDialogVisible: false, //退款弹窗
      orderNo: "", //退款订单号
      reason: "", //退款原因,
      refundSubmitBtnDisabled: false, //防止重复提交
      paymentType: "",
    };
  },

  created() {
    this.showOrderList();
  },

  methods: {
    //显示订单列表
    showOrderList() {
      orderInfoApi.list().then((response) => {
        this.list = response.data;
      });
    },

    //用户取消订单
    cancel(orderNo, paymentType) {
      if (paymentType === "微信") {
        wxPayApi.cancel(orderNo).then((response) => {
          this.$message.success(response.message);
          //刷新订单列表
          this.showOrderList();
        });
      } else {
        aliPay.cancel(orderNo).then((response) => {
          this.$message.success(response.message);
          //刷新订单列表
          this.showOrderList();
        });
      }
    },

    //退款对话框
    refund(orderNo, paymentType) {
      this.refundDialogVisible = true;
      this.orderNo = orderNo;
      this.paymentType = paymentType;
    },

    //关闭退款对话框
    closeDialog() {
      console.log("close.................");
      this.refundDialogVisible = false;
      //还原组件状态
      this.orderNo = "";
      this.reason = "";
      this.refundSubmitBtnDisabled = false;
    },

    //确认退款
    toRefunds() {
      this.refundSubmitBtnDisabled = true; //禁用按钮，防止重复提交

      if (this.paymentType === "微信") {
        wxPayApi.refunds(this.orderNo, this.reason).then((response) => {
          console.log("response", response);
          this.closeDialog();
          this.showOrderList();
        });
      } else {
        aliPay.refunds(this.orderNo, this.reason).then((response) => {
          console.log("response", response);
          this.closeDialog();
          this.showOrderList();
        });
      }
    },
  },
};
</script>
