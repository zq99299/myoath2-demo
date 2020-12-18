# oath2 练习代码

- xtapp：小兔 APP 第三方客户端应用
- oauth-server：授权服务 为了方便演示：protected-server 受保护的资源服务，写在 授权服务上，不过写在另一个 controller 中。
  原因是因为：受保护的资源服务，拿到 token 之后，需要授权服务器验证 token 是否有效，获取到他的 scope ，是否有权限访问受保护的资源服务
  
## spring security oath 练习代码
- springsecurity101-cloud-oauth2-client，用来扮演客户端角色；

- springsecurity101-cloud-oauth2-server，用来扮演授权服务器角色；

- springsecurity101-cloud-oauth2-userservice，是用户服务，用来扮演资源提供者角色。