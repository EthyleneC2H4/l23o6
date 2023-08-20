<script setup lang="ts">

import {Van} from "@element-plus/icons-vue";
</script>

<template>
  <i class="el-icon-delete"></i>
  <el-tabs tab-position="top" stretch >
    <el-tab-pane label="里程积分" name="first">
        <el-text size="large" tag="b">
            <h1>1.用户可以通过成功支付订单积累里程积分</h1>
            <h1>2.积累够一定积分可以升级会员等级</h1>
            <h1>3.更高的会员等级可以获得更大的折扣</h1>
        </el-text>
    </el-tab-pane>
    <el-tab-pane label="抵扣积分" name="second">
        <el-text size="large" tag="b">
            <h1>1.用户可以通过成功支付订单积累抵扣积分</h1>
            <h1>2.积累够一定积分可以直接折现抵扣订单中的一定金额</h1>
        </el-text>
    </el-tab-pane>
  </el-tabs>

</template>

<style scoped>
</style>