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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.icgc.dcc.metadata.core.retry.RetryPolicies.getRetryableExceptions;
import static org.springframework.retry.backoff.ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;
import lombok.val;

import org.icgc.dcc.metadata.core.retry.DefaultRetryListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@Profile("secure")
public class RetryConfig {

  private static final int DEFAULT_MAX_RETRIES = 5;
  private static final long DEFAULT_INITIAL_BACKOFF_INTERVAL = SECONDS.toMillis(15L);

  @Value("${auth.connection.maxRetries}")
  private int maxRetries = DEFAULT_MAX_RETRIES;
  @Value("${auth.connection.initialBackoff}")
  private long initialBackoff = DEFAULT_INITIAL_BACKOFF_INTERVAL;
  @Value("${auth.connection.multiplier}")
  private double multiplier = DEFAULT_MULTIPLIER;

  @Bean
  public RetryTemplate retryTemplate() {
    val result = new RetryTemplate();
    result.setBackOffPolicy(defineBackOffPolicy());

    result.setRetryPolicy(new SimpleRetryPolicy(maxRetries, getRetryableExceptions(), true));
    result.registerListener(new DefaultRetryListener());

    return result;
  }

  private BackOffPolicy defineBackOffPolicy() {
    val backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(initialBackoff);
    backOffPolicy.setMultiplier(multiplier);

    return backOffPolicy;
  }

}
