package org.ss.employee.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.client.RestTemplate;
import org.ss.department.api.DepartmentControllerApi;
import org.ss.department.invoker.ApiClient;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Value("${services.department.url}")
    private String departmentUrl;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
//            .antMatchers("/test/anonymous").permitAll()
//            .antMatchers("/test/user").hasAnyRole("user")
//            .antMatchers("/test/admin").hasAnyRole("admin")
//            .antMatchers("/test/all-user").hasAnyRole("user","admin")
            .anyRequest()
            .permitAll();
        http.csrf().disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public KeycloakConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "security.oauth2.client", value = "grant-type", havingValue = "client_credentials")
    public static class OAuthRestTemplateConfigurer {

        @Bean
        public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails details) {
            OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(details);
            /* To validate if required configurations are in place during startup */
            oAuth2RestTemplate.getAccessToken();
            return oAuth2RestTemplate;
        }
    }

    @Bean
    public ApiClient configureApiClient(final RestTemplate restTemplate){
        final ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(departmentUrl);
        return  apiClient;
    }

    @Bean
    public DepartmentControllerApi configureDepartmentControllerApi(final ApiClient apiClient){
        return new DepartmentControllerApi(apiClient);
    }
}