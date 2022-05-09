<template>
  <!-- 公共头 -->
  <header id="header">
    <section class="container">
      <div>
        <ul class="nav">
          <router-link to="/" tag="li" active-class="current" exact>
            <a>购买商品</a>
          </router-link>
          <router-link to="/orders" tag="li" active-class="current">
            <a>我的订单</a>
          </router-link>
          <router-link to="/download" tag="li" active-class="current">
            <a>下载账单</a>
          </router-link>
        </ul>
      </div>
      <div class="clear"></div>
    </section>
  </header>
</template>