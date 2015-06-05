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

import static java.lang.Boolean.TRUE;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.net.ConnectException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@Slf4j
@Configuration
public class RetryConfig {

  private static final long INITIAL_BACKOFF_INTERVAL = 15000L; // 15 seconds

  @Value("${connection.maxRetries:3}")
  private int maxRetries;

  @Bean
  public RetryTemplate retryTemplate() {
    val result = new RetryTemplate();

    // Define what exceptions should be retried
    Builder<Class<? extends Throwable>, Boolean> exceptions = ImmutableMap.builder();

    // Timeout exception
    exceptions.put(ResourceAccessException.class, TRUE);

    // 503 Service Unavailable
    exceptions.put(HttpServerErrorException.class, TRUE);
    result.setRetryPolicy(new SimpleRetryPolicy(maxRetries, exceptions.build(), true));

    val backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(INITIAL_BACKOFF_INTERVAL);
    result.setBackOffPolicy(backOffPolicy);

    result.registerListener(new CustomRetryListener());

    return result;
  }

  public static class CustomRetryListener extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
      if (!(isConnectionTimeout(throwable) || isServiceUnavailable(throwable))) {
        log.debug("Received a ResourceAccessException, but it's not the connection timeout or 503 Service Unavailabe. "
            + "Do not retry.");
        context.setExhaustedOnly();
      }
    }

    private static boolean isConnectionTimeout(Throwable throwable) {
      if (!(throwable instanceof ResourceAccessException)) {
        return false;
      }

      val cause = throwable.getCause();
      if (cause instanceof ConnectException && cause.getMessage().equals("Operation timed out")) {
        log.debug("Operation timed out. Retrying...");
        return true;
      }

      return false;
    }

    private static boolean isServiceUnavailable(Throwable throwable) {
      if (!(throwable instanceof HttpServerErrorException)) {
        return false;
      }

      val e = (HttpServerErrorException) throwable;
      if (e.getStatusCode() == SERVICE_UNAVAILABLE) {
        log.warn("Service unavailable. Retrying...");
        return true;
      }

      return false;
    }

  }

}
