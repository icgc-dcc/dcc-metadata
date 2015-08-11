/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.metadata.server.config;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.common.base.Joiner;

/**
 * Resource service configuration file.<br>
 * Protects resources with access token obtained at the authorization server.
 */
@Slf4j
@Configuration
@Profile("secure")
@EnableWebSecurity
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

  private static final String PERMISSION_SEPARATOR = " or ";
  private static final String PERMISSION_TEMPLATE = "#oauth2.hasScope('%s')";
  private TokenExtractor tokenExtractor = new BearerTokenExtractor();

  @Autowired
  private MetadataServerProperties properties;

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.addFilterAfter(new OncePerRequestFilter() {

      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
          FilterChain filterChain)
          throws ServletException, IOException {
        // We don't want to allow access to a controller with no token so clear
        // the security context in case it is actually an OAuth2Authentication
        if (tokenExtractor.extract(request) == null) {
          SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
      }

    }, AbstractPreAuthenticatedProcessingFilter.class);

    http.csrf().disable();
    configureAuthorization(http);
  }

  private void configureAuthorization(HttpSecurity http) throws Exception {
    val accessConfig = resolveUploadScopes();
    // @formatter:off
      http
        .authorizeRequests()
        .antMatchers(POST,"/entities/**")
        .access(accessConfig)
        .antMatchers(PUT,"/entities/**")
        .access(accessConfig)
        .antMatchers(DELETE,"/entities/**")
        .access(accessConfig)
        .and()
        
        .authorizeRequests()
        .anyRequest()
        .permitAll();
      // @formatter:on
  }

  private String resolveUploadScopes() {
    val uploadScopes = properties.getScopes().stream()
        .map(s -> format(PERMISSION_TEMPLATE, s))
        .collect(Collectors.toList());

    val securityConfiguration = Joiner.on(PERMISSION_SEPARATOR).join(uploadScopes);
    log.info("Resolved security configuration to: {}", securityConfiguration);

    return securityConfiguration;
  }

}
