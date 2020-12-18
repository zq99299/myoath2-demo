package cn.mrcode.cloudoath2.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置用户账户的认证方式。
     * <pre>
     * 显然，我们把用户存在了数据库中希望配置 JDBC 的方式。
     * 此外，我们还配置了使用 BCryptPasswordEncoder 哈希来保存用户的密码（生产环境中，用户密码肯定不能是明文保存的）
     * </pre>
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    /**
     * <pre>
     * 开放 /login 和 /oauth/authorize 两个路径的匿名访问。
     * 前者用于登录，后者用于换授权码，这两个端点访问的时机都在登录之前。
     * 设置 /login 使用表单验证进行登录。
     *
     * </pre>
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login", "/oauth/authorize")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login");
    }
}